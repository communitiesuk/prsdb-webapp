package uk.gov.communities.prsdb.webapp.journeys.shared.helpers

import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.helpers.RentDataHelper
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.BedroomsState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FurnishedStatusState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.HouseholdsAndTenantsState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.OccupationState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.RentFrequencyAndAmountState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.RentIncludesBillsState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.TenancyDetailsState
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

    fun <T> getRestructuredCheckYourAnswersSummaryList(
        state: T,
        messageSource: MessageSource,
    ): List<SummaryListRowViewModel> where T : OccupationState, T : CheckYourAnswersJourneyState =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                val isOccupied = state.occupied.formModel.occupied ?: false
                add(getOccupancyStatusRow(isOccupied, state.occupied, state.getCyaJourneyId(state.occupied)))
                if (isOccupied) addAll(getRestructuredOccupiedTenancyDetailsSummaryList(state, messageSource))
            }

    fun <T> getCheckYourTenancyDetailsAnswersSummaryList(
        state: T,
        messageSource: MessageSource,
    ): List<SummaryListRowViewModel> where T : TenancyDetailsState, T : BedroomsState, T : CheckYourAnswersJourneyState =
        getCheckYourHouseHoldsAndTenantsAnswersSummaryList(state, state.householdsAndTenantsTask) +
            getBedroomsRow(state, state) +
            getRentBillsAndFurnishingsSummaryList(
                state,
                state,
                state.rentIncludesBillsTask,
                state.rentFrequencyAndAmountTask,
                messageSource,
            )

    fun getCheckYourHouseHoldsAndTenantsAnswersSummaryList(
        state: CheckYourAnswersJourneyState,
        householdsAndTenantsState: HouseholdsAndTenantsState,
    ): List<SummaryListRowViewModel> =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                val householdsStep = householdsAndTenantsState.households
                val tenantsStep = householdsAndTenantsState.tenants
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

    fun getCheckYourRentIncludesBillsAnswersSummaryList(
        state: CheckYourAnswersJourneyState,
        rentIncludesBillsState: RentIncludesBillsState,
        messageSource: MessageSource,
    ): List<SummaryListRowViewModel> =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                val rentIncludesBillsStep = rentIncludesBillsState.rentIncludesBills
                val billsIncludedStep = rentIncludesBillsState.billsIncluded
                val rentIncludesBills = rentIncludesBillsState.doesRentIncludeBills()
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
                            rentIncludesBillsState.getBillsIncluded(messageSource),
                            Destination.VisitableStep(billsIncludedStep, state.getCyaJourneyId(billsIncludedStep)),
                        ),
                    )
                }
            }

    fun getCheckYourRentFrequencyAndAmountAnswersSummaryList(
        state: CheckYourAnswersJourneyState,
        rentFrequencyAndAmountState: RentFrequencyAndAmountState,
        messageSource: MessageSource,
    ): List<SummaryListRowViewModel> =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                val rentFrequencyStep = rentFrequencyAndAmountState.rentFrequency
                val rentAmountStep = rentFrequencyAndAmountState.rentAmount
                val rentFrequency = rentFrequencyStep.formModel.notNullValue(RentFrequencyFormModel::rentFrequency)
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
                        rentFrequencyAndAmountState.getRentAmount(messageSource),
                        Destination.VisitableStep(rentAmountStep, state.getCyaJourneyId(rentAmountStep)),
                    ),
                )
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
        getCheckYourHouseHoldsAndTenantsAnswersSummaryList(state, state.householdsAndTenantsTask) +
            getBedroomsRow(state, state) +
            getRentBillsAndFurnishingsSummaryList(
                state,
                state,
                state.rentIncludesBillsTask,
                state.rentFrequencyAndAmountTask,
                messageSource,
            )

    private fun <T> getRestructuredOccupiedTenancyDetailsSummaryList(
        state: T,
        messageSource: MessageSource,
    ): List<SummaryListRowViewModel> where T : OccupationState, T : CheckYourAnswersJourneyState =
        getCheckYourHouseHoldsAndTenantsAnswersSummaryList(state, state.householdsAndTenantsTask) +
            getRentBillsAndFurnishingsSummaryList(
                state,
                state,
                state.rentIncludesBillsTask,
                state.rentFrequencyAndAmountTask,
                messageSource,
            )

    private fun getBedroomsRow(
        state: CheckYourAnswersJourneyState,
        bedroomsState: BedroomsState,
    ): SummaryListRowViewModel {
        val bedroomsStep = bedroomsState.bedrooms
        return SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkPropertyAnswers.tenancyDetails.bedrooms",
            bedroomsStep.formModel.numberOfBedrooms,
            Destination.VisitableStep(bedroomsStep, state.getCyaJourneyId(bedroomsStep)),
        )
    }

    private fun getRentBillsAndFurnishingsSummaryList(
        state: CheckYourAnswersJourneyState,
        furnishedStatusState: FurnishedStatusState,
        rentIncludesBillsState: RentIncludesBillsState,
        rentFrequencyAndAmountState: RentFrequencyAndAmountState,
        messageSource: MessageSource,
    ): List<SummaryListRowViewModel> =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                val furnishedStatusStep = furnishedStatusState.furnishedStatus
                addAll(getCheckYourRentIncludesBillsAnswersSummaryList(state, rentIncludesBillsState, messageSource))
                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkPropertyAnswers.tenancyDetails.furnishedStatus",
                        furnishedStatusStep.formModel.furnishedStatus,
                        Destination.VisitableStep(furnishedStatusStep, state.getCyaJourneyId(furnishedStatusStep)),
                    ),
                )
                addAll(getCheckYourRentFrequencyAndAmountAnswersSummaryList(state, rentFrequencyAndAmountState, messageSource))
            }
}
