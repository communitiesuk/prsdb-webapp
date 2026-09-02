package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateHouseholdsAndTenantsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.NumberOfPeopleFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.TenantsStep
import java.util.UUID

class HouseholdsNumberOfPeopleFormPageLettingAgentUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : NumberOfPeopleFormPage(
        page,
        LettingAgentUpdateHouseholdsAndTenantsController.getRoute(
            UUID.fromString(urlArguments["token"]!!),
            TenantsStep.ROUTE_SEGMENT,
        ),
    )
