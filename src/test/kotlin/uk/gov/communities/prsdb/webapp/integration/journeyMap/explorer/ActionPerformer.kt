package uk.gov.communities.prsdb.webapp.integration.journeyMap.explorer

import uk.gov.communities.prsdb.webapp.integration.journeyMap.walker.JourneyWalker
import uk.gov.communities.prsdb.webapp.integration.journeyMap.walker.Navigation
import uk.gov.communities.prsdb.webapp.journeyMap.discovery.CandidateGenerator
import uk.gov.communities.prsdb.webapp.journeyMap.discovery.PageSnapshot
import uk.gov.communities.prsdb.webapp.journeyMap.introspection.IntrospectedStep
import uk.gov.communities.prsdb.webapp.journeyMap.introspection.IntrospectionResponse
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestCondition
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestNode
import java.nio.file.Path
import java.nio.file.Paths

data class ActionContext(
    val before: String,
    val sourceUrl: String,
    val step: IntrospectedStep,
    val introspection: IntrospectionResponse,
    val page: PageSnapshot,
    val conditions: List<ManifestCondition>,
    val origin: String = ManifestNode.ORIGIN_MAIN,
    val nodeIdFor: (IntrospectedStep, String?) -> String = { landingStep, _ -> landingStep.id },
)

class ActionPerformer(
    private val walker: JourneyWalker,
    private val urls: JourneyUrls,
    private val recorder: ExplorationRecorder,
    private val candidateGenerator: CandidateGenerator,
    private val actionFactory: ActionFactory,
    private val viaStepResolver: ViaStepResolver,
    private val resetDatabase: () -> Unit,
) {
    private val succeeded = mutableSetOf<String>()

    fun perform(
        context: ActionContext,
        action: WalkAction,
        condition: ManifestCondition?,
        reportValidationFailures: Boolean = true,
    ): Landing =
        attempt(context, action, condition, reportValidationFailures).also {
            if (it.kind != LandingKind.FAILED) succeeded += "${context.step.id}|${action.key}"
        }

    private fun attempt(
        context: ActionContext,
        action: WalkAction,
        condition: ManifestCondition?,
        reportValidationFailures: Boolean,
    ): Landing {
        val step = context.step
        val navigation =
            try {
                executeWithRetries(context, action)
            } catch (exception: Exception) {
                recorder.error(step.id, "navigation", "\"${action.label}\": ${describe(exception)}")
                return Landing(action, LandingKind.FAILED, condition = condition)
            }
        if (navigation.status >= 400) {
            // Repeating a submission that worked can fail once the seed data restored since removed what earlier pages saved
            val repeatedAfterReset = walker.sessionPredatesDatabaseReset && "${step.id}|${action.key}" in succeeded
            if (!repeatedAfterReset) recorder.error(step.id, "http-${navigation.status}", "\"${action.label}\" led to ${navigation.url}")
            return Landing(action, LandingKind.FAILED, condition = condition)
        }
        if (isRerenderedSubmission(navigation, context.sourceUrl)) {
            val page = walker.discover()
            if (page.errors.isNotEmpty()) {
                if (reportValidationFailures) {
                    recorder.gap(step.id, null, "Validation failed for \"${action.label}\": ${page.errors.joinToString("; ") { it.text }}")
                }
                return Landing(action, LandingKind.FAILED, condition = condition)
            }
            val resultId = "${step.id}#result"
            recorder.recordRender(
                resultId,
                ManifestNode.TERMINAL,
                null,
                page,
                walker.signature(),
                context.conditions + listOfNotNull(condition),
                context.origin,
            )
            return Landing(
                action,
                LandingKind.TERMINAL,
                resultId,
                url = navigation.url,
                condition = condition,
            )
        }

        val fromChangeFlow = context.origin == ManifestNode.ORIGIN_CHANGE
        val landingPath = urls.stepPathOf(navigation.url)
        val landingIntrospection =
            landingPath?.let { walker.introspect(navigation.url) }?.also { recorder.registerStructure(it, fromChangeFlow) }
        val landingStep = landingPath?.let { landingIntrospection?.stepByPath(it) }?.takeIf { it.requestable }
        val sameJourney = urls.journeyIdOf(navigation.url) == urls.journeyIdOf(context.sourceUrl)
        val afterIntrospection =
            landingIntrospection?.takeIf { sameJourney }
                ?: takeSubmission(action, context.sourceUrl)?.also { recorder.registerStructure(it, fromChangeFlow) }
                ?: walker.introspect(context.sourceUrl)?.also { recorder.registerStructure(it, fromChangeFlow) }
        val outcome = afterIntrospection?.stepById(step.id)?.outcome
        if (outcome == null && afterIntrospection != null && action !is WalkAction.Follow) recorder.markOutcomeUnobservable(step.id)
        val via =
            afterIntrospection
                ?.let { viaStepResolver.resolve(context.introspection, it, navigation.chain, step.id, landingStep?.id) }
                .orEmpty()
        via.filter { it.outcome == null }.forEach { recorder.markOutcomeUnobservable(it.id) }
        val newModes = recorder.observe(step.id, outcome, via)

        if (landingStep == null) {
            val terminalId = urls.pathOf(navigation.url)
            val terminalPage = walker.discover()
            recorder.recordRender(
                terminalId,
                ManifestNode.TERMINAL,
                terminalId,
                terminalPage,
                walker.signature(),
                context.conditions + listOfNotNull(condition),
                context.origin,
            )
            // Leaving the journey by submitting a form usually commits it, so the seed data is restored
            if (action !is WalkAction.Follow) resetDatabase()
            return Landing(
                action,
                LandingKind.TERMINAL,
                terminalId,
                url = navigation.url,
                outcome = outcome,
                via = via,
                condition = condition,
                newModes = newModes,
            )
        }

        val landingPage = walker.discover()
        val hash = walker.signature()
        val nodeId = context.nodeIdFor(landingStep, hash)
        val journeyId = urls.journeyIdOf(navigation.url)
        if (actionFactory.actionsFor(landingPage).isEmpty()) {
            recorder.recordRender(
                nodeId,
                ManifestNode.PAGE,
                landingPath,
                landingPage,
                hash,
                context.conditions + listOfNotNull(condition),
                context.origin,
            )
            recorder.recordExitLinks(nodeId, landingPage)
            return Landing(
                action,
                LandingKind.DEAD_END,
                nodeId,
                landingStep.id,
                navigation.url,
                journeyId,
                hash,
                landingPage,
                outcome,
                via,
                null,
                condition,
                newModes,
            )
        }
        return Landing(
            action,
            LandingKind.PAGE,
            nodeId,
            landingStep.id,
            navigation.url,
            journeyId,
            hash,
            landingPage,
            outcome,
            via,
            walker.snapshot(),
            condition,
            newModes,
        )
    }

    private fun executeWithRetries(
        context: ActionContext,
        action: WalkAction,
    ): Navigation {
        var attempt = action
        var navigation = execute(context, attempt)
        var attempts = 1
        while (attempts < MAX_ATTEMPTS && attempt is WalkAction.Submit && isRerenderedSubmission(navigation, context.sourceUrl)) {
            val pageErrors = walker.discover().errors
            if (pageErrors.isEmpty()) break
            val retry = candidateGenerator.retry(context.page, attempt.candidate, pageErrors, context.step.fields) ?: break
            attempt = WalkAction.Submit(retry, action.label)
            navigation = execute(context, attempt)
            attempts++
        }
        return navigation
    }

    private fun takeSubmission(
        action: WalkAction,
        sourceUrl: String,
    ) = if (action is WalkAction.Follow) null else urls.journeyIdOf(sourceUrl)?.let { walker.takeSubmission(it) }

    private fun execute(
        context: ActionContext,
        action: WalkAction,
    ): Navigation {
        walker.restore(context.before)
        return when (action) {
            is WalkAction.Submit -> walker.submit(context.page.action ?: context.sourceUrl, action.candidate.params)
            is WalkAction.Upload -> walker.upload(context.sourceUrl, UPLOAD_FILE)
            is WalkAction.Follow -> walker.navigate(action.href)
        }
    }

    private fun isRerenderedSubmission(
        navigation: Navigation,
        sourceUrl: String,
    ) = navigation.method == "POST" && navigation.chain.isEmpty() && urls.pathOf(navigation.url) == urls.pathOf(sourceUrl)

    companion object {
        private const val MAX_ATTEMPTS = 5

        private val UPLOAD_FILE: Path by lazy {
            Paths.get(
                ActionPerformer::class.java.classLoader
                    .getResource("data/certificates/validFile.png")!!
                    .toURI(),
            )
        }

        fun describe(exception: Exception) = "${exception.javaClass.simpleName}: ${exception.message?.lines()?.firstOrNull().orEmpty()}"
    }
}
