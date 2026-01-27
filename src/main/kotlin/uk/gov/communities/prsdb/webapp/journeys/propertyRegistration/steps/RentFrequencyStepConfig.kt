package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.OccupationState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RentFrequencyFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

@JourneyFrameworkComponent
class RentFrequencyStepConfig : AbstractRequestableStepConfig<Complete, RentFrequencyFormModel, OccupationState>() {
    override val formModelClass = RentFrequencyFormModel::class

    override fun getStepSpecificContent(state: OccupationState) =
        mapOf(
            "fieldSetHeading" to "forms.rentFrequency.fieldSetHeading",
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
                    ),
                    RadiosButtonViewModel(
                        value = RentFrequency.OTHER,
                        labelMsgKey = "forms.rentFrequency.radios.option.other.label",
                        conditionalFragment = "customRentFrequencyInput",
                    ),
                ),
        )

    override fun chooseTemplate(state: OccupationState): String = "forms/rentFrequencyForm"

    override fun mode(state: OccupationState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class RentFrequencyStep(
    stepConfig: RentFrequencyStepConfig,
) : RequestableStep<Complete, RentFrequencyFormModel, OccupationState>(stepConfig)
