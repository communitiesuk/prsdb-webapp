package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.laUserRegistrationJourneyPages.EmailFormPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.laUserRegistrationJourneyPages.NameFormPageLaUserRegistration
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

    fun goToLaUserRegistrationNameFormPage(): NameFormPageLaUserRegistration {
        navigate("register-local-authority-user/name")
        return BasePage.createValid(page, NameFormPageLaUserRegistration::class)
    }

    private fun completeLaUserRegistrationNameStep(): EmailFormPageLaUserRegistration {
        val namePage = goToLaUserRegistrationNameFormPage()
        namePage.fillName("Test user")
        return namePage.submit()
    }

    fun goToLaUserRegistrationEmailFormPage(): EmailFormPageLaUserRegistration = completeLaUserRegistrationNameStep()

    fun skipToLaUserRegistrationEmailFormPage(): NameFormPageLaUserRegistration {
        navigate("register-local-authority-user/email")
        return BasePage.createValid(page, NameFormPageLaUserRegistration::class)
    }

    private fun navigate(path: String) {
        page.navigate("http://localhost:$port/$path")
    }
}
