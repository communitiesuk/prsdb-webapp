package uk.gov.communities.prsdb.webapp.forms.pages.cya

import uk.gov.communities.prsdb.webapp.constants.EPC_ACCEPTABLE_RATING_RANGE
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.forms.steps.factories.PropertyComplianceSharedStepFactory
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
    val stepFactory: PropertyComplianceSharedStepFactory,
) {
    fun createRows(filteredJourneyData: JourneyData) =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                add(getEpcStatusRow(filteredJourneyData))
                if (filteredJourneyData.getAcceptedEpcDetails(stepFactory.checkAutoMatchedEpcStepId) != null) {
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

        val certificateNumber = filteredJourneyData.getAcceptedEpcDetails(stepFactory.checkAutoMatchedEpcStepId)?.certificateNumber
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
                val epcDetails = filteredJourneyData.getAcceptedEpcDetails(stepFactory.checkAutoMatchedEpcStepId)!!
                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.epc.expiryDate",
                        epcDetails.expiryDate,
                        null,
                    ),
                )

                val expiryCheckResult = filteredJourneyData.getDidTenancyStartBeforeEpcExpiry(stepFactory.epcExpiryCheckStepId)
                if (expiryCheckResult != null) {
                    add(
                        SummaryListRowViewModel.forCheckYourAnswersPage(
                            "forms.checkComplianceAnswers.epc.expiryCheck",
                            expiryCheckResult,
                            stepFactory.epcExpiryCheckStepId.urlPathSegment,
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
                    val changeUrl =
                        if (filteredJourneyData.getHasCompletedEpcExpired()) {
                            PropertyComplianceStepId.EPC.urlPathSegment
                        } else {
                            PropertyComplianceStepId.MeesExemptionReason.urlPathSegment
                        }

                    add(
                        SummaryListRowViewModel.forCheckYourAnswersPage(
                            "forms.checkComplianceAnswers.epc.meesExemption",
                            filteredJourneyData.getMeesExemptionReason(stepFactory.meesExemptionReasonStepId) ?: "commonText.none",
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
                stepFactory.epcExemptionReasonStepId.urlPathSegment
            }

        return SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkComplianceAnswers.epc.exemption",
            filteredJourneyData.getEpcExemptionReason(stepFactory.epcExemptionReasonStepId) ?: "commonText.none",
            changeUrl,
        )
    }
}
