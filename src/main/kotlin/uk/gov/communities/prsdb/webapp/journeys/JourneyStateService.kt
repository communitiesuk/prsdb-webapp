package uk.gov.communities.prsdb.webapp.journeys

import jakarta.servlet.ServletRequest
import jakarta.servlet.http.HttpSession
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.context.annotation.RequestScope
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.objectToStringKeyedMap
import uk.gov.communities.prsdb.webapp.forms.objectToTypedStringKeyedMap
import java.util.UUID

@PrsdbWebService
@RequestScope
class JourneyStateService(
    private val session: HttpSession,
    private val journeyIdOrNull: String?,
) {
    val journeyId: String get() = journeyIdOrNull ?: throw NoSuchJourneyException()

    @Autowired
    constructor(
        session: HttpSession,
        request: ServletRequest,
    ) : this(
        session,
        request.getParameter(JOURNEY_ID_PARAM),
    )

    val journeyStateMetadataMap get() = objectToTypedStringKeyedMap<String>(session.getAttribute(JOURNEY_STATE_KEY_STORE_KEY)) ?: mapOf()
    val journeyDataKey get() = journeyStateMetadataMap[journeyId] ?: throw NoSuchJourneyException(journeyId)

    fun getValue(key: String): Any? = objectToStringKeyedMap(session.getAttribute(journeyDataKey))?.get(key)

    fun addSingleStepData(
        key: String,
        value: PageData,
    ) {
        val newJourneyData = getSubmittedStepData() + (key to value)
        setValue(STEP_DATA_KEY, newJourneyData)
    }

    fun getSubmittedStepData() = objectToStringKeyedMap(getValue(STEP_DATA_KEY)) ?: emptyMap()

    fun setValue(
        key: String,
        value: Any?,
    ) {
        val journeyState = objectToStringKeyedMap(session.getAttribute(journeyDataKey)) ?: mapOf()
        session.setAttribute(journeyDataKey, journeyState + (key to value))
    }

    fun deleteState() {
        session.removeAttribute(journeyDataKey)
        // TODO PRSD-1550 - Ensure other metadata keys referencing this journey are also cleaned up
        session.setAttribute(JOURNEY_STATE_KEY_STORE_KEY, journeyStateMetadataMap - journeyId)
    }

    fun initialiseJourneyWithId(
        newJourneyId: String,
        stateInitialiser: JourneyStateService.() -> Unit = {},
    ) {
        val journeyDataKey = journeyStateMetadataMap[newJourneyId] ?: UUID.randomUUID().toString()
        session.setAttribute(JOURNEY_STATE_KEY_STORE_KEY, journeyStateMetadataMap + (newJourneyId to journeyDataKey))
        JourneyStateService(session, newJourneyId).stateInitialiser()
    }

    companion object {
        private const val STEP_DATA_KEY = "journeyData"
        private const val JOURNEY_STATE_KEY_STORE_KEY = "journeyStateKeyStore"
        private const val JOURNEY_ID_PARAM = "journeyId"

        fun urlWithJourneyState(
            path: String,
            journeyId: String,
        ): String =
            UriComponentsBuilder
                .newInstance()
                .path(path)
                .queryParam(JOURNEY_ID_PARAM, journeyId)
                .build(true)
                .toUriString()

        fun urlToStep(step: JourneyStep.RequestableStep<*, *, *>): String =
            UriComponentsBuilder
                .newInstance()
                .path(step.routeSegment)
                .queryParam(JOURNEY_ID_PARAM, step.currentJourneyId)
                .build(true)
                .toUriString()

        fun urlToStepIfReachable(step: JourneyStep.RequestableStep<*, *, *>) =
            if (step.isStepReachable) {
                urlToStep(step)
            } else {
                null
            }
    }
}
