package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateElectricalSafetyJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateElectricalSafetyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertMissingStep
import java.util.UUID

class ElectricalCertMissingFormPageLettingAgentUpdateElectricalSafety(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        LettingAgentUpdateElectricalSafetyController.getUpdateElectricalSafetyRoute(
            UUID.fromString(urlArguments["token"]!!),
        ) + "/${ElectricalCertMissingStep.ROUTE_SEGMENT}",
    ) {
    val form = PostForm(page)
}
