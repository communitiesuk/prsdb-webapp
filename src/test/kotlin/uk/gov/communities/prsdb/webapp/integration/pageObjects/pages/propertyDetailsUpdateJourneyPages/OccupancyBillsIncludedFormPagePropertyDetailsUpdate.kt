package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOccupancyController
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BillsIncludedFormBasePage

class OccupancyBillsIncludedFormPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : BillsIncludedFormBasePage(
        page,
        UpdateOccupancyController.getUpdateOccupancyRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${RegisterPropertyStepId.BillsIncluded.urlPathSegment}",
    )
