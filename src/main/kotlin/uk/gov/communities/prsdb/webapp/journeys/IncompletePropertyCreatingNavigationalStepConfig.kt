package uk.gov.communities.prsdb.webapp.journeys

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import uk.gov.communities.prsdb.webapp.services.IncompletePropertyForLandlordService

@JourneyFrameworkComponent
class IncompletePropertyCreatingNavigationalStepConfig(
    private val incompletePropertyForLandlordService: IncompletePropertyForLandlordService,
) : NavigationalStepConfig() {
    override fun afterSaveState(
        state: JourneyState,
        saveStateId: SavedJourneyState,
    ) {
        super.afterSaveState(state, saveStateId)
        incompletePropertyForLandlordService.addIncompletePropertyToLandlord(saveStateId)
    }
}

@JourneyFrameworkComponent
class IncompletePropertyCreatingNavigationalStep(
    config: IncompletePropertyCreatingNavigationalStepConfig,
) : NavigationalStep(config)
