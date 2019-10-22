package se.filmstund.domain

import org.junit.Assert
import org.junit.jupiter.api.Test
import se.filmstund.domain.id.Base64ID

internal class Base64IDTest {
  companion object {
    const val STANDARD_ID_LENGTH = 7
  }

  @Test
  fun testLengthOfRandomId() {
    val randomId = Base64ID.random()
    println("Randomized ID: $randomId")
    Assert.assertEquals(STANDARD_ID_LENGTH, randomId.id.length)
  }
}