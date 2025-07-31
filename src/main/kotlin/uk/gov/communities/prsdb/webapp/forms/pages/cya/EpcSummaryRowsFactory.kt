package uk.gov.communities.prsdb.webapp.forms.pages.cya

import uk.gov.communities.prsdb.webapp.constants.EPC_ACCEPTABLE_RATING_RANGE
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getAcceptedEpcDetails
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getDidTenancyStartBeforeEpcExpiry
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEpcExemptionReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedEpcExemptionConfirmation
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedEpcExpired
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedEpcMissing
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedEpcNotFound
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getMeesExemptionReason
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider

class EpcSummaryRowsFactory(
    val epcStartingStep: PropertyComplianceStepId,
    val epcCertificateUrlProvider: EpcCertificateUrlProvider,
) {
    fun createRows(filteredJourneyData: JourneyData) =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                add(getEpcStatusRow(filteredJourneyData))
                if (filteredJourneyData.getAcceptedEpcDetails() != null) {
                    addAll(getEpcDetailRows(filteredJourneyData))
                } else {
                    add(getEpcExemptionRow(filteredJourneyData))
                }
            }.toList()

    private fun getEpcStatusRow(filteredJourneyData: JourneyData): SummaryListRowViewModel {
        val fieldValue =
            if (filteredJourneyData.getHasCompletedEpcExemptionConfirmation()) {
                "forms.checkComplianceAnswers.certificate.notRequired"
            } else if (filteredJourneyData.getHasCompletedEpcExpired()) {
                "forms.checkComplianceAnswers.epc.viewExpired"
            } else if (filteredJourneyData.getHasCompletedEpcMissing() || filteredJourneyData.getHasCompletedEpcNotFound()) {
                "forms.checkComplianceAnswers.certificate.notAdded"
            } else {
                "forms.checkComplianceAnswers.epc.view"
            }

        val certificateNumber = filteredJourneyData.getAcceptedEpcDetails()?.certificateNumber
        val valueUrl =
            if (certificateNumber != null) {
                epcCertificateUrlProvider.getEpcCertificateUrl(certificateNumber)
            } else {
                null
            }

        return SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkComplianceAnswers.epc.certificate",
            fieldValue,
            epcStartingStep.urlPathSegment,
            valueUrl,
        )
    }

    private fun getEpcDetailRows(filteredJourneyData: JourneyData) =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                val epcDetails = filteredJourneyData.getAcceptedEpcDetails()!!
                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.epc.expiryDate",
                        epcDetails.expiryDate,
                        null,
                    ),
                )

                val expiryCheckResult = filteredJourneyData.getDidTenancyStartBeforeEpcExpiry()
                if (expiryCheckResult != null) {
                    add(
                        SummaryListRowViewModel.forCheckYourAnswersPage(
                            "forms.checkComplianceAnswers.epc.expiryCheck",
                            expiryCheckResult,
                            PropertyComplianceStepId.EpcExpiryCheck.urlPathSegment,
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

                if (epcDetails.energyRating.uppercase() !in EPC_ACCEPTABLE_RATING_RANGE) {
                    val exemptionReason = filteredJourneyData.getMeesExemptionReason()
                    val changeUrl =
                        if (filteredJourneyData.getHasCompletedEpcExpired()) {
                            epcStartingStep.urlPathSegment
                        } else if (exemptionReason == null) {
                            PropertyComplianceStepId.MeesExemptionCheck.urlPathSegment
                        } else {
                            PropertyComplianceStepId.MeesExemptionReason.urlPathSegment
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

    private fun getEpcExemptionRow(filteredJourneyData: JourneyData): SummaryListRowViewModel {
        val changeUrl =
            if (filteredJourneyData.getHasCompletedEpcMissing() || filteredJourneyData.getHasCompletedEpcNotFound()) {
                epcStartingStep.urlPathSegment
            } else {
                PropertyComplianceStepId.EpcExemptionReason.urlPathSegment
            }

        return SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkComplianceAnswers.epc.exemption",
            filteredJourneyData.getEpcExemptionReason() ?: "commonText.none",
            changeUrl,
        )
    }
}
