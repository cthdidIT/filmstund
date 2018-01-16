package rocks.didit.sefilm.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import rocks.didit.sefilm.*
import rocks.didit.sefilm.database.entities.ParticipantPaymentInfo
import rocks.didit.sefilm.database.entities.Showing
import rocks.didit.sefilm.database.entities.User
import rocks.didit.sefilm.database.repositories.ParticipantPaymentInfoRepository
import rocks.didit.sefilm.database.repositories.ShowingRepository
import rocks.didit.sefilm.domain.*
import rocks.didit.sefilm.domain.dto.*
import rocks.didit.sefilm.events.*
import rocks.didit.sefilm.services.external.SFService
import rocks.didit.sefilm.utils.SwishUtil.Companion.constructSwishUri
import java.time.LocalDate
import java.util.*

@Service
class ShowingService(
  private val showingRepo: ShowingRepository,
  private val paymentInfoRepo: ParticipantPaymentInfoRepository,
  private val movieService: MovieService,
  private val userService: UserService,
  private val ticketService: TicketService,
  private val sfService: SFService,
  private val locationService: LocationService,
  private val eventPublisher: EventPublisher,
  private val assertionService: AssertionService) {

  companion object {
    private val log: Logger = LoggerFactory.getLogger(ShowingService::class.java)
  }

  private fun getShowingEntity(id: UUID): Showing
    = showingRepo.findById(id).orElseThrow { NotFoundException("showing", showingId = id) }

  fun getShowing(id: UUID): ShowingDTO?
    = showingRepo
    .findById(id)
    .map { it.toDto() }
    .orElse(null)

  fun getShowingOrThrow(id: UUID): ShowingDTO
    = getShowing(id)
    ?: throw NotFoundException(what = "showing", showingId = id)

  fun getShowingByMovie(movieId: UUID): List<ShowingDTO>
    = showingRepo
    .findByMovieIdOrderByDateDesc(movieId)
    .map { it.toDto() }

  fun getShowingByUser(user: UserID): List<ShowingDTO>
    = showingRepo
    .findAll()
    .filter { it.userIsInvolvedInThisShowing(user) }
    .map { it.toDto() }

  fun getAllPublicShowings(afterDate: LocalDate = LocalDate.MIN): List<ShowingDTO>
    = showingRepo.findByPrivateOrderByDateDesc(false).toList()
    .filter { it.date?.isAfter(afterDate) ?: false }
    .map { it.toDto() }

  fun getPrivateShowingsForCurrentUser(afterDate: LocalDate = LocalDate.MIN): List<ShowingDTO> {
    val currentLoggedInUser = currentLoggedInUser()
    return showingRepo.findByPrivateOrderByDateDesc(true)
      .filter { it.userIsInvolvedInThisShowing(currentLoggedInUser) }
      .map { it.toDto() }
  }

  /** Info that is needed before you buy the tickets at SF */
  fun getAdminPaymentDetails(showingId: UUID): AdminPaymentDetailsDTO? {
    val showing = getShowingEntity(showingId)
    if (showing.admin.id != currentLoggedInUser()) {
      return null
    }

    // Note that this list is empty before the showing has been marked as bought
    val paymentInfos = paymentInfoRepo.findByShowingId(showingId)
    val ticketMap = showing.participants.map {
      val user = userService.getCompleteUser(it.userId).orElseThrow { NotFoundException("user", it.userId, showingId) }
      val ftgTicket = (it as? FtgBiljettParticipant)?.ticketNumber
      UserAndSfData(user.id, user.sfMembershipId, ftgTicket)
    }

    return AdminPaymentDetailsDTO(sfService.getSfBuyLink(showing.movie.id), ticketMap, paymentInfos)
  }

  /** Info a user needs for paying the one who bought the tickets */
  fun getAttendeePaymentDetails(showingId: UUID): AttendeePaymentDetailsDTO?
    = getAttendeePaymentDetailsForUser(currentLoggedInUser(), showingId)

  fun getAttendeePaymentDetailsForUser(userID: UserID, showingId: UUID): AttendeePaymentDetailsDTO? {
    val showing = getShowingEntity(showingId)
    val payeePhone = showing.payToUser.phone
      .orElseThrow { MissingPhoneNumberException(showing.payToUser.id) }

    val participantInfo = paymentInfoRepo
      .findByShowingIdAndUserId(showingId, userID)
      .orElse(null) ?: return null

    val movieTitle = movieService.getMovieOrThrow(showing.movie.id).title

    val swishTo = when {
      !participantInfo.hasPaid && participantInfo.amountOwed.ören > 0 -> constructSwishUri(showing, payeePhone, participantInfo, movieTitle)
      else -> null
    }

    return AttendeePaymentDetailsDTO(participantInfo.hasPaid, participantInfo.amountOwed, showing.payToUser.id, swishTo, payeePhone.number, userID)
  }

  fun attendShowing(showingId: UUID, paymentOption: PaymentOption): ShowingDTO {
    val showing = getShowingEntity(showingId)
    val userId = currentLoggedInUser()
    assertionService.assertTicketsNotBought(userId, showing)
    assertionService.assertUserNotAlreadyAttended(userId, showing)

    val participant: Participant = createParticipantBasedOnPaymentType(paymentOption, userId)

    val newParticipants = showing.participants.plus(participant)
    return showingRepo
      .save(showing.copy(participants = newParticipants))
      .also {
        val user = userService.getCompleteUser(userId)
        eventPublisher.publish(UserAttendedEvent(this, it, user, paymentOption.type))
      }
      .toDto()

  }

  fun unattendShowing(showingId: UUID): ShowingDTO {
    val showing = getShowingEntity(showingId)
    val currentUserId = currentLoggedInUser()
    assertionService.assertTicketsNotBought(currentUserId, showing)

    val participantLst = showing
      .participants
      .filter { it.userId == currentUserId }

    if (participantLst.isEmpty()) {
      return showing.toDto()
    } else if (participantLst.size > 1) {
      throw IllegalStateException("Participant $currentUserId has participated more than one time on showing $showingId")
    }

    val participant = participantLst.first()
    val participantsWithoutLoggedInUser = showing.participants.minus(participant)
    return showingRepo
      .save(showing.copy(participants = participantsWithoutLoggedInUser))
      .also {
        val user = userService.getCompleteUser(participant.userId)
        eventPublisher.publish(UserUnattendedEvent(this, it, user))
      }
      .toDto()
  }

  fun createShowing(data: CreateShowingDTO): ShowingDTO {
    if (data.date == null || data.location == null || data.movieId == null || data.time == null) throw MissingParametersException()
    if (!movieService.movieExists(data.movieId)) {
      throw NotFoundException("movie '${data.movieId}'. Can't create showing for movie that does not exist")
    }

    val adminUser = userService.getCompleteUser(currentLoggedInUser())
    return showingRepo
      .save(data.toShowing(adminUser))
      .also {
        eventPublisher.publish(NewShowingEvent(this, it, adminUser))
      }
      .toDto()
  }

  /** Delete the selected showing and return all public showings */
  fun deleteShowing(showingId: UUID): List<ShowingDTO> {
    val showing = getShowingEntity(showingId)
    assertionService.assertLoggedInUserIsAdmin(showing.admin.id)

    paymentInfoRepo.deleteByShowingIdAndUserId(showing.id, currentLoggedInUser())
    ticketService.deleteTickets(showing)
    showingRepo.delete(showing)

    eventPublisher.publish(DeletedShowingEvent(this, showing, showing.admin))
    return getAllPublicShowings()
  }

  fun markAsBought(showingId: UUID): ShowingDTO {
    val showing = getShowingEntity(showingId)
    assertionService.assertLoggedInUserIsAdmin(showing.admin.id)
    assertionService.assertUserHasPhoneNumber(showing.admin.id)

    if (showing.ticketsBought) {
      log.info("Showing $showingId is already bought")
      return showing.toDto()
    }

    createInitialPaymentInfo(showing)
    return showingRepo
      .save(showing.copy(ticketsBought = true))
      .also {
        eventPublisher.publish(TicketsBoughtEvent(this, it, it.admin))
      }
      .toDto()
  }

  fun updateShowing(showingId: UUID, newValues: UpdateShowingDTO): ShowingDTO {
    val showing = getShowingEntity(showingId)
    assertionService.assertLoggedInUserIsAdmin(showing.admin.id)

    log.info("Updating showing ($showingId) to new values: $newValues")
    return showingRepo.save(showing.copy(
      price = SEK(newValues.price),
      private = newValues.private,
      payToUser = userService.getCompleteUser(UserID(newValues.payToUser)),
      expectedBuyDate = newValues.expectedBuyDate,
      location = locationService.getOrCreateNewLocation(newValues.location),
      time = newValues.time
    ))
      .also {
        eventPublisher.publish(UpdatedShowingEvent(this, it, it.admin))
      }
      .toDto()
  }

  private fun createParticipantBasedOnPaymentType(paymentOption: PaymentOption, userId: UserID): Participant
    = when (paymentOption.type) {
    PaymentType.Foretagsbiljett -> {
      val suppliedTicket
        = paymentOption.ticketNumber
        ?: throw MissingParametersException("User chose to pay with a företagsbiljett, but no ticket number were supplied")
      val ticketNumber = TicketNumber(suppliedTicket)

      assertionService.assertForetagsbiljettIsAvailable(userId, ticketNumber)
      FtgBiljettParticipant(userId, ticketNumber)
    }
    PaymentType.Swish -> SwishParticipant(userId)
  }

  private fun Showing.userIsInvolvedInThisShowing(userID: UserID): Boolean {
    return this.isAdmin(userID) || this.isParticipantInShowing(userID)
      || this.payToUser.id == userID
  }

  private fun Showing.isAdmin(userID: UserID): Boolean
    = this.admin.id == userID

  private fun Showing.isParticipantInShowing(userID: UserID): Boolean
    = this.participants.any { it.userId == userID }

  /* Fetch location from db or create it if it does not exist before converting the showing */
  private fun CreateShowingDTO.toShowing(admin: User): Showing {
    if (this.location == null) {
      throw IllegalArgumentException("Location may not be null")
    }
    val location = locationService.getOrCreateNewLocation(this.location)
    return Showing(date = this.date,
      time = this.time,
      movie = movieService.getMovieOrThrow(this.movieId),
      location = location,
      admin = admin,
      payToUser = admin,
      expectedBuyDate = this.expectedBuyDate,
      participants = setOf(SwishParticipant(admin.id)))
  }

  private fun createInitialPaymentInfo(showing: Showing) {
    val participants = showing
      .participants
      .map { it ->
        val hasPaid = it.userId == showing.payToUser.id || it is FtgBiljettParticipant
        ParticipantPaymentInfo(userId = it.userId,
          showingId = showing.id,
          hasPaid = hasPaid,
          amountOwed = if (hasPaid || showing.price == null) SEK(0) else showing.price)
      }
    paymentInfoRepo.saveAll(participants)
  }

}