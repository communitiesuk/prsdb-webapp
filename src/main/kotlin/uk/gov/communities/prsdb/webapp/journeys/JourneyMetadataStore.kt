package uk.gov.communities.prsdb.webapp.journeys

import kotlinx.serialization.Serializable

@Serializable
data class JourneyMetadataStore(
    private val entries: Map<String, JourneyMetadata> = emptyMap(),
) : Collection<JourneyMetadata> {
    operator fun get(journeyId: String): JourneyMetadata? = entries[journeyId]

    operator fun plus(metadata: JourneyMetadata): JourneyMetadataStore = JourneyMetadataStore(entries + (metadata.journeyId to metadata))

    operator fun minus(journeyId: String): JourneyMetadataStore = JourneyMetadataStore(entries - journeyId)

    operator fun contains(element: String): Boolean = element in entries

    override operator fun contains(element: JourneyMetadata): Boolean = element.journeyId in entries

    override val size: Int = entries.size

    override fun containsAll(elements: Collection<JourneyMetadata>): Boolean = entries.keys.containsAll(elements.map { it.journeyId })

    override fun isEmpty(): Boolean = entries.isEmpty()

    override fun iterator(): Iterator<JourneyMetadata> = entries.values.iterator()
}
