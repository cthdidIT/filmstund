package rocks.didit.sefilm.domain

import com.fasterxml.jackson.annotation.JsonValue
import java.security.SecureRandom
import java.util.*

data class Base64ID(val id: String = MISSING_VALUE) {
  companion object {
    const val STANDARD_LENGTH = 7
    const val MISSING_VALUE = "MISSING-BASE64-ID"

    val MISSING = Base64ID(MISSING_VALUE)

    fun random(): Base64ID {
      val random = SecureRandom()
      val bytes = ByteArray(128)
      random.nextBytes(bytes)
      val id = Base64.getEncoder()
        .withoutPadding()
        .encodeToString(bytes)
        .substring(0, STANDARD_LENGTH)
        .replace('+', randomChar(random))
        .replace('/', randomChar(random))
      return Base64ID(id)
    }

    private fun randomChar(random: SecureRandom): Char {
      val value = random.nextInt(26)
      return 'a' + value
    }
  }

  init {
    if (id.isBlank()) {
      throw IllegalArgumentException("ID may not be blank")
    }
  }

  @JsonValue
  override fun toString(): String {
    return id
  }
}