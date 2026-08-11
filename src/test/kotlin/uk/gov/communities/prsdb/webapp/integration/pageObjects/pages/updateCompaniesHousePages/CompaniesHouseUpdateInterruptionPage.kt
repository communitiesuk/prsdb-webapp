package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateCompaniesHousePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateCompaniesHouseController.Companion.UPDATE_COMPANIES_HOUSE_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.InterruptionPage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompaniesHouseInterruptionStep

class CompaniesHouseUpdateInterruptionPage(
    page: Page,
) : InterruptionPage(page, "$UPDATE_COMPANIES_HOUSE_ROUTE/${OrgCompaniesHouseInterruptionStep.ROUTE_SEGMENT}")
