package uk.gov.communities.prsdb.webapp.journeys.organisationalLandlordDeregistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.organisationalLandlordDeregistration.OrganisationalLandlordDeregistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent("organisationalLandlordDeregistrationAreYouSureStepConfig")
class AreYouSureStepConfig :
    AbstractRequestableStepConfig<Complete, NoInputFormModel, OrganisationalLandlordDeregistrationJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: OrganisationalLandlordDeregistrationJourneyState) =
        mapOf("todoComment" to "TODO: PDJB-1482 - Org landlord deregistration are you sure page")

    override fun chooseTemplate(state: OrganisationalLandlordDeregistrationJourneyState) = "forms/todo"

    override fun mode(state: OrganisationalLandlordDeregistrationJourneyState) =
        getFormModelFromStateOrNull(
            state,
        )?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent("organisationalLandlordDeregistrationAreYouSureStep")
final class AreYouSureStep(
    stepConfig: AreYouSureStepConfig,
) : RequestableStep<Complete, NoInputFormModel, OrganisationalLandlordDeregistrationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "are-you-sure"
    }
}
