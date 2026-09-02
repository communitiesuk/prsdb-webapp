package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateHouseholdsAndTenantsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.CheckOccupancyAnswersFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.householdsAndTenants.UpdateHouseholdsAndTenantsCyaStep
import java.util.UUID

class CheckHouseholdsAnswersPageLettingAgentUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : CheckOccupancyAnswersFormPage(
        page,
        LettingAgentUpdateHouseholdsAndTenantsController.getRoute(
            UUID.fromString(urlArguments["token"]!!),
            UpdateHouseholdsAndTenantsCyaStep.ROUTE_SEGMENT,
        ),
    )
