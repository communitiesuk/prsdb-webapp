package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcAgeCheckMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcEnergyRatingCheckMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcInDateAtStartOfTenancyCheckMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcMode
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardActionViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider

class EpcRegistrationCyaSummaryRowsFactory(
    private val epcCertificateUrlProvider: EpcCertificateUrlProvider,
    private val state: EpcState,
) {
    fun createEpcCardTitle(): String? = if (!isEpcCardHidden()) "propertyCompliance.epcTask.checkEpcAnswers.epc.yourEpc" else null

    fun createEpcCardActions(): List<SummaryCardActionViewModel>? {
        if (isEpcCardHidden()) return null
        val epc = state.acceptedEpc!!
        val epcUrl = epcCertificateUrlProvider.getEpcCertificateUrl(epc.certificateNumber)
        val changeUrl = Destination(state.hasEpcStep).toUrlStringOrNull()
        return listOfNotNull(
            SummaryCardActionViewModel(
                "propertyCompliance.epcTask.checkEpcAnswers.epc.viewFullEpc",
                epcUrl,
                opensInNewTab = true,
            ),
            changeUrl?.let { SummaryCardActionViewModel("forms.links.change", it) },
        )
    }

    fun createEpcCardRows(): List<SummaryListRowViewModel>? {
        if (isEpcCardHidden()) return null
        val epc = state.acceptedEpc!!
        return listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "propertyCompliance.epcTask.checkEpcAnswers.epc.address",
                epc.singleLineAddress,
                null,
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "propertyCompliance.epcTask.checkEpcAnswers.epc.energyRating",
                epc.energyRatingUppercase,
                null,
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "propertyCompliance.epcTask.checkEpcAnswers.epc.expiryDate",
                epc.expiryDateAsJavaLocalDate,
                null,
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "propertyCompliance.epcTask.checkEpcAnswers.epc.certificateNumber",
                epc.certificateNumber,
                null,
            ),
        )
    }

    fun showEpcExpiredText(): Boolean = isEpcExpired() && !isEpcCardHidden()

    fun createTenancyCheckRows(): List<SummaryListRowViewModel> {
        val formModel = state.epcInDateAtStartOfTenancyCheckStep.formModelIfReachableOrNull ?: return emptyList()
        return listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "propertyCompliance.epcTask.checkEpcAnswers.epc.tenancyStartCheck",
                formModel.tenancyStartedBeforeExpiry,
                Destination(state.epcInDateAtStartOfTenancyCheckStep),
            ),
        )
    }

    fun showMeetsRequirementsInset(): Boolean {
        val energyRatingCheck = state.epcEnergyRatingCheckStep.outcome
        if (energyRatingCheck != EpcEnergyRatingCheckMode.EPC_MEETS_ENERGY_REQUIREMENTS) return false

        val ageCheck = state.epcAgeCheckStep.outcome
        val tenancyCheck = state.epcInDateAtStartOfTenancyCheckStep.outcome
        return ageCheck == EpcAgeCheckMode.EPC_10_YEARS_OR_NEWER ||
            tenancyCheck == EpcInDateAtStartOfTenancyCheckMode.IN_DATE
    }

    fun showLowRatingText(): Boolean = showMeesExemptionSection()

    fun createAdditionalRows(): List<SummaryListRowViewModel> {
        if (!showMeesExemptionSection()) return emptyList()
        return listOfNotNull(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "propertyCompliance.epcTask.checkEpcAnswers.epc.meesExemptionCheck",
                state.hasMeesExemptionStep.formModelIfReachableOrNull?.propertyHasExemption,
                Destination(state.hasMeesExemptionStep),
            ),
            if (state.meesExemptionStep.isStepReachable) {
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.epc.meesExemption",
                    state.meesExemptionStep.formModelIfReachableOrNull?.exemptionReason,
                    Destination(state.meesExemptionStep),
                )
            } else {
                null
            },
        )
    }

    fun showLowRatingOccupiedInset(): Boolean =
        state.isOccupied == true &&
            (
                state.lowEnergyRatingStep.isStepReachable ||
                    (isEpcExpired() && state.epcInDateAtStartOfTenancyCheckStep.outcome == EpcInDateAtStartOfTenancyCheckMode.NOT_IN_DATE)
            )

    fun createNonEpcRows(): List<SummaryListRowViewModel> {
        if (!isEpcCardHidden()) return emptyList()
        return listOfNotNull(
            getHasEpcRow(),
            if (state.isEpcRequiredStep.isStepReachable &&
                (state.isOccupied == true || state.isEpcRequiredStep.outcome == YesOrNo.NO)
            ) {
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.isEpcRequired",
                    state.isEpcRequiredStep.formModelIfReachableOrNull?.epcRequired,
                    Destination(state.isEpcRequiredStep),
                )
            } else {
                null
            },
            if (state.epcExemptionStep.isStepReachable) {
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.epcExemption",
                    state.epcExemptionStep.formModelIfReachableOrNull?.exemptionReason,
                    Destination(state.epcExemptionStep),
                )
            } else {
                null
            },
        )
    }

    fun showOccupiedNoEpcInset(): Boolean = state.isEpcRequiredStep.outcome == YesOrNo.YES && state.isOccupied == true

    private fun isEpcExpired(): Boolean = state.epcAgeCheckStep.outcome == EpcAgeCheckMode.EPC_OLDER_THAN_10_YEARS

    private fun isExpiredEpcUnoccupied(): Boolean = isEpcExpired() && state.isOccupied == false

    private fun isLowRatingNoExemptionUnoccupied(): Boolean =
        state.hasMeesExemptionStep.isStepReachable &&
            !state.meesExemptionStep.isStepReachable &&
            state.isOccupied == false

    private fun isEpcCardHiddenDueToUnoccupied(): Boolean = isExpiredEpcUnoccupied() || isLowRatingNoExemptionUnoccupied()

    private fun isEpcCardHidden(): Boolean =
        state.acceptedEpc == null ||
            state.hasEpcStep.outcome == HasEpcMode.PROVIDE_LATER ||
            state.hasEpcStep.outcome == HasEpcMode.NO_EPC ||
            isEpcCardHiddenDueToUnoccupied()

    private fun showMeesExemptionSection(): Boolean = state.hasMeesExemptionStep.isStepReachable && !isEpcCardHidden()

    private fun getHasEpcRow(): SummaryListRowViewModel {
        val fieldValue =
            when {
                state.hasEpcStep.outcome == HasEpcMode.PROVIDE_LATER && state.isOccupied == true ->
                    "propertyCompliance.epcTask.checkEpcAnswers.hasEpc.provideEpcLaterOccupied"

                state.hasEpcStep.outcome == HasEpcMode.PROVIDE_LATER ||
                    isEpcCardHiddenDueToUnoccupied() ||
                    (
                        state.hasEpcStep.outcome == HasEpcMode.NO_EPC &&
                            state.isOccupied == false &&
                            state.isEpcRequiredStep.outcome == YesOrNo.YES
                    ) ->
                    "propertyCompliance.epcTask.checkEpcAnswers.hasEpc.provideEpcLaterUnoccupied"

                else ->
                    "commonText.no"
            }
        return SummaryListRowViewModel.forCheckYourAnswersPage(
            "propertyCompliance.epcTask.checkEpcAnswers.hasEpc.label",
            fieldValue,
            Destination(state.hasEpcStep),
        )
    }
}
