package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateLeadTrusteeController.Companion.UPDATE_LEAD_TRUSTEE_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.PageWithYesNoRadios
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AddressInUkStep
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.TrusteeAddressTask

class LeadTrusteeAddressInUkPageUpdateLeadTrustee(
    page: Page,
) : PageWithYesNoRadios(
        page,
        "$UPDATE_LEAD_TRUSTEE_ROUTE/${TrusteeAddressTask.ROUTE_SEGMENT}/${AddressInUkStep.ROUTE_SEGMENT}",
    )
