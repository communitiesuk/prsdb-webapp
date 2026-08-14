package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.governingBody

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator.RedirectingStepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.dataModels.GoverningBodyMemberDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService

@JourneyFrameworkComponent
class InitialiseGovBodyMembersForGovBodyUpdateStepConfig(
    private val userToLandlordService: UserToLandlordService,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, UpdateGoverningBodyJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepLifecycleOrchestrator(journeyStep: JourneyStep<*, *, *>) = RedirectingStepLifecycleOrchestrator(journeyStep)

    override fun getStepSpecificContent(state: UpdateGoverningBodyJourneyState): Map<String, Any?> = emptyMap()

    override fun chooseTemplate(state: UpdateGoverningBodyJourneyState): String = ""

    override fun mode(state: UpdateGoverningBodyJourneyState): Complete = Complete.COMPLETE

    override fun beforeChoosingNextDestination(state: UpdateGoverningBodyJourneyState) {
        if (state.governingBodyMembersInitialised == true) return
        val existingMembers = userToLandlordService.getCurrentOrganisationLandlordForUser().governingBodyMembers
        state.governingBodyMembersMap =
            existingMembers
                .mapIndexed { index, member -> (index + 1) to GoverningBodyMemberDataModel.fromEntity(member) }
                .toMap()
        state.nextGoverningBodyMemberId = existingMembers.size + 1
        state.governingBodyMembersInitialised = true
    }
}

@JourneyFrameworkComponent
class InitialiseGovBodyMembersForGovBodyUpdateStep(
    stepConfig: InitialiseGovBodyMembersForGovBodyUpdateStepConfig,
) : RequestableStep<Complete, NoInputFormModel, UpdateGoverningBodyJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "start"
    }
}
