package uk.gov.communities.prsdb.webapp.journeyMap.model

import uk.gov.communities.prsdb.webapp.journeyMap.introspection.IntrospectionResponse

data class Exploration(
    val structures: List<IntrospectionResponse>,
    val pages: List<PageRecord>,
    val transitions: List<Transition>,
    val gaps: List<ManifestGap>,
    val errors: List<ManifestError>,
    val dependencies: List<ManifestDependency> = emptyList(),
    val outcomesPassed: Map<String, List<String>> = emptyMap(),
    val changeFlowSteps: Set<String> = emptySet(),
    val entries: List<EntryRecord> = emptyList(),
)

// The node a walk entered the journey at: the decision the journey starts with, or the first page when that is not drawn
data class EntryRecord(
    val node: String,
    val firstPage: String,
    val label: String?,
)

data class PageRecord(
    val id: String,
    val kind: String,
    val route: String?,
    val title: String,
    val variants: List<VariantRecord>,
    val origin: String = ManifestNode.ORIGIN_MAIN,
    val skippedVariants: Int = 0,
)

data class VariantRecord(
    val hash: String,
    val image: String?,
    val thumbnail: String?,
    val conditions: List<ManifestCondition>,
    val origin: String = ManifestNode.ORIGIN_MAIN,
    val mobileImage: String? = null,
)

data class Transition(
    val from: String,
    val to: String,
    val label: String,
    val mode: String?,
    val via: List<ViaStep> = emptyList(),
    val kind: String = ManifestEdge.OBSERVED,
)

data class ViaStep(
    val id: String,
    val outcome: String?,
)
