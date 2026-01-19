package uk.gov.communities.prsdb.webapp.journeys

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState

@JourneyFrameworkComponent
class IncompletePropertyCreatingSubjourneyExitStepConfig(
    private val incompletePropertiesService: IncompletePropertiesService,
) : SubjourneyExitStepConfig() {
    override fun afterSaveState(
        state: JourneyState,
        saveStateId: SavedJourneyState,
    ) {
        super.afterSaveState(state, saveStateId)
        incompletePropertiesService.addIncompletePropertyToLandlord(saveStateId)
    }
}

@JourneyFrameworkComponent
class IncompletePropertyCreatingSubjourneyExitStep(
    config: IncompletePropertyCreatingSubjourneyExitStepConfig,
) : SubjourneyExitStep(config)
