package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.OccupancyDetailsHelper
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NewNumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfBedroomsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OccupancyFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyUpdateEmailService

// TODO(PDJB-1340): delete this old (flag-off) check-your-answers step when
// PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING is removed. The redesigned occupancy update is a single-page
// update (see UpdateOccupancyJourneyFactory.journeyMap) and does not use this step.
@JourneyFrameworkComponent
class UpdateOccupancyCyaConfig(
    private val occupancyDetailsHelper: OccupancyDetailsHelper,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val messageSource: MessageSource,
    private val propertyUpdateEmailService: PropertyUpdateEmailService,
) : AbstractCheckYourAnswersStepConfig<UpdateOccupancyJourneyState>() {
    override fun getStepSpecificContent(state: UpdateOccupancyJourneyState): Map<String, Any?> =
        mapOf(
            "title" to "propertyDetails.update.title",
            "showWarning" to true,
            "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
            "insetText" to true,
            "summaryListData" to occupancyDetailsHelper.getCheckYourAnswersSummaryList(state, messageSource),
            "summaryName" to
                if (isOccupied(state)) {
                    "forms.update.checkOccupancy.occupied.summaryName"
                } else {
                    "forms.update.checkOccupancy.notOccupied.summaryName"
                },
        )

    override fun afterStepDataIsAdded(state: UpdateOccupancyJourneyState) {
        val isOccupied = isOccupied(state)
        val billsIncludedDataModel = state.rentIncludesBillsTask.getBillsIncludedOrNull()
        try {
            propertyOwnershipService.updateOccupancy(
                id = state.propertyId,
                isOccupied = isOccupied,
                numberOfHouseholds =
                    if (isOccupied) {
                        state.householdsAndTenantsTask.households.formModel
                            .notNullValue(NumberOfHouseholdsFormModel::numberOfHouseholds)
                            .toInt()
                    } else {
                        0
                    },
                numberOfPeople =
                    if (isOccupied) {
                        state.householdsAndTenantsTask.tenants.formModel
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
                billsIncludedList = if (isOccupied) billsIncludedDataModel?.standardBillsIncludedListAsString else null,
                customBillsIncluded = if (isOccupied) billsIncludedDataModel?.customBillsIncluded else null,
                furnishedStatus = if (isOccupied) state.furnishedStatus.formModel.furnishedStatus else null,
                rentFrequency = if (isOccupied) state.rentFrequencyAndAmountTask.rentFrequency.formModel.rentFrequency else null,
                customRentFrequency = if (isOccupied) state.rentFrequencyAndAmountTask.getCustomRentFrequencyIfSelected() else null,
                rentAmount =
                    if (isOccupied) {
                        state.rentFrequencyAndAmountTask.rentAmount.formModel.rentAmount
                            .toBigDecimal()
                    } else {
                        null
                    },
                initialLastModifiedDate = Instant.parse(state.lastModifiedDate).toJavaInstant(),
            )
        } catch (ex: UpdateConflictException) {
            state.deleteJourney()
            throw ex
        }
        sendUpdateConfirmationEmail(state, isOccupied = isOccupied)
    }

    private fun sendUpdateConfirmationEmail(
        state: UpdateOccupancyJourneyState,
        isOccupied: Boolean,
    ) {
        val bullets =
            buildList {
                add("Whether the property is occupied by tenants")
                if (!state.propertyIsOccupied && isOccupied) {
                    add("The number of households living in this property")
                    add("The number of people living in this property")
                }
            }
        propertyUpdateEmailService.sendUpdateEmails(state.propertyId, bullets)
    }

    private fun isOccupied(state: UpdateOccupancyJourneyState) = state.occupied.formModel.notNullValue(OccupancyFormModel::occupied)
}

@JourneyFrameworkComponent
final class UpdateOccupancyCyaStep(
    stepConfig: UpdateOccupancyCyaConfig,
) : AbstractCheckYourAnswersStep<UpdateOccupancyJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "occupancy-check-your-answers"
    }
}
