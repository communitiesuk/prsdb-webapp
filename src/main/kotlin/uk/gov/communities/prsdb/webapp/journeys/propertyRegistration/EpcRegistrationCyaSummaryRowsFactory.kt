package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcAgeAndEnergyRatingCheckMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcInDateAtStartOfTenancyCheckMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcMode
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardActionViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class EpcRegistrationCyaSummaryRowsFactory(
    private val epcCertificateUrlProvider: EpcCertificateUrlProvider,
    private val state: EpcState,
) {
    // --- EPC Summary Card (shown when user has an accepted EPC) ---

    fun createEpcCardTitle(): String? = if (state.acceptedEpc != null) "propertyCompliance.epcTask.checkEpcAnswers.epc.yourEpc" else null

    fun createEpcCardActions(): List<SummaryCardActionViewModel>? {
        val epc = state.acceptedEpc ?: return null
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
        val epc = state.acceptedEpc ?: return null
        return listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "propertyCompliance.epcTask.checkEpcAnswers.epc.address",
                epc.singleLineAddress,
                null as String?,
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "propertyCompliance.epcTask.checkEpcAnswers.epc.energyRating",
                epc.energyRatingUppercase,
                null as String?,
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "propertyCompliance.epcTask.checkEpcAnswers.epc.expiryDate",
                epc.expiryDateAsJavaLocalDate,
                null as String?,
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "propertyCompliance.epcTask.checkEpcAnswers.epc.certificateNumber",
                epc.certificateNumber,
                null as String?,
            ),
        )
    }

    // --- Status texts and rows below the EPC card ---

    fun showEpcExpiredText(): Boolean =
        state.epcAgeAndEnergyRatingCheckStep.outcome == EpcAgeAndEnergyRatingCheckMode.EPC_OLDER_THAN_10_YEARS

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
        val ageCheck = state.epcAgeAndEnergyRatingCheckStep.outcome
        val tenancyCheck = state.epcInDateAtStartOfTenancyCheckStep.outcome
        return ageCheck == EpcAgeAndEnergyRatingCheckMode.EPC_COMPLIANT ||
            tenancyCheck == EpcInDateAtStartOfTenancyCheckMode.IN_DATE
    }

    fun showLowRatingText(): Boolean = state.hasMeesExemptionStep.isStepReachable

    fun createAdditionalRows(): List<SummaryListRowViewModel> {
        if (!state.hasMeesExemptionStep.isStepReachable) return emptyList()
        return buildList {
            add(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.epc.meesExemptionCheck",
                    state.hasMeesExemptionStep.formModelIfReachableOrNull?.propertyHasExemption,
                    Destination(state.hasMeesExemptionStep),
                ),
            )
            if (state.meesExemptionStep.isStepReachable) {
                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "propertyCompliance.epcTask.checkEpcAnswers.epc.meesExemption",
                        state.meesExemptionStep.formModelIfReachableOrNull?.exemptionReason,
                        Destination(state.meesExemptionStep),
                    ),
                )
            }
        }
    }

    fun showLowRatingOccupiedInset(): Boolean = state.lowEnergyRatingStep.isStepReachable && state.isOccupied == true

    // --- Non-EPC rows (shown when no accepted EPC) ---

    fun createNonEpcRows(): List<SummaryListRowViewModel> {
        if (state.acceptedEpc != null) return emptyList()
        return buildList {
            add(getHasEpcRow())
            if (state.isEpcRequiredStep.isStepReachable) {
                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "propertyCompliance.epcTask.checkEpcAnswers.isEpcRequired",
                        state.isEpcRequiredStep.formModelIfReachableOrNull?.epcRequired,
                        Destination(state.isEpcRequiredStep),
                    ),
                )
            }
            if (state.epcExemptionStep.isStepReachable) {
                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "propertyCompliance.epcTask.checkEpcAnswers.epcExemption",
                        state.epcExemptionStep.formModelIfReachableOrNull?.exemptionReason,
                        Destination(state.epcExemptionStep),
                    ),
                )
            }
        }
    }

    fun showOccupiedNoEpcInset(): Boolean = state.isEpcRequiredStep.outcome == YesOrNo.YES && state.isOccupied == true

    private fun getHasEpcRow(): SummaryListRowViewModel {
        val fieldValue =
            when {
                state.hasEpcStep.outcome == HasEpcMode.PROVIDE_LATER && state.isOccupied == true -> {
                    val deadline =
                        java.time.LocalDate
                            .now(ZoneId.of("Europe/London"))
                            .plusDays(28)
                            .format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH))
                    "Provide this later (before $deadline)"
                }

                state.hasEpcStep.outcome == HasEpcMode.PROVIDE_LATER -> {
                    "propertyCompliance.epcTask.checkEpcAnswers.hasEpc.provideEpcLaterUnoccupied"
                }

                else -> {
                    "commonText.no"
                }
            }
        return SummaryListRowViewModel.forCheckYourAnswersPage(
            "propertyCompliance.epcTask.checkEpcAnswers.hasEpc.label",
            fieldValue,
            Destination(state.hasEpcStep),
        )
    }
}
