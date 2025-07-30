package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.pages.cya.EicrSummaryRowsFactory
import uk.gov.communities.prsdb.webapp.forms.pages.cya.EpcSummaryRowsFactory
import uk.gov.communities.prsdb.webapp.forms.pages.cya.GasSafetySummaryRowsFactory
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
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
    val eicrDataFactory =
        EicrSummaryRowsFactory(
            doesDataHaveEicr = { data -> data.getHasEICR()!! },
            eicrStartingStep = PropertyComplianceStepId.EICR,
            changeExemptionStep = PropertyComplianceStepId.EicrExemption,
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
        modelAndView.addObject("eicrData", eicrDataFactory.createRows(filteredJourneyData))
        modelAndView.addObject("epcData", epcDataFactory.createRows(filteredJourneyData))
        modelAndView.addObject("responsibilityData", getResponsibilityData(filteredJourneyData))
    }

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
}
