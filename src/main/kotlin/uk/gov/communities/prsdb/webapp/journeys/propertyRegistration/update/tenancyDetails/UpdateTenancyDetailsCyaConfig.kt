package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.tenancyDetails

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.OccupancyDetailsHelper
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FurnishedStatusFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NewNumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfBedroomsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RentFrequencyFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyUpdateEmailService

@JourneyFrameworkComponent
class UpdateTenancyDetailsCyaConfig(
    private val occupancyDetailsHelper: OccupancyDetailsHelper,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val messageSource: MessageSource,
    private val propertyUpdateEmailService: PropertyUpdateEmailService,
) : AbstractCheckYourAnswersStepConfig<UpdateTenancyDetailsJourneyState>() {
    override fun getStepSpecificContent(state: UpdateTenancyDetailsJourneyState): Map<String, Any?> =
        mapOf(
            "title" to "propertyDetails.update.title",
            "showWarning" to true,
            "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
            "insetText" to true,
            "summaryListData" to occupancyDetailsHelper.getCheckYourTenancyDetailsAnswersSummaryList(state, messageSource),
            "summaryName" to "forms.update.checkOccupancy.occupied.summaryName",
        )

    override fun afterStepDataIsAdded(state: UpdateTenancyDetailsJourneyState) {
        val billsIncludedDataModel = state.rentIncludesBillsTask.getBillsIncludedOrNull()
        try {
            propertyOwnershipService.updateTenancyDetails(
                id = state.propertyId,
                numberOfHouseholds =
                    state.householdsAndTenantsTask.households.formModel
                        .notNullValue(NumberOfHouseholdsFormModel::numberOfHouseholds)
                        .toInt(),
                numberOfPeople =
                    state.householdsAndTenantsTask.tenants.formModel
                        .notNullValue(NewNumberOfPeopleFormModel::numberOfPeople)
                        .toInt(),
                numBedrooms =
                    state.bedrooms.formModel
                        .notNullValue(NumberOfBedroomsFormModel::numberOfBedrooms)
                        .toInt(),
                billsIncludedList = billsIncludedDataModel?.standardBillsIncludedListAsString,
                customBillsIncluded = billsIncludedDataModel?.customBillsIncluded,
                furnishedStatus = state.furnishedStatus.formModel.notNullValue(FurnishedStatusFormModel::furnishedStatus),
                rentFrequency =
                    state.rentFrequencyAndAmountTask.rentFrequency.formModel.notNullValue(
                        RentFrequencyFormModel::rentFrequency,
                    ),
                customRentFrequency = state.rentFrequencyAndAmountTask.getCustomRentFrequencyIfSelected(),
                rentAmount =
                    state.rentFrequencyAndAmountTask.rentAmount.formModel.rentAmount
                        .toBigDecimal(),
                initialLastModifiedDate = Instant.parse(state.lastModifiedDate).toJavaInstant(),
            )
        } catch (ex: UpdateConflictException) {
            state.deleteJourney()
            throw ex
        }
        sendUpdateConfirmationEmail(state)
    }

    private fun sendUpdateConfirmationEmail(state: UpdateTenancyDetailsJourneyState) {
        propertyUpdateEmailService.sendUpdateEmails(
            state.propertyId,
            listOf(
                "The number of households living in this property",
                "The number of people living in this property",
                "The number of bedrooms in this property",
                "Whether the rent includes bills",
                "Whether the property is furnished",
                "How often the rent is charged",
                "The amount of rent charged",
            ),
        )
    }
}

@JourneyFrameworkComponent
final class UpdateTenancyDetailsCyaStep(
    stepConfig: UpdateTenancyDetailsCyaConfig,
) : AbstractCheckYourAnswersStep<UpdateTenancyDetailsJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "tenancy-details-check-your-answers"
    }
}
