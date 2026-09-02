package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateHouseholdsAndTenantsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.NumberOfHouseholdsFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdStep
import java.util.UUID

class NumberOfHouseholdsFormPageLettingAgentUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : NumberOfHouseholdsFormBasePage(
        page,
        LettingAgentUpdateHouseholdsAndTenantsController.getRoute(
            UUID.fromString(urlArguments["token"]!!),
            HouseholdStep.ROUTE_SEGMENT,
        ),
    )
