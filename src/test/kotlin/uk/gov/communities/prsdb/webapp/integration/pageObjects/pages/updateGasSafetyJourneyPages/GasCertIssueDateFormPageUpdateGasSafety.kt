package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateGasSafetyJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordUpdateGasSafetyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.DateFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.GasCertIssueDateStep

class GasCertIssueDateFormPageUpdateGasSafety(
    page: Page,
    urlArguments: Map<String, String>,
) : DateFormPage(
        page,
        LandlordUpdateGasSafetyController.UPDATE_GAS_SAFETY_ROUTE
            .replace("{propertyOwnershipId}", urlArguments["propertyOwnershipId"]!!) +
            "/${GasCertIssueDateStep.ROUTE_SEGMENT}",
    ) {
    // TODO PDJB-1926: Use a fixed expectedTitleHeading when GAS_CERTIFICATE_14_MONTH_EXPIRY is permanently enabled.
    override val expectedTitleHeading: String?
        get() =
            when (page.locator("h1").innerText()) {
                "When was this certificate issued?" -> "When was the gas safety certificate issued?"
                else -> null
            }
}
