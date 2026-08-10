package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.companiesHouse

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.InternalStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgCompanyNumberFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgIsRegisteredCompanyFormModel
import uk.gov.communities.prsdb.webapp.services.LandlordService

@JourneyFrameworkComponent
class CompleteCompaniesHouseUpdateStepConfig(
    private val landlordService: LandlordService,
) : AbstractInternalStepConfig<Complete, UpdateCompaniesHouseJourneyState>() {
    override fun mode(state: UpdateCompaniesHouseJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: UpdateCompaniesHouseJourneyState) {
        val isRegisteredWithCompaniesHouse =
            state.orgIsRegisteredCompanyStep.formModel.notNullValue(OrgIsRegisteredCompanyFormModel::companiesHouse)
        if (isRegisteredWithCompaniesHouse) {
            val companyNumber =
                state.orgCompanyNumberStep.formModel.notNullValue(OrgCompanyNumberFormModel::companyNumber)
            landlordService.updateOrganisationalLandlordCompaniesHouseDetails(
                companyNumber = companyNumber,
                governingBodyMembers = emptyList(),
            )
        } else {
            landlordService.updateOrganisationalLandlordCompaniesHouseDetails(
                companyNumber = null,
                governingBodyMembers = state.orgGovBodyMembersTask.governingBodyMembersMap?.values?.toList().orEmpty(),
            )
        }
    }

    override fun resolveNextDestination(
        state: UpdateCompaniesHouseJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class CompleteCompaniesHouseUpdateStep(
    stepConfig: CompleteCompaniesHouseUpdateStepConfig,
) : InternalStep<Complete, UpdateCompaniesHouseJourneyState>(stepConfig)
