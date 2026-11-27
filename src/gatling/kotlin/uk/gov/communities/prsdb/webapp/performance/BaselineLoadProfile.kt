package uk.gov.communities.prsdb.webapp.performance

internal enum class BaselineFamily {
    LANDLORD,
    SAVED,
    PROPERTY,
    ORGANISATION_PROFILE,
    COMPLIANCE,
    ORGANISATION_TEAM,
    JOINT,
    AGENT,
    OPERATOR,
    OFFBOARDING,
    PAID_REGISTRATION,
    COUNCIL_READ,
}

internal object BaselineLoadProfile {
    fun schedules(properties: Map<String, String>): Map<BaselineFamily, RampSchedule> {
        fun decimal(
            key: String,
            default: Double,
        ): Double {
            val value = properties[key]?.toDouble() ?: default
            require(value.isFinite()) { "$key must be finite" }
            return value
        }
        val rampSeconds = decimal("gatling.ramp.rampMinutes", 5.0) * 60
        val holdSeconds = decimal("gatling.ramp.holdMinutes", 15.0) * 60
        val registrations = decimal("gatling.ramp.registrationsPerMinute", 26.0)
        val updates = decimal("gatling.ramp.updatesPerMinute", 4.0)
        val additional = decimal("gatling.ramp.additionalPerMinute", 1.0)
        val paidRegistrations = decimal("gatling.ramp.paidRegistrationsPerMinute", 26.0)
        val councilReads = decimal("gatling.ramp.councilReadsPerMinute", 1.0)
        return BaselineFamily.entries.associateWith {
            val rate =
                when (it) {
                    BaselineFamily.LANDLORD -> registrations
                    BaselineFamily.SAVED, BaselineFamily.PROPERTY, BaselineFamily.ORGANISATION_PROFILE -> updates
                    BaselineFamily.PAID_REGISTRATION -> paidRegistrations
                    BaselineFamily.COUNCIL_READ -> councilReads
                    else -> additional
                }
            RampSchedule(rate, rampSeconds, holdSeconds)
        }
    }
}
