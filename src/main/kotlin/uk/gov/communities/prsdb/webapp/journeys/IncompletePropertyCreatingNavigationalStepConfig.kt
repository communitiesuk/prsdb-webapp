package uk.gov.communities.prsdb.webapp.journeys

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import uk.gov.communities.prsdb.webapp.services.IncompletePropertyService

@JourneyFrameworkComponent
class IncompletePropertyCreatingNavigationalStepConfig(
    private val incompletePropertyService: IncompletePropertyService,
) : NavigationalStepConfig() {
    override fun afterSaveState(
        state: JourneyState,
        saveStateId: SavedJourneyState,
    ) {
        super.afterSaveState(state, saveStateId)
        incompletePropertyService.addIncompletePropertyToLandlord(saveStateId)
    }
}

@JourneyFrameworkComponent
class IncompletePropertyCreatingNavigationalStep(
    config: IncompletePropertyCreatingNavigationalStepConfig,
) : NavigationalStep(config)
