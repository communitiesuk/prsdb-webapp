package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class OrgGovBodyMustProvideInfoStepConfig :
    AbstractRequestableStepConfig<Complete, NoInputFormModel, JourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "title" to "forms.orgGovBodyMustProvideInfo.heading",
            "orgTypeUrl" to JourneyStateService.urlWithJourneyState(OrgTypeStep.ROUTE_SEGMENT, state.journeyId),
            "registerAsIndividualUrl" to JourneyStateService.urlWithJourneyState(LandlordTypeStep.ROUTE_SEGMENT, state.journeyId),
        )

    override fun chooseTemplate(state: JourneyState) = "forms/orgGovBodyMustProvideInfoForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class OrgGovBodyMustProvideInfoStep(
    stepConfig: OrgGovBodyMustProvideInfoStepConfig,
) : RequestableStep<Complete, NoInputFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "organisation-governing-body-must-provide-info"
    }
}
