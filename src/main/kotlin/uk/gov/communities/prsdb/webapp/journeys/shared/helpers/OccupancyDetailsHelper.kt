package uk.gov.communities.prsdb.webapp.journeys.shared.helpers

import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.helpers.RentDataHelper
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.OccupationState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RentFrequencyFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RentIncludesBillsFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

@PrsdbWebService
class OccupancyDetailsHelper {
    fun getCheckYourAnswersSummaryList(
        state: OccupationState,
        childJourneyId: String,
        messageSource: MessageSource,
    ): List<SummaryListRowViewModel> =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                val isOccupied = state.occupied.formModel.occupied ?: false
                add(getOccupancyStatusRow(isOccupied, state.occupied, childJourneyId))
                if (isOccupied) addAll(getOccupiedTenancyDetailsSummaryList(state, childJourneyId, messageSource))
            }

    private fun getOccupancyStatusRow(
        isOccupied: Boolean,
        occupiedStep: RequestableStep<*, *, *>,
        childJourneyId: String,
    ): SummaryListRowViewModel =
        SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkPropertyAnswers.tenancyDetails.occupied",
            isOccupied,
            Destination.VisitableStep(occupiedStep, childJourneyId),
        )

    private fun getOccupiedTenancyDetailsSummaryList(
        state: OccupationState,
        childJourneyId: String,
        messageSource: MessageSource,
    ) = mutableListOf<SummaryListRowViewModel>()
        .apply {
            val householdsStep = state.households
            val tenantsStep = state.tenants
            val bedroomsStep = state.bedrooms
            val rentIncludesBillsStep = state.rentIncludesBills
            val billsIncludedStep = state.billsIncluded
            val furnishedStatusStep = state.furnishedStatus
            val rentFrequencyStep = state.rentFrequency
            val rentAmountStep = state.rentAmount
            val rentIncludesBills = rentIncludesBillsStep.formModel.notNullValue(RentIncludesBillsFormModel::rentIncludesBills)
            val rentFrequency = rentFrequencyStep.formModel.notNullValue(RentFrequencyFormModel::rentFrequency)
            add(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkPropertyAnswers.tenancyDetails.households",
                    householdsStep.formModel.numberOfHouseholds,
                    Destination.VisitableStep(householdsStep, childJourneyId),
                ),
            )
            add(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkPropertyAnswers.tenancyDetails.people",
                    tenantsStep.formModel.numberOfPeople,
                    Destination.VisitableStep(tenantsStep, childJourneyId),
                ),
            )
            add(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkPropertyAnswers.tenancyDetails.bedrooms",
                    bedroomsStep.formModel.numberOfBedrooms,
                    Destination.VisitableStep(bedroomsStep, childJourneyId),
                ),
            )
            add(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkPropertyAnswers.tenancyDetails.rentIncludesBills",
                    rentIncludesBills,
                    Destination.VisitableStep(rentIncludesBillsStep, childJourneyId),
                ),
            )
            if (rentIncludesBills) {
                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkPropertyAnswers.tenancyDetails.billsIncluded",
                        state.getBillsIncluded(messageSource),
                        Destination.VisitableStep(billsIncludedStep, childJourneyId),
                    ),
                )
            }
            add(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkPropertyAnswers.tenancyDetails.furnishedStatus",
                    furnishedStatusStep.formModel.furnishedStatus,
                    Destination.VisitableStep(furnishedStatusStep, childJourneyId),
                ),
            )
            add(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkPropertyAnswers.tenancyDetails.rentFrequency",
                    RentDataHelper.getRentFrequency(rentFrequency, rentFrequencyStep.formModel.customRentFrequency),
                    Destination.VisitableStep(rentFrequencyStep, childJourneyId),
                ),
            )
            add(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkPropertyAnswers.tenancyDetails.rentAmount",
                    state.getRentAmount(messageSource),
                    Destination.VisitableStep(rentAmountStep, childJourneyId),
                ),
            )
        }
}
