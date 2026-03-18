package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.RentFrequencyAndAmountState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RentFrequencyFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

@JourneyFrameworkComponent
class RentFrequencyStepConfig : AbstractRequestableStepConfig<Complete, RentFrequencyFormModel, RentFrequencyAndAmountState>() {
    override val formModelClass = RentFrequencyFormModel::class

    override fun getStepSpecificContent(state: RentFrequencyAndAmountState) =
        mapOf(
            "heading" to "forms.rentFrequency.heading",
            "fieldSetHeading" to "forms.rentFrequency.fieldSetHeading",
            "label" to "forms.rentFrequency.label",
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = RentFrequency.MONTHLY,
                        labelMsgKey = "forms.rentFrequency.radios.option.monthly.label",
                        hintMsgKey = "forms.rentFrequency.radios.option.monthly.hint",
                    ),
                    RadiosButtonViewModel(
                        value = RentFrequency.FOUR_WEEKLY,
                        labelMsgKey = "forms.rentFrequency.radios.option.fourWeekly.label",
                        hintMsgKey = "forms.rentFrequency.radios.option.fourWeekly.hint",
                    ),
                    RadiosButtonViewModel(
                        value = RentFrequency.WEEKLY,
                        labelMsgKey = "forms.rentFrequency.radios.option.weekly.label",
                        hintMsgKey = "forms.rentFrequency.radios.option.weekly.hint",
                    ),
                    RadiosButtonViewModel(
                        value = RentFrequency.OTHER,
                        labelMsgKey = "forms.rentFrequency.radios.option.other.label",
                        hintMsgKey = "forms.rentFrequency.radios.option.other.hint",
                        conditionalFragment = "customRentFrequencyInput",
                    ),
                ),
        )

    override fun chooseTemplate(state: RentFrequencyAndAmountState): String = "forms/rentFrequencyForm"

    override fun mode(state: RentFrequencyAndAmountState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class RentFrequencyStep(
    stepConfig: RentFrequencyStepConfig,
) : RequestableStep<Complete, RentFrequencyFormModel, RentFrequencyAndAmountState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "rent-frequency"
    }
}
