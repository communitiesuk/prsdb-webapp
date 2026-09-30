package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateTenancyDetailsJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateTenancyDetailsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.CheckOccupancyAnswersFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.tenancyDetails.UpdateTenancyDetailsCyaStep
import java.util.UUID

class CheckTenancyDetailsAnswersPageLettingAgentUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : CheckOccupancyAnswersFormPage(
        page,
        LettingAgentUpdateTenancyDetailsController.getUpdateTenancyDetailsRoute(
            UUID.fromString(urlArguments["token"]!!),
        ) + "/${UpdateTenancyDetailsCyaStep.ROUTE_SEGMENT}",
    )
