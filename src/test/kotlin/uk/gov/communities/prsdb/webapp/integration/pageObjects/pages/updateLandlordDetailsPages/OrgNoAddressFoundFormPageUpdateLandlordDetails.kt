package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationLandlordAddressController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.NoAddressFoundFormPage
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NoAddressFoundStep

class OrgNoAddressFoundFormPageUpdateLandlordDetails(
    page: Page,
) : NoAddressFoundFormPage(
        page,
        "${UpdateOrganisationLandlordAddressController.UPDATE_ORG_ADDRESS_ROUTE}/${NoAddressFoundStep.ROUTE_SEGMENT}",
    )
