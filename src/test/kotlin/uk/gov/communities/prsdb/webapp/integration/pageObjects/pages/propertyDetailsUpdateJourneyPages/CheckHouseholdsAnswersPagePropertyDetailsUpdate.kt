package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateHouseholdsAndTenantsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.CheckOccupancyAnswersFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.householdsAndTenants.UpdateHouseholdsAndTenantsCyaStep

class CheckHouseholdsAnswersPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : CheckOccupancyAnswersFormPage(
        page,
        UpdateHouseholdsAndTenantsController.getUpdateHouseholdsAndTenantsRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${UpdateHouseholdsAndTenantsCyaStep.ROUTE_SEGMENT}",
    )
