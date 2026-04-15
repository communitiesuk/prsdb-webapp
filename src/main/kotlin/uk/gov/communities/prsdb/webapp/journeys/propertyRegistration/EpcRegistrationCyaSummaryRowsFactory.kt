package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcScenario
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardActionViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider

class EpcRegistrationCyaSummaryRowsFactory(
    private val epcCertificateUrlProvider: EpcCertificateUrlProvider,
    private val state: EpcState,
    private val scenario: EpcScenario,
) {
    fun createEpcCardTitle(): String? = if (isEpcCardShown()) "propertyCompliance.epcTask.checkEpcAnswers.epc.yourEpc" else null

    fun createEpcCardActions(): List<SummaryCardActionViewModel>? {
        if (!isEpcCardShown()) return null
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
        if (!isEpcCardShown()) return null
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

    fun getEpcExpiredTextKey(): String? = if (epcIsExpired()) "propertyCompliance.epcTask.checkEpcAnswers.epc.expired" else null

    fun createTenancyCheckRows(): List<SummaryListRowViewModel> {
        if (!epcIsExpired()) return emptyList()
        return listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "propertyCompliance.epcTask.checkEpcAnswers.epc.tenancyStartCheck",
                state.epcInDateAtStartOfTenancyCheckStep.formModelIfReachableOrNull?.tenancyStartedBeforeExpiry,
                Destination(state.epcInDateAtStartOfTenancyCheckStep),
            ),
        )
    }

    fun getLowRatingTextKey(): String? = if (epcIsLowRating()) "propertyCompliance.epcTask.checkEpcAnswers.epc.lowRating" else null

    fun createAdditionalRows(): List<SummaryListRowViewModel> {
        if (!epcIsLowRating()) return emptyList()
        val hasMeesExemptionRow =
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "propertyCompliance.epcTask.checkEpcAnswers.epc.meesExemptionCheck",
                state.hasMeesExemptionStep.formModelIfReachableOrNull?.propertyHasExemption,
                Destination(state.hasMeesExemptionStep),
            )
        if (!epcHasMeesExemption()) return listOf(hasMeesExemptionRow)
        val meesExemptionRow =
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "propertyCompliance.epcTask.checkEpcAnswers.epc.meesExemption",
                state.meesExemptionStep.formModelIfReachableOrNull?.exemptionReason,
                Destination(state.meesExemptionStep),
            )
        return listOf(hasMeesExemptionRow, meesExemptionRow)
    }

    fun createNonEpcRows(): List<SummaryListRowViewModel> =
        if (isEpcCardShown()) {
            emptyList()
        } else {
            listOfNotNull(getHasEpcRow(), createIsEpcRequiredRow(), createEpcExemptionRow())
        }

    fun getInsetTextKey(): String? =
        when (scenario) {
            EpcScenario.VALID_EPC,
            EpcScenario.EPC_EXPIRED_IN_DATE_OCCUPIED,
            -> "propertyCompliance.epcTask.checkEpcAnswers.epc.meetsRequirements"
            EpcScenario.LOW_ENERGY_EPC_NO_EXEMPTION_OCCUPIED,
            EpcScenario.EPC_EXPIRED_NOT_IN_DATE_OCCUPIED,
            EpcScenario.LOW_ENERGY_EPC_EXPIRED_IN_DATE_NO_EXEMPTION_OCCUPIED,
            -> "propertyCompliance.epcTask.checkEpcAnswers.epc.lowRatingOccupiedInset"
            EpcScenario.NO_EPC_NO_EXEMPTION_OCCUPIED -> "propertyCompliance.epcTask.checkEpcAnswers.occupiedNoEpcInset"
            else -> null
        }

    private fun isEpcCardShown(): Boolean =
        when (scenario) {
            EpcScenario.VALID_EPC,
            EpcScenario.LOW_ENERGY_EPC_MEES_EXEMPTION,
            EpcScenario.LOW_ENERGY_EPC_NO_EXEMPTION_OCCUPIED,
            EpcScenario.EPC_EXPIRED_NOT_IN_DATE_OCCUPIED,
            EpcScenario.EPC_EXPIRED_IN_DATE_OCCUPIED,
            EpcScenario.LOW_ENERGY_EPC_EXPIRED_IN_DATE_MEES_EXEMPTION_OCCUPIED,
            EpcScenario.LOW_ENERGY_EPC_EXPIRED_IN_DATE_NO_EXEMPTION_OCCUPIED,
            -> true
            else -> false
        }

    private fun epcIsExpired(): Boolean =
        when (scenario) {
            EpcScenario.EPC_EXPIRED_NOT_IN_DATE_OCCUPIED,
            EpcScenario.EPC_EXPIRED_IN_DATE_OCCUPIED,
            EpcScenario.LOW_ENERGY_EPC_EXPIRED_IN_DATE_MEES_EXEMPTION_OCCUPIED,
            EpcScenario.LOW_ENERGY_EPC_EXPIRED_IN_DATE_NO_EXEMPTION_OCCUPIED,
            -> true
            else -> false
        }

    private fun epcIsLowRating(): Boolean =
        when (scenario) {
            EpcScenario.LOW_ENERGY_EPC_MEES_EXEMPTION,
            EpcScenario.LOW_ENERGY_EPC_NO_EXEMPTION_OCCUPIED,
            EpcScenario.LOW_ENERGY_EPC_EXPIRED_IN_DATE_MEES_EXEMPTION_OCCUPIED,
            EpcScenario.LOW_ENERGY_EPC_EXPIRED_IN_DATE_NO_EXEMPTION_OCCUPIED,
            -> true
            else -> false
        }

    private fun epcHasMeesExemption(): Boolean =
        when (scenario) {
            EpcScenario.LOW_ENERGY_EPC_MEES_EXEMPTION,
            EpcScenario.LOW_ENERGY_EPC_EXPIRED_IN_DATE_MEES_EXEMPTION_OCCUPIED,
            -> true
            else -> false
        }

    private fun getHasEpcRow(): SummaryListRowViewModel {
        val fieldValue =
            when (scenario) {
                EpcScenario.SKIPPED_OCCUPIED -> "propertyCompliance.epcTask.checkEpcAnswers.hasEpc.provideEpcLaterOccupied"
                EpcScenario.SKIPPED_UNOCCUPIED,
                EpcScenario.EPC_EXPIRED_UNOCCUPIED,
                EpcScenario.LOW_ENERGY_EPC_NO_EXEMPTION_UNOCCUPIED,
                EpcScenario.NO_EPC_NO_EXEMPTION_UNOCCUPIED,
                -> "propertyCompliance.epcTask.checkEpcAnswers.hasEpc.provideEpcLaterUnoccupied"
                else -> "commonText.no"
            }
        return SummaryListRowViewModel.forCheckYourAnswersPage(
            "propertyCompliance.epcTask.checkEpcAnswers.hasEpc.label",
            fieldValue,
            Destination(state.hasEpcStep),
        )
    }

    private fun createIsEpcRequiredRow(): SummaryListRowViewModel? =
        when (scenario) {
            EpcScenario.NO_EPC_EXEMPT,
            EpcScenario.NO_EPC_NO_EXEMPTION_OCCUPIED,
            ->
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.isEpcRequired",
                    state.isEpcRequiredStep.formModelIfReachableOrNull?.epcRequired,
                    Destination(state.isEpcRequiredStep),
                )
            else -> null
        }

    private fun createEpcExemptionRow(): SummaryListRowViewModel? =
        when (scenario) {
            EpcScenario.NO_EPC_EXEMPT ->
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.epcExemption",
                    state.epcExemptionStep.formModelIfReachableOrNull?.exemptionReason,
                    Destination(state.epcExemptionStep),
                )
            else -> null
        }
}
