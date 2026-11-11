package uk.gov.communities.prsdb.webapp.performance

enum class RampedJourneyFamily(
    val arrivalsPerMinute: Double,
) {
    INDIVIDUAL_LANDLORD(1.0),
    PROPERTY_REGISTRATION(26.0),
    SAVED_PROPERTY(1.0),
    COMPLETED_PROPERTY(1.0),
    CERTIFICATES_AND_COMPLIANCE(1.0),
    ORGANISATION(1.0),
    JOINT_LANDLORD(1.0),
    LETTING_AGENT(1.0),
    SYSTEM_OPERATOR(1.0),
    OFFBOARDING(1.0),
}

object RampedLoadProfile {
    fun schedules(
        rampSeconds: Double,
        holdSeconds: Double,
    ): Map<RampedJourneyFamily, RampSchedule> =
        RampedJourneyFamily.entries.associateWith { family ->
            RampSchedule(family.arrivalsPerMinute, rampSeconds, holdSeconds)
        }
}
