package uk.gov.communities.prsdb.webapp.journeyMap.server

import jakarta.servlet.http.HttpSession
import kotlinx.serialization.json.Json
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbRestController
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Profile("local")
@PrsdbRestController
@RequestMapping("/local/journey-map")
class JourneyMapSessionController(
    private val session: HttpSession,
) {
    @PostMapping("/snapshot")
    fun snapshot(): String {
        val attributes = session.attributeNames.toList().associateWith { session.getAttribute(it) }
        val snapshotId = UUID.randomUUID().toString()
        snapshots[snapshotId] = attributes
        return snapshotId
    }

    // The security context is restored too, because completing a journey can refresh the user's roles
    @PostMapping("/restore/{snapshotId}")
    fun restore(
        @PathVariable snapshotId: String,
    ) {
        val attributes =
            snapshots[snapshotId] ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown journey map snapshot $snapshotId")
        session.attributeNames
            .toList()
            .filterNot { it in attributes }
            .forEach(session::removeAttribute)
        attributes.forEach { (name, value) -> session.setAttribute(name, value) }
    }

    @PostMapping("/clear")
    fun clear() {
        snapshots.clear()
        JourneyDependencyRecorder.log.clear()
    }

    @PostMapping("/submission/{journeyId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun submission(
        @PathVariable journeyId: String,
    ): String =
        JourneyIntrospectionObserver.takeSubmission(journeyId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "No submission has left journey $journeyId")

    @GetMapping("/dependencies", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun dependencies(): String = json.encodeToString(JourneyDependencyRecorder.log.snapshot())

    companion object {
        const val SNAPSHOT_ROUTE = "local/journey-map/snapshot"
        const val RESTORE_ROUTE = "local/journey-map/restore"
        const val CLEAR_ROUTE = "local/journey-map/clear"
        const val SUBMISSION_ROUTE = "local/journey-map/submission"
        const val DEPENDENCIES_ROUTE = "local/journey-map/dependencies"

        private val snapshots = ConcurrentHashMap<String, Map<String, Any?>>()
        private val json = Json { encodeDefaults = true }
    }
}
