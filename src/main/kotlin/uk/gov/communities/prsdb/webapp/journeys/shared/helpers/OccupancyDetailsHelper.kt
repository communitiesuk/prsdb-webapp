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
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.TenancyDetailsState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FurnishedStatusStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdMode
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RentFrequencyFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

@PrsdbWebService
class OccupancyDetailsHelper {
    fun <T> getOccupancySummaryList(state: T): List<SummaryListRowViewModel> where T : OccupationState, T : CheckYourAnswersJourneyState {
        val occupiedStep = state.occupied
        val isOccupied = occupiedStep.formModel.occupied ?: false
        return listOf(
            getOccupancyStatusRow(
                isOccupied,
                occupiedStep,
                state.getCyaJourneyId(occupiedStep),
                "forms.checkPropertyAnswers.occupancy.question",
            ),
        )
    }

    fun <T> getOccupancyStatusSummaryList(
        state: T,
    ): List<SummaryListRowViewModel> where T : OccupationState, T : CheckYourAnswersJourneyState {
        val isOccupied = state.occupied.formModel.occupied ?: false
        return listOf(getOccupancyStatusRow(isOccupied, state.occupied, state.getCyaJourneyId(state.occupied)))
    }

    fun <T> getCheckYourAnswersSummaryList(
        state: T,
        messageSource: MessageSource,
        provideLaterDestination: Destination? = null,
    ): List<SummaryListRowViewModel> where T : OccupationState, T : TenancyDetailsState, T : CheckYourAnswersJourneyState =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                val isOccupied = state.occupied.formModel.occupied ?: false
                if (isOccupied) {
                    addAll(getTenancyDetailsSummaryList(state, messageSource, provideLaterDestination))
                }
            }

    fun getCheckYourAnswersSummaryListWithoutProvideLater(
        state: CheckYourAnswersJourneyState,
        tenancyDetailsState: TenancyDetailsState,
        messageSource: MessageSource,
    ): List<SummaryListRowViewModel> =
        buildList {
            addAll(
                this@OccupancyDetailsHelper.getCheckYourHouseHoldsAndTenantsAnswersSummaryList(
                    state,
                    tenancyDetailsState.householdsAndTenantsTask,
                ),
            )
            addAll(
                this@OccupancyDetailsHelper.getCheckYourRentIncludesBillsAnswersSummaryList(
                    state,
                    tenancyDetailsState.rentIncludesBillsTask,
                    messageSource,
                ),
            )
            add(this@OccupancyDetailsHelper.getFurnishedStatusRow(state, tenancyDetailsState.furnishedStatus))
            addAll(
                this@OccupancyDetailsHelper.getCheckYourRentFrequencyAndAmountAnswersSummaryList(
                    state,
                    tenancyDetailsState.rentFrequencyAndAmountTask,
                    messageSource,
                ),
            )
        }

    fun <T> getCheckYourAnswersSummaryListWithoutProvideLater(
        state: T,
        messageSource: MessageSource,
    ): List<SummaryListRowViewModel> where T : TenancyDetailsState, T : CheckYourAnswersJourneyState =
        getCheckYourAnswersSummaryListWithoutProvideLater(state, state, messageSource)

    fun getTenancyDetailsCyaContent(
        state: CheckYourAnswersJourneyState,
        tenancyDetailsState: TenancyDetailsState,
        messageSource: MessageSource,
    ): Map<String, Any?> =
        mapOf("tenancyRows" to getCheckYourAnswersSummaryListWithoutProvideLater(state, tenancyDetailsState, messageSource))

    fun getCheckYourHouseHoldsAndTenantsAnswersSummaryList(
        state: CheckYourAnswersJourneyState,
        householdsAndTenantsState: HouseholdsAndTenantsState,
        provideLaterDestination: Destination? = null,
    ): List<SummaryListRowViewModel> =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                val householdsStep = householdsAndTenantsState.households
                if (householdsStep.outcome == HouseholdMode.PROVIDE_THIS_LATER) {
                    add(
                        SummaryListRowViewModel.forCheckYourAnswersPage(
                            "tenancyDetails.checkAnswers.tenancyDetailsRow",
                            "tenancyDetails.checkAnswers.provideLater",
                            provideLaterDestination
                                ?: Destination.VisitableStep(
                                    householdsStep,
                                    state.getCyaJourneyId(householdsStep),
                                ),
                        ),
                    )
                    return@apply
                }
                val tenantsStep = householdsAndTenantsState.tenants
                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "tenancyDetails.checkAnswers.households",
                        householdsStep.formModel.numberOfHouseholds,
                        Destination.VisitableStep(householdsStep, state.getCyaJourneyId(householdsStep)),
                    ),
                )
                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "tenancyDetails.checkAnswers.people",
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
                        "tenancyDetails.checkAnswers.rentIncludesBills",
                        rentIncludesBills,
                        Destination.VisitableStep(rentIncludesBillsStep, state.getCyaJourneyId(rentIncludesBillsStep)),
                    ),
                )
                if (rentIncludesBills) {
                    add(
                        SummaryListRowViewModel.forCheckYourAnswersPage(
                            "tenancyDetails.checkAnswers.billsIncluded",
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
        listOf(
            getRentFrequencyRow(state, rentFrequencyAndAmountState),
            getRentAmountRow(state, rentFrequencyAndAmountState, messageSource),
        )

    private fun getFurnishedStatusRow(
        state: CheckYourAnswersJourneyState,
        furnishedStatusStep: FurnishedStatusStep,
    ): SummaryListRowViewModel =
        SummaryListRowViewModel.forCheckYourAnswersPage(
            "tenancyDetails.checkAnswers.furnishedStatus",
            furnishedStatusStep.formModel.furnishedStatus,
            Destination.VisitableStep(furnishedStatusStep, state.getCyaJourneyId(furnishedStatusStep)),
        )

    private fun getRentFrequencyRow(
        state: CheckYourAnswersJourneyState,
        rentFrequencyAndAmountState: RentFrequencyAndAmountState,
    ): SummaryListRowViewModel {
        val rentFrequencyStep = rentFrequencyAndAmountState.rentFrequency
        val rentFrequency = rentFrequencyStep.formModel.notNullValue(RentFrequencyFormModel::rentFrequency)
        return SummaryListRowViewModel.forCheckYourAnswersPage(
            "tenancyDetails.checkAnswers.rentFrequency",
            RentDataHelper.getRentFrequency(rentFrequency, rentFrequencyStep.formModel.customRentFrequency),
            Destination.VisitableStep(rentFrequencyStep, state.getCyaJourneyId(rentFrequencyStep)),
        )
    }

    private fun getRentAmountRow(
        state: CheckYourAnswersJourneyState,
        rentFrequencyAndAmountState: RentFrequencyAndAmountState,
        messageSource: MessageSource,
    ): SummaryListRowViewModel {
        val rentAmountStep = rentFrequencyAndAmountState.rentAmount
        return SummaryListRowViewModel.forCheckYourAnswersPage(
            "tenancyDetails.checkAnswers.rentAmount",
            rentFrequencyAndAmountState.getRentAmount(messageSource),
            Destination.VisitableStep(rentAmountStep, state.getCyaJourneyId(rentAmountStep)),
        )
    }

    private fun getOccupancyStatusRow(
        isOccupied: Boolean,
        occupiedStep: RequestableStep<*, *, *>,
        childJourneyId: String,
        labelKey: String = "tenancyDetails.checkAnswers.occupied",
    ): SummaryListRowViewModel =
        SummaryListRowViewModel.forCheckYourAnswersPage(
            labelKey,
            isOccupied,
            Destination.VisitableStep(occupiedStep, childJourneyId),
        )

    private fun <T> getTenancyDetailsSummaryList(
        state: T,
        messageSource: MessageSource,
        provideLaterDestination: Destination? = null,
    ): List<SummaryListRowViewModel> where T : TenancyDetailsState, T : CheckYourAnswersJourneyState =
        if (state.provideTenancyDetailsLater) {
            getCheckYourHouseHoldsAndTenantsAnswersSummaryList(state, state.householdsAndTenantsTask, provideLaterDestination)
        } else {
            getCheckYourHouseHoldsAndTenantsAnswersSummaryList(state, state.householdsAndTenantsTask) +
                getFurnishedStatusRow(state, state.furnishedStatus) +
                getCheckYourRentIncludesBillsAnswersSummaryList(state, state.rentIncludesBillsTask, messageSource) +
                getCheckYourRentFrequencyAndAmountAnswersSummaryList(state, state.rentFrequencyAndAmountTask, messageSource)
        }
}
