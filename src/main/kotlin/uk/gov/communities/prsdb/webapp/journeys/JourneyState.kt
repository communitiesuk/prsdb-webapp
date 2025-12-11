package uk.gov.communities.prsdb.webapp.journeys

import uk.gov.communities.prsdb.webapp.forms.PageData

interface JourneyState {
    fun getStepData(key: String): PageData?

    fun addStepData(
        key: String,
        value: PageData,
    )

    fun getSubmittedStepData(): Map<String, Any?>

    val journeyId: String
    val journeyMetadata: JourneyMetadata

    fun deleteJourney()

    fun initializeChildState(
        childJourneyName: String,
        seed: Any? = null,
    ): String

    fun initializeState(seed: Any? = null): String

    fun generateJourneyId(seed: Any? = null): String =
        if (seed == null) {
            val allowedChars = ('a'..'z') + ('0'..'9')
            String(CharArray(7) { allowedChars.random() })
        } else {
            seed
                .hashCode()
                .toUInt()
                .times(111113111U)
                .and(0x7FFFFFFFu)
                .toString(36)
        }

    fun save(): Long
}
