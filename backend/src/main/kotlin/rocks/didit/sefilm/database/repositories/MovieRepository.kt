package rocks.didit.sefilm.database.repositories

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import rocks.didit.sefilm.database.entities.Movie
import java.util.*

interface MovieRepository : ReactiveCrudRepository<Movie, UUID>