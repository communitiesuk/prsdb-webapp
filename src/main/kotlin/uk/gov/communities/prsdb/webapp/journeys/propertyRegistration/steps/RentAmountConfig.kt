package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.OccupationState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RentAmountFormModel

@JourneyFrameworkComponent
class RentAmountStepConfig : AbstractGenericStepConfig<Complete, RentAmountFormModel, OccupationState>() {
    override val formModelClass = RentAmountFormModel::class

    override fun getStepSpecificContent(state: OccupationState): Map<String, Any?> {
        val rentFrequency = state.rentFrequency.formModel.rentFrequency!!
        return mapOf(
            "title" to "registerProperty.title",
            "heading" to getHeading(rentFrequency),
            "fieldSetHint" to "forms.rentAmount.fieldSetHint",
            "billsExplanationForRentFrequency" to getBillsExplanationForRentFrequency(rentFrequency),
            "showCalculationSection" to (rentFrequency == RentFrequency.OTHER),
        )
    }

    override fun chooseTemplate(state: OccupationState): String = "forms/rentAmountForm"

    override fun mode(state: OccupationState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

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
}

@JourneyFrameworkComponent
final class RentAmountStep(
    stepConfig: RentAmountStepConfig,
) : RequestableStep<Complete, RentAmountFormModel, OccupationState>(stepConfig)
