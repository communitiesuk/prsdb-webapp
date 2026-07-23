package uk.gov.communities.prsdb.webapp.journeys.shared.helpers

import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.helpers.RentDataHelper
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.HouseholdsAndTenantsState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.OccupationState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.RentFrequencyAndAmountState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.RentIncludesBillsState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdMode
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

    fun <T> getCheckYourHouseHoldsAndTenantsAnswersSummaryList(
        state: T,
    ): List<SummaryListRowViewModel> where T : HouseholdsAndTenantsState, T : CheckYourAnswersJourneyState =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                val householdsStep = state.households
                if (householdsStep.outcome == HouseholdMode.PROVIDE_THIS_LATER) {
                    add(
                        SummaryListRowViewModel.forCheckYourAnswersPage(
                            "forms.checkPropertyAnswers.tenancyDetails.households",
                            "forms.checkPropertyAnswers.tenancyDetails.provideLater",
                            Destination.VisitableStep(
                                householdsStep,
                                state.getCyaJourneyId(householdsStep),
                            ),
                        ),
                    )
                    return@apply
                }
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

    fun <T> getCheckYourRentFrequencyAndAmountAnswersSummaryList(
        state: T,
        messageSource: MessageSource,
    ): List<SummaryListRowViewModel> where T : RentFrequencyAndAmountState, T : CheckYourAnswersJourneyState =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                val rentFrequencyStep = state.rentFrequency
                val rentAmountStep = state.rentAmount
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
                        state.getRentAmount(messageSource),
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
        if (state.households.outcome == HouseholdMode.PROVIDE_THIS_LATER) {
            getCheckYourHouseHoldsAndTenantsAnswersSummaryList(state)
        } else {
            getCheckYourHouseHoldsAndTenantsAnswersSummaryList(state) +
                getBedroomsRow(state) +
                getRentBillsAndFurnishingsSummaryList(state, messageSource)
        }

    private fun <T> getRestructuredOccupiedTenancyDetailsSummaryList(
        state: T,
        messageSource: MessageSource,
    ): List<SummaryListRowViewModel> where T : OccupationState, T : CheckYourAnswersJourneyState =
        if (state.households.outcome == HouseholdMode.PROVIDE_THIS_LATER) {
            getCheckYourHouseHoldsAndTenantsAnswersSummaryList(state)
        } else {
            getCheckYourHouseHoldsAndTenantsAnswersSummaryList(state) +
                getRentBillsAndFurnishingsSummaryList(state, messageSource)
        }

    private fun <T> getBedroomsRow(state: T): SummaryListRowViewModel where T : OccupationState, T : CheckYourAnswersJourneyState {
        val bedroomsStep = state.bedrooms
        return SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkPropertyAnswers.tenancyDetails.bedrooms",
            bedroomsStep.formModel.numberOfBedrooms,
            Destination.VisitableStep(bedroomsStep, state.getCyaJourneyId(bedroomsStep)),
        )
    }

    private fun <T> getRentBillsAndFurnishingsSummaryList(
        state: T,
        messageSource: MessageSource,
    ): List<SummaryListRowViewModel> where T : OccupationState, T : CheckYourAnswersJourneyState =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                val furnishedStatusStep = state.furnishedStatus
                addAll(getCheckYourRentIncludesBillsAnswersSummaryList(state, messageSource))
                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkPropertyAnswers.tenancyDetails.furnishedStatus",
                        furnishedStatusStep.formModel.furnishedStatus,
                        Destination.VisitableStep(furnishedStatusStep, state.getCyaJourneyId(furnishedStatusStep)),
                    ),
                )
                addAll(getCheckYourRentFrequencyAndAmountAnswersSummaryList(state, messageSource))
            }
}
