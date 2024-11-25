package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.Response
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages.EmailFormPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages.LandingPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages.NameFormPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.EmailFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.NameFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.PhoneNumberFormPageLandlordRegistration

class Navigator(
    private val page: Page,
    private val port: Int,
) {
    fun goToManageLaUsers(authorityId: Int): ManageLaUsersPage {
        navigate("local-authority/$authorityId/manage-users")?.url()
        return BasePage.createValidPage(page, ManageLaUsersPage::class)
    }

    fun goToInviteNewLaUser(authorityId: Int): InviteNewLaUserPage {
        navigate("local-authority/$authorityId/invite-new-user")
        return BasePage.createValidPage(page, InviteNewLaUserPage::class)
    }

    fun goToLandlordRegistrationNameFormPage(): NameFormPageLandlordRegistration {
        navigate("register-as-a-landlord/name")
        return BasePage.createValidPage(page, NameFormPageLandlordRegistration::class)
    }

    fun goToLandlordRegistrationEmailFormPage(): EmailFormPageLandlordRegistration {
        val nameFormPage = goToLandlordRegistrationNameFormPage()
        nameFormPage.nameInput.fill("Arthur Dent")
        val emailFormPage = nameFormPage.submitFormAndAssertNextPage()
        return emailFormPage
    }

    fun goToLandlordRegistrationPhoneNumberFormPage(): PhoneNumberFormPageLandlordRegistration {
        val emailFormPage = goToLandlordRegistrationEmailFormPage()
        emailFormPage.emailInput.fill("test@example.com")
        val phoneNumberPage = emailFormPage.submitFormAndAssertNextPage()
        return phoneNumberPage
    }

    fun goToLaUserRegistrationLandingPage(): LandingPageLaUserRegistration {
        navigate("register-local-authority-user/landing-page")
        return BasePage.createValidPage(page, LandingPageLaUserRegistration::class)
    }

    fun goToLaUserRegistrationNameFormPage(): NameFormPageLaUserRegistration {
        val landingPage = goToLaUserRegistrationLandingPage()
        val namePage = landingPage.clickBeginAndAssertNextPage()
        return namePage
    }

    fun goToLaUserRegistrationEmailFormPage(): EmailFormPageLaUserRegistration {
        val namePage = goToLaUserRegistrationNameFormPage()
        namePage.nameInput.fill("Test user")
        val emailPage = namePage.submitFormAndAssertNextPage()
        return emailPage
    }

    fun navigate(path: String): Response? = page.navigate("http://localhost:$port/$path")
}
