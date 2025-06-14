package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Table
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Tabs

abstract class LandlordDetailsBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val tabs = LandlordDetailsTabs(page)
    val backLink = BackLink.default(page)
    val personalDetailsSummaryList = LandlordPersonalDetailsSummaryList(page)
    val registeredPropertiesTable = Table(page)

    fun getPropertyAddressLink(address: String) = Link.byText(page, address)

    class LandlordDetailsTabs(
        page: Page,
    ) : Tabs(page) {
        fun goToRegisteredProperties() {
            goToTab("Registered properties")
        }
    }

    class LandlordPersonalDetailsSummaryList(
        page: Page,
    ) : SummaryList(page) {
        val nameRow = getRow("Name")
        val emailRow = getRow("Email address")
        val phoneNumberRow = getRow("Telephone number")
        val addressRow = getRow("Contact address")
        val dateOfBirthRow = getRow("Date of birth")
    }
}
