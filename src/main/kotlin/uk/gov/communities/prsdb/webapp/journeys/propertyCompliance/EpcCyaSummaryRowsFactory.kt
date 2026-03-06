package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance

import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider

class EpcCyaSummaryRowsFactory(
    private val epcStartingStep: Destination.VisitableStep,
    private val epcCertificateUrlProvider: EpcCertificateUrlProvider,
    private val state: EpcState,
    private val childJourneyId: String,
) {
    fun createRows() =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                add(getEpcStatusRow())
                if (state.acceptedEpc != null) {
                    addAll(getEpcDetailRows())
                } else {
                    add(getEpcExemptionRow())
                }
            }.toList()

    private fun getEpcStatusRow(): SummaryListRowViewModel {
        val fieldValue =
            if (state.epcExemptionConfirmationStep.outcome == Complete.COMPLETE) {
                "forms.checkComplianceAnswers.certificate.notRequired"
            } else if (state.epcExpiredStep.outcome == Complete.COMPLETE) {
                "forms.checkComplianceAnswers.epc.viewExpired"
            } else if (state.epcMissingStep.outcome == Complete.COMPLETE || state.epcNotFoundStep.outcome == Complete.COMPLETE) {
                "forms.checkComplianceAnswers.certificate.notAdded"
            } else {
                "forms.checkComplianceAnswers.epc.view"
            }

        val certificateNumber = state.acceptedEpc?.certificateNumber
        val valueUrl =
            if (certificateNumber != null) {
                epcCertificateUrlProvider.getEpcCertificateUrl(certificateNumber)
            } else {
                null
            }

        return SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkComplianceAnswers.epc.certificate",
            fieldValue,
            epcStartingStep,
            valueUrl,
            valueUrlOpensNewTab = valueUrl != null,
        )
    }

    private fun getEpcDetailRows() =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                val epcDetails = state.getNotNullAcceptedEpc()
                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.epc.expiryDate",
                        epcDetails.expiryDate,
                        null,
                    ),
                )

                val expiryCheckResult = state.epcExpiryCheckStep.formModelIfReachableOrNull?.tenancyStartedBeforeExpiry
                if (expiryCheckResult != null) {
                    add(
                        SummaryListRowViewModel.forCheckYourAnswersPage(
                            "forms.checkComplianceAnswers.epc.expiryCheck",
                            expiryCheckResult,
                            Destination.VisitableStep(state.epcExpiryCheckStep, childJourneyId),
                        ),
                    )
                }

                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.epc.energyRating",
                        epcDetails.energyRating.uppercase(),
                        null,
                    ),
                )

                if (state.meesExemptionCheckStep.isStepReachable) {
                    val exemptionReason = state.meesExemptionReasonStep.formModelIfReachableOrNull?.exemptionReason
                    val changeUrl =
                        if (state.epcExpiredStep.outcome == Complete.COMPLETE) {
                            epcStartingStep
                        } else if (exemptionReason == null) {
                            Destination.VisitableStep(state.meesExemptionCheckStep, childJourneyId)
                        } else {
                            Destination.VisitableStep(state.meesExemptionReasonStep, childJourneyId)
                        }

                    add(
                        SummaryListRowViewModel.forCheckYourAnswersPage(
                            "forms.checkComplianceAnswers.epc.meesExemption",
                            exemptionReason ?: "commonText.none",
                            changeUrl,
                        ),
                    )
                }
            }.toList()

    private fun getEpcExemptionRow(): SummaryListRowViewModel {
        val changeUrl =
            if (state.epcMissingStep.outcome == Complete.COMPLETE || state.epcNotFoundStep.outcome == Complete.COMPLETE) {
                epcStartingStep
            } else {
                Destination.VisitableStep(state.epcExemptionReasonStep, childJourneyId)
            }

        return SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkComplianceAnswers.epc.exemption",
            state.epcExemptionReasonStep.formModelIfReachableOrNull?.exemptionReason ?: "commonText.none",
            changeUrl,
        )
    }
}
