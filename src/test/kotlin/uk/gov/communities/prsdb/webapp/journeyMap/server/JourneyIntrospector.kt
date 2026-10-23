package uk.gov.communities.prsdb.webapp.journeyMap.server

import org.springframework.context.MessageSource
import org.springframework.util.ClassUtils
import uk.gov.communities.prsdb.webapp.journeyMap.JourneyMapText
import uk.gov.communities.prsdb.webapp.journeyMap.introspection.IntrospectedContainer
import uk.gov.communities.prsdb.webapp.journeyMap.introspection.IntrospectedDestination
import uk.gov.communities.prsdb.webapp.journeyMap.introspection.IntrospectedParentage
import uk.gov.communities.prsdb.webapp.journeyMap.introspection.IntrospectedStep
import uk.gov.communities.prsdb.webapp.journeyMap.introspection.IntrospectionResponse
import uk.gov.communities.prsdb.webapp.journeys.AbstractStepConfig
import uk.gov.communities.prsdb.webapp.journeys.AndParents
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyContainer
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.NoParents
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Parentage
import uk.gov.communities.prsdb.webapp.journeys.SingleParent
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.SubjourneyExitStep
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy
import java.util.IdentityHashMap
import java.util.Locale
import kotlin.reflect.KClass
import kotlin.reflect.full.allSupertypes
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

