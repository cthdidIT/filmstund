package se.filmstund.domain.id

import com.fasterxml.jackson.annotation.JsonValue

enum class ValueState {
  Missing,
  Unknown,
  Supplied
}

fun ExternalProviderId?.isMissing() = this == null || state == ValueState.Missing
fun ExternalProviderId?.isSupplied() = this != null && this.isSupplied()

sealed class ExternalProviderId(val state: ValueState) {

  fun isUnknown() = state == ValueState.Unknown

  fun isSupplied() = state == ValueState.Supplied

  /** The ID is either Unknown or Missing */
  fun isNotSupplied() = !isSupplied()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other?.javaClass != javaClass) return false

    other as ExternalProviderId

    if (state != other.state) return false

    return true
  }

  override fun hashCode(): Int {
    return state.hashCode()
  }

  override fun toString(): String {
    return "ExternalProviderId(state=$state)"
  }
}

class IMDbID(
  @JsonValue
  val value: String? = null,
  state: ValueState = ValueState.Missing
) : ExternalProviderId(state) {

  companion object {
    fun valueOf(imdbId: String) =
      IMDbID(imdbId, ValueState.Supplied)

    /** Missing value but may be updated in the future */
    val MISSING = IMDbID()

    /** We've searched for a value, but none were found */
    val UNKNOWN = IMDbID(null, ValueState.Unknown)
  }

  init {
    if (value != null && !value.matches(Regex("^tt[0-9]{7,8}"))) {
      throw IllegalArgumentException("Illegal IMDb ID format: $value")
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other?.javaClass != javaClass) return false
    if (!super.equals(other)) return false

    other as IMDbID

    if (value != other.value) return false

    return true
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + (value?.hashCode() ?: 0)
    return result
  }

  override fun toString(): String {
    return "IMDbID(value=$value, state=$state)"
  }
}

class TMDbID(
  @JsonValue
  val value: Long? = null,
  state: ValueState = ValueState.Missing
) : ExternalProviderId(state) {

  companion object {
    fun valueOf(tmdbId: Long) =
      TMDbID(tmdbId, ValueState.Supplied)

    /** Missing value but may be updated in the future */
    val MISSING = TMDbID()

    /** We've searched for a value, but none were found */
    val UNKNOWN = TMDbID(null, ValueState.Unknown)
  }

  init {
    require(!(value != null && value <= 0)) { "Illegal TMDb ID. Got: $value" }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other?.javaClass != javaClass) return false
    if (!super.equals(other)) return false

    other as TMDbID

    if (value != other.value) return false

    return true
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + (value?.hashCode() ?: 0)
    return result
  }

  override fun toString(): String {
    return "TMDbID(value=$value, State=$state)"
  }
}