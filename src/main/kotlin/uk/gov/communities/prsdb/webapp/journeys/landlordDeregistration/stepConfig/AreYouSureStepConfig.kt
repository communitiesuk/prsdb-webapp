package uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.LandlordDeregistrationJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LandlordDeregistrationAreYouSureFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel

@JourneyFrameworkComponent("landlordDeregistrationAreYouSureStepConfig")
class AreYouSureStepConfig :
    AbstractRequestableStepConfig<AreYouSureMode, LandlordDeregistrationAreYouSureFormModel, LandlordDeregistrationJourneyState>() {
    override val formModelClass = LandlordDeregistrationAreYouSureFormModel::class

    override fun getStepSpecificContent(state: LandlordDeregistrationJourneyState): Map<String, Any?> {
        val content =
            mutableMapOf<String, Any?>(
                "radioOptions" to RadiosViewModel.yesOrNoRadios(),
            )

        if (!state.userHasRegisteredProperties) {
            content["fieldSetHeading"] = "forms.areYouSure.landlordDeregistration.noProperties.fieldSetHeading"
        } else {
            content["fieldSetHeading"] = "forms.areYouSure.landlordDeregistration.hasProperties.fieldSetHeading"
            content["fieldSetHint"] = "forms.areYouSure.landlordDeregistration.hasProperties.fieldSetHint"
        }

        return content
    }

    override fun chooseTemplate(state: LandlordDeregistrationJourneyState) = "forms/areYouSureForm"

    override fun enrichSubmittedDataBeforeValidation(
        state: LandlordDeregistrationJourneyState,
        formData: PageData,
    ): PageData {
        val enrichedData = formData.toMutableMap()
        enrichedData[LandlordDeregistrationAreYouSureFormModel::userHasRegisteredProperties.name] = state.userHasRegisteredProperties
        return enrichedData
    }

    override fun mode(state: LandlordDeregistrationJourneyState): AreYouSureMode? =
        getFormModelFromStateOrNull(state)?.wantsToProceed?.let {
            if (it) AreYouSureMode.WANTS_TO_PROCEED else AreYouSureMode.DOES_NOT_WANT_TO_PROCEED
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
