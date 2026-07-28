package uk.gov.communities.prsdb.webapp.journeys.builders

import uk.gov.communities.prsdb.webapp.journeys.JourneyState

// EmbedBuilder allows one "journey" to be embedded in another. The outer journey can be accessed as `outerState` and the embedded
// journey can be accessed as `journey` as usual
class EmbedBuilder<TEmbeddedState : JourneyState, TOuterState : JourneyState>(
    val task: TEmbeddedState,
    override val journey: TOuterState,
) : AbstractJourneyBuilder<TEmbeddedState, TOuterState>(task)
