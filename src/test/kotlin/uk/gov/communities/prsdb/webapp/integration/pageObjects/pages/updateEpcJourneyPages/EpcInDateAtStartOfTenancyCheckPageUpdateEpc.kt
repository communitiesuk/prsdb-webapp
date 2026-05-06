package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateEpcJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateEpcController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EpcInDateAtStartOfTenancyCheckBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcInDateAtStartOfTenancyCheckStep

class EpcInDateAtStartOfTenancyCheckPageUpdateEpc(
    page: Page,
    urlArguments: Map<String, String>,
) : EpcInDateAtStartOfTenancyCheckBasePage(
        page,
        UpdateEpcController.UPDATE_EPC_ROUTE
            .replace("{propertyOwnershipId}", urlArguments["propertyOwnershipId"]!!) +
            "/${EpcInDateAtStartOfTenancyCheckStep.ROUTE_SEGMENT}",
    )
