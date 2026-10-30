package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateElectricalSafetyJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateElectricalSafetyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.DateFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EicrExpiryDateStep
import java.util.UUID

class EicrExpiryDateFormPageLettingAgentUpdateElectricalSafety(
    page: Page,
    urlArguments: Map<String, String>,
) : DateFormPage(
        page,
        LettingAgentUpdateElectricalSafetyController.getUpdateElectricalSafetyRoute(
            UUID.fromString(urlArguments["token"]!!),
        ) + "/${EicrExpiryDateStep.ROUTE_SEGMENT}",
    )
