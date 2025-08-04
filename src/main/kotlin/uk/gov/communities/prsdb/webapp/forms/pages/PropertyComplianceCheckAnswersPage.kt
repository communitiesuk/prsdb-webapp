package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.pages.cya.EicrSummaryRowsFactory
import uk.gov.communities.prsdb.webapp.forms.pages.cya.EpcSummaryRowsFactory
import uk.gov.communities.prsdb.webapp.forms.pages.cya.GasSafetySummaryRowsFactory
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasEICR
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasGasSafetyCert
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

class PropertyComplianceCheckAnswersPage(
    journeyDataService: JourneyDataService,
    epcCertificateUrlProvider: EpcCertificateUrlProvider,
    missingAnswersRedirect: String,
    private val propertyAddressProvider: () -> String,
) : CheckAnswersPage(
        content = emptyMap(),
        journeyDataService = journeyDataService,
        templateName = "forms/propertyComplianceCheckAnswersForm",
        missingAnswersRedirect = missingAnswersRedirect,
    ) {
    private val gasSafetyDataFactory =
        GasSafetySummaryRowsFactory(
            doesDataHaveGasSafetyCert = { data -> data.getHasGasSafetyCert()!! },
            gasSafetyStartingStep = PropertyComplianceStepId.GasSafety,
            changeExemptionStep = PropertyComplianceStepId.GasSafetyExemption,
        )

    private val eicrDataFactory =
        EicrSummaryRowsFactory(
            doesDataHaveEicr = { data -> data.getHasEICR()!! },
            eicrStartingStep = PropertyComplianceStepId.EICR,
            changeExemptionStep = PropertyComplianceStepId.EicrExemption,
        )

    private val epcDataFactory =
        EpcSummaryRowsFactory(
            epcCertificateUrlProvider = epcCertificateUrlProvider,
            epcStartingStep = PropertyComplianceStepId.EPC,
        )

    private val responsibilityData =
        listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "forms.checkComplianceAnswers.responsibilities.fireSafety",
                true,
                PropertyComplianceStepId.FireSafetyDeclaration.urlPathSegment,
                actionValue = "forms.links.view",
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

    override fun addPageContentToModel(
        modelAndView: ModelAndView,
        filteredJourneyData: JourneyData,
    ) {
        modelAndView.addObject("propertyAddress", propertyAddressProvider())
        modelAndView.addObject("gasSafetyData", gasSafetyDataFactory.createRows(filteredJourneyData))
        modelAndView.addObject("eicrData", eicrDataFactory.createRows(filteredJourneyData))
        modelAndView.addObject("epcData", epcDataFactory.createRows(filteredJourneyData))
        modelAndView.addObject("responsibilityData", responsibilityData)
    }
}
