package rocks.didit.sefilm.domain.dto

import rocks.didit.sefilm.database.entities.Location
import java.io.Serializable
import java.time.ZonedDateTime

data class CalendarEventDTO(val summary: String,
                            val location: String? = null,
                            val attendees: List<Map<String, String>>,
                            val start: Map<String, Serializable>,
                            val end: Map<String, Serializable>) {

  companion object Factory {
    fun of(summary: String, location: Location?, emails: Collection<String>, start: ZonedDateTime, end: ZonedDateTime): CalendarEventDTO {

      return CalendarEventDTO(
        summary = summary,
        location = location?.name ?: "N/A",
        attendees = emails.map { mapOf("email" to it) },
        start = mapOf("dateTime" to start.toOffsetDateTime().toString()),
        end = mapOf("dateTime" to end.toOffsetDateTime().toString())
      )
    }
  }
}