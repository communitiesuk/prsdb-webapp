package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateCompaniesHousePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateCompaniesHouseController.Companion.UPDATE_COMPANIES_HOUSE_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryCard
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.companiesHouse.CompaniesHouseUpdateCheckAnswersStep

class CompaniesHouseUpdateCheckAnswersPage(
    page: Page,
) : BasePage(page, "$UPDATE_COMPANIES_HOUSE_ROUTE/${CompaniesHouseUpdateCheckAnswersStep.ROUTE_SEGMENT}") {
    val form = PostForm(page)

    val companyDetails = CompanyDetailsSummaryList(page)

    fun governingBodyMemberCard(title: String) = SummaryCard(page, title)

    fun confirmAndSubmit() = form.submit()

    class CompanyDetailsSummaryList(
        page: Page,
    ) : SummaryList(page) {
        val registeredWithCompaniesHouseRow = getRow("Registered with Companies House")
        val companiesHouseNumberRow = getRow("Companies House number")
    }
}
