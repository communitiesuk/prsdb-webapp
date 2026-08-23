package uk.gov.communities.prsdb.webapp.constants.enums

enum class FeatureFlagOverrideChoice {
    DEFAULT,
    ON,
    OFF,
    ;

    fun toOverrideOrNull(): Boolean? =
        when (this) {
            DEFAULT -> null
            ON -> true
            OFF -> false
        }

    companion object {
        fun fromOverride(override: Boolean?): FeatureFlagOverrideChoice =
            when (override) {
                null -> DEFAULT
                true -> ON
                false -> OFF
            }
    }
}
