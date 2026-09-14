package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateGasSafetyJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordUpdateGasSafetyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithRadios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemoveGasCertUploadStep

class RemoveGasCertUploadFormPageUpdateGasSafety(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        LandlordUpdateGasSafetyController.UPDATE_GAS_SAFETY_ROUTE
            .replace("{propertyOwnershipId}", urlArguments["propertyOwnershipId"]!!) +
            "/${RemoveGasCertUploadStep.ROUTE_SEGMENT}",
    ) {
    val form = FormWithRadios(page)
}
