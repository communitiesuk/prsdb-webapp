package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.rentIncludesBills

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.OccupancyDetailsHelper
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class UpdateRentIncludesBillsCyaConfig(
    private val occupancyDetailsHelper: OccupancyDetailsHelper,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val messageSource: MessageSource,
) : AbstractCheckYourAnswersStepConfig<UpdateRentIncludesBillsJourneyState>() {
    override fun getStepSpecificContent(state: UpdateRentIncludesBillsJourneyState): Map<String, Any> =
        mapOf(
            "title" to "propertyDetails.update.title",
            "showWarning" to true,
            "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
            "insetText" to true,
            "summaryListData" to occupancyDetailsHelper.getCheckYourRentIncludesBillsAnswersSummaryList(state, messageSource),
            "submittedFilteredJourneyData" to CheckAnswersFormModel.serializeJourneyData(state.getSubmittedStepData()),
            "summaryName" to "forms.update.checkOccupancy.occupied.summaryName",
        )

    override fun afterStepDataIsAdded(state: UpdateRentIncludesBillsJourneyState) {
        val billsIncludedDataModel = state.getBillsIncludedOrNull()
        propertyOwnershipService.updateRentIncludesBills(
            id = state.propertyId,
            billsIncludedList = billsIncludedDataModel?.standardBillsIncludedListAsString,
            customBillsIncluded = billsIncludedDataModel?.customBillsIncluded,
            initialLastModifiedDate = Instant.parse(state.lastModifiedDate).toJavaInstant(),
        )
    }
}

@JourneyFrameworkComponent
final class UpdateRentIncludesBillsCyaStep(
    stepConfig: UpdateRentIncludesBillsCyaConfig,
) : AbstractCheckYourAnswersStep<UpdateRentIncludesBillsJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "rent-includes-bills-check-your-answers"
    }
}
