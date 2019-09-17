@file:Suppress("unused")

package rocks.didit.sefilm.graphql

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import com.coxautodev.graphql.tools.GraphQLResolver
import org.springframework.stereotype.Component
import rocks.didit.sefilm.NotFoundException
import rocks.didit.sefilm.database.entities.Movie
import rocks.didit.sefilm.database.entities.Ticket
import rocks.didit.sefilm.database.repositories.ParticipantRepository
import rocks.didit.sefilm.domain.Base64ID
import rocks.didit.sefilm.domain.dto.AdminPaymentDetailsDTO
import rocks.didit.sefilm.domain.dto.AttendeePaymentDetailsDTO
import rocks.didit.sefilm.domain.dto.FilmstadenSeatMapDTO
import rocks.didit.sefilm.domain.dto.ParticipantDTO
import rocks.didit.sefilm.domain.dto.PublicUserDTO
import rocks.didit.sefilm.domain.dto.ShowingDTO
import rocks.didit.sefilm.domain.dto.TicketRange
import rocks.didit.sefilm.orElseThrow
import rocks.didit.sefilm.services.MovieService
import rocks.didit.sefilm.services.ShowingService
import rocks.didit.sefilm.services.TicketService
import rocks.didit.sefilm.services.UserService
import java.time.LocalDate
import java.util.*

@Component
class ShowingQueryResolver(private val showingService: ShowingService) : GraphQLQueryResolver {
  fun publicShowings(afterDate: LocalDate?) = showingService.getAllPublicShowings(afterDate ?: LocalDate.MIN)

  fun showing(id: UUID?, webId: Base64ID?): ShowingDTO? {
    return when {
      id != null -> showingService.getShowing(id)
      webId != null -> showingService.getShowing(webId)
      else -> null
    }
  }

  fun showing(webId: Base64ID): ShowingDTO? = showingService.getShowing(webId)
  fun showingForMovie(movieId: UUID) = showingService.getShowingByMovie(movieId)
}

@Component
class ShowingResolver(
  private val showingService: ShowingService,
  private val participantRepo: ParticipantRepository,
  private val userService: UserService,
  private val movieService: MovieService,
  private val ticketService: TicketService
) : GraphQLResolver<ShowingDTO> {
  fun admin(showing: ShowingDTO): PublicUserDTO = userService
    .getUser(showing.admin)
    .orElseThrow { NotFoundException("admin user", showing.admin, showing.id) }

  fun payToUser(showing: ShowingDTO): PublicUserDTO = userService
    .getUser(showing.payToUser)
    .orElseThrow { NotFoundException("payment receiver user", showing.payToUser, showing.id) }

  fun movie(showing: ShowingDTO): Movie {
    val id = showing.movieId
    return movieService.getMovie(id)
      .orElseThrow { NotFoundException("movie with id: ${showing.movieId}") }
  }

  fun participants(showing: ShowingDTO): List<ParticipantDTO> =
    participantRepo.findById_Showing_Id(showing.id).map { it.toDTO() }

  // FIXME: remove and rename this to filmstadenShowingId instead
  fun filmstadenRemoteEntityId(showing: ShowingDTO): String? = showing.filmstadenShowingId

  fun myTickets(showing: ShowingDTO): List<Ticket> = ticketService.getTicketsForCurrentUserAndShowing(showing.id)

  fun ticketRange(showing: ShowingDTO): TicketRange? = ticketService.getTicketRange(showing.id)

  fun adminPaymentDetails(showing: ShowingDTO): AdminPaymentDetailsDTO? =
    showingService.getAdminPaymentDetails(showing.id)

  fun attendeePaymentDetails(showing: ShowingDTO): AttendeePaymentDetailsDTO? =
    showingService.getAttendeePaymentDetails(showing.id)

  fun filmstadenSeatMap(showing: ShowingDTO): List<FilmstadenSeatMapDTO> = showingService.fetchSeatMap(showing.id)
}

@Component
class ParticipantUserResolver(private val userService: UserService, private val showingService: ShowingService) :
  GraphQLResolver<ParticipantDTO> {
  fun user(participant: ParticipantDTO): PublicUserDTO = userService
    .getUserOrThrow(participant.userId)

  // TODO: why is this needed?
  fun id(participant: ParticipantDTO) = participant.userId

  // TODO: why is this needed?
  fun showing(participant: ParticipantDTO) = showingService.getShowingOrThrow(participant.showingId)
}

@Component
class TicketUserResolver(private val userService: UserService) : GraphQLResolver<Ticket> {
  fun assignedToUser(ticket: Ticket): PublicUserDTO = userService
    .getUserOrThrow(ticket.assignedToUser.id)
}

