package uk.gov.communities.prsdb.webapp.journeys

import jakarta.servlet.ServletRequest
import jakarta.servlet.http.HttpSession
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
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

@PrsdbWebService
@Scope("request")
class JourneyStateService(
    private val session: HttpSession,
    private val journeyIdOrNull: String?,
    private val persistenceService: JourneyStatePersistenceService,
) {
    val journeyId: String get() = journeyIdOrNull ?: throw NoSuchJourneyException()

    @Autowired
    constructor(
        session: HttpSession,
        request: ServletRequest,
        persistenceService: JourneyStatePersistenceService,
    ) : this(
        session,
        request.getParameter(JOURNEY_ID_PARAM),
        persistenceService,
    )

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

    fun removeSingleStepData(key: String) {
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
        JourneyStateService(session, newJourneyId, persistenceService).stateInitialiser()
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
        JourneyStateService(session, newJourneyId, persistenceService).stateInitialiser()
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
        private const val JOURNEY_ID_PARAM = "journeyId"

        fun urlWithJourneyState(
            path: String,
            journeyId: String,
            urlParams: Map<String, String> = mapOf(),
        ): String =
            UriComponentsBuilder
                .newInstance()
                .path(path)
                .queryParam(JOURNEY_ID_PARAM, journeyId)
                .apply { urlParams.forEach { (key, values) -> values.forEach { value -> queryParam(key, value) } } }
                .build(true)
                .toUriString()
    }
}
