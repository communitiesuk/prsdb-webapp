package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateLicensingJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateLicensingController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.LicenceNumberFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SelectiveLicenceStep
import java.util.UUID

class SelectiveLicenceFormPageLettingAgentUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : LicenceNumberFormPage(
        page,
        LettingAgentUpdateLicensingController.getUpdateLicensingRoute(UUID.fromString(urlArguments["token"]!!)) +
            "/${SelectiveLicenceStep.ROUTE_SEGMENT}",
    )
