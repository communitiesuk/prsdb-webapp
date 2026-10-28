package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FreeholderProvideDetailsFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

// TODO PDJB-1963: replace this placeholder with the real "can you provide details about the freeholder" page.
// Implement the routing logic
@JourneyFrameworkComponent
class PersonProvideDetailsStepConfig : AbstractRequestableStepConfig<YesOrNo, FreeholderProvideDetailsFormModel, JourneyState>() {
    override val formModelClass = FreeholderProvideDetailsFormModel::class

    override fun getStepSpecificContent(state: JourneyState): Map<String, Any?> =
        mapOf(
            "fieldName" to "provideDetails",
            "fieldSetHeading" to "registerProperty.freeholders.provideDetails.fieldSetHeading",
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(value = YesOrNo.YES, labelMsgKey = "forms.radios.option.yes.label"),
                    RadiosButtonViewModel(value = YesOrNo.NO, labelMsgKey = "forms.radios.option.no.label"),
                ),
        )

    override fun chooseTemplate(state: JourneyState) = "forms/todoWithRadios"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.provideDetails
}

@JourneyFrameworkComponent
final class PersonProvideDetailsStep(
    stepConfig: PersonProvideDetailsStepConfig,
) : RequestableStep<YesOrNo, FreeholderProvideDetailsFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "freeholder-provide-details"
    }
}
