package uk.gov.communities.prsdb.webapp.journeyMap.server

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.springframework.context.annotation.Profile
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.journeyMap.dependencies.DependencyLog
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateAccessObserver
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep

@Profile("local")
@PrsdbWebComponent
class JourneyDependencyRecorder : JourneyStateAccessObserver {
    private class RequestScope(
        val stepId: String,
        val journeyId: String?,
    )

    private val scope = ThreadLocal<RequestScope?>()
    private val modeStack = ThreadLocal.withInitial { ArrayDeque<String>() }
    private val captured = ThreadLocal<MutableSet<String>?>()

    @PostConstruct
    fun register() {
        JourneyStateService.accessObserver = this
    }

    @PreDestroy
    fun unregister() {
        if (JourneyStateService.accessObserver === this) JourneyStateService.accessObserver = null
    }

    // Writes are attributed to the step a request submits or opens, and steps passed to the journey it is in, until the
    // request completes
    fun beginRequest(
        stepId: String,
        journeyId: String?,
    ) {
        scope.set(RequestScope(stepId, journeyId))
        modeStack.get().clear()
        RequestContextHolder.getRequestAttributes()?.registerDestructionCallback(
            CALLBACK_NAME,
            {
                scope.remove()
                modeStack.remove()
            },
            RequestAttributes.SCOPE_REQUEST,
        )
    }

    override fun read(key: String) {
        captured.get()?.add(key)
        log.read(modeStack.get().toList(), key)
    }

    override fun written(key: String) = log.written(scope.get()?.stepId, key)

    override fun modeStarted(step: JourneyStep<*, *, *>) {
        modeStack.get().addLast(JourneyIntrospector.stepId(step))
    }

    override fun modeFinished(
        step: JourneyStep<*, *, *>,
        mode: Enum<*>?,
    ) {
        modeStack.get().removeLastOrNull()
    }

    override fun outcomeRead(step: JourneyStep<*, *, *>) {
        val stepId = JourneyIntrospector.stepId(step)
        captured.get()?.add(OUTCOME_PREFIX + stepId)
        log.outcomeRead(modeStack.get().lastOrNull(), stepId)
    }

    // What a block of code reads from journey state, such as the routing code for a step
    fun readsDuring(block: () -> Unit): Set<String> {
        val reads = sortedSetOf<String>()
        captured.set(reads)
        try {
            block()
        } finally {
            captured.remove()
        }
        return reads
    }

    override fun stepPassed(
        step: JourneyStep<*, *, *>,
        mode: Enum<*>?,
    ) = log.passed(scope.get()?.journeyId, JourneyIntrospector.stepId(step), mode?.name)

    companion object {
        private const val CALLBACK_NAME = "journeyMapDependencyScope"
        const val OUTCOME_PREFIX = "outcome:"
        val log = DependencyLog()
    }
}
