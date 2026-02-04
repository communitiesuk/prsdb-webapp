package uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.PropertyDeregistrationJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyDeregistrationAreYouSureFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

@JourneyFrameworkComponent
class AreYouSureStepConfig :
    AbstractRequestableStepConfig<AreYouSureMode, PropertyDeregistrationAreYouSureFormModel, PropertyDeregistrationJourneyState>() {
    override val formModelClass = PropertyDeregistrationAreYouSureFormModel::class

    override fun getStepSpecificContent(state: PropertyDeregistrationJourneyState) =
        mapOf(
            "fieldSetHeading" to "forms.areYouSure.propertyDeregistration.fieldSetHeading",
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = true,
                        valueStr = "yes",
                        labelMsgKey = "forms.radios.option.yes.label",
                    ),
                    RadiosButtonViewModel(
                        value = false,
                        valueStr = "no",
                        labelMsgKey = "forms.radios.option.no.label",
                    ),
                ),
            "optionalFieldSetHeadingParam" to state.getPropertySingleLineAddress(),
        )

    override fun chooseTemplate(state: PropertyDeregistrationJourneyState) = "forms/areYouSureForm"

    override fun mode(state: PropertyDeregistrationJourneyState): AreYouSureMode? =
        getFormModelFromStateOrNull(state)?.wantsToProceed?.let {
            if (it) AreYouSureMode.WANTS_TO_PROCEED else AreYouSureMode.DOES_NOT_WANT_TO_PROCEED
        }

    override fun resolveNextDestination(
        state: PropertyDeregistrationJourneyState,
        defaultDestination: Destination,
    ): Destination =
        if (mode(state) == AreYouSureMode.DOES_NOT_WANT_TO_PROCEED) {
            Destination.ExternalUrl(PropertyDetailsController.getPropertyDetailsPath(state.propertyOwnershipId))
        } else {
            defaultDestination
        }
}

@JourneyFrameworkComponent
final class AreYouSureStep(
    stepConfig: AreYouSureStepConfig,
) : RequestableStep<AreYouSureMode, PropertyDeregistrationAreYouSureFormModel, PropertyDeregistrationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "are-you-sure"
    }
}

enum class AreYouSureMode {
    WANTS_TO_PROCEED,
    DOES_NOT_WANT_TO_PROCEED,
}
