package rocks.didit.sefilm.database.dao

import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.customizer.Timestamped
import org.jdbi.v3.sqlobject.statement.SqlBatch
import org.jdbi.v3.sqlobject.statement.SqlQuery
import rocks.didit.sefilm.domain.dto.core.MovieDTO
import java.util.*

interface MovieDao {
  @SqlQuery("SELECT count(1) FROM movie")
  fun count(): Int

  @SqlQuery("SELECT exists(SELECT 1 FROM movie WHERE id = :id)")
  fun existsById(id: UUID): Boolean

  @SqlQuery("SELECT exists(SELECT 1 FROM movie WHERE filmstaden_id = :filmstadenId)")
  fun existsByFilmstadenId(filmstadenId: String): Boolean

  @SqlQuery("SELECT * FROM movie m ORDER BY archived, popularity desc")
  fun findAll(): List<MovieDTO>

  @SqlQuery("SELECT * FROM movie m WHERE m.archived = :archived ORDER BY popularity desc")
  fun findByArchivedOrderByPopularityDesc(archived: Boolean = false): List<MovieDTO>

  @SqlQuery("SELECT * FROM movie WHERE id = :id")
  fun findById(id: UUID): MovieDTO?

  @SqlBatch("INSERT INTO movie (id, filmstaden_id, imdb_id, tmdb_id, slug, title, synopsis, original_title, release_date, production_year, runtime, poster, genres, popularity, popularity_last_updated, archived, last_modified_date, created_date) VALUES (:id, :filmstadenId, :imdbId, :tmdbId, :slug, :title, :synopsis, :originalTitle, :releaseDate, :productionYear, :runtime, :poster, :genres, :popularity, :popularityLastUpdated, :archived, :lastModifiedDate, :createdDate)")
  fun insertMovies(@BindBean movie: List<MovieDTO>): IntArray

  fun insertMovie(movie: MovieDTO) = insertMovies(listOf(movie))

  @Timestamped
  @SqlQuery("UPDATE movie m SET filmstaden_id = :filmstadenId, imdb_id = :imdbId, tmdb_id = :tmdbId, slug = :slug, title = :title, synopsis = :synopsis, original_title = :originalTitle, release_date = :releaseDate, production_year = :productionYear, runtime = :runtime, poster = :poster, genres = :genres, popularity = :popularity, popularity_last_updated = :popularityLastUpdated, last_modified_date = :now WHERE m.id = :id AND m.last_modified_date = :lastModifiedDate RETURNING *")
  fun updateMovie(@BindBean movie: MovieDTO): MovieDTO?

  @Timestamped
  @SqlQuery("UPDATE movie m SET archived = true, last_modified_date = :now WHERE m.id = :id AND m.last_modified_date = :lastModifiedDate RETURNING *")
  fun archiveMovie(@BindBean movie: MovieDTO): MovieDTO?
}