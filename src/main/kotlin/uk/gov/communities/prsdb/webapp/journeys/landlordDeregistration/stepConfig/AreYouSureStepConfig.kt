package uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController.Companion.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.LandlordDeregistrationJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LandlordDeregistrationAreYouSureFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel

@JourneyFrameworkComponent("landlordDeregistrationAreYouSureStepConfig")
class AreYouSureStepConfig :
    AbstractRequestableStepConfig<AreYouSureMode, LandlordDeregistrationAreYouSureFormModel, LandlordDeregistrationJourneyState>() {
    override val formModelClass = LandlordDeregistrationAreYouSureFormModel::class

    override fun getStepSpecificContent(state: LandlordDeregistrationJourneyState): Map<String, Any?> {
        val content = mutableMapOf<String, Any?>()

        if (!state.userHasRegisteredProperties) {
            content["radioOptions"] = RadiosViewModel.yesOrNoRadios()
            content["fieldSetHeading"] = "deregisterLandlord.areYouSure.noProperties.fieldSetHeading"
        } else {
            content["cancelLinkUrl"] = LANDLORD_DETAILS_FOR_LANDLORD_ROUTE
        }

        return content
    }

    override fun chooseTemplate(state: LandlordDeregistrationJourneyState) =
        if (state.userHasRegisteredProperties) "forms/landlordDeregistrationAreYouSure" else "forms/areYouSureForm"

    override fun enrichSubmittedDataBeforeValidation(
        state: LandlordDeregistrationJourneyState,
        formData: FormData,
    ): FormData {
        val enrichedData = formData.toMutableMap()
        enrichedData[LandlordDeregistrationAreYouSureFormModel::userHasRegisteredProperties.name] = state.userHasRegisteredProperties
        if (state.userHasRegisteredProperties) {
            enrichedData[LandlordDeregistrationAreYouSureFormModel::wantsToProceed.name] = true
        }
        return enrichedData
    }

    override fun mode(state: LandlordDeregistrationJourneyState): AreYouSureMode? =
        getFormModelFromStateOrNull(state)?.let { formModel ->
            if (state.userHasRegisteredProperties) {
                AreYouSureMode.WANTS_TO_PROCEED
            } else {
                formModel.wantsToProceed?.let {
                    if (it) AreYouSureMode.WANTS_TO_PROCEED else AreYouSureMode.DOES_NOT_WANT_TO_PROCEED
                }
            }
        }
}

@JourneyFrameworkComponent("landlordDeregistrationAreYouSureStep")
final class AreYouSureStep(
    stepConfig: AreYouSureStepConfig,
) : RequestableStep<AreYouSureMode, LandlordDeregistrationAreYouSureFormModel, LandlordDeregistrationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "are-you-sure"
    }
}

enum class AreYouSureMode {
    WANTS_TO_PROCEED,
    DOES_NOT_WANT_TO_PROCEED,
}
