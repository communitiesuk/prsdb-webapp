package uk.gov.communities.prsdb.webapp.journeys

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class JourneyMetadata(
    val journeyId: String,
    val lastUpdated: Instant,
    val baseJourneyId: String? = null,
) {
    companion object {
        fun createNew(
            journeyId: String,
            baseJourneyId: String? = null,
        ) = JourneyMetadata(journeyId, Clock.System.now(), baseJourneyId)
    }
}
