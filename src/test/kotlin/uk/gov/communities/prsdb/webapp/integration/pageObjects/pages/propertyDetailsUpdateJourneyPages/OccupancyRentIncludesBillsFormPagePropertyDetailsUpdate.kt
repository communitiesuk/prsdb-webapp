package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOccupancyController
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.RentIncludesBillsFormBasePage

class OccupancyRentIncludesBillsFormPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : RentIncludesBillsFormBasePage(
        page,
        UpdateOccupancyController.getUpdateOccupancyRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${RegisterPropertyStepId.RentIncludesBills.urlPathSegment}",
    )
