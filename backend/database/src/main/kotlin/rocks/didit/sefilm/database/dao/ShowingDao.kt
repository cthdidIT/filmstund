package rocks.didit.sefilm.database.dao

import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import org.jdbi.v3.sqlobject.statement.UseRowReducer
import rocks.didit.sefilm.database.ShowingLocationReducer
import rocks.didit.sefilm.domain.dto.FilmstadenLiteScreenDTO
import rocks.didit.sefilm.domain.dto.core.ShowingDTO
import java.util.*

interface ShowingDao {
  companion object {
    const val SELECTABLE_FIELDS =
      "s.id, s.web_id, s.slug, s.date, s.time, s.movie_id, s.filmstaden_showing_id, s.price, s.tickets_bought, s.admin, s.pay_to_user, s.last_modified_date, s.created_date"
  }

  @SqlQuery("SELECT $SELECTABLE_FIELDS, ${LocationDao.SELECTABLE_FIELDS}, la.alias la_alias, cs.id cs_filmstadenId, cs.name cs_name FROM showing s JOIN location l ON s.location_id = l.name LEFT JOIN location_alias la ON l.name = la.location LEFT JOIN cinema_screen cs ON s.cinema_screen_id = cs.id WHERE s.id = :showingId")
  @UseRowReducer(ShowingLocationReducer::class)
  fun findById(showingId: UUID): ShowingDTO?

  @SqlQuery("SELECT exists(SELECT 1 FROM participant p WHERE p.showing_id = :showingId and p.user_id = :userId)")
  fun isParticipantOnShowing(userId: UUID, showingId: UUID): Boolean

  @SqlUpdate("UPDATE showing s SET admin = :newAdmin, pay_to_user = :newAdmin WHERE s.id = :showingId and s.admin = :currentAdmin")
  fun promoteNewUserToAdmin(showingId: UUID, currentAdmin: UUID, newAdmin: UUID): Int

  @SqlUpdate("INSERT INTO showing(id, web_id, slug, date, time, movie_id, location_id, cinema_screen_id, filmstaden_showing_id, price, tickets_bought, admin, pay_to_user) values (:id, :webId, :slug, :date, :time, :movieId, :location.name, :cinemaScreen.filmstadenId, :filmstadenShowingId, :price, :ticketsBought, :admin, :payToUser)")
  fun insertNewShowing(@BindBean showing: ShowingDTO)

  @SqlUpdate("INSERT INTO cinema_screen (id, name) VALUES (:filmstadenId, :name) ON CONFLICT DO NOTHING")
  fun maybeInsertCinemaScreen(@BindBean cinemaScreen: FilmstadenLiteScreenDTO): Int

  fun insertShowingAndCinemaScreen(showing: ShowingDTO) {
    showing.cinemaScreen?.let { maybeInsertCinemaScreen(it) }
    insertNewShowing(showing)
  }
}