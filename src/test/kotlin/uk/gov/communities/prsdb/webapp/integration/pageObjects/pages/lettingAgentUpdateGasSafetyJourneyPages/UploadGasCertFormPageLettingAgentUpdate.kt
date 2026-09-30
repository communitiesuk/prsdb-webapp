package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateGasSafetyJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateGasSafetyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.UploadCertFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadGasCertStep
import java.util.UUID

class UploadGasCertFormPageLettingAgentUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : UploadCertFormBasePage(
        page,
        LettingAgentUpdateGasSafetyController.getUpdateGasSafetyRoute(UUID.fromString(urlArguments["token"]!!)) +
            "/${UploadGasCertStep.ROUTE_SEGMENT}",
    )
