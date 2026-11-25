package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.helpers.extensions.LandlordExtensions.Companion.getContactEmail
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholdersState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService

// Adds the logged-in landlord to the freeholders list the first time a joint freeholder reaches it, so the list matches
// what CYA and the property record will show. Once added, the card is treated like any other freeholder.
@JourneyFrameworkComponent
class AddLoggedInLandlordFreeholderStepConfig(
    private val userToLandlordService: UserToLandlordService,
) : AbstractInternalStepConfig<Complete, FreeholdersState>() {
    override fun mode(state: FreeholdersState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: FreeholdersState) {
        if (!state.isJointFreeholder) return
        val currentMap = state.freeholdersMap.orEmpty()
        if (currentMap.isNotEmpty() && state.loggedInLandlordFreeholderInitialised == true) return

        val landlord = userToLandlordService.getCurrentLandlordForUser()
        val nextKey = state.nextFreeholderId ?: ((currentMap.keys.maxOrNull() ?: 0) + 1)
        state.freeholdersMap =
            currentMap + (nextKey to FreeholderDataModel(detailsKnown = true, name = landlord.name, email = landlord.getContactEmail()))
        state.nextFreeholderId = nextKey + 1
        state.loggedInLandlordFreeholderInitialised = true
    }
}

@JourneyFrameworkComponent
final class AddLoggedInLandlordFreeholderStep(
    stepConfig: AddLoggedInLandlordFreeholderStepConfig,
) : JourneyStep.InternalStep<Complete, FreeholdersState>(stepConfig)
