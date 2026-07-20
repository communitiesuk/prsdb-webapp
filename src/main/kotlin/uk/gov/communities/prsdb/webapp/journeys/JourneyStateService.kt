package uk.gov.communities.prsdb.webapp.journeys

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException

@JourneyFrameworkComponent
class JourneyStateService(
    private val session: HttpSession,
    private val journeyIdProvider: JourneyIdProvider,
    private val persistenceService: JourneyStatePersistenceService,
) {
    private var _journeyId: String? = null
    private val loadAttempted = mutableSetOf<String>()

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
            session
                .getAttribute(JOURNEY_STATE_METADATA_STORE_KEY)
                ?.let { it as? String }
                ?.let { Json.decodeFromString(it) }
                ?: JourneyMetadataStore()
        set(value) = session.setAttribute(JOURNEY_STATE_METADATA_STORE_KEY, Json.encodeToString(value))

    val journeyMetadata get() = journeyStateMetadataStore[journeyId] ?: restoreJourneyOrNull() ?: throw NoSuchJourneyException(journeyId)

    private fun restoreJourneyOrNull(journeyToRestore: String = journeyId): JourneyMetadata? {
        if (journeyStateMetadataStore.contains(journeyToRestore)) {
            throw JourneyInitialisationException("Journey with ID $journeyToRestore already exists in session")
        }

        val stateToRestore = persistenceService.retrieveJourneyStateData(journeyToRestore) ?: return null

        val metadata = JourneyMetadata.createNew(journeyToRestore, basePath = currentRequestBasePath())
        journeyStateMetadataStore += metadata

        session.setAttribute(metadata.journeyId, stateToRestore)
        return metadata
    }

    fun copyJourneyTo(newJourneyId: String) {
        val newMetadata =
            journeyStateMetadataStore[newJourneyId]
                ?: JourneyMetadata.createNew(
                    newJourneyId,
                    baseJourneyId = journeyId,
                    basePath = journeyStateMetadataStore[journeyId]?.basePath,
                )
        journeyStateMetadataStore += newMetadata.copy(lastUpdated = Clock.System.now())
        val journeyState = session.getAttribute(journeyId) ?: mapOf<String, Any?>()
        session.setAttribute(newMetadata.journeyId, journeyState)
    }

    fun save(): SavedJourneyState = persistenceService.saveJourneyStateData(session.getAttribute(journeyId), journeyId)

    fun getValue(key: String): Any? {
        ensureJourneyDataLoaded()
        return objectToStringKeyedMap(session.getAttribute(journeyId))?.get(key)
    }

    private fun ensureJourneyDataLoaded() {
        if (journeyId in loadAttempted) return
        if (session.getAttribute(journeyId) != null) return
        if (journeyStateMetadataStore.contains(journeyId)) return

        loadAttempted.add(journeyId)
        restoreJourneyOrNull(journeyId)
    }

    fun addSingleStepData(
        key: String,
        value: FormData,
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
        updateLastUpdated()
    }

    private fun updateLastUpdated() {
        val metadata = journeyStateMetadataStore[journeyId] ?: return
        journeyStateMetadataStore += metadata.copy(lastUpdated = Clock.System.now())
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
        journeyStateMetadataStore += JourneyMetadata.createNew(newJourneyId, basePath = currentRequestBasePath())
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

        journeyStateMetadataStore += JourneyMetadata.createNew(newJourneyId, basePath = currentRequestBasePath())
        val newService = JourneyStateService(session, journeyIdProvider, persistenceService)
        newService.setJourneyId(newJourneyId)
        newService.stateInitialiser()
    }

    companion object {
        private const val STEP_DATA_KEY = "journeyData"
        private const val JOURNEY_STATE_METADATA_STORE_KEY = "journeyStateKeyStore"

        // Set on the request by the journey dispatcher and read once, when a journey is first created, so the
        // base path can be stored against the journey (JourneyMetadata.basePath). URLs are NOT resolved against
        // this attribute - they look the base path up by journeyId - so a URL depends only on its journey, not
        // on the current request. Journeys created without it (e.g. in mocked controller tests) keep relative URLs.
        const val JOURNEY_BASE_PATH_ATTRIBUTE = "prsdbJourneyBasePath"

        fun urlWithJourneyState(
            path: String,
            journeyId: String,
            urlParams: Map<String, String> = mapOf(),
        ): String =
            UriComponentsBuilder
                .newInstance()
                .path(resolvePathAgainstJourneyBase(path, journeyId))
                .queryParam(JourneyIdProvider.PARAMETER_NAME, journeyId)
                .apply { urlParams.forEach { (key, value) -> queryParam(key, value) } }
                .build(true)
                .toUriString()

        private fun resolvePathAgainstJourneyBase(
            path: String,
            journeyId: String,
        ): String {
            if (path.startsWith("/")) return path
            // Prefer the base path stored against the journey. When none is stored - for example the journey has
            // expired from the session but its id is still in the URL - fall back to the base path the dispatcher
            // derived for the current request, so the redirect stays absolute rather than relative (a relative URL
            // would be resolved by the browser against the current path and duplicate the task route segment).
            // If neither is available - a controller not yet ported to the journey dispatcher never sets the
            // request base path - the URL stays relative and behaves exactly as before.
            val basePath = journeyBasePath(journeyId) ?: currentRequestBasePath()
            return if (!basePath.isNullOrEmpty()) "$basePath/$path" else path
        }

        private fun journeyBasePath(journeyId: String): String? {
            val session = currentRequest()?.getSession(false) ?: return null
            val storeJson = session.getAttribute(JOURNEY_STATE_METADATA_STORE_KEY) as? String ?: return null
            return runCatching { Json.decodeFromString<JourneyMetadataStore>(storeJson)[journeyId]?.basePath }.getOrNull()
        }

        private fun currentRequestBasePath(): String? = currentRequest()?.getAttribute(JOURNEY_BASE_PATH_ATTRIBUTE) as? String

        private fun currentRequest(): HttpServletRequest? =
            (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)?.request
    }
}
