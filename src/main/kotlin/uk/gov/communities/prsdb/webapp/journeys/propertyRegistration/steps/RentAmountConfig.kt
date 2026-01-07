package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RentAmountFormModel

@JourneyFrameworkComponent
class RentAmountStepConfig : AbstractGenericStepConfig<Complete, RentAmountFormModel, JourneyState>() {
    override val formModelClass = RentAmountFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "title" to "registerProperty.title",
            "heading" to "QQ - getHeading(state.QQ)",
            "fieldSetHint" to "forms.rentAmount.fieldSetHint",
            "billsExplanationForRentFrequency" to "QQ getBillsExplanationForRentFrequency(state.QQ)",
            "showCalculationSection" to "QQ getShowCalculationSection(state.QQ)",
        )

    override fun chooseTemplate(state: JourneyState): String = "forms/rentAmountForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    // TODO PDJB-102 - Remove the "QQ" placeholders once the RentFrequencyStep is implemented
    private fun getHeading(rentFrequency: RentFrequency): String =
        when (rentFrequency) {
            RentFrequency.WEEKLY -> "forms.rentAmount.heading.weekly"
            RentFrequency.FOUR_WEEKLY -> "forms.rentAmount.heading.fourWeekly"
            else -> "forms.rentAmount.heading.monthly"
        }

    private fun getBillsExplanationForRentFrequency(rentFrequency: RentFrequency): String =
        when (rentFrequency) {
            RentFrequency.WEEKLY -> "forms.rentAmount.bullet.three.partTwo.weekly"
            RentFrequency.FOUR_WEEKLY -> "forms.rentAmount.bullet.three.partTwo.fourWeekly"
            else -> "forms.rentAmount.bullet.three.partTwo.monthly"
        }

    private fun getShowCalculationSection(rentFrequency: RentFrequency): Boolean = rentFrequency == RentFrequency.OTHER
}

@JourneyFrameworkComponent
final class RentAmountStep(
    stepConfig: RentAmountStepConfig,
) : RequestableStep<Complete, RentAmountFormModel, JourneyState>(stepConfig)
