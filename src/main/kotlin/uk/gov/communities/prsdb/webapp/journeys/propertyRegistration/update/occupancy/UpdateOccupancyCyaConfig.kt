package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.OccupancyDetailsHelper
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NewNumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfBedroomsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OccupancyFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class UpdateOccupancyCyaConfig(
    private val occupancyDetailsHelper: OccupancyDetailsHelper,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val messageSource: MessageSource,
) : AbstractCheckYourAnswersStepConfig<UpdateOccupancyJourneyState>() {
    override fun getStepSpecificContent(state: UpdateOccupancyJourneyState) =
        mapOf(
            "title" to "propertyDetails.update.title",
            "showWarning" to true,
            "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
            "insetText" to true,
            "summaryListData" to occupancyDetailsHelper.getCheckYourAnswersSummaryList(state, childJourneyId, messageSource),
            "submittedFilteredJourneyData" to CheckAnswersFormModel.serializeJourneyData(state.getSubmittedStepData()),
            "summaryName" to
                if (isOccupied(state)) {
                    "forms.update.checkOccupancy.occupied.summaryName"
                } else {
                    "forms.update.checkOccupancy.notOccupied.summaryName"
                },
        )

    override fun afterStepDataIsAdded(state: UpdateOccupancyJourneyState) {
        val isOccupied = state.occupied.formModel.notNullValue(OccupancyFormModel::occupied)
        val billsIncludedDataModel = state.getBillsIncludedOrNull()
        propertyOwnershipService.updateOccupancy(
            id = state.propertyId,
            numberOfHouseholds =
                if (isOccupied) {
                    state.households.formModel
                        .notNullValue(NumberOfHouseholdsFormModel::numberOfHouseholds)
                        .toInt()
                } else {
                    0
                },
            numberOfPeople =
                if (isOccupied) {
                    state.tenants.formModel
                        .notNullValue(NewNumberOfPeopleFormModel::numberOfPeople)
                        .toInt()
                } else {
                    0
                },
            numBedrooms =
                if (isOccupied) {
                    state.bedrooms.formModel
                        .notNullValue(NumberOfBedroomsFormModel::numberOfBedrooms)
                        .toInt()
                } else {
                    null
                },
            billsIncludedList = if (isOccupied) billsIncludedDataModel?.standardBillsIncludedString else null,
            customBillsIncluded = if (isOccupied) billsIncludedDataModel?.customBillsIncluded else null,
            furnishedStatus = if (isOccupied) state.furnishedStatus.formModel.furnishedStatus else null,
            rentFrequency = if (isOccupied) state.rentFrequency.formModel.rentFrequency else null,
            customRentFrequency = if (isOccupied) state.getCustomRentFrequencyIfSelected() else null,
            rentAmount =
                if (isOccupied) {
                    state.rentAmount.formModel.rentAmount
                        .toBigDecimal()
                } else {
                    null
                },
            lastModifiedDate = Instant.parse(state.lastModifiedDate).toJavaInstant(),
        )
    }

    private fun isOccupied(state: UpdateOccupancyJourneyState) = state.occupied.formModel.notNullValue(OccupancyFormModel::occupied)
}

@JourneyFrameworkComponent
final class UpdateOccupancyCyaStep(
    stepConfig: UpdateOccupancyCyaConfig,
) : AbstractCheckYourAnswersStep<UpdateOccupancyJourneyState>(stepConfig)
