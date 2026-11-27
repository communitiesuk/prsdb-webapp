package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateLeadTrusteeController.Companion.UPDATE_LEAD_TRUSTEE_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.ManualAddressFormPage
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.InternationalAddressTask
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.TrusteeAddressTask

class LeadTrusteeNonUkAddressPageUpdateLeadTrustee(
    page: Page,
) : ManualAddressFormPage(
        page,
        "$UPDATE_LEAD_TRUSTEE_ROUTE/${TrusteeAddressTask.ROUTE_SEGMENT}/" +
            InternationalAddressTask.INTERNATIONAL_MANUAL_ADDRESS_ROUTE_SEGMENT,
    )
