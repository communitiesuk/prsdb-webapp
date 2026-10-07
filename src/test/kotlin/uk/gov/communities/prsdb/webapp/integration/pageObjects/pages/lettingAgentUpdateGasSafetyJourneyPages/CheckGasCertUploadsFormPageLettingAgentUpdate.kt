package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateGasSafetyJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateGasSafetyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Table
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CheckGasCertUploadsFormPagePropertyRegistration.CheckUploadsForm
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckGasCertUploadsStep
import java.util.UUID

class CheckGasCertUploadsFormPageLettingAgentUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        LettingAgentUpdateGasSafetyController.getUpdateGasSafetyRoute(UUID.fromString(urlArguments["token"]!!)) +
            "/${CheckGasCertUploadsStep.ROUTE_SEGMENT}",
    ) {
    override val expectedTitleHeading: String
        get() = page.locator("h1").textContent().trim().replace(" file", " gas safety certificate file")

    val table = Table(page)
    val form = CheckUploadsForm(page)
}
