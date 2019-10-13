package rocks.didit.sefilm.services

import org.assertj.core.api.Assertions.assertThat
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import rocks.didit.sefilm.NotFoundException
import rocks.didit.sefilm.TestConfig
import rocks.didit.sefilm.database.DbConfig
import rocks.didit.sefilm.database.dao.MovieDao
import rocks.didit.sefilm.nextMovie
import rocks.didit.sefilm.services.external.FilmstadenService
import rocks.didit.sefilm.utils.MovieFilterUtil
import java.util.concurrent.ThreadLocalRandom

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [Jdbi::class, MovieService::class, MovieFilterUtil::class])
@Import(TestConfig::class, DbConfig::class)
internal class MovieServiceTest {
  @MockBean
  private lateinit var filmstadenService: FilmstadenService

  @Autowired
  private lateinit var movieService: MovieService

  @Autowired
  private lateinit var movieDao: MovieDao

  private val rnd: ThreadLocalRandom = ThreadLocalRandom.current()

  @Test
  internal fun `given no movie, when getMovie(null), then null is returned`() {
    assertThat(movieService.getMovie(null)).isNull()
  }

  @Test
  internal fun `given no movie, when getMovieOrThrow(null), then an exception is thrown`() {
    assertThrows<NotFoundException> {
      movieService.getMovieOrThrow(null)
    }
  }

  @Test
  internal fun `given a movie, when getMovie(id), then movie is returned`() {
    val rndMovie = rnd.nextMovie()
    movieDao.insertMovie(rndMovie)

    val dbMovie = movieService.getMovie(rndMovie.id)
    assertThat(dbMovie)
      .isNotNull
      .isEqualTo(rndMovie)
  }

  // TODO: test fetchNewMoviesFromFilmstaden
}