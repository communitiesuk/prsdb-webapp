package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.rentFrequencyAndAmount

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.OccupancyDetailsHelper
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RentFrequencyFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class UpdateRentFrequencyAndAmountCyaConfig(
    private val occupancyDetailsHelper: OccupancyDetailsHelper,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val messageSource: MessageSource,
) : AbstractCheckYourAnswersStepConfig<UpdateRentFrequencyAndAmountJourneyState>() {
    override fun getStepSpecificContent(state: UpdateRentFrequencyAndAmountJourneyState): Map<String, Any> =
        mapOf(
            "title" to "propertyDetails.update.title",
            "showWarning" to true,
            "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
            "insetText" to true,
            "summaryListData" to occupancyDetailsHelper.getCheckYourRentFrequencyAndAmountAnswersSummaryList(state, messageSource),
            "submittedFilteredJourneyData" to CheckAnswersFormModel.serializeJourneyData(state.getSubmittedStepData()),
            "summaryName" to "forms.update.checkOccupancy.occupied.summaryName",
        )

    override fun afterStepDataIsAdded(state: UpdateRentFrequencyAndAmountJourneyState) {
        propertyOwnershipService.updateRentFrequencyAndAmount(
            id = state.propertyId,
            rentFrequency = state.rentFrequency.formModel.notNullValue(RentFrequencyFormModel::rentFrequency),
            customRentFrequency = state.getCustomRentFrequencyIfSelected(),
            rentAmount = state.rentAmount.formModel.rentAmount.toBigDecimal(),
            initialLastModifiedDate = Instant.parse(state.lastModifiedDate).toJavaInstant(),
        )
    }
}

@JourneyFrameworkComponent
final class UpdateRentFrequencyAndAmountCyaStep(
    stepConfig: UpdateRentFrequencyAndAmountCyaConfig,
) : AbstractCheckYourAnswersStep<UpdateRentFrequencyAndAmountJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "rent-frequency-and-amount-check-your-answers"
    }
}
