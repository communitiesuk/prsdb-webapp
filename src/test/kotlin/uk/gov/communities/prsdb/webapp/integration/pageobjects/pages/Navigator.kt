package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.landlordRegistrationJourneyPages.EmailFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.landlordRegistrationJourneyPages.PhoneNumberFormPageLandlordRegistration

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

    fun goToLandlordRegistrationEmailFormPage(): EmailFormPageLandlordRegistration {
        navigate("register-as-a-landlord/email")
        return BasePage.createValid(page, EmailFormPageLandlordRegistration::class)
    }

    fun goToLandlordRegistrationPhoneNumberFormPage(): PhoneNumberFormPageLandlordRegistration {
        navigate("register-as-a-landlord/phone-number")
        return BasePage.createValid(page, PhoneNumberFormPageLandlordRegistration::class)
    }

    private fun navigate(path: String) {
        page.navigate("http://localhost:$port/$path")
    }
}
