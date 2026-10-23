package uk.gov.communities.prsdb.webapp.journeyMap.server

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.serialization.json.Json
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.View
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.journeys.JourneyIdProvider
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.JourneyStepDispatcher
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import java.net.URI
import java.util.concurrent.ConcurrentHashMap

@Profile("local")
@PrsdbWebComponent
class JourneyIntrospectionObserver(
    private val messageSource: MessageSource,
    private val dependencyRecorder: JourneyDependencyRecorder,
) {
    private val observer: (String, Map<String, StepLifecycleOrchestrator>) -> ModelAndView? = ::observe

    @PostConstruct
    fun register() {
        JourneyStepDispatcher.routingMapObserver = observer
    }

    @PreDestroy
    fun unregister() {
        if (JourneyStepDispatcher.routingMapObserver === observer) {
            JourneyStepDispatcher.routingMapObserver = null
        }
    }

    private fun observe(
        stepPath: String,
        routingMap: Map<String, StepLifecycleOrchestrator>,
    ): ModelAndView? {
        val attributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes ?: return null
        val request = attributes.request
        val journeyId = request.getParameter(JourneyIdProvider.PARAMETER_NAME)
        if (request.getHeader(HEADER_NAME) != HEADER_VALUE) {
            dependencyRecorder.beginRequest(stepPath, journeyId)
            if (request.method == "POST" && journeyId != null) captureIfLeavingJourney(attributes, stepPath, routingMap, journeyId)
            return null
        }

        val introspection = introspector().introspect(stepPath, routingMap, journeyId)
        return ModelAndView(JsonView(json.encodeToString(introspection)))
    }

    // Submitting the last page of a journey can clear its state before the generator can introspect it, so the outcomes
    // worked out while handling a submission that leaves the journey are kept until the generator asks for them
    private fun captureIfLeavingJourney(
        attributes: ServletRequestAttributes,
        stepPath: String,
        routingMap: Map<String, StepLifecycleOrchestrator>,
        journeyId: String,
    ) {
        submissions.remove(journeyId)
        attributes.registerDestructionCallback(
            SUBMISSION_CALLBACK_NAME,
            {
                val location = attributes.response?.getHeader(HttpHeaders.LOCATION)
                val basePath = attributes.request.getAttribute(JourneyStateService.JOURNEY_BASE_PATH_ATTRIBUTE) as? String
                if (location != null && basePath != null) {
                    val targetPath =
                        runCatching { URI(attributes.request.requestURL.toString()).resolve(location).path }.getOrNull().orEmpty()
                    val staysInJourney = targetPath.startsWith("$basePath/") && targetPath.removePrefix("$basePath/") in routingMap
                    if (!staysInJourney) {
                        runCatching { introspector().introspect(stepPath, routingMap, journeyId) }
                            .onSuccess { submissions[journeyId] = json.encodeToString(it) }
                    }
                }
            },
            RequestAttributes.SCOPE_REQUEST,
        )
    }

    private fun introspector() = JourneyIntrospector(messageSource, dependencyRecorder::readsDuring)

    private class JsonView(
        private val body: String,
    ) : View {
        override fun getContentType(): String = MediaType.APPLICATION_JSON_VALUE

        override fun render(
            model: MutableMap<String, *>?,
            request: HttpServletRequest,
            response: HttpServletResponse,
        ) {
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.characterEncoding = Charsets.UTF_8.name()
            response.writer.write(body)
        }
    }

    companion object {
        const val HEADER_NAME = "X-Journey-Map"
        const val HEADER_VALUE = "introspect"
        private const val SUBMISSION_CALLBACK_NAME = "journeyMapSubmissionCapture"

        private val json = Json { encodeDefaults = true }
        private val submissions = ConcurrentHashMap<String, String>()

        fun takeSubmission(journeyId: String): String? = submissions.remove(journeyId)
    }
}
