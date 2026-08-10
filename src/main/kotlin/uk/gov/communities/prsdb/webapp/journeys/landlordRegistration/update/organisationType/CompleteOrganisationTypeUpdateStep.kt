package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationType

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.OrgType
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.InternalStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LeadTrusteeEmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LeadTrusteeNameFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LeadTrusteePhoneFormModel
import uk.gov.communities.prsdb.webapp.services.LandlordService

@JourneyFrameworkComponent
class CompleteOrganisationTypeUpdateStepConfig(
    private val landlordService: LandlordService,
) : AbstractInternalStepConfig<Complete, UpdateOrganisationTypeJourneyState>() {
    override fun mode(state: UpdateOrganisationTypeJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: UpdateOrganisationTypeJourneyState) {
        val selectedOrgTypes = state.orgTypeStep.formModel.getSelectedOrgTypes()
        val addingTrust = state.orgTypeUpdateRoutingStep.outcome == OrgTypeUpdateRouteMode.ADDING_TRUST

        if (addingTrust) {
            landlordService.updateOrganisationLandlordTypeAndLeadTrustee(
                isCompany = OrgType.COMPANY in selectedOrgTypes,
                isCharity = OrgType.CHARITY in selectedOrgTypes,
                isTrust = OrgType.TRUST in selectedOrgTypes,
                leadTrusteeName =
                    state.leadTrusteeTask.leadTrusteeNameStep.formModel
                        .notNullValue(LeadTrusteeNameFormModel::name),
                leadTrusteeDateOfBirth =
                    state.leadTrusteeTask.leadTrusteeDobStep.formModel
                        .toLocalDateOrNull(),
                leadTrusteeEmail =
                    state.leadTrusteeTask.leadTrusteeEmailStep.formModel
                        .notNullValue(LeadTrusteeEmailFormModel::emailAddress),
                leadTrusteePhone =
                    state.leadTrusteeTask.leadTrusteePhoneStep.formModel
                        .notNullValue(LeadTrusteePhoneFormModel::phoneNumber),
                leadTrusteeAddress =
                    state.leadTrusteeTask.trusteeAddressTask.getAddress(),
            )
        } else {
            landlordService.updateOrganisationLandlordType(
                isCompany = OrgType.COMPANY in selectedOrgTypes,
                isCharity = OrgType.CHARITY in selectedOrgTypes,
                isTrust = OrgType.TRUST in selectedOrgTypes,
            )
        }
    }

    override fun resolveNextDestination(
        state: UpdateOrganisationTypeJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class CompleteOrganisationTypeUpdateStep(
    stepConfig: CompleteOrganisationTypeUpdateStepConfig,
) : InternalStep<Complete, UpdateOrganisationTypeJourneyState>(stepConfig)
