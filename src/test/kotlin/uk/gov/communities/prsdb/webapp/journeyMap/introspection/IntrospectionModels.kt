package uk.gov.communities.prsdb.webapp.journeyMap.introspection

import kotlinx.serialization.Serializable

@Serializable
data class IntrospectionResponse(
    val stepPath: String,
    val journeyId: String?,
    val steps: List<IntrospectedStep>,
) {
    fun stepById(id: String): IntrospectedStep? = steps.firstOrNull { it.id == id }

    fun stepByPath(path: String): IntrospectedStep? = steps.firstOrNull { it.path == path }
}

@Serializable
data class IntrospectedStep(
    val id: String,
    val path: String?,
    val requestable: Boolean,
    val redirecting: Boolean,
    val stepClass: String,
    val stepConfigClass: String,
    val modeValues: List<String>,
    val outcome: String?,
    val reachable: Boolean,
    val exit: Boolean,
    val initial: Boolean,
    val containers: List<IntrospectedContainer>,
    val parentage: IntrospectedParentage,
    val fields: Map<String, List<String>>,
    val destinations: List<IntrospectedDestination> = emptyList(),
)

// Where the step's routing code sends the journey for one outcome: a step, or a URL outside the journey's steps. Reads
// are the journey state the routing code looked at besides the outcome, which make the destination depend on earlier answers
@Serializable
data class IntrospectedDestination(
    val mode: String,
    val step: String? = null,
    val url: String? = null,
    val reads: List<String> = emptyList(),
)

@Serializable
data class IntrospectedContainer(
    val id: String,
    val kind: String,
    val label: String,
)

@Serializable
data class IntrospectedParentage(
    val kind: String,
    val stepId: String? = null,
    val label: String? = null,
    val children: List<IntrospectedParentage> = emptyList(),
) {
    fun singleParents(): List<IntrospectedParentage> = if (kind == SINGLE) listOf(this) else children.flatMap { it.singleParents() }

    // Whether every way of satisfying the parentage needs a parent outcome that matches
    fun alwaysNeedsOutcome(matching: (stepId: String, outcome: String) -> Boolean): Boolean =
        when (kind) {
            SINGLE -> stepId != null && label != null && matching(stepId, label)
            AND -> children.any { it.alwaysNeedsOutcome(matching) }
            OR -> children.isNotEmpty() && children.all { it.alwaysNeedsOutcome(matching) }
            else -> false
        }

    companion object {
        const val NONE = "none"
        const val SINGLE = "single"
        const val AND = "and"
        const val OR = "or"
        const val OTHER = "other"
    }
}
