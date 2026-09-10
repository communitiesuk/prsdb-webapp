package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateLicensingJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateLicensingController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.LicensingTypeFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LicensingTypeStep
import java.util.UUID

class LicensingTypeFormPageLettingAgentUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : LicensingTypeFormPage(
        page,
        LettingAgentUpdateLicensingController.getUpdateLicensingRoute(UUID.fromString(urlArguments["token"]!!)) +
            "/${LicensingTypeStep.ROUTE_SEGMENT}",
    )
