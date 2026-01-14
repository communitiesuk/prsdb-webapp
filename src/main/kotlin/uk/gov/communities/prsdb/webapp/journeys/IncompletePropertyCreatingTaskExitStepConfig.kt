package uk.gov.communities.prsdb.webapp.journeys

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState

@JourneyFrameworkComponent
class IncompletePropertyCreatingTaskExitStepConfig(
    private val incompletePropertiesService: IncompletePropertiesService,
) : TaskExitStepConfig() {
    override fun afterSaveState(
        state: JourneyState,
        saveStateId: SavedJourneyState,
    ) {
        super.afterSaveState(state, saveStateId)
        incompletePropertiesService.addIncompletePropertyToLandlord(saveStateId)
    }
}

@JourneyFrameworkComponent
class IncompletePropertyCreatingTaskExitStep(
    config: IncompletePropertyCreatingTaskExitStepConfig,
) : TaskExitStep(config)
