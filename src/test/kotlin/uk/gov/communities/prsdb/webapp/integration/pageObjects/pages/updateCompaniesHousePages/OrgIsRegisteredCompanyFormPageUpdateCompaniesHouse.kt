package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateCompaniesHousePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateCompaniesHouseController.Companion.UPDATE_COMPANIES_HOUSE_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.PageWithYesNoRadios
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCompanyStep

class OrgIsRegisteredCompanyFormPageUpdateCompaniesHouse(
    page: Page,
) : PageWithYesNoRadios(page, "$UPDATE_COMPANIES_HOUSE_ROUTE/${OrgIsRegisteredCompanyStep.ROUTE_SEGMENT}")
