package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateTenancyDetailsJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateTenancyDetailsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.NumberOfHouseholdsFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdStep
import java.util.UUID

class NumberOfHouseholdsFormPageLettingAgentUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : NumberOfHouseholdsFormBasePage(
        page,
        LettingAgentUpdateTenancyDetailsController.getUpdateTenancyDetailsRoute(
            UUID.fromString(urlArguments["token"]!!),
        ) + "/${HouseholdStep.ROUTE_SEGMENT}",
    )
