package uk.gov.communities.prsdb.webapp.integration.journeyMap.explorer

import uk.gov.communities.prsdb.webapp.journeyMap.discovery.Candidate
import uk.gov.communities.prsdb.webapp.journeyMap.discovery.PageSnapshot
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestCondition
import uk.gov.communities.prsdb.webapp.journeyMap.model.ViaStep

sealed class WalkAction {
    abstract val key: String
    abstract val label: String

    data class Submit(
        val candidate: Candidate,
        override val label: String,
    ) : WalkAction() {
        override val key: String get() = candidate.key
    }

    data class Upload(
        val candidate: Candidate,
    ) : WalkAction() {
        override val key: String get() = "upload:${candidate.key}"
        override val label: String get() = "Upload a file"
    }

    data class Follow(
        val href: String,
        override val label: String,
        override val key: String,
    ) : WalkAction()
}

enum class LandingKind {
    PAGE,
    DEAD_END,
    TERMINAL,
    FAILED,
}

data class Landing(
    val action: WalkAction,
    val kind: LandingKind,
    val nodeId: String? = null,
    val stepId: String? = null,
    val url: String = "",
    val journeyId: String? = null,
    val hash: String? = null,
    val page: PageSnapshot? = null,
    val outcome: String? = null,
    val via: List<ViaStep> = emptyList(),
    val sessionSnapshot: String? = null,
    val condition: ManifestCondition? = null,
    val newModes: Boolean = false,
) {
    // A landing is walked further only when it changes the journey's structure, not just the content of the next page.
    // Without a continuation to compare against, only a landing that produced new modes is walked
    fun propagatesFrom(continuation: Landing?) =
        kind == LandingKind.PAGE && (newModes || (continuation != null && changesStructureFrom(continuation)))

    fun isContentVariantOf(continuation: Landing) = kind == LandingKind.PAGE && !propagatesFrom(continuation) && hash != continuation.hash

    private fun changesStructureFrom(continuation: Landing) =
        stepId != continuation.stepId || outcome != continuation.outcome || via != continuation.via
}

data class WalkStart(
    val sessionSnapshot: String,
    val url: String,
    val conditions: List<ManifestCondition>,
    val visited: Set<String>,
    val isMainWalk: Boolean = false,
    val resumed: Boolean = false,
    val origin: String? = null,
)

data class ChangePosition(
    val sessionSnapshot: String,
    val url: String,
    val nodeId: String,
    val depth: Int,
)
