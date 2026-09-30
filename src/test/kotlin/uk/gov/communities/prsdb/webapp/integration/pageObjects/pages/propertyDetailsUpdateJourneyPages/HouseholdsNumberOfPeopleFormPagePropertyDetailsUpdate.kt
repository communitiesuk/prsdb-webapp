package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordUpdateHouseholdsAndTenantsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.NumberOfPeopleFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.TenantsStep

class HouseholdsNumberOfPeopleFormPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : NumberOfPeopleFormPage(
        page,
        LandlordUpdateHouseholdsAndTenantsController.getUpdateHouseholdsAndTenantsRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${TenantsStep.ROUTE_SEGMENT}",
    )
