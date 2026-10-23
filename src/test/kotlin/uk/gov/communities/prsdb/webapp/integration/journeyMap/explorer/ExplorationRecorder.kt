package uk.gov.communities.prsdb.webapp.integration.journeyMap.explorer

import uk.gov.communities.prsdb.webapp.integration.journeyMap.registry.JourneyMapRegistry
import uk.gov.communities.prsdb.webapp.integration.journeyMap.walker.VariantRenderer
import uk.gov.communities.prsdb.webapp.journeyMap.discovery.PageSnapshot
import uk.gov.communities.prsdb.webapp.journeyMap.introspection.IntrospectedStep
import uk.gov.communities.prsdb.webapp.journeyMap.introspection.IntrospectionResponse
import uk.gov.communities.prsdb.webapp.journeyMap.model.EntryRecord
import uk.gov.communities.prsdb.webapp.journeyMap.model.Exploration
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestCondition
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestDependency
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestEdge
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestError
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestGap
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestNode
import uk.gov.communities.prsdb.webapp.journeyMap.model.PageRecord
import uk.gov.communities.prsdb.webapp.journeyMap.model.Transition
import uk.gov.communities.prsdb.webapp.journeyMap.model.VariantRecord
import uk.gov.communities.prsdb.webapp.journeyMap.model.ViaStep

