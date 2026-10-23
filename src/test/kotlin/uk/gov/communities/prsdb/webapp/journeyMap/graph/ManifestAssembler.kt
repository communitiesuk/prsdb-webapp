package uk.gov.communities.prsdb.webapp.journeyMap.graph

import uk.gov.communities.prsdb.webapp.journeyMap.JourneyMapText
import uk.gov.communities.prsdb.webapp.journeyMap.introspection.IntrospectedDestination
import uk.gov.communities.prsdb.webapp.journeyMap.introspection.IntrospectedStep
import uk.gov.communities.prsdb.webapp.journeyMap.model.Exploration
import uk.gov.communities.prsdb.webapp.journeyMap.model.JourneyInfo
import uk.gov.communities.prsdb.webapp.journeyMap.model.JourneyMapManifest
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestArrow
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestContainer
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestEdge
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestEntry
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestNode
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestVariant

class ManifestAssembler {
    fun assemble(
        journey: JourneyInfo,
        exploration: Exploration,
    ): JourneyMapManifest {
        val steps = linkedMapOf<String, IntrospectedStep>()
        exploration.structures.forEach { structure -> structure.steps.forEach { steps.putIfAbsent(it.id, it) } }

        val nodes = linkedMapOf<String, ManifestNode>()
        exploration.pages.forEach { page ->
            val step = steps[page.id]
            val containers =
                if (page.origin == ManifestNode.ORIGIN_CHANGE && step == null) {
                    listOf(CHANGES_CONTAINER.id)
                } else {
                    step?.containers?.map { it.id } ?: emptyList()
                }
            nodes[page.id] =
                ManifestNode(
                    id = page.id,
                    kind = page.kind,
                    route = page.route,
                    title = page.title,
                    stepConfig = step?.stepConfigClass ?: page.route?.let { steps[it]?.stepConfigClass },
                    containers = containers,
                    variants =
                        page.variants.mapIndexed { index, variant ->
                            ManifestVariant(
                                "v${index + 1}",
                                variant.image,
                                variant.thumbnail,
                                variant.mobileImage,
                                variant.conditions,
                                variant.origin,
                            )
                        },
                    origin = page.origin,
                    skippedVariants = page.skippedVariants,
                )
        }

        val edges = linkedSetOf<ManifestEdge>()
        exploration.transitions.forEach { transition ->
            var from = transition.from
            var label = transition.label
            var mode = transition.mode
            transition.via
                .filter { isDecision(steps[it.id]) }
                .forEach { via ->
                    addStepNode(nodes, steps, via.id)
                    edges += ManifestEdge(from, via.id, label, mode, transition.kind)
                    from = via.id
                    label = via.outcome?.let { JourneyMapText.humaniseMode(it) } ?: ""
                    mode = via.outcome
                }
            addStepNode(nodes, steps, transition.from)
            addStepNode(nodes, steps, transition.to)
            edges += ManifestEdge(from, transition.to, label, mode, transition.kind)
        }

        // An outcome the journey moved on with took its arrow. Arrows whose routing code also reads journey state are drawn
        // by the walks alone, as introspection only shows where they lead with the answers given at the time
        val arrows =
            routingArrows(steps, journey.basePath, exploration.changeFlowSteps).map { arrow ->
                arrow.copy(walked = arrow.mode in exploration.outcomesPassed[arrow.from].orEmpty())
            }
        val drawn = edges.filter { it.kind == ManifestEdge.OBSERVED }.map { it.from to it.to }.toSet()
        arrows.forEach { arrow ->
            val to = arrow.to ?: return@forEach
            if (arrow.walked && arrow.from to to in drawn) return@forEach
            addStepNode(nodes, steps, arrow.from)
            addStepNode(nodes, steps, to)
            val kind = if (arrow.walked) ManifestEdge.OBSERVED else ManifestEdge.NOT_WALKED
            edges += ManifestEdge(arrow.from, to, JourneyMapText.humaniseMode(arrow.mode), arrow.mode, kind)
        }

        exploration.structures
            .firstOrNull()
            ?.steps
            .orEmpty()
            .filter { it.requestable && !it.redirecting && it.id !in nodes }
            .forEach { addStepNode(nodes, steps, it.id) }
        addStructuralEdges(nodes, steps, edges)

        val usedContainers = nodes.values.flatMap { it.containers }.toSet()
        val containers = linkedMapOf<String, ManifestContainer>()
        steps.values.forEach { step ->
            step.containers.forEachIndexed { index, container ->
                if (container.id in usedContainers) {
                    containers.putIfAbsent(
                        container.id,
                        ManifestContainer(container.id, container.kind, container.label, step.containers.getOrNull(index - 1)?.id),
                    )
                }
            }
        }
        if (CHANGES_CONTAINER.id in usedContainers) {
            containers[CHANGES_CONTAINER.id] = CHANGES_CONTAINER
        }

        val entries =
            exploration.entries
                .mapNotNull { entry ->
                    val node = listOf(entry.node, entry.firstPage).firstOrNull { it in nodes } ?: return@mapNotNull null
                    node to entry.label
                }.groupBy({ it.first }, { it.second })
                .map { (node, labels) ->
                    ManifestEntry(node, labels.filterNotNull().filter { it.isNotBlank() }.distinct().joinToString(" or "))
                }

        return JourneyMapManifest(
            journey = journey,
            containers = containers.values.toList(),
            nodes = nodes.values.toList(),
            edges = mergeEdges(edges.toList(), nodes),
            gaps = exploration.gaps,
            errors = exploration.errors,
            dependencies = exploration.dependencies,
            arrows = arrows,
            entries = entries,
        )
    }

