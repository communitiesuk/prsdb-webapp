package uk.gov.communities.prsdb.webapp.journeys

import jakarta.servlet.http.HttpSession
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.objectToStringKeyedMap

@JourneyFrameworkComponent
class JourneyStateService(
    private val session: HttpSession,
    private val journeyIdProvider: JourneyIdProvider,
    private val persistenceService: JourneyStatePersistenceService,
) {
    private var _journeyId: String? = null

    val journeyId: String
        get() {
            _journeyId?.let { return it }
            val idFromRequest = journeyIdProvider.getParameterOrNull()
            if (idFromRequest != null) {
                _journeyId = idFromRequest
                return idFromRequest
            }
            throw NoSuchJourneyException()
        }

    fun setJourneyId(id: String) {
        if (_journeyId != null) {
            throw JourneyInitialisationException("Journey ID has already been set to $_journeyId and cannot be changed")
        }
        _journeyId = id
    }

    private var journeyStateMetadataStore: JourneyMetadataStore
        get() =
            session.getAttribute(JOURNEY_STATE_METADATA_STORE_KEY)?.let { it as? String }?.let { Json.decodeFromString(it) }
                ?: JourneyMetadataStore()
        set(value) = session.setAttribute(JOURNEY_STATE_METADATA_STORE_KEY, Json.encodeToString(value))

    val journeyMetadata get() = journeyStateMetadataStore[journeyId] ?: restoreJourneyOrNull() ?: throw NoSuchJourneyException(journeyId)

    private fun restoreJourneyOrNull(journeyToRestore: String = journeyId): JourneyMetadata? {
        if (journeyStateMetadataStore.contains(journeyToRestore)) {
            throw JourneyInitialisationException("Journey with ID $journeyToRestore already exists in session")
        }

        val stateToRestore = persistenceService.retrieveJourneyStateData(journeyToRestore) ?: return null

        val metadata = JourneyMetadata(journeyToRestore)
        journeyStateMetadataStore += metadata

        session.setAttribute(metadata.journeyId, stateToRestore)
        return metadata
    }

    fun copyJourneyTo(newJourneyId: String) {
        val newMetadata = journeyStateMetadataStore[newJourneyId] ?: JourneyMetadata(newJourneyId, baseJourneyId = journeyId)
        journeyStateMetadataStore += newMetadata
        val journeyState = session.getAttribute(journeyId) ?: mapOf<String, Any?>()
        session.setAttribute(newMetadata.journeyId, journeyState)
    }

    fun save(): SavedJourneyState = persistenceService.saveJourneyStateData(session.getAttribute(journeyId), journeyId)

    fun getValue(key: String): Any? = objectToStringKeyedMap(session.getAttribute(journeyId))?.get(key)

    fun addSingleStepData(
        key: String,
        value: PageData,
    ) {
        val newJourneyData = getSubmittedStepData() + (key to value)
        setValue(STEP_DATA_KEY, newJourneyData)
    }

    fun clearStepData(key: String) {
        val newJourneyData = getSubmittedStepData() - key
        setValue(STEP_DATA_KEY, newJourneyData)
    }

    fun getSubmittedStepData() = objectToStringKeyedMap(getValue(STEP_DATA_KEY)) ?: emptyMap()

    fun setValue(
        key: String,
        value: Any?,
    ) {
        val journeyState = objectToStringKeyedMap(session.getAttribute(journeyId)) ?: mapOf()
        session.setAttribute(journeyId, journeyState + (key to value))
    }

    fun deleteState() {
        val dependentJourneys = journeyStateMetadataStore.filter { it.baseJourneyId == journeyId }

        dependentJourneys.forEach {
            session.removeAttribute(it.journeyId)
            persistenceService.deleteJourneyStateData(it.journeyId)
            journeyStateMetadataStore -= it.journeyId
        }

        session.removeAttribute(journeyId)

        persistenceService.deleteJourneyStateData(journeyId)

        journeyStateMetadataStore -= journeyId
    }

    fun initialiseJourneyWithId(
        newJourneyId: String,
        stateInitialiser: JourneyStateService.() -> Unit = { },
    ) {
        if (journeyStateMetadataStore.contains(newJourneyId)) {
            throw JourneyInitialisationException("Journey with ID $newJourneyId already exists")
        }
        journeyStateMetadataStore += JourneyMetadata(newJourneyId)
        val newService = JourneyStateService(session, journeyIdProvider, persistenceService)
        newService.setJourneyId(newJourneyId)
        newService.stateInitialiser()
    }

    fun initialiseOrRestoreJourneyWithId(
        newJourneyId: String,
        stateInitialiser: JourneyStateService.() -> Unit = { },
    ) {
        if (journeyStateMetadataStore.contains(newJourneyId)) {
            return
        }

        val restoredMetadata = restoreJourneyOrNull(newJourneyId)
        if (restoredMetadata != null) {
            return
        }

        journeyStateMetadataStore += JourneyMetadata(newJourneyId)
        val newService = JourneyStateService(session, journeyIdProvider, persistenceService)
        newService.setJourneyId(newJourneyId)
        newService.stateInitialiser()
    }

    companion object {
        private const val STEP_DATA_KEY = "journeyData"
        private const val JOURNEY_STATE_METADATA_STORE_KEY = "journeyStateKeyStore"

        fun urlWithJourneyState(
            path: String,
            journeyId: String,
            urlParams: Map<String, String> = mapOf(),
        ): String =
            UriComponentsBuilder
                .newInstance()
                .path(path)
                .queryParam(JourneyIdProvider.PARAMETER_NAME, journeyId)
                .apply { urlParams.forEach { (key, value) -> queryParam(key, value) } }
                .build(true)
                .toUriString()
    }
}
