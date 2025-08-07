package uk.gov.communities.prsdb.webapp.forms.pages

import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.pages.cya.EicrSummaryRowsFactory
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasNewEICR
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.UploadService

class CheckUpdateEicrAnswersPage(
    journeyDataService: JourneyDataService,
    missingAnswersRedirect: String,
    uploadService: UploadService,
) : BasicCheckAnswersPage(
        content =
            mapOf(
                "title" to "propertyDetails.update.title",
                "summaryName" to "forms.update.eicr.checkYourAnswers.summary",
                "showWarning" to true,
                "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
            ),
        journeyDataService = journeyDataService,
        missingAnswersRedirect = missingAnswersRedirect,
    ) {
    val eicrDataFactory =
        EicrSummaryRowsFactory(
            doesDataHaveEicr = { data -> data.getHasNewEICR()!! },
            eicrStartingStep = PropertyComplianceStepId.UpdateEICR,
            changeExemptionStep = PropertyComplianceStepId.EicrExemptionReason,
            uploadService = uploadService,
        )

    override fun getSummaryList(filteredJourneyData: JourneyData) = eicrDataFactory.createRows(filteredJourneyData)
}
