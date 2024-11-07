package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType

class Navigator(
    private val page: Page,
    private val port: Int,
) {
    fun goToInviteNewLaUser(authorityId: Int): InviteNewLaUserPage {
        navigate("local-authority/$authorityId/invite-new-user")
        return BasePage.createValid(page, InviteNewLaUserPage::class)
    }

    fun goToManageLaUsers(authorityId: Int): ManageLaUsersPage {
        navigate("local-authority/$authorityId/manage-users")
        return BasePage.createValid(page, ManageLaUsersPage::class)
    }

    fun goToEditLaUser(
        authorityId: Int,
        userId: Int,
    ): EditLaUserPage {
        navigate("local-authority/$authorityId/edit-user/$userId")
        return BasePage.createValid(page, EditLaUserPage::class)
    }

    fun goToEmailFormPage(journeyType: JourneyType): EmailFormPage {
        navigate("${journeyType.urlPathSegment}/email")
        return BasePage.createValid<EmailFormPage>(page)
    }

    fun goToPhoneNumberFormPage(journeyType: JourneyType): PhoneNumberFormPage {
        navigate("${journeyType.urlPathSegment}/phone-number")
        return BasePage.createValid<PhoneNumberFormPage>(page)
    }

    private fun navigate(path: String) {
        page.navigate("http://localhost:$port/$path")
    }
}
