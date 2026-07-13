package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.LandlordRegistrationOrgLandlordState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class OrgGovBodyMustProvideInfoStepConfig :
    AbstractRequestableStepConfig<Complete, NoInputFormModel, LandlordRegistrationOrgLandlordState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: LandlordRegistrationOrgLandlordState) =
        mapOf(
            "title" to "forms.orgGovBodyMustProvideInfo.heading",
            "orgTypeUrl" to JourneyStateService.urlWithJourneyState(OrgTypeStep.ROUTE_SEGMENT, state.journeyId),
            "registerAsIndividualUrl" to JourneyStateService.urlWithJourneyState(OrgTypeStep.ROUTE_SEGMENT, state.journeyId),
        )

    override fun chooseTemplate(state: LandlordRegistrationOrgLandlordState) = "forms/orgGovBodyMustProvideInfoForm"

    override fun mode(state: LandlordRegistrationOrgLandlordState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class OrgGovBodyMustProvideInfoStep(
    stepConfig: OrgGovBodyMustProvideInfoStepConfig,
) : RequestableStep<Complete, NoInputFormModel, LandlordRegistrationOrgLandlordState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "organisation-governing-body-must-provide-info"
    }
}