class ExplorationRecorder(
    private val journeyName: String,
    private val urls: JourneyUrls,
    private val renderer: VariantRenderer,
    private val singleRenderPaths: Set<String>,
    private val maxVariantsPerPage: Int,
    private val structuralSignature: () -> String,
) {
    private class PageBuilder(
        val id: String,
        val kind: String,
        val route: String?,
        val title: String,
        val origin: String,
    ) {
        val variants = mutableListOf<VariantRecord>()
        val hashes = mutableSetOf<String>()
        val seenHashes = mutableSetOf<String>()
        val structuralHashes = mutableSetOf<String>()
        var skippedVariants = 0

        fun toRecord() = PageRecord(id, kind, route, title, variants.toList(), origin, skippedVariants)
    }

    private val structures = mutableListOf<IntrospectionResponse>()
    private val stepsById = mutableMapOf<String, IntrospectedStep>()
    private val pages = linkedMapOf<String, PageBuilder>()
    private val transitions = linkedSetOf<Transition>()
    private val gaps = mutableListOf<ManifestGap>()
    private val errors = mutableListOf<ManifestError>()
    private val observedModes = mutableMapOf<String, MutableSet<String>>()
    private val passedSteps = mutableSetOf<String>()
    private val unobservableSteps = mutableSetOf<String>()
    private val changeFlowSteps = mutableSetOf<String>()
    private val entries = mutableListOf<EntryRecord>()

    val pageCount get() = pages.size

    fun recordEntry(entry: EntryRecord) {
        entries += entry
    }

    fun step(id: String) = stepsById[id]

    fun containersOfStepConfigs(stepConfigNames: Set<String>) =
        stepsById.values
            .filter { it.stepConfigClass in stepConfigNames }
            .flatMap { step -> step.containers.map { it.id } }
            .toSet()

    // Changing an answer from check your answers starts another journey, whose steps are not part of the journey mapped
    fun registerStructure(
        introspection: IntrospectionResponse,
        fromChangeFlow: Boolean = false,
    ) {
        val newSteps = introspection.steps.filter { it.id !in stepsById }
        if (fromChangeFlow) changeFlowSteps += newSteps.map { it.id }
        if (structures.isEmpty() || newSteps.isNotEmpty()) {
            structures += introspection
            newSteps.forEach { stepsById[it.id] = it }
        }
    }

    fun hasMainStructure(
        id: String,
        structuralHash: String,
    ) = pages[id]?.let { it.origin == ManifestNode.ORIGIN_MAIN && structuralHash in it.structuralHashes } ?: false

    fun hasSeen(
        id: String?,
        hash: String?,
    ) = id != null && hash != null && pages[id]?.seenHashes?.contains(hash) == true

    fun hasRendered(id: String?) = id != null && pages.containsKey(id)

    fun recordRender(
        id: String,
        kind: String,
        route: String?,
        page: PageSnapshot,
        hash: String,
        conditions: List<ManifestCondition>,
        origin: String,
    ) {
        val builder = pages.getOrPut(id) { PageBuilder(id, kind, route, titleOf(page), origin) }
        builder.seenHashes += hash
        if (hash in builder.hashes) return
        if (route != null && route in singleRenderPaths && builder.variants.isNotEmpty()) return
        if (builder.variants.size >= maxVariantsPerPage) {
            builder.skippedVariants++
            return
        }
        builder.hashes += hash
        builder.structuralHashes += structuralSignature()
        val images = renderer.capture(id, builder.variants.size + 1)
        builder.variants += VariantRecord(hash, images.image, images.thumbnail, conditions, origin, images.mobileImage)
    }

    fun recordExitLinks(
        nodeId: String,
        page: PageSnapshot,
    ) {
        page.links
            .filter { urls.stepPathOf(it.href) == null && urls.isLocal(it.href) }
            .forEach { link ->
                val target = urls.pathOf(link.href)
                pages.getOrPut(target) { PageBuilder(target, ManifestNode.TERMINAL, target, link.text, ManifestNode.ORIGIN_MAIN) }
                transitions += Transition(nodeId, target, link.text, null)
            }
    }

    fun recordTransition(
        fromId: String,
        landing: Landing,
        kind: String = ManifestEdge.OBSERVED,
    ) {
        val target = landing.nodeId ?: return
        transitions += Transition(fromId, target, landing.action.label, landing.outcome, landing.via, kind)
    }

    fun recordChangeTransition(
        fromId: String,
        toId: String,
        label: String,
        landing: Landing?,
    ) {
        val involvesChangeOnlyPage = fromId.startsWith(CHANGE_PREFIX) || toId.startsWith(CHANGE_PREFIX)
        val bothInMainFlow = pages[fromId]?.origin == ManifestNode.ORIGIN_MAIN && pages[toId]?.origin == ManifestNode.ORIGIN_MAIN
        if (bothInMainFlow && !involvesChangeOnlyPage && !isCheckAnswersEdge(fromId, toId)) return
        transitions += Transition(fromId, toId, label, landing?.outcome, landing?.via ?: emptyList(), ManifestEdge.CHANGE)
    }

    private val checkAnswersSteps = mutableSetOf<String>()

    fun markCheckAnswersStep(stepId: String) {
        checkAnswersSteps += stepId
    }

    private fun isCheckAnswersEdge(
        fromId: String,
        toId: String,
    ) = fromId in checkAnswersSteps || toId in checkAnswersSteps

    // Returns true when this submission produced a mode not seen before on the submitted step or a step it passed through
    fun observe(
        stepId: String,
        outcome: String?,
        via: List<ViaStep>,
    ): Boolean {
        var isNew = false
        (listOf(ViaStep(stepId, outcome)) + via).forEach { (id, mode) ->
            passedSteps += id
            if (mode != null && observedModes.getOrPut(id) { mutableSetOf() }.add(mode)) isNew = true
        }
        return isNew
    }

    // Steps that decide where the journey starts, such as whether a property has joint landlords, are passed on the way in
    fun observeStart(introspection: IntrospectionResponse) {
        introspection.steps
            .filter { (it.redirecting || !it.requestable) && !it.exit && it.outcome != null }
            .forEach { observe(it.id, it.outcome, emptyList()) }
    }

    // A later step can clear a step's answer during the same redirect, such as saving a governing body member's address
    fun markOutcomeUnobservable(stepId: String) {
        unobservableSteps += stepId
    }

    fun unseenModes(stepId: String): List<String> {
        val step = stepsById[stepId] ?: return emptyList()
        if (step.modeValues.size < 2 || step.exit || stepId in unobservableSteps) return emptyList()
        return step.modeValues - observedModes[stepId].orEmpty()
    }

    fun gap(
        node: String,
        mode: String?,
        reason: String,
    ) {
        gaps += ManifestGap(node, mode, reason)
    }

    fun error(
        node: String,
        kind: String,
        detail: String,
    ) {
        errors += ManifestError(node, kind, detail)
    }

    fun toExploration(): Exploration {
        gaps += coverageGaps()
        return Exploration(
            structures.toList(),
            pages.values.map { it.toRecord() },
            transitions.toList(),
            gaps.toList(),
            errors.toList(),
            dependencies,
            outcomesPassed,
            changeFlowSteps.toSet(),
            entries.toList(),
        )
    }

    var dependencies: List<ManifestDependency> = emptyList()
    var outcomesPassed: Map<String, List<String>> = emptyMap()

    fun decisionSteps() = stepsById.values.filter { it.modeValues.size > 1 && !it.exit }.map { it.id }

    fun coverageGaps(): List<ManifestGap> {
        val main = structures.firstOrNull() ?: return emptyList()
        return main.steps.flatMap { step ->
            when {
                onlyReachedThroughExemptOutcomes(step) -> emptyList()
                step.requestable && !step.redirecting && step.id !in pages -> listOf(ManifestGap(step.id, null, "never visited"))
                step.modeValues.size < 2 || step.exit -> emptyList()
                step.id !in passedSteps ->
                    if (step.redirecting || !step.requestable) listOf(ManifestGap(step.id, null, "never reached")) else emptyList()
                else ->
                    unseenModes(step.id)
                        .filter { mode -> JourneyMapRegistry.exemptionFor(journeyName, step.stepConfigClass, mode) == null }
                        .map { mode -> ManifestGap(step.id, mode, "never produced") }
            }
        }
    }

    // Steps only reached through an outcome the generator cannot produce, such as providing a certificate later while
    // updating it, are not gaps
    private fun onlyReachedThroughExemptOutcomes(step: IntrospectedStep) =
        step.id !in pages &&
            step.id !in passedSteps &&
            step.parentage.alwaysNeedsOutcome { parentId, outcome ->
                stepsById[parentId]?.let { JourneyMapRegistry.exemptionFor(journeyName, it.stepConfigClass, outcome) } != null
            }

    companion object {
        const val CHANGE_PREFIX = "change:"

        fun titleOf(page: PageSnapshot) = page.h1.ifBlank { page.title.substringBefore(" - ").ifBlank { page.url } }
    }
}
