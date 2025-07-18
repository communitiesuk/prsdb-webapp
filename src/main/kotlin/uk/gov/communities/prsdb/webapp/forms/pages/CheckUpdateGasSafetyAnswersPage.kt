package uk.gov.communities.prsdb.webapp.forms.pages

import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.pages.cya.GasSafetySummaryRowsFactory
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasNewGasSafetyCertificate
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

class CheckUpdateGasSafetyAnswersPage(
    journeyDataService: JourneyDataService,
) : CheckAnswersPage(
        content =
            mapOf(
                "title" to "propertyDetails.update.title",
                "summaryName" to "forms.update.checkOccupancy.summaryName",
                "showWarning" to true,
                "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
            ),
        journeyDataService = journeyDataService,
    ) {
    val gasSafetyDataFactory =
        GasSafetySummaryRowsFactory(
            doesDataHaveGasSafetyCert = { data -> data.getHasNewGasSafetyCertificate() },
            gasSafetyStartingStep = PropertyComplianceStepId.UpdateGasSafety,
            changeExemptionStep = PropertyComplianceStepId.GasSafetyExemptionReason,
        )

    override fun getSummaryList(filteredJourneyData: JourneyData) = gasSafetyDataFactory.createRows(filteredJourneyData)
}
