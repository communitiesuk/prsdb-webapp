package uk.gov.communities.prsdb.webapp.integration.journeyMap.explorer

import uk.gov.communities.prsdb.webapp.integration.journeyMap.walker.JourneyWalker
import uk.gov.communities.prsdb.webapp.journeyMap.discovery.PageLink
import uk.gov.communities.prsdb.webapp.journeyMap.introspection.IntrospectedStep
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestCondition
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestNode

// Follows each check your answers change link once and keeps only pages that look different from the main journey.
// Changing an answer starts another journey, whose journey ids are kept so that its steps are not counted as walking the
// journey mapped
class ChangeFlowExplorer(
    private val walker: JourneyWalker,
    private val urls: JourneyUrls,
    private val recorder: ExplorationRecorder,
    private val actionFactory: ActionFactory,
    private val performer: ActionPerformer,
    private val budget: ActionBudget,
) {
    private val exploredLinks = mutableSetOf<String>()
    private val submitted = mutableSetOf<String>()
    val journeyIds = mutableSetOf<String>()

    fun explore(checkAnswers: ActionContext) {
        val links = actionFactory.checkAnswersChangeLinks(checkAnswers.page)
        if (links.isEmpty()) return
        recorder.markCheckAnswersStep(checkAnswers.step.id)
        for (link in links) {
            if (budget.exceeded) return
            if (!exploredLinks.add("${checkAnswers.step.id}|${urls.stepPathOf(link.href)}")) continue
            try {
                exploreLink(checkAnswers, link)
            } catch (exception: Exception) {
                recorder.error(checkAnswers.step.id, "change-flow", "${link.text}: ${ActionPerformer.describe(exception)}")
            }
        }
    }

    private fun exploreLink(
        checkAnswers: ActionContext,
        link: PageLink,
    ) {
        if (!budget.spend()) return
        val conditions = checkAnswers.conditions + ManifestCondition(ExplorationRecorder.titleOf(checkAnswers.page), link.text)
        val checkAnswersJourneyId = urls.journeyIdOf(checkAnswers.sourceUrl)
        urls.journeyIdOf(link.href)?.let { journeyIds += it }
        walker.restore(checkAnswers.before)
        val navigation = walker.navigate(link.href)
        if (navigation.status >= 400) {
            recorder.error(checkAnswers.step.id, "http-${navigation.status}", "\"${link.text}\" led to ${navigation.url}")
            return
        }
        val firstNodeId = recordCurrentPage(conditions) ?: return
        recorder.recordChangeTransition(checkAnswers.step.id, firstNodeId, link.text, null)

        val pending = ArrayDeque(listOf(ChangePosition(walker.snapshot(), walker.currentUrl(), firstNodeId, 0)))
        while (pending.isNotEmpty()) {
            val position = pending.removeFirst()
            walker.restore(position.sessionSnapshot)
            walker.navigate(position.url)
            val url = walker.currentUrl()
            urls.journeyIdOf(url)?.takeIf { it != checkAnswersJourneyId }?.let { journeyIds += it }
            val path = urls.stepPathOf(url) ?: continue
            val introspection = walker.introspect(url)?.also { recorder.registerStructure(it, fromChangeFlow = true) } ?: continue
            val step = introspection.stepByPath(path) ?: continue
            val page = walker.discover()
            val actions = actionFactory.actionsFor(page, step.fields, introspection)
            val context =
                ActionContext(walker.snapshot(), url, step, introspection, page, conditions, ManifestNode.ORIGIN_CHANGE, ::nodeIdFor)
            for (action in actions) {
                val key = "${step.id}|${action.key}"
                if (!submitted.add(key)) continue
                if (!budget.spend()) return
                val landing = performer.perform(context, action, actionFactory.deviation(page, action, actions))
                landing.journeyId?.takeIf { it != checkAnswersJourneyId }?.let { journeyIds += it }
                val target = landing.nodeId ?: continue
                val returnedToCheckAnswers = landing.stepId == checkAnswers.step.id && landing.journeyId == checkAnswersJourneyId
                if (returnedToCheckAnswers) {
                    recorder.recordChangeTransition(position.nodeId, checkAnswers.step.id, action.label, landing)
                    continue
                }
                if (landing.kind == LandingKind.PAGE) {
                    recorder.recordRender(
                        target,
                        ManifestNode.PAGE,
                        urls.stepPathOf(landing.url),
                        landing.page!!,
                        landing.hash!!,
                        conditions + listOfNotNull(landing.condition),
                        ManifestNode.ORIGIN_CHANGE,
                    )
                    if (position.depth < MAX_DEPTH) {
                        pending += ChangePosition(landing.sessionSnapshot!!, landing.url, target, position.depth + 1)
                    }
                }
                recorder.recordChangeTransition(position.nodeId, target, action.label, landing)
            }
        }
    }

    private fun recordCurrentPage(conditions: List<ManifestCondition>): String? {
        val url = walker.currentUrl()
        val path = urls.stepPathOf(url) ?: return null
        val introspection = walker.introspect(url)?.also { recorder.registerStructure(it, fromChangeFlow = true) } ?: return null
        val step = introspection.stepByPath(path) ?: return null
        val hash = walker.signature()
        val nodeId = nodeIdFor(step, hash)
        recorder.recordRender(nodeId, ManifestNode.PAGE, path, walker.discover(), hash, conditions, ManifestNode.ORIGIN_CHANGE)
        return nodeId
    }

    private fun nodeIdFor(
        step: IntrospectedStep,
        hash: String?,
    ) = if (recorder.hasMainStructure(step.id, walker.structuralSignature())) {
        step.id
    } else {
        ExplorationRecorder.CHANGE_PREFIX + (step.path ?: step.id)
    }

    companion object {
        private const val MAX_DEPTH = 3
    }
}
