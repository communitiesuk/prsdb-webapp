package uk.gov.communities.prsdb.webapp.journeyMap.model

import kotlinx.serialization.Serializable

@Serializable
data class JourneyMapManifest(
    val journey: JourneyInfo,
    val containers: List<ManifestContainer>,
    val nodes: List<ManifestNode>,
    val edges: List<ManifestEdge>,
    val gaps: List<ManifestGap>,
    val errors: List<ManifestError>,
    val dependencies: List<ManifestDependency> = emptyList(),
    val arrows: List<ManifestArrow> = emptyList(),
    val entries: List<ManifestEntry> = emptyList(),
)

@Serializable
data class JourneyInfo(
    val name: String,
    val title: String,
    val basePath: String,
    val gitCommit: String?,
    val generatedAt: String,
    val featureFlags: Map<String, Boolean>,
)

@Serializable
data class ManifestContainer(
    val id: String,
    val kind: String,
    val label: String,
    val parent: String?,
)

@Serializable
data class ManifestNode(
    val id: String,
    val kind: String,
    val route: String?,
    val title: String,
    val stepConfig: String?,
    val containers: List<String>,
    val variants: List<ManifestVariant>,
    val origin: String = ORIGIN_MAIN,
    val skippedVariants: Int = 0,
) {
    companion object {
        const val PAGE = "page"
        const val DECISION = "decision"
        const val TERMINAL = "terminal"
        const val ORIGIN_MAIN = "main"
        const val ORIGIN_CHANGE = "change"
    }
}

@Serializable
data class ManifestVariant(
    val id: String,
    val image: String?,
    val thumbnail: String?,
    val mobileImage: String? = null,
    val conditions: List<ManifestCondition>,
    val origin: String,
)

@Serializable
data class ManifestCondition(
    val step: String,
    val answer: String,
)

@Serializable
data class ManifestEdge(
    val from: String,
    val to: String,
    val label: String,
    val mode: String?,
    val kind: String,
) {
    companion object {
        const val OBSERVED = "observed"
        const val STRUCTURAL = "structural"
        const val CHANGE = "change"
        const val NOT_WALKED = "not walked"
    }
}

@Serializable
data class ManifestGap(
    val node: String,
    val mode: String?,
    val reason: String,
)

@Serializable
data class ManifestError(
    val node: String,
    val kind: String,
    val detail: String,
)

@Serializable
data class ManifestDependency(
    val decision: String,
    val writers: List<String>,
    val readsAllAnswers: Boolean = false,
)

// An arrow in the journey's routing code: where an outcome of a page or decision leads, and whether a walk took it. Reads
// are the journey state the routing code looked at besides the outcome. Where an arrow that reads journey state leads
// depends on earlier answers, so it has no destination of its own and only the walks show where it went
@Serializable
data class ManifestArrow(
    val from: String,
    val mode: String,
    val to: String?,
    val walked: Boolean,
    val reads: List<String> = emptyList(),
)

// Where users enter the journey, and how they get there, such as the text of the link they follow
@Serializable
data class ManifestEntry(
    val node: String,
    val label: String,
)
