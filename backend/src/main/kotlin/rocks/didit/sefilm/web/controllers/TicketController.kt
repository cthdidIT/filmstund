package rocks.didit.sefilm.web.controllers

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import rocks.didit.sefilm.Application
import rocks.didit.sefilm.NotFoundException
import rocks.didit.sefilm.UserHasNotPaidException
import rocks.didit.sefilm.currentLoggedInUser
import rocks.didit.sefilm.database.entities.Ticket
import rocks.didit.sefilm.database.repositories.ParticipantPaymentInfoRepository
import rocks.didit.sefilm.database.repositories.TicketRepository
import java.time.LocalDate
import java.util.*

@RestController
@RequestMapping(Application.API_BASE_PATH + "/tickets")
class TicketController(private val ticketRepository: TicketRepository,
                       private val paymentInfoRepository: ParticipantPaymentInfoRepository) {

  @GetMapping("/", produces = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE))
  fun allMyTickets(): List<Ticket> {
    val currentLoggedInUser = currentLoggedInUser()
    val now = LocalDate.now()
    return ticketRepository.findByAssignedToUser(currentLoggedInUser)
      .filter {
        it.date.isEqual(now) || it.date.isAfter(now)
      }
  }

  @GetMapping("/{showingId}", produces = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE))
  fun myTickets(@PathVariable showingId: UUID): List<Ticket> {
    val currentLoggedInUser = currentLoggedInUser()

    val participant = paymentInfoRepository
      .findByShowingIdAndUserId(showingId, currentLoggedInUser)
      .orElseThrow {
        throw NotFoundException("user. Not enrolled on this showing")
      }

    if (!participant.hasPaid) {
      throw UserHasNotPaidException("User has not paid for this showing")
    }

    return ticketRepository.findByShowingIdAndAssignedToUser(showingId, currentLoggedInUser)
  }
}