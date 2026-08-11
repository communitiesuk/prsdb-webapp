package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateCompaniesHousePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateCompaniesHouseController.Companion.UPDATE_COMPANIES_HOUSE_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.OrgCompanyNumberFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompanyNumberStep

class OrgCompanyNumberFormPageUpdateCompaniesHouse(
    page: Page,
) : OrgCompanyNumberFormBasePage(page, "$UPDATE_COMPANIES_HOUSE_ROUTE/${OrgCompanyNumberStep.ROUTE_SEGMENT}")
