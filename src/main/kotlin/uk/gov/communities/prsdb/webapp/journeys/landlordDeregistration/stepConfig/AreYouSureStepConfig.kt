package uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController.Companion.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.LandlordDeregistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent("landlordDeregistrationAreYouSureStepConfig")
class AreYouSureStepConfig :
    AbstractRequestableStepConfig<Complete, NoInputFormModel, LandlordDeregistrationJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: LandlordDeregistrationJourneyState) =
        mapOf(
            "userHasRegisteredProperties" to state.userHasRegisteredProperties,
            "cancelLinkUrl" to LANDLORD_DETAILS_FOR_LANDLORD_ROUTE,
        )

    override fun chooseTemplate(state: LandlordDeregistrationJourneyState) = "forms/landlordDeregistrationAreYouSure"

    override fun mode(state: LandlordDeregistrationJourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent("landlordDeregistrationAreYouSureStep")
final class AreYouSureStep(
    stepConfig: AreYouSureStepConfig,
) : RequestableStep<Complete, NoInputFormModel, LandlordDeregistrationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "are-you-sure"
    }
}
