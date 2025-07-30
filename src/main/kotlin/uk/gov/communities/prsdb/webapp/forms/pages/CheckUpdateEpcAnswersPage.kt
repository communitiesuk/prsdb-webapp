package uk.gov.communities.prsdb.webapp.forms.pages

import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.pages.cya.EpcSummaryRowsFactory
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

class CheckUpdateEpcAnswersPage(
    journeyDataService: JourneyDataService,
    epcCertificateUrlProvider: EpcCertificateUrlProvider,
    missingAnswersRedirect: String,
) : BasicCheckAnswersPage(
        content =
            mapOf(
                "title" to "propertyDetails.update.title",
                "summaryName" to "forms.update.epc.checkYourAnswers.summary",
                "showWarning" to true,
                "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
            ),
        journeyDataService = journeyDataService,
        missingAnswersRedirect = missingAnswersRedirect,
    ) {
    val epcDataFactory =
        EpcSummaryRowsFactory(
            epcCertificateUrlProvider = epcCertificateUrlProvider,
            epcStartingStep = PropertyComplianceStepId.UpdateEpc,
        )

    override fun getSummaryList(filteredJourneyData: JourneyData) = epcDataFactory.createRows(filteredJourneyData)
}
