package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateLeadTrusteeController.Companion.UPDATE_LEAD_TRUSTEE_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.SelectAddressFormPage
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.TrusteeAddressTask

class LeadTrusteeSelectAddressPageUpdateLeadTrustee(
    page: Page,
) : SelectAddressFormPage(
        page,
        "$UPDATE_LEAD_TRUSTEE_ROUTE/${TrusteeAddressTask.ROUTE_SEGMENT}/${SelectAddressStep.ROUTE_SEGMENT}",
    )
