package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Paragraph
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Table
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Tabs

abstract class LandlordDetailsBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    open val tabs: LandlordDetailsTabs = LandlordDetailsTabs(page)
    val backLink = BackLink.default(page)
    val personalDetailsSummaryList = LandlordPersonalDetailsSummaryList(page)
    val registeredPropertiesTable = Table(page)
    val noRegisteredPropertiesMessage = Paragraph.byTestId(page, "no-registered-properties-msg")
    val noRegisteredPropertiesLink = Link.byText(page, "Register a property")
    val activePropertiesTable = Table(page.locator("#active-properties"))
    val deactivatedPropertiesTable = Table(page.locator("#deactivated-properties"))
    val deactivatedPropertiesLinks = Link.default(page.locator("#deactivated-properties"))
    val noActivePropertiesMessage = Paragraph.byTestId(page, "no-active-properties-msg")
    val noDeactivatedPropertiesMessage = Paragraph.byTestId(page, "no-deactivated-properties-msg")

    fun getPropertyAddressLink(address: String) = Link.byText(page, address)

    open class LandlordDetailsTabs(
        page: Page,
    ) : Tabs(page) {
        val activePropertiesTab = tabsList.filter(Locator.FilterOptions().setHasText("Active properties"))
        val deactivatedPropertiesTab = tabsList.filter(Locator.FilterOptions().setHasText("Deactivated properties"))

        fun goToActiveProperties() = activePropertiesTab.click()

        fun goToDeactivatedProperties() = deactivatedPropertiesTab.click()

        // TODO PDJB-2149: Delete goToRegisteredProperties and the noRegisteredProperties* locators if no longer used by
        //  the local council pages when the ACTIVE_INACTIVE feature flag is removed
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
        val landlordTypeRow = getRow("Landlord type")
    }
}
