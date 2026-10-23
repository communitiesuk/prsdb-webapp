package uk.gov.communities.prsdb.webapp.integration.journeyMap.explorer

import uk.gov.communities.prsdb.webapp.journeyMap.discovery.CandidateGenerator
import uk.gov.communities.prsdb.webapp.journeyMap.discovery.PageLink
import uk.gov.communities.prsdb.webapp.journeyMap.discovery.PageSnapshot
import uk.gov.communities.prsdb.webapp.journeyMap.introspection.IntrospectedStep
import uk.gov.communities.prsdb.webapp.journeyMap.introspection.IntrospectionResponse
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestCondition

class ActionFactory(
    private val candidateGenerator: CandidateGenerator,
    private val urls: JourneyUrls,
) {
    fun actionsFor(
        page: PageSnapshot,
        validators: Map<String, List<String>> = emptyMap(),
        structure: IntrospectionResponse? = null,
    ): List<WalkAction> {
        val actions = mutableListOf<WalkAction>()
        // Some pages wrap their content in a form for layout but give it no submit button, so cannot be submitted
        if (page.hasForm && page.buttons.any { it.type == "submit" }) {
            val buttonText = page.buttons.firstOrNull { it.type == "submit" }?.text?.ifBlank { null } ?: "Continue"
            candidateGenerator.generate(page, validators).forEach { candidate ->
                actions +=
                    if (candidate.requiresUpload) {
                        WalkAction.Upload(candidate)
                    } else {
                        WalkAction.Submit(candidate, candidate.label.ifBlank { buttonText })
                    }
            }
        }

        // Summary list actions in the same journey (such as removing a joint landlord) are part of the main flow;
        // those in another journey are check your answers change links, which are explored separately. Content links,
        // such as removing an uploaded file from a table, are followed when they stay in the same task or lead to a step
        // that comes after this one, because other links go back to change an earlier answer, like a back link
        val journeyId = urls.journeyIdOf(page.url)
        val currentPath = urls.pathOf(page.url)
        val sameJourneyActions = page.changeLinks.filter { urls.journeyIdOf(it.href) == journeyId }
        val contentLinks =
            structure
                ?.let { page.contentLinks.filter { link -> staysInTask(page, link, structure) || leadsOn(page, link, structure) } }
                .orEmpty()
        (page.links + sameJourneyActions + contentLinks)
            .filter { urls.stepPathOf(it.href) != null && urls.pathOf(it.href) != currentPath }
            .distinctBy { urls.linkKey(it.href) }
            .forEach { actions += WalkAction.Follow(it.href, it.text.ifBlank { "Link" }, urls.linkKey(it.href)) }
        return actions
    }

    fun deviation(
        page: PageSnapshot,
        action: WalkAction,
        actions: List<WalkAction>,
    ): ManifestCondition? = if (actions.size > 1) ManifestCondition(ExplorationRecorder.titleOf(page), action.label) else null

    private fun staysInTask(
        page: PageSnapshot,
        link: PageLink,
        structure: IntrospectionResponse,
    ): Boolean {
        val current = urls.stepPathOf(page.url)?.let { structure.stepByPath(it) } ?: return false
        val target = urls.stepPathOf(link.href)?.let { structure.stepByPath(it) } ?: return false
        val task = innermostTask(current) ?: return false
        return task == innermostTask(target)
    }

    private fun innermostTask(step: IntrospectedStep) = step.containers.lastOrNull { it.kind == TASK_CONTAINER }?.id

    private fun leadsOn(
        page: PageSnapshot,
        link: PageLink,
        structure: IntrospectionResponse,
    ): Boolean {
        val current = urls.stepPathOf(page.url)?.let { structure.stepByPath(it) } ?: return false
        val target = urls.stepPathOf(link.href)?.let { structure.stepByPath(it) } ?: return false
        return target.parentage.singleParents().any { it.stepId == current.id }
    }

    fun checkAnswersChangeLinks(page: PageSnapshot) =
        page.changeLinks.filter { link ->
            val linkJourneyId = urls.journeyIdOf(link.href)
            linkJourneyId != null && linkJourneyId != urls.journeyIdOf(page.url) && urls.stepPathOf(link.href) != null
        }

    companion object {
        private const val TASK_CONTAINER = "task"
    }
}
