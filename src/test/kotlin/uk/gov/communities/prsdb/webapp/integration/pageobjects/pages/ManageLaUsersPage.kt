package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole
import uk.gov.communities.prsdb.webapp.integration.pageobjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageobjects.components.Pagination
import uk.gov.communities.prsdb.webapp.integration.pageobjects.components.Table

private const val USERNAME_COL_INDEX: Int = 0
private const val ACCESS_LEVEL_COL_INDEX: Int = 1
private const val ACCOUNT_STATUS_COL_INDEX: Int = 2
private const val ACTIONS_COL_INDEX: Int = 3

class ManageLaUsersPage(
    page: Page,
) : BasePage(page) {
    override fun validate() {
        assertThat(header).containsText("Manage Betelgeuse's users")
    }

    val table = UsersTable(page)

    val inviteAnotherButton: Locator = page.getByRole(AriaRole.BUTTON).getByText("Invite another user")

    val returnToDashboardButton: Locator = page.getByRole(AriaRole.BUTTON).getByText("Return to dashboard")

    val pagination by lazy { Pagination(page.locator("nav.govuk-pagination"), this::class) }

    fun inviteNewUser(): InviteNewLaUserPage {
        inviteAnotherButton.click()
        return createValid(page, InviteNewLaUserPage::class)
    }

    class UsersTable(
        page: Page,
    ) : Table(page.locator("table.govuk-table")) {
        fun header() = Row(locator.locator("thead tr"))

        fun row(rowIndex: Int) = Row(locator.locator("tbody tr").nth(rowIndex))
    }

    class Row(
        rowLocator: Locator,
    ) : BaseComponent(rowLocator) {
        private val changeLink: Locator = locator.locator("td").nth(ACTIONS_COL_INDEX).locator("a")

        fun username(): String = locator.locator("td, th").nth(USERNAME_COL_INDEX).textContent()

        fun accessLevel(): String = locator.locator("td, th").nth(ACCESS_LEVEL_COL_INDEX).textContent()

        fun accountStatus(): String = locator.locator("td, th").nth(ACCOUNT_STATUS_COL_INDEX).textContent()

        fun editUser(): EditLaUserPage {
            changeLink.click()
            return createValid(locator.page(), EditLaUserPage::class)
        }
    }
}
