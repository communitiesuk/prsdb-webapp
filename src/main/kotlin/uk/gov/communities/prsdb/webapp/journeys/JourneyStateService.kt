package uk.gov.communities.prsdb.webapp.journeys

import jakarta.servlet.http.HttpSession
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.objectToStringKeyedMap
import java.util.UUID

@Serializable
data class JourneyMetadata(
    val dataKey: String,
    val baseJourneyId: String? = null,
    val childJourneyName: String? = null,
) {
    companion object {
        fun withNewDataKey(): JourneyMetadata = JourneyMetadata(UUID.randomUUID().toString())
    }
}

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

    var journeyStateMetadataMap: Map<String, JourneyMetadata>
        get() = session.getAttribute(JOURNEY_STATE_METADATA_STORE_KEY)?.let { it as? String }?.let { Json.decodeFromString(it) } ?: mapOf()
        set(value) = session.setAttribute(JOURNEY_STATE_METADATA_STORE_KEY, Json.encodeToString(value))

    val journeyMetadata get() = journeyStateMetadataMap[journeyId] ?: restoreJourneyOrNull() ?: throw NoSuchJourneyException(journeyId)

    private fun restoreJourneyOrNull(journeyToRestore: String = journeyId): JourneyMetadata? {
        if (journeyStateMetadataMap.containsKey(journeyToRestore)) {
            throw JourneyInitialisationException("Journey with ID $journeyToRestore already exists in session")
        }

        val stateToRestore = persistenceService.retrieveJourneyStateData(journeyToRestore) ?: return null

        val metadata = JourneyMetadata.withNewDataKey()
        journeyStateMetadataMap += (journeyToRestore to metadata)

        session.setAttribute(metadata.dataKey, stateToRestore)
        return metadata
    }

    fun copyJourneyTo(newJourneyId: String) {
        val journeyState = session.getAttribute(journeyMetadata.dataKey) ?: mapOf<String, Any?>()
        val newMetadata = journeyStateMetadataMap[newJourneyId] ?: JourneyMetadata.withNewDataKey()
        journeyStateMetadataMap += (newJourneyId to newMetadata)
        session.setAttribute(newMetadata.dataKey, journeyState)
    }

    fun save(): SavedJourneyState {
        val journeyState = session.getAttribute(journeyMetadata.dataKey) ?: mapOf<String, Any?>()
        return persistenceService.saveJourneyStateData(journeyState, journeyId)
    }

    fun getValue(key: String): Any? = objectToStringKeyedMap(session.getAttribute(journeyMetadata.dataKey))?.get(key)

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
        val journeyState = objectToStringKeyedMap(session.getAttribute(journeyMetadata.dataKey)) ?: mapOf()
        session.setAttribute(journeyMetadata.dataKey, journeyState + (key to value))
    }

    fun deleteState() {
        session.removeAttribute(journeyMetadata.dataKey)

        persistenceService.deleteJourneyStateData(journeyMetadata.baseJourneyId ?: journeyId)

        journeyStateMetadataMap = journeyStateMetadataMap.filterNot { (_, metadata) -> metadata.dataKey == journeyMetadata.dataKey }
    }

    fun initialiseJourneyWithId(
        newJourneyId: String,
        stateInitialiser: JourneyStateService.() -> Unit = { },
    ) {
        if (journeyStateMetadataMap.containsKey(newJourneyId)) {
            throw JourneyInitialisationException("Journey with ID $newJourneyId already exists")
        }
        journeyStateMetadataMap += (newJourneyId to JourneyMetadata.withNewDataKey())
        val newService = JourneyStateService(session, journeyIdProvider, persistenceService)
        newService.setJourneyId(newJourneyId)
        newService.stateInitialiser()
    }

    fun initialiseOrRestoreJourneyWithId(
        newJourneyId: String,
        stateInitialiser: JourneyStateService.() -> Unit = { },
    ) {
        if (journeyStateMetadataMap.containsKey(newJourneyId)) {
            return
        }

        val restoredMetadata = restoreJourneyOrNull(newJourneyId)
        if (restoredMetadata != null) {
            return
        }

        journeyStateMetadataMap += (newJourneyId to JourneyMetadata.withNewDataKey())
        val newService = JourneyStateService(session, journeyIdProvider, persistenceService)
        newService.setJourneyId(newJourneyId)
        newService.stateInitialiser()
    }

    fun initialiseChildJourney(
        newJourneyId: String,
        childJourneyName: String,
    ) {
        val existingMetadata = journeyStateMetadataMap[newJourneyId]
        if (existingMetadata != null) {
            throw JourneyInitialisationException("Journey with ID $newJourneyId already exists")
        }
        val metadata =
            JourneyMetadata(
                dataKey = journeyMetadata.dataKey,
                baseJourneyId = journeyId,
                childJourneyName = childJourneyName,
            )
        journeyStateMetadataMap = journeyStateMetadataMap + (newJourneyId to metadata)
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
