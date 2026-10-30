package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateElectricalSafetyJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateElectricalSafetyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.CheckElectricalCertUploadsFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckEicrUploadsStep
import java.util.UUID

class CheckEicrUploadsFormPageLettingAgentUpdateElectricalSafety(
    page: Page,
    urlArguments: Map<String, String>,
) : CheckElectricalCertUploadsFormBasePage(
        page,
        LettingAgentUpdateElectricalSafetyController.getUpdateElectricalSafetyRoute(
            UUID.fromString(urlArguments["token"]!!),
        ) + "/${CheckEicrUploadsStep.ROUTE_SEGMENT}",
    )
