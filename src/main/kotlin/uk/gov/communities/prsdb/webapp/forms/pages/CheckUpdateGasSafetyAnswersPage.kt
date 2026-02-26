package uk.gov.communities.prsdb.webapp.forms.pages

import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.pages.cya.LegacyGasSafetySummaryRowsFactory
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasNewGasSafetyCertificate
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.UploadService

class CheckUpdateGasSafetyAnswersPage(
    journeyDataService: JourneyDataService,
    missingAnswersRedirect: String,
    uploadService: UploadService,
) : BasicCheckAnswersPage(
        content =
            mapOf(
                "title" to "propertyDetails.update.title",
                "summaryName" to "forms.update.checkGasSafety.summary",
                "showWarning" to true,
                "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
                "insetText" to true,
            ),
        journeyDataService = journeyDataService,
        missingAnswersRedirect = missingAnswersRedirect,
    ) {
    val gasSafetyDataFactory =
        LegacyGasSafetySummaryRowsFactory(
            doesDataHaveGasSafetyCert = { data -> data.getHasNewGasSafetyCertificate()!! },
            gasSafetyStartingStep = PropertyComplianceStepId.UpdateGasSafety,
            changeExemptionStep = PropertyComplianceStepId.GasSafetyExemptionReason,
            uploadService = uploadService,
        )

    override fun getSummaryList(filteredJourneyData: JourneyData) = gasSafetyDataFactory.createRows(filteredJourneyData)
}
