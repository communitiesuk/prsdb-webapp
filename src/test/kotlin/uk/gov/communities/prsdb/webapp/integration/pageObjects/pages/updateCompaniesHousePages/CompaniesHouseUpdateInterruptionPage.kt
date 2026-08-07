package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateCompaniesHousePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateCompaniesHouseController.Companion.UPDATE_COMPANIES_HOUSE_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.companiesHouse.CompaniesHouseUpdateInterruptionStep

class CompaniesHouseUpdateInterruptionPage(
    page: Page,
) : BasePage(page, "$UPDATE_COMPANIES_HOUSE_ROUTE/${CompaniesHouseUpdateInterruptionStep.ROUTE_SEGMENT}") {
    val form = PostForm(page)

    fun clickContinue() = form.submit()
}
