package rocks.didit.sefilm.services

import biweekly.ICalendar
import biweekly.component.VEvent
import biweekly.parameter.ParticipationStatus
import biweekly.property.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import rocks.didit.sefilm.Properties
import rocks.didit.sefilm.database.entities.Movie
import rocks.didit.sefilm.domain.dto.ShowingDTO
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

@Component
class CalendarService(
  private val userService: UserService,
  private val showingService: ShowingService,
  private val movieService: MovieService,
  private val properties: Properties
) {

  private val calendarDescription: String = "Dina visningar på $CALENDAR_NAME (${properties.baseUrl.frontend})"

  companion object {
    private const val CALENDAR_NAME = "ITBio"
    private val stockholmZoneId = TimeZone.getTimeZone("Europe/Stockholm").toZoneId()

    private val log: Logger = LoggerFactory.getLogger(CalendarService::class.java)
  }

  fun getCalendarFeed(userFeedId: UUID): ICalendar {
    val user = userService.lookupUserFromCalendarFeedId(userFeedId) ?: return setupCalendar(userFeedId)

    val cal = setupCalendar(userFeedId)
    showingService
      .getShowingByUser(user.id)
      .map { it.toVEvent() }
      .forEach { cal.addEvent(it) }

    return cal
  }

  private fun setupCalendar(id: UUID): ICalendar {
    val calendar = ICalendar()
    calendar.setUid(id.toString())
    calendar.setExperimentalProperty("X-WR-RECALID", id.toString())
    calendar.setMethod("PUBLISH")
    calendar.productId = ProductId("-//cthdidIT//itbio//EN")
    calendar.calendarScale = CalendarScale.gregorian()
    calendar.addName(CALENDAR_NAME)
    calendar.addExperimentalProperty("X-WR-CALNAME", CALENDAR_NAME)
    calendar.addExperimentalProperty("X-WR-CALDESC", calendarDescription)
    calendar.addDescription(calendarDescription)

    return calendar
  }

  private fun ShowingDTO.toVEvent(): VEvent {
    val movie = movieService.getMovie(this.movieId) ?: return VEvent()
    val showingUrl = "${properties.baseUrl.frontend}/showing/$id"

    val vEvent = VEvent()
    vEvent.setSummary(movie.title).language = "en-us"
    vEvent.setDateStart(Date.from(this.getStartDate()))
    vEvent.setDateEnd(this.getEndDate(movie))
    vEvent.setUid(this.id.toString())
    vEvent.setLocation(this.location.formatAddress())
    vEvent.setDescription(formatDescription(movie))
    vEvent.setUrl(showingUrl)
    vEvent.addCategories(Categories("bio"))
    vEvent.addParticipants(this)
    vEvent.status = if (this.ticketsBought) Status.confirmed() else Status.tentative()

    return vEvent
  }

  private fun formatDescription(movie: Movie): String {
    return "Kolla på bio!\n${if (movie.imdbId.isSupplied()) "http://www.imdb.com/${movie.imdbId.value}/" else ""}"
  }

  private fun VEvent.addParticipants(showingDTO: ShowingDTO) {
    showingDTO.participants.forEach {
      val user = userService.getUser(it.userId)
      if (user != null) {
        val attendee = Attendee("${user.firstName} '${user.nick}' ${user.lastName}", "N/A")
        attendee.participationStatus = ParticipationStatus.CONFIRMED
        this.addAttendee(attendee)
      }
    }
  }

  private fun ShowingDTO.getStartDate(): Instant {
    return LocalDateTime.of(this.date, this.time)
      .atZone(stockholmZoneId)
      .toInstant()
  }

  private fun ShowingDTO.getEndDate(movie: Movie): Date {
    val end = this.getStartDate()
      .plusMillis(movie.getDurationOrDefault2hours().toMillis())
    return Date.from(end)
  }

  private fun Movie.getDurationOrDefault2hours()
    = when {
    this.runtime.isZero -> Duration.ofHours(2).plusMinutes(30)
    else -> this.runtime
  }
}


