package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.IncompletePropertyForLandlordService

@JourneyFrameworkComponent
class AddToLandlordIncompletePropertiesStepConfig(
    private val incompletePropertyForLandlordService: IncompletePropertyForLandlordService,
) : AbstractInternalStepConfig<Complete, JourneyState>() {
    override fun mode(state: JourneyState) = Complete.COMPLETE

    override fun afterSaveState(
        state: JourneyState,
        saveStateId: SavedJourneyState,
    ) {
        incompletePropertyForLandlordService.addIncompletePropertyToLandlord(saveStateId)
    }
}

@JourneyFrameworkComponent
final class AddToLandlordIncompletePropertiesStep(
    stepConfig: AddToLandlordIncompletePropertiesStepConfig,
) : JourneyStep.InternalStep<Complete, JourneyState>(stepConfig)
