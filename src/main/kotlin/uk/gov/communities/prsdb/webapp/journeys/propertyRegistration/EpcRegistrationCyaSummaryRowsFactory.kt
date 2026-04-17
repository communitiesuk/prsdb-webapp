package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcAgeCheckMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcEnergyRatingCheckMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcInDateAtStartOfTenancyCheckMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcScenario
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcMode
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardActionViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider

class EpcRegistrationCyaSummaryRowsFactory(
    private val epcCertificateUrlProvider: EpcCertificateUrlProvider,
    private val state: EpcState,
) {
    private val scenario: EpcScenario = determineScenario(state)

    private fun determineScenario(state: EpcState): EpcScenario {
        val isOccupied = state.isOccupied == true
        return when {
            state.hasEpcStep.outcome == HasEpcMode.PROVIDE_LATER -> {
                if (isOccupied) EpcScenario.SKIPPED_OCCUPIED else EpcScenario.SKIPPED_UNOCCUPIED
            }

            state.acceptedEpc == null -> {
                determineNoEpcScenario(state, isOccupied)
            }

            else -> {
                determineEpcPresentScenario(state, isOccupied)
            }
        }
    }

    private fun determineNoEpcScenario(
        state: EpcState,
        isOccupied: Boolean,
    ): EpcScenario =
        when {
            state.epcExemptionStep.outcome == Complete.COMPLETE -> EpcScenario.NO_EPC_EXEMPT
            isOccupied -> EpcScenario.NO_EPC_NO_EXEMPTION_OCCUPIED
            else -> EpcScenario.NO_EPC_NO_EXEMPTION_UNOCCUPIED
        }

    private fun determineEpcPresentScenario(
        state: EpcState,
        isOccupied: Boolean,
    ): EpcScenario {
        val isExpired = state.epcAgeCheckStep.outcome == EpcAgeCheckMode.EPC_OLDER_THAN_10_YEARS
        return if (!isExpired) {
            determineNotExpiredEpcScenario(state, isOccupied)
        } else {
            determineExpiredEpcScenario(state, isOccupied)
        }
    }

    private fun determineNotExpiredEpcScenario(
        state: EpcState,
        isOccupied: Boolean,
    ): EpcScenario {
        if (state.epcEnergyRatingCheckStep.outcome != EpcEnergyRatingCheckMode.EPC_LOW_ENERGY_RATING) {
            return EpcScenario.VALID_EPC
        }
        return when {
            state.meesExemptionStep.outcome == Complete.COMPLETE -> EpcScenario.LOW_ENERGY_EPC_MEES_EXEMPTION
            isOccupied -> EpcScenario.LOW_ENERGY_EPC_NO_EXEMPTION_OCCUPIED
            else -> EpcScenario.LOW_ENERGY_EPC_NO_EXEMPTION_UNOCCUPIED
        }
    }

    private fun determineExpiredEpcScenario(
        state: EpcState,
        isOccupied: Boolean,
    ): EpcScenario {
        if (!isOccupied) return EpcScenario.EPC_EXPIRED_UNOCCUPIED
        return when (state.epcInDateAtStartOfTenancyCheckStep.outcome) {
            EpcInDateAtStartOfTenancyCheckMode.NOT_IN_DATE -> {
                EpcScenario.EPC_EXPIRED_NOT_IN_DATE_OCCUPIED
            }

            EpcInDateAtStartOfTenancyCheckMode.IN_DATE -> {
                if (state.epcEnergyRatingCheckStep.outcome != EpcEnergyRatingCheckMode.EPC_LOW_ENERGY_RATING) {
                    EpcScenario.EPC_EXPIRED_IN_DATE_OCCUPIED
                } else if (state.meesExemptionStep.outcome == Complete.COMPLETE) {
                    EpcScenario.LOW_ENERGY_EPC_EXPIRED_IN_DATE_MEES_EXEMPTION_OCCUPIED
                } else {
                    EpcScenario.LOW_ENERGY_EPC_EXPIRED_IN_DATE_NO_EXEMPTION_OCCUPIED
                }
            }

            null -> {
                throw IllegalStateException(
                    "CheckEpcAnswersStep is not reachable for an occupied property " +
                        "with an expired EPC before the tenancy check is answered",
                )
            }
        }
    }

    fun createEpcCardTitle(): String? = if (isEpcCardShown()) "propertyCompliance.epcTask.checkEpcAnswers.epc.yourEpc" else null

    fun createEpcCardActions(): List<SummaryCardActionViewModel>? {
        if (!isEpcCardShown()) return null
        val epc = state.acceptedEpc ?: throw IllegalStateException("An EPC should be present when showing EPC card")
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
        val epc = state.acceptedEpc ?: throw IllegalStateException("An EPC should be present when showing EPC card")
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

    fun createExemptionReasonRows(): List<SummaryListRowViewModel> {
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
            -> {
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.isEpcRequired",
                    state.isEpcRequiredStep.formModelIfReachableOrNull?.epcRequired,
                    Destination(state.isEpcRequiredStep),
                )
            }

            else -> {
                null
            }
        }

    private fun createEpcExemptionRow(): SummaryListRowViewModel? =
        when (scenario) {
            EpcScenario.NO_EPC_EXEMPT -> {
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.epcExemption",
                    state.epcExemptionStep.formModelIfReachableOrNull?.exemptionReason,
                    Destination(state.epcExemptionStep),
                )
            }

            else -> {
                null
            }
        }
}
