package rocks.didit.sefilm.database.entities

import java.time.LocalDate
import java.time.LocalTime
import javax.persistence.*

@Entity
@Table
data class Ticket(
  @Id
  val id: String = "",

  @OneToOne(optional = false, orphanRemoval = true)
  val showing: Showing,

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "assigned_to_user")
  val assignedToUser: User,

  val profileId: String?,

  val barcode: String,

  val customerType: String,
  val customerTypeDefinition: String,

  val cinema: String,
  val cinemaCity: String?,
  val screen: String,
  val seatRow: Int,
  val seatNumber: Int,
  val date: LocalDate,
  val time: LocalTime,
  val movieName: String,
  val movieRating: String, // 15 år, 11 år etc.

  @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
  @JoinColumn(name = "ticket_id")
  val showAttributes: MutableList<TicketAttribute> = mutableListOf() // "textad", "en" etc
)
