package uk.gov.communities.prsdb.webapp.journeys

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.ServletRequest
import jakarta.servlet.http.HttpSession
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import uk.gov.communities.prsdb.webapp.database.repository.OneLoginUserRepository
import uk.gov.communities.prsdb.webapp.database.repository.SavedJourneyStateRepository
import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.objectToStringKeyedMap
import java.util.UUID
import kotlin.collections.plus

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
class StateSaver(
    private val journeyRepository: SavedJourneyStateRepository,
    private val oneLoginUserRepository: OneLoginUserRepository,
    private val objectMapper: ObjectMapper,
) {
    val user: Authentication get() = SecurityContextHolder.getContext().authentication

    fun saveJourneyStateData(
        stateData: Any,
        journeyId: String,
    ): Long {
        val serializedState = objectMapper.writeValueAsString(stateData)
        val formContext =
            journeyRepository
                .findByJourneyIdAndUser_Id(journeyId, user.name)
                ?.also { it.serializedState = serializedState }
                ?: SavedJourneyState(
                    serializedState = serializedState,
                    user = oneLoginUserRepository.getReferenceById(user.name),
                    journeyId = journeyId,
                )
        val savedState = journeyRepository.save(formContext)
        return savedState.id
    }

    fun retrieveJourneyStateData(journeyId: String): Any =
        journeyRepository
            .findByJourneyIdAndUser_Id(journeyId, user.name)
            ?.let { objectMapper.readValue(it.toSessionState(), Any::class.java) }
            ?: throw NoSuchJourneyException(journeyId)
}

@PrsdbWebService
@Scope("request")
class JourneyStateService(
    private val session: HttpSession,
    private val journeyIdOrNull: String?,
    private val stateSaver: StateSaver? = null,
) {
    val journeyId: String get() = journeyIdOrNull ?: throw NoSuchJourneyException()

    @Autowired
    constructor(
        session: HttpSession,
        request: ServletRequest,
        stateSaver: StateSaver,
    ) : this(
        session,
        request.getParameter(JOURNEY_ID_PARAM),
        stateSaver,
    )

    var journeyStateMetadataMap: Map<String, JourneyMetadata>
        get() = session.getAttribute(JOURNEY_STATE_METADATA_STORE_KEY)?.let { it as? String }?.let { Json.decodeFromString(it) } ?: mapOf()
        set(value) = session.setAttribute(JOURNEY_STATE_METADATA_STORE_KEY, Json.encodeToString(value))

    val journeyMetadata get() = journeyStateMetadataMap[journeyId] ?: restoreJourney()

    fun restoreJourney(): JourneyMetadata {
        if (journeyStateMetadataMap.containsKey(journeyId)) {
            throw JourneyInitialisationException("Journey with ID $journeyId already exists in session")
        }
        val stateToRestore =
            stateSaver?.retrieveJourneyStateData(journeyId) ?: throw Exception("Optional stateSaver not provided to restore journey state")

        val metadata = JourneyMetadata.withNewDataKey()
        journeyStateMetadataMap += (journeyId to metadata)

        session.setAttribute(metadata.dataKey, stateToRestore)
        return metadata
    }

    fun save(): Long {
        val journeyState = session.getAttribute(journeyMetadata.dataKey) ?: mapOf<String, Any?>()
        return stateSaver?.saveJourneyStateData(journeyState, journeyId)
            ?: throw Exception("Optional stateSaver not provided to restore journey state")
    }

    fun getValue(key: String): Any? = objectToStringKeyedMap(session.getAttribute(journeyMetadata.dataKey))?.get(key)

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
        val journeyState = objectToStringKeyedMap(session.getAttribute(journeyMetadata.dataKey)) ?: mapOf()
        session.setAttribute(journeyMetadata.dataKey, journeyState + (key to value))
    }

    fun deleteState() {
        session.removeAttribute(journeyMetadata.dataKey)

        val journeyIdsToRemove =
            journeyStateMetadataMap
                .filter { (_, metadata) -> metadata.dataKey == journeyMetadata.dataKey }
                .keys

        journeyIdsToRemove.forEach { id ->
            journeyStateMetadataMap -= id
        }
    }

    fun initialiseJourneyWithId(
        newJourneyId: String,
        stateInitialiser: (JourneyStateService.() -> Unit)? = null,
    ) {
        if (journeyStateMetadataMap.containsKey(newJourneyId)) {
            throw JourneyInitialisationException("Journey with ID $newJourneyId already exists")
        }
        journeyStateMetadataMap += (newJourneyId to JourneyMetadata.withNewDataKey())
        stateInitialiser?.let {
            JourneyStateService(session, newJourneyId, stateSaver).it()
        }
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
        ): String =
            UriComponentsBuilder
                .newInstance()
                .path(path)
                .queryParam(JOURNEY_ID_PARAM, journeyId)
                .build(true)
                .toUriString()
    }
}
