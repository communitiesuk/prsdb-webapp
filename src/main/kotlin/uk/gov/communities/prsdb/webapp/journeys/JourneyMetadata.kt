package uk.gov.communities.prsdb.webapp.journeys

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class JourneyMetadata(
    val journeyId: String,
    val baseJourneyId: String? = null,
    val lastUpdated: Instant = Clock.System.now(),
)
