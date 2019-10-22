package se.filmstund.events

import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import se.filmstund.logger

@Component
@Profile("dev")
class EventLogger {
  companion object {
    private val log by logger()
  }

  @EventListener
  fun logEvents(event: FilmstundEvent) {
    return when (event) {
      is NewShowingEvent -> log.info("User ${event.triggeredBy?.nick} created a new showing with ID=${event.showing.id} for movie '${event.showing.movieId}'")
      is UpdatedShowingEvent -> log.info("User ${event.triggeredBy?.nick} updated a new showing with ID=${event.showing.id}")
      is DeletedShowingEvent -> log.info("User ${event.triggeredBy?.nick} deleted a new showing with ID=${event.showing.id}")
      is TicketsBoughtEvent -> log.info("User ${event.triggeredBy?.nick} bought tickets for showing ${event.showing.id}")
      is UserAttendedEvent -> log.info("User ${event.triggeredBy?.nick} attended showing ${event.showing.id} with payment type ${event.attendee.type}")
      is UserUnattendedEvent -> log.info("User ${event.triggeredBy?.nick} unattended showing ${event.showing.id}")
      is PushoverUserKeyInvalidEvent -> log.info("Got an invalid Pushover user key: ${event.userKey}")
    }
  }
}