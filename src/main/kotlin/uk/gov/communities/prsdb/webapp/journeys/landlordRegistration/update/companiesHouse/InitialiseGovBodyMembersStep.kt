package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.companiesHouse

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.InternalStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.dataModels.GoverningBodyMemberDataModel
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService

@JourneyFrameworkComponent
class InitialiseGovBodyMembersStepConfig(
    private val userToLandlordService: UserToLandlordService,
) : AbstractInternalStepConfig<Complete, UpdateCompaniesHouseTaskState>() {
    override fun mode(state: UpdateCompaniesHouseTaskState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: UpdateCompaniesHouseTaskState) {
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
class InitialiseGovBodyMembersStep(
    stepConfig: InitialiseGovBodyMembersStepConfig,
) : InternalStep<Complete, UpdateCompaniesHouseTaskState>(stepConfig)
