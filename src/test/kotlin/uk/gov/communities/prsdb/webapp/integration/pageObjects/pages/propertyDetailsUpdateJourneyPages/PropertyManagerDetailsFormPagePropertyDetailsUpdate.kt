package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdatePropertyManagerController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.PropertyManagerDetailsFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyManagerDetailsStep

class PropertyManagerDetailsFormPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : PropertyManagerDetailsFormBasePage(
        page,
        UpdatePropertyManagerController.getUpdatePropertyManagerRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${PropertyManagerDetailsStep.ROUTE_SEGMENT}",
    )
