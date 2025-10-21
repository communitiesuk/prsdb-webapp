package uk.gov.communities.prsdb.webapp.journeys

import jakarta.servlet.ServletRequest
import jakarta.servlet.http.HttpSession
import org.springframework.web.context.annotation.RequestScope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.objectToStringKeyedMap
import uk.gov.communities.prsdb.webapp.forms.objectToTypedStringKeyedMap
import java.util.UUID

@PrsdbWebService
@RequestScope
class JourneyStateServiceFactory(
    private val session: HttpSession,
    request: ServletRequest,
) {
    val journeyId: String = request.getParameter("journeyId") ?: UUID.randomUUID().toString()

    fun createForNewJourney(): JourneyStateService {
        val journeyStateMetadataMap = objectToTypedStringKeyedMap<String>(session.getAttribute(JOURNEY_STATE_KEY_STORE_KEY)) ?: mapOf()
        val journeyDataKey = UUID.randomUUID().toString()
        session.setAttribute(JOURNEY_STATE_KEY_STORE_KEY, journeyStateMetadataMap + (journeyId to journeyDataKey))
        return JourneyStateService(session, journeyDataKey, journeyId)
    }

    fun createForExistingJourney(): JourneyStateService {
        val journeyStateMetadataMap = objectToTypedStringKeyedMap<String>(session.getAttribute(JOURNEY_STATE_KEY_STORE_KEY)) ?: mapOf()
        val journeyDataKey = journeyStateMetadataMap[journeyId] ?: throw NoSuchJourneyException(journeyId)
        return JourneyStateService(session, journeyDataKey, journeyId)
    }

    companion object {
        const val JOURNEY_STATE_KEY_STORE_KEY = "journeyStateKeyStore"
    }
}

class JourneyStateService(
    private val session: HttpSession,
    private val journeyDataKey: String,
    val journeyId: String,
) {
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
    }

    companion object {
        private const val STEP_DATA_KEY = "journeyData"
    }
}
