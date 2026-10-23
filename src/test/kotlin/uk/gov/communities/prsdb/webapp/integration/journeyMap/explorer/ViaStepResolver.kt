package uk.gov.communities.prsdb.webapp.integration.journeyMap.explorer

import uk.gov.communities.prsdb.webapp.journeyMap.introspection.IntrospectionResponse
import uk.gov.communities.prsdb.webapp.journeyMap.model.ViaStep

class ViaStepResolver(
    private val urls: JourneyUrls,
) {
    // Redirecting steps show up in the redirect chain; internal steps are found by comparing outcomes before and after,
    // and by tracing back from the landing step, because some internal steps have an outcome before they are reached
    fun resolve(
        before: IntrospectionResponse,
        after: IntrospectionResponse,
        redirectChain: List<String>,
        sourceId: String,
        targetId: String?,
    ): List<ViaStep> {
        val chainIds =
            redirectChain
                .mapNotNull { urls.stepPathOf(it) }
                .mapNotNull { after.stepByPath(it)?.id }
                .filter { it != sourceId && it != targetId }
                .distinct()
        val reached = (listOf(sourceId) + chainIds).toMutableSet()
        val candidates =
            after.steps.filter { step ->
                !step.requestable && !step.exit && step.outcome != null && before.stepById(step.id)?.outcome == null
            }
        val internal = mutableListOf<String>()
        var added = true
        while (added) {
            added = false
            candidates
                .filter { it.id !in reached && parentIds(after, it.id).any { parent -> parent in reached } }
                .forEach {
                    reached += it.id
                    internal += it.id
                    added = true
                }
        }
        val leading = leadingInternalSteps(after, reached, targetId).filter { it !in reached }
        return (chainIds + internal + leading).map { ViaStep(it, after.stepById(it)?.outcome) }
    }

    private fun leadingInternalSteps(
        structure: IntrospectionResponse,
        reached: Set<String>,
        targetId: String?,
    ): List<String> {
        val path = mutableListOf<String>()
        val seen = mutableSetOf<String>()
        var current = targetId ?: return emptyList()
        while (seen.add(current)) {
            val parents = satisfiedParentIds(structure, current)
            if (parents.any { it in reached }) return path.reversed()
            val next =
                parents.firstOrNull { parentId ->
                    structure.stepById(parentId)?.let { it.exit || (!it.requestable && it.outcome != null) } ?: false
                } ?: return emptyList()
            if (structure.stepById(next)?.exit == false) path += next
            current = next
        }
        return emptyList()
    }

    private fun satisfiedParentIds(
        structure: IntrospectionResponse,
        stepId: String,
    ) = structure
        .stepById(stepId)
        ?.parentage
        ?.singleParents()
        .orEmpty()
        .filter { link ->
            val outcome = link.stepId?.let { structure.stepById(it) }?.let { if (it.exit) "exit" else it.outcome }
            val label = link.label
            when {
                outcome == null -> false
                label == null -> true
                label.startsWith("not ") -> outcome != label.removePrefix("not ")
                else -> outcome == label
            }
        }.mapNotNull { it.stepId }

    private fun parentIds(
        structure: IntrospectionResponse,
        stepId: String,
        seen: MutableSet<String> = mutableSetOf(),
    ): Set<String> {
        val step = structure.stepById(stepId) ?: return emptySet()
        return step.parentage
            .singleParents()
            .mapNotNull { it.stepId }
            .flatMap { parentId ->
                val parent = structure.stepById(parentId)
                if (parent != null && parent.exit && seen.add(parentId)) {
                    parentIds(structure, parentId, seen) + parentId
                } else {
                    setOf(parentId)
                }
            }.toSet()
    }
}
