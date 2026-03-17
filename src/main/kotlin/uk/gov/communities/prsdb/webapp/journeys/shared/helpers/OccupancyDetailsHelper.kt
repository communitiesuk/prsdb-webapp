package uk.gov.communities.prsdb.webapp.journeys.shared.helpers

import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.helpers.RentDataHelper
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.HouseholdsAndTenantsState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.OccupationState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.RentIncludesBillsState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RentFrequencyFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

@PrsdbWebService
class OccupancyDetailsHelper {
    fun <T> getCheckYourAnswersSummaryList(
        state: T,
        messageSource: MessageSource,
    ): List<SummaryListRowViewModel> where T : OccupationState, T : CheckYourAnswersJourneyState =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                val isOccupied = state.occupied.formModel.occupied ?: false
                add(getOccupancyStatusRow(isOccupied, state.occupied, state.getCyaJourneyId(state.occupied)))
                if (isOccupied) addAll(getOccupiedTenancyDetailsSummaryList(state, messageSource))
            }

    fun <T> getCheckYourHouseHoldsAndTenantsAnswersSummaryList(
        state: T,
    ): List<SummaryListRowViewModel> where T : HouseholdsAndTenantsState, T : CheckYourAnswersJourneyState =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                val householdsStep = state.households
                val tenantsStep = state.tenants
                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkPropertyAnswers.tenancyDetails.households",
                        householdsStep.formModel.numberOfHouseholds,
                        Destination.VisitableStep(householdsStep, state.getCyaJourneyId(householdsStep)),
                    ),
                )
                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkPropertyAnswers.tenancyDetails.people",
                        tenantsStep.formModel.numberOfPeople,
                        Destination.VisitableStep(tenantsStep, state.getCyaJourneyId(tenantsStep)),
                    ),
                )
            }

    fun <T> getCheckYourRentIncludesBillsAnswersSummaryList(
        state: T,
        messageSource: MessageSource,
    ): List<SummaryListRowViewModel> where T : RentIncludesBillsState, T : CheckYourAnswersJourneyState =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                val rentIncludesBillsStep = state.rentIncludesBills
                val billsIncludedStep = state.billsIncluded
                val rentIncludesBills = state.doesRentIncludeBills()
                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkPropertyAnswers.tenancyDetails.rentIncludesBills",
                        rentIncludesBills,
                        Destination.VisitableStep(rentIncludesBillsStep, state.getCyaJourneyId(rentIncludesBillsStep)),
                    ),
                )
                if (rentIncludesBills) {
                    add(
                        SummaryListRowViewModel.forCheckYourAnswersPage(
                            "forms.checkPropertyAnswers.tenancyDetails.billsIncluded",
                            state.getBillsIncluded(messageSource),
                            Destination.VisitableStep(billsIncludedStep, state.getCyaJourneyId(billsIncludedStep)),
                        ),
                    )
                }
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

    private fun <T> getOccupiedTenancyDetailsSummaryList(
        state: T,
        messageSource: MessageSource,
    ): List<SummaryListRowViewModel> where T : OccupationState, T : CheckYourAnswersJourneyState =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                val bedroomsStep = state.bedrooms
                val furnishedStatusStep = state.furnishedStatus
                val rentFrequencyStep = state.rentFrequency
                val rentAmountStep = state.rentAmount
                val rentFrequency = rentFrequencyStep.formModel.notNullValue(RentFrequencyFormModel::rentFrequency)
                addAll(getCheckYourHouseHoldsAndTenantsAnswersSummaryList(state))
                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkPropertyAnswers.tenancyDetails.bedrooms",
                        bedroomsStep.formModel.numberOfBedrooms,
                        Destination.VisitableStep(bedroomsStep, state.getCyaJourneyId(bedroomsStep)),
                    ),
                )
                addAll(getCheckYourRentIncludesBillsAnswersSummaryList(state, messageSource))
                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkPropertyAnswers.tenancyDetails.furnishedStatus",
                        furnishedStatusStep.formModel.furnishedStatus,
                        Destination.VisitableStep(furnishedStatusStep, state.getCyaJourneyId(furnishedStatusStep)),
                    ),
                )
                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkPropertyAnswers.tenancyDetails.rentFrequency",
                        RentDataHelper.getRentFrequency(rentFrequency, rentFrequencyStep.formModel.customRentFrequency),
                        Destination.VisitableStep(rentFrequencyStep, state.getCyaJourneyId(rentFrequencyStep)),
                    ),
                )
                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkPropertyAnswers.tenancyDetails.rentAmount",
                        state.getRentAmount(messageSource),
                        Destination.VisitableStep(rentAmountStep, state.getCyaJourneyId(rentAmountStep)),
                    ),
                )
            }
}
