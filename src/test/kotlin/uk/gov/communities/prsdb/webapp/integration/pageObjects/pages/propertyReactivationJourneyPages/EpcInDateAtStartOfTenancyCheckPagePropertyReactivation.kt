package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.ReactivatePropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EpcInDateAtStartOfTenancyCheckBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcInDateAtStartOfTenancyCheckStep

class EpcInDateAtStartOfTenancyCheckPagePropertyReactivation(
    page: Page,
    urlArguments: Map<String, String>,
) : EpcInDateAtStartOfTenancyCheckBasePage(
        page,
        ReactivatePropertyController.getPropertyReactivationBasePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${EpcInDateAtStartOfTenancyCheckStep.ROUTE_SEGMENT}",
    )
