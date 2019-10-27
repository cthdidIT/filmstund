package se.filmstund.services

import org.assertj.core.api.Assertions.assertThat
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import se.filmstund.TestConfig
import se.filmstund.WithLoggedInUser
import se.filmstund.currentLoggedInUser
import se.filmstund.database.DbConfig
import se.filmstund.database.dao.UserDao
import se.filmstund.domain.Nick
import se.filmstund.domain.PhoneNumber
import se.filmstund.domain.dto.core.GiftCertificateDTO
import se.filmstund.domain.dto.input.UserDetailsDTO
import se.filmstund.domain.id.FilmstadenMembershipId
import se.filmstund.nextGiftCerts
import java.time.LocalDate
import java.util.concurrent.ThreadLocalRandom

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [Jdbi::class, UserService::class, GiftCertificateService::class])
@Import(TestConfig::class, DbConfig::class)
internal class UserServiceTest {
  @Autowired
  private lateinit var userService: UserService

  @Autowired
  private lateinit var userDao: UserDao

  @Test
  @WithLoggedInUser
  internal fun `given a logged in user, when updateUser(null, null, null), then all values are removed`() {
    val userDetails = UserDetailsDTO(null, null, null)

    val beforeUpdate = userDao.findById(currentLoggedInUser().id)
    assertThat(beforeUpdate?.nick)
      .isNotNull
    assertThat(beforeUpdate?.phone)
      .isNotNull
    assertThat(beforeUpdate?.filmstadenMembershipId)
      .isNotNull

    val afterUpdate = userService.updateUser(userDetails)
    assertThat(afterUpdate)
      .isEqualToIgnoringGivenFields(beforeUpdate, "nick", "phone", "filmstadenMembershipId", "lastModifiedDate")
    assertThat(afterUpdate.nick)
      .describedAs("nick")
      .isNotNull
      .isEqualTo(Nick(""))
    assertThat(afterUpdate.phone)
      .describedAs("phone")
      .isNull()
    assertThat(afterUpdate.filmstadenMembershipId)
      .describedAs("filmstadenId")
      .isNull()
    assertThat(afterUpdate.lastModifiedDate)
      .isAfter(beforeUpdate?.lastModifiedDate)
  }

  @Test
  @WithLoggedInUser
  internal fun `given a logged in user, when updateUser(), then all values are updated`() {
    val userDetails = UserDetailsDTO(Nick("newNick"), PhoneNumber("073-0000000"), FilmstadenMembershipId("abc-xyz"))

    val beforeUpdate = userDao.findById(currentLoggedInUser().id)
    assertThat(beforeUpdate?.nick)
      .isNotNull
      .isNotEqualTo("newNick")
    assertThat(beforeUpdate?.phone)
      .isNotNull
      .isNotEqualTo("073-0000000")
    assertThat(beforeUpdate?.filmstadenMembershipId)
      .isNotNull
      .isNotEqualTo("abc-xyz")

    val afterUpdate = userService.updateUser(userDetails)
    assertThat(afterUpdate)
      .isEqualToIgnoringGivenFields(beforeUpdate, "nick", "phone", "filmstadenMembershipId", "lastModifiedDate")
    assertThat(afterUpdate.nick)
      .describedAs("nick")
      .isNotNull
      .isEqualTo(Nick("newNick"))
    assertThat(afterUpdate.phone)
      .describedAs("phone")
      .isNotNull
      .isEqualTo(PhoneNumber("073-000 00 00"))
    assertThat(afterUpdate.filmstadenMembershipId)
      .describedAs("filmstadenId")
      .isNotNull
      .isEqualTo(FilmstadenMembershipId("abc-xyz"))
    assertThat(afterUpdate.lastModifiedDate)
      .isAfter(beforeUpdate?.lastModifiedDate)
  }

  @Test
  @WithLoggedInUser
  internal fun `given a logged in user, when invalidateCalendarFeedId(), then a new calendar feed is generated`() {
    val beforeUpdate = userDao.findById(currentLoggedInUser().id)

    val afterUpdate = userService.invalidateCalendarFeedId()
    assertThat(afterUpdate)
      .isEqualToIgnoringGivenFields(beforeUpdate, "calendarFeedId", "lastModifiedDate")
    assertThat(afterUpdate.calendarFeedId)
      .isNotEqualTo(beforeUpdate?.calendarFeedId)
  }

  @Test
  @WithLoggedInUser
  internal fun `given a logged in user, when disableCalendarFeedId(), then the calendar feed id is nullified`() {
    val beforeUpdate = userDao.findById(currentLoggedInUser().id)

    val afterUpdate = userService.disableCalendarFeed()
    assertThat(afterUpdate)
      .isEqualToIgnoringGivenFields(beforeUpdate, "calendarFeedId", "lastModifiedDate")
    assertThat(beforeUpdate?.calendarFeedId)
      .isNotNull
    assertThat(afterUpdate.calendarFeedId)
      .isNull()
  }

  @Test
  @WithLoggedInUser
  internal fun `given a logged in user, when getCurrentUser(), then the gift cert statuses are not unknown`() {
    val nextGiftCerts = ThreadLocalRandom.current().nextGiftCerts(currentLoggedInUser().id)
    userDao.insertGiftCertificates(nextGiftCerts)

    nextGiftCerts.forEach {
      assertThat(it.status).isEqualTo(GiftCertificateDTO.Status.UNKNOWN)
    }
    val currentUser = userService.getCurrentUser()
    currentUser.giftCertificates.forEach {
      if (it.expiresAt.isBefore(LocalDate.now())) {
        assertThat(it.status).isEqualTo(GiftCertificateDTO.Status.EXPIRED)
      } else {
        assertThat(it.status).isEqualTo(GiftCertificateDTO.Status.AVAILABLE)
      }
    }
  }
}