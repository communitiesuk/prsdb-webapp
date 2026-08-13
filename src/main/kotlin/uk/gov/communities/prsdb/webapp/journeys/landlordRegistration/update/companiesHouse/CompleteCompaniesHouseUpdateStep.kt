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
        // The standalone journey always routes through the company number / governing body questions (there's a single
        // change link), so the collected answers are always persisted here regardless of whether the registration
        // status itself changed.
        val isRegisteredWithCompaniesHouse =
            state.updateCompaniesHouseTask.orgIsRegisteredCompanyStep.formModel
                .notNullValue(OrgIsRegisteredCompanyFormModel::companiesHouse)
        if (isRegisteredWithCompaniesHouse) {
            val companyNumber =
                state.updateCompaniesHouseTask.orgCompanyNumberStep.formModel
                    .notNullValue(OrgCompanyNumberFormModel::companyNumber)
            landlordService.updateOrganisationalLandlordToRegisteredCompany(companyNumber)
        } else {
            landlordService.updateOrganisationalLandlordToNonRegisteredCompany(
                state.updateCompaniesHouseTask.governingBodyMembersMap?.values?.toList().orEmpty(),
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
