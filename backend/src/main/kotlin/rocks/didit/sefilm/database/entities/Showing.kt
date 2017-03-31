package rocks.didit.sefilm.database.entities

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

@Document
data class Showing(@Id
                   val id: UUID = UUID.randomUUID(),
                   val date: LocalDate? = null,
                   val time: LocalTime? = null,
                   val movieId: UUID? = null,
                   val location: Location? = null,
                   val private: Boolean = false,
                   val admin: User? = null,
                   val participants: Collection<User> = listOf(),
                   @LastModifiedDate
                   val lastModifiedDate: Instant = Instant.EPOCH)
