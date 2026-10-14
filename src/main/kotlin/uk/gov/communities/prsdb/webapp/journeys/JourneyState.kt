package uk.gov.communities.prsdb.webapp.journeys

import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState

interface JourneyState : DelegateKeysOwner {
    fun getStepData(key: String): FormData?

    fun addStepData(
        key: String,
        value: FormData,
    )

    fun clearStepData(key: String)

    fun getSubmittedStepData(): Map<String, Any?>

    /** The scoped keys of every delegate in the journey that holds an answer shown on a check your answers page. */
    val cyaAnswerDelegateKeys: Set<String>

    /** The value stored for the delegate with the given scoped key, in its encoded form. */
    fun getEncodedDelegateValue(key: String): Any?

    val journeyId: String
    val journeyMetadata: JourneyMetadata

    fun deleteJourney()

    fun initializeState(seed: Any? = null): String

    fun initializeOrRestoreState(seed: Any?): String

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

    fun save(): SavedJourneyState

    fun setJourneyId(newJourneyId: String)

    fun copyJourneyTo(newJourneyId: String)
}
