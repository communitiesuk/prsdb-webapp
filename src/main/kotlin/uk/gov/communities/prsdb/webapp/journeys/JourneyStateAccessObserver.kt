package uk.gov.communities.prsdb.webapp.journeys

// Lets local tooling see which journey state each step reads and writes; it is never set outside tests.
interface JourneyStateAccessObserver {
    fun read(key: String) {}

    fun written(key: String) {}

    fun modeStarted(step: JourneyStep<*, *, *>) {}

    fun modeFinished(
        step: JourneyStep<*, *, *>,
        mode: Enum<*>?,
    ) {}

    fun outcomeRead(step: JourneyStep<*, *, *>) {}

    fun stepPassed(
        step: JourneyStep<*, *, *>,
        mode: Enum<*>?,
    ) {}

    companion object {
        const val STEP_KEY_PREFIX = "step:"
        const val ALL_STEP_DATA = "step:*"

        fun stepKey(key: String) = STEP_KEY_PREFIX + key
    }
}
