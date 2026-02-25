package uk.gov.communities.prsdb.webapp.journeys

import kotlinx.serialization.Serializable

@Serializable
data class JourneyMetadataStore(
    private val entries: Map<String, JourneyMetadata> = emptyMap(),
) {
    operator fun get(journeyId: String): JourneyMetadata? = entries[journeyId]

    operator fun contains(journeyId: String): Boolean = journeyId in entries

    operator fun plus(metadata: JourneyMetadata): JourneyMetadataStore = JourneyMetadataStore(entries + (metadata.journeyId to metadata))

    operator fun minus(journeyId: String): JourneyMetadataStore = JourneyMetadataStore(entries - journeyId)
}