// Reads made while working out where a step leads are watched by the caller, which knows how journey state is observed
class JourneyIntrospector(
    private val messageSource: MessageSource?,
    private val readsDuring: (() -> Unit) -> Set<String> = { block ->
        block()
        emptySet()
    },
) {
    private class Route(
        val mode: String,
        val destination: Destination?,
        val reads: Set<String>,
    )

    fun introspect(
        stepPath: String,
        routingMap: Map<String, StepLifecycleOrchestrator>,
        journeyId: String?,
    ): IntrospectionResponse {
        val routes = IdentityHashMap<JourneyStep<*, *, *>, List<Route>>()
        val steps = collectSteps(routingMap.values.map { it.journeyStep }, routes)
        val ids = assignIds(steps)
        return IntrospectionResponse(stepPath, journeyId, steps.map { describe(it, ids, routes[it].orEmpty()) })
    }

    // Steps that are no step's parent, such as one that saves the journey before its confirmation page, are only found
    // by working out where each outcome of the steps before them leads
    private fun collectSteps(
        roots: List<JourneyStep<*, *, *>>,
        routes: IdentityHashMap<JourneyStep<*, *, *>, List<Route>>,
    ): List<JourneyStep<*, *, *>> {
        val ordered = mutableListOf<JourneyStep<*, *, *>>()
        val queue = ArrayDeque(roots)
        while (queue.isNotEmpty()) {
            val step = queue.removeFirst()
            if (routes.containsKey(step)) continue
            val stepRoutes = routesOf(step)
            routes[step] = stepRoutes
            ordered.add(step)
            queue.addAll(runCatching { step.parentage.potentialParents }.getOrDefault(emptyList()))
            queue.addAll(stepRoutes.mapNotNull { stepOf(it.destination) })
        }
        return ordered
    }

    // The routing code is called for every outcome, whether or not the journey can produce it now
    private fun routesOf(step: JourneyStep<*, *, *>): List<Route> {
        @Suppress("UNCHECKED_CAST")
        val nextDestination = runCatching { nextDestinationField.get(step) as? (Any) -> Destination }.getOrNull() ?: return emptyList()
        return modeClass(step)?.java?.enumConstants.orEmpty().map { mode ->
            var destination: Destination? = null
            val reads = readsDuring { destination = runCatching { nextDestination(mode) }.getOrNull() }
            Route((mode as Enum<*>).name, destination, reads)
        }
    }

    private fun stepOf(destination: Destination?): JourneyStep<*, *, *>? =
        when (destination) {
            is Destination.VisitableStep -> destination.step
            is Destination.NavigationalStep -> destination.step
            else -> null
        }

    private fun describeRoute(
        route: Route,
        ids: IdentityHashMap<JourneyStep<*, *, *>, String>,
    ): IntrospectedDestination {
        val reads = route.reads.toList()
        return when (val destination = route.destination) {
            is Destination.VisitableStep, is Destination.NavigationalStep ->
                IntrospectedDestination(route.mode, step = stepOf(destination)?.let { ids[it] ?: stepId(it) }, reads = reads)
            is Destination.ExternalUrl ->
                IntrospectedDestination(route.mode, url = destination.externalUrl.substringBefore('?'), reads = reads)
            is Destination.StepRoute -> IntrospectedDestination(route.mode, url = destination.routeSegment, reads = reads)
            else -> IntrospectedDestination(route.mode, reads = reads)
        }
    }

    private fun assignIds(steps: List<JourneyStep<*, *, *>>): IdentityHashMap<JourneyStep<*, *, *>, String> {
        val ids = IdentityHashMap<JourneyStep<*, *, *>, String>()
        val used = mutableMapOf<String, Int>()
        steps.forEach { step ->
            val baseId = stepId(step)
            val count = used.merge(baseId, 1, Int::plus)!!
            ids[step] = if (count == 1) baseId else "$baseId#$count"
        }
        return ids
    }

    private fun describe(
        step: JourneyStep<*, *, *>,
        ids: IdentityHashMap<JourneyStep<*, *, *>, String>,
        routes: List<Route>,
    ): IntrospectedStep {
        val parentage = runCatching { step.parentage }.getOrNull()
        return IntrospectedStep(
            id = ids.getValue(step),
            path = step.getUrlPathOrNull(),
            requestable = step is JourneyStep.RequestableStep<*, *, *>,
            redirecting =
                runCatching { step.lifecycleOrchestrator is StepLifecycleOrchestrator.RedirectingStepLifecycleOrchestrator }
                    .getOrDefault(false),
            stepClass = simpleName(step.javaClass),
            stepConfigClass = simpleName(step.stepConfig.javaClass),
            modeValues = modeValues(step),
            outcome = runCatching { (step.outcome as? Enum<*>)?.name }.getOrNull(),
            reachable = runCatching { step.isStepReachable }.getOrDefault(false),
            exit = step is SubjourneyExitStep,
            initial = parentage is NoParents,
            containers = step.containers.map { describeContainer(it) },
            parentage = describeParentage(parentage, ids),
            fields = if (step is JourneyStep.RequestableStep<*, *, *>) fields(step.stepConfig) else emptyMap(),
            destinations = routes.map { describeRoute(it, ids) },
        )
    }

    private fun describeContainer(container: JourneyContainer): IntrospectedContainer =
        when (container) {
            is JourneyContainer.Section ->
                IntrospectedContainer(
                    id = containerId(container),
                    kind = "section",
                    label =
                        messageSource?.getMessage(container.headingMessageKey, null, container.headingMessageKey, Locale.UK)
                            ?: container.headingMessageKey,
                )
            is JourneyContainer.Task ->
                IntrospectedContainer(
                    id = containerId(container),
                    kind = "task",
                    label = JourneyMapText.humaniseClassName(simpleName(container.taskClass.java)),
                )
        }

    private fun describeParentage(
        parentage: Parentage?,
        ids: IdentityHashMap<JourneyStep<*, *, *>, String>,
    ): IntrospectedParentage =
        when (parentage) {
            null -> IntrospectedParentage(IntrospectedParentage.OTHER)
            is NoParents -> IntrospectedParentage(IntrospectedParentage.NONE)
            is SingleParent -> IntrospectedParentage(IntrospectedParentage.SINGLE, ids[parentage.step], parentage.label)
            is AndParents ->
                IntrospectedParentage(IntrospectedParentage.AND, children = parentage.parents.map { describeParentage(it, ids) })
            is OrParents ->
                IntrospectedParentage(IntrospectedParentage.OR, children = parentage.parents.map { describeParentage(it, ids) })
            else ->
                IntrospectedParentage(
                    IntrospectedParentage.OTHER,
                    children = parentage.potentialParents.map { IntrospectedParentage(IntrospectedParentage.SINGLE, ids[it]) },
                )
        }

    private fun modeValues(step: JourneyStep<*, *, *>): List<String> =
        modeClass(step)?.java?.enumConstants?.map { (it as Enum<*>).name } ?: emptyList()

    private fun modeClass(step: JourneyStep<*, *, *>): KClass<*>? =
        firstTypeArgument(ClassUtils.getUserClass(step.javaClass).kotlin, JourneyStep::class)
            ?: firstTypeArgument(ClassUtils.getUserClass(step.stepConfig.javaClass).kotlin, AbstractStepConfig::class)

    private fun firstTypeArgument(
        kClass: KClass<*>,
        supertype: KClass<*>,
    ): KClass<*>? =
        runCatching {
            kClass.allSupertypes
                .firstOrNull { it.classifier == supertype }
                ?.arguments
                ?.firstOrNull()
                ?.type
                ?.classifier as? KClass<*>
        }.getOrNull()

    private fun fields(stepConfig: AbstractStepConfig<*, *, *>): Map<String, List<String>> =
        runCatching {
            stepConfig.formModelClass.memberProperties.associate { property ->
                property.name to
                    (property.findAnnotation<ValidatedBy>()?.constraints?.map { it.validatorType.simpleName ?: "" } ?: emptyList())
            }
        }.getOrDefault(emptyMap())

    companion object {
        private val nextDestinationField = JourneyStep::class.java.getDeclaredField("nextDestination").apply { isAccessible = true }

        // Steps are identified by their path, or by their class and the containers they are built in
        fun stepId(step: JourneyStep<*, *, *>): String {
            step.getUrlPathOrNull()?.let { return it }
            val containerIds = step.containers.map { containerId(it) }
            if (step is SubjourneyExitStep) {
                return "exit:" + (containerIds.lastOrNull() ?: "journey")
            }
            val suffix = if (containerIds.isEmpty()) "" else "@" + containerIds.joinToString(">")
            return "internal:" + simpleName(step.javaClass) + suffix
        }

        fun containerId(container: JourneyContainer): String =
            when (container) {
                is JourneyContainer.Section -> "section:${container.headingMessageKey}"
                is JourneyContainer.Task ->
                    "task:" + simpleName(container.taskClass.java) + (container.routeSegment?.let { "/$it" } ?: "")
            }

        private fun simpleName(javaClass: Class<*>): String = ClassUtils.getUserClass(javaClass).simpleName.substringBefore("$$")
    }
}
