package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateElectricalSafetyJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateElectricalSafetyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Table
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CheckElectricalCertUploadsFormPagePropertyRegistration.CheckUploadsForm
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckElectricalCertUploadsStep
import java.util.UUID

class CheckElectricalCertUploadsFormPageLettingAgentUpdateElectricalSafety(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        LettingAgentUpdateElectricalSafetyController.getUpdateElectricalSafetyRoute(
            UUID.fromString(urlArguments["token"]!!),
        ) + "/${CheckElectricalCertUploadsStep.ROUTE_SEGMENT}",
    ) {
    val table = Table(page)
    val form = CheckUploadsForm(page)
}
