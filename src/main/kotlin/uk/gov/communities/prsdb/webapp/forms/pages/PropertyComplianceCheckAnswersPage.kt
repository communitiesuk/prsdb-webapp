package uk.gov.communities.prsdb.webapp.forms.pages

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.constants.EICR_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.pages.cya.EpcSummaryRowsFactory
import uk.gov.communities.prsdb.webapp.forms.pages.cya.GasSafetySummaryRowsFactory
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEicrExemptionOtherReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEicrExemptionReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEicrIssueDate
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedEicrExemptionConfirmation
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedEicrExemptionMissing
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedEicrUploadConfirmation
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasEICR
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasFireSafetyDeclaration
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasGasSafetyCert
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

class PropertyComplianceCheckAnswersPage(
    journeyDataService: JourneyDataService,
    private val epcCertificateUrlProvider: EpcCertificateUrlProvider,
    missingAnswersRedirect: String,
    private val propertyAddressProvider: () -> String,
) : CheckAnswersPage(
        content = emptyMap(),
        journeyDataService = journeyDataService,
        templateName = "forms/propertyComplianceCheckAnswersForm",
        missingAnswersRedirect = missingAnswersRedirect,
    ) {
    val gasSafetyDataFactory =
        GasSafetySummaryRowsFactory(
            doesDataHaveGasSafetyCert = { data -> data.getHasGasSafetyCert()!! },
            gasSafetyStartingStep = PropertyComplianceStepId.GasSafety,
            changeExemptionStep = PropertyComplianceStepId.GasSafetyExemption,
        )

    val epcDataFactory =
        EpcSummaryRowsFactory(
            epcCertificateUrlProvider = epcCertificateUrlProvider,
            epcStartingStep = PropertyComplianceStepId.EPC,
        )

    override fun addPageContentToModel(
        modelAndView: ModelAndView,
        filteredJourneyData: JourneyData,
    ) {
        modelAndView.addObject("propertyAddress", propertyAddressProvider())
        modelAndView.addObject("gasSafetyData", gasSafetyDataFactory.createRows(filteredJourneyData))
        modelAndView.addObject("eicrData", getEicrData(filteredJourneyData))
        modelAndView.addObject("epcData", epcDataFactory.createRows(filteredJourneyData))
        modelAndView.addObject("responsibilityData", getResponsibilityData(filteredJourneyData))
    }

    private fun getEicrData(filteredJourneyData: JourneyData) =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                add(getEicrStatusRow(filteredJourneyData))
                if (filteredJourneyData.getHasEICR()!!) {
                    addAll(getEicrDetailRows(filteredJourneyData))
                } else {
                    add(getEicrExemptionRow(filteredJourneyData))
                }
            }.toList()

    private fun getResponsibilityData(filteredJourneyData: JourneyData) =
        listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "forms.checkComplianceAnswers.responsibilities.fireSafety",
                filteredJourneyData.getHasFireSafetyDeclaration()!!,
                PropertyComplianceStepId.FireSafetyDeclaration.urlPathSegment,
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "forms.checkComplianceAnswers.responsibilities.keepPropertySafe",
                true,
                PropertyComplianceStepId.KeepPropertySafe.urlPathSegment,
                actionValue = "forms.links.view",
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "forms.checkComplianceAnswers.responsibilities.responsibilityToTenants",
                true,
                PropertyComplianceStepId.ResponsibilityToTenants.urlPathSegment,
                actionValue = "forms.links.view",
            ),
        )

    private fun getEicrStatusRow(filteredJourneyData: JourneyData): SummaryListRowViewModel {
        val fieldValue =
            // TODO PRSD-976: Add link to gas safety cert (or appropriate message if virus scan failed)
            if (filteredJourneyData.getHasCompletedEicrUploadConfirmation()) {
                "forms.checkComplianceAnswers.eicr.download"
            } else if (filteredJourneyData.getHasCompletedEicrExemptionConfirmation()) {
                "forms.checkComplianceAnswers.certificate.notRequired"
            } else if (filteredJourneyData.getHasCompletedEicrExemptionMissing()) {
                "forms.checkComplianceAnswers.certificate.notAdded"
            } else {
                "forms.checkComplianceAnswers.certificate.expired"
            }

        return SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkComplianceAnswers.eicr.certificate",
            fieldValue,
            PropertyComplianceStepId.EICR.urlPathSegment,
        )
    }

    private fun getEicrDetailRows(filteredJourneyData: JourneyData): List<SummaryListRowViewModel> {
        val issueDate = filteredJourneyData.getEicrIssueDate()!!
        return listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "forms.checkComplianceAnswers.certificate.issueDate",
                issueDate,
                PropertyComplianceStepId.EicrIssueDate.urlPathSegment,
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "forms.checkComplianceAnswers.certificate.validUntil",
                issueDate.plus(DatePeriod(years = EICR_VALIDITY_YEARS)),
                null,
            ),
        )
    }

    private fun getEicrExemptionRow(filteredJourneyData: JourneyData): SummaryListRowViewModel {
        val fieldValue: Any =
            when (val exemptionReason = filteredJourneyData.getEicrExemptionReason()) {
                null -> "commonText.none"
                EicrExemptionReason.OTHER -> listOf(exemptionReason, filteredJourneyData.getEicrExemptionOtherReason())
                else -> exemptionReason
            }

        return SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkComplianceAnswers.certificate.exemption",
            fieldValue,
            PropertyComplianceStepId.EicrExemption.urlPathSegment,
        )
    }
}
