package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordUpdateHouseholdsAndTenantsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.CheckOccupancyAnswersFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.householdsAndTenants.UpdateHouseholdsAndTenantsCyaStep

class CheckHouseholdsAnswersPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : CheckOccupancyAnswersFormPage(
        page,
        LandlordUpdateHouseholdsAndTenantsController.getUpdateHouseholdsAndTenantsRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${UpdateHouseholdsAndTenantsCyaStep.ROUTE_SEGMENT}",
    )