    // Arrows join pages and decisions, so they pass straight through steps with one outcome and the exits of tasks. Steps
    // only seen while changing answers belong to other journeys, so they have no arrows here
    private fun routingArrows(
        steps: Map<String, IntrospectedStep>,
        basePath: String,
        changeFlowSteps: Set<String>,
    ): List<ManifestArrow> =
        steps.values
            .filter { isArrowEnd(it) && it.id !in changeFlowSteps }
            .flatMap { step ->
                step.destinations.mapNotNull { destination ->
                    val (target, reads) = resolve(destination, steps, basePath)
                    when {
                        reads.isNotEmpty() -> ManifestArrow(step.id, destination.mode, null, walked = false, reads = reads.toList())
                        target != null -> ManifestArrow(step.id, destination.mode, target, walked = false)
                        else -> null
                    }
                }
            }.distinct()

    private fun resolve(
        destination: IntrospectedDestination,
        steps: Map<String, IntrospectedStep>,
        basePath: String,
    ): Pair<String?, Set<String>> {
        val reads = destination.reads.toSortedSet()
        var target = targetOf(destination, steps, basePath) ?: return null to reads
        val passed = mutableSetOf<String>()
        while (true) {
            val step = steps[target] ?: break
            if (isArrowEnd(step) || !passed.add(target)) break
            val next = step.destinations.firstOrNull() ?: break
            reads += next.reads
            target = targetOf(next, steps, basePath) ?: return null to reads
        }
        return target to reads
    }

    private fun targetOf(
        destination: IntrospectedDestination,
        steps: Map<String, IntrospectedStep>,
        basePath: String,
    ): String? =
        destination.step ?: destination.url?.let { url ->
            val path = url.removePrefix("$basePath/")
            steps.values.firstOrNull { it.path == path }?.id ?: url
        }

    private fun isArrowEnd(step: IntrospectedStep) = (step.requestable && !step.redirecting) || isDecision(step)

    private fun mergeEdges(
        edges: List<ManifestEdge>,
        nodes: Map<String, ManifestNode>,
    ): List<ManifestEdge> {
        val merged =
            edges
                .groupBy { Triple(it.from, it.to, it.kind) }
                .map { (_, group) ->
                    val labels = group.map { it.label }.filter { it.isNotBlank() }.distinct()
                    val label =
                        if (labels.size > MAX_MERGED_LABELS) {
                            labels.take(2).joinToString(" / ") + " and ${labels.size - 2} more"
                        } else {
                            labels.joinToString(" / ")
                        }
                    group.first().copy(label = label)
                }
        val singleDestinationSources =
            merged
                .filter { it.kind == ManifestEdge.OBSERVED }
                .groupBy { it.from }
                .filterValues { outgoing -> outgoing.size == 1 }
                .keys
        return merged.map { edge ->
            val fromDecision = nodes[edge.from]?.kind == ManifestNode.DECISION
            if (edge.kind == ManifestEdge.OBSERVED && edge.from in singleDestinationSources && !fromDecision) {
                edge.copy(label = "")
            } else {
                edge
            }
        }
    }

    private fun isDecision(step: IntrospectedStep?): Boolean = step != null && !step.exit && step.modeValues.size > 1

    private fun addStructuralEdges(
        nodes: MutableMap<String, ManifestNode>,
        steps: Map<String, IntrospectedStep>,
        edges: MutableSet<ManifestEdge>,
    ) {
        val withIncoming = edges.map { it.to }.toMutableSet()
        val queue = ArrayDeque(nodes.keys.filter { it !in withIncoming })
        val handled = mutableSetOf<String>()
        while (queue.isNotEmpty()) {
            val nodeId = queue.removeFirst()
            if (!handled.add(nodeId)) continue
            val step = steps[nodeId] ?: continue
            step.parentage.singleParents().forEach { single ->
                val label = single.label
                resolveParents(single.stepId, steps, mutableSetOf()).forEach { parentId ->
                    if (parentId !in nodes) {
                        addStepNode(nodes, steps, parentId)
                        if (parentId !in withIncoming) queue.add(parentId)
                    }
                    if (parentId in nodes) {
                        edges +=
                            ManifestEdge(
                                parentId,
                                nodeId,
                                label?.let { JourneyMapText.humaniseMode(it) } ?: "",
                                label,
                                ManifestEdge.STRUCTURAL,
                            )
                        withIncoming += nodeId
                    }
                }
            }
        }
    }

    private fun resolveParents(
        stepId: String?,
        steps: Map<String, IntrospectedStep>,
        visited: MutableSet<String>,
    ): List<String> {
        val step = steps[stepId ?: return emptyList()] ?: return emptyList()
        if (!visited.add(step.id)) return emptyList()
        return if (step.exit) {
            step.parentage.singleParents().flatMap { resolveParents(it.stepId, steps, visited) }
        } else {
            listOf(step.id)
        }
    }

    private fun addStepNode(
        nodes: MutableMap<String, ManifestNode>,
        steps: Map<String, IntrospectedStep>,
        id: String,
    ) {
        if (id in nodes) return
        val step = steps[id]
        val kind =
            when {
                step == null -> ManifestNode.TERMINAL
                step.requestable && !step.redirecting -> ManifestNode.PAGE
                else -> ManifestNode.DECISION
            }
        nodes[id] =
            ManifestNode(
                id = id,
                kind = kind,
                route = step?.path ?: id,
                title = step?.let { JourneyMapText.humaniseClassName(it.stepClass) } ?: id,
                stepConfig = step?.stepConfigClass,
                containers = step?.containers?.map { it.id } ?: emptyList(),
                variants = emptyList(),
            )
    }

    companion object {
        val CHANGES_CONTAINER = ManifestContainer("changes", "changes", "Changing answers", null)
        private const val MAX_MERGED_LABELS = 3
    }
}
