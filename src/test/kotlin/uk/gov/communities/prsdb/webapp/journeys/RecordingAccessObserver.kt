package uk.gov.communities.prsdb.webapp.journeys

class RecordingAccessObserver : JourneyStateAccessObserver {
    val events = mutableListOf<String>()

    override fun read(key: String) {
        events += "read:$key"
    }

    override fun written(key: String) {
        events += "written:$key"
    }

    override fun modeStarted(step: JourneyStep<*, *, *>) {
        events += "modeStarted"
    }

    override fun modeFinished(
        step: JourneyStep<*, *, *>,
        mode: Enum<*>?,
    ) {
        events += "modeFinished:${mode?.name}"
    }

    override fun outcomeRead(step: JourneyStep<*, *, *>) {
        events += "outcomeRead"
    }

    override fun stepPassed(
        step: JourneyStep<*, *, *>,
        mode: Enum<*>?,
    ) {
        events += "stepPassed:${mode?.name}"
    }

    fun <T> observe(block: () -> T): T {
        val previous = JourneyStateService.accessObserver
        JourneyStateService.accessObserver = this
        try {
            return block()
        } finally {
            JourneyStateService.accessObserver = previous
        }
    }
}
