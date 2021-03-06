package rocks.didit.sefilm.domain.dto

import rocks.didit.sefilm.domain.UserID
import rocks.didit.sefilm.notification.NotificationSettings
import java.time.Instant
import java.util.*

class UserDTO(
  val id: UserID = UserID("N/A"),
  val name: String? = null,
  val firstName: String? = null,
  val lastName: String? = null,
  val nick: String? = null,
  val email: String = "",
  val filmstadenMembershipId: String? = null,
  val phone: String? = null,
  val avatar: String? = null,
  val foretagsbiljetter: List<ForetagsbiljettDTO> = emptyList(),
  val notificationSettings: NotificationSettings,
  val lastLogin: Instant = Instant.ofEpochSecond(0L),
  val signupDate: Instant = Instant.ofEpochSecond(0L),
  val calendarFeedId: UUID?
) {
  fun toLimitedUserDTO() =
    LimitedUserDTO(this.id, this.name, this.firstName, this.lastName, this.nick, this.phone, this.avatar)
}