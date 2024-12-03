package uk.gov.communities.prsdb.webapp.integration.pageObjects

import com.microsoft.playwright.Page
import com.microsoft.playwright.Response
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.InviteNewLaUserPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLaUsersPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.createValidPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages.EmailFormPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages.LandingPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages.NameFormPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages.SuccessPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages.SummaryPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.ConfirmIdentityFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.CountryOfResidenceFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.DateOfBirthFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.EmailFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.InternationalAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.LookupAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.LookupContactAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.ManualAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.ManualContactAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.NameFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.PhoneNumberFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.SelectAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.SelectContactAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.OwnershipTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.PropertyTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.RegisterPropertyStartPage

class Navigator(
    private val page: Page,
    private val port: Int,
) {
    fun goToManageLaUsers(authorityId: Int): ManageLaUsersPage {
        navigate("local-authority/$authorityId/manage-users")?.url()
        return createValidPage(page, ManageLaUsersPage::class)
    }

    fun goToInviteNewLaUser(authorityId: Int): InviteNewLaUserPage {
        navigate("local-authority/$authorityId/invite-new-user")
        return createValidPage(page, InviteNewLaUserPage::class)
    }

    fun goToLandlordRegistrationNameFormPage(): NameFormPageLandlordRegistration {
        navigate("register-as-a-landlord/name")
        return createValidPage(page, NameFormPageLandlordRegistration::class)
    }

    fun goToLandlordRegistrationDateOfBirthFormPage(): DateOfBirthFormPageLandlordRegistration {
        val nameFormPage = goToLandlordRegistrationNameFormPage()
        nameFormPage.nameInput.fill("Arthur Dent")
        nameFormPage.form.submit()
        val dateOfBirthFormPage = createValidPage(page, DateOfBirthFormPageLandlordRegistration::class)
        return dateOfBirthFormPage
    }

    fun goToLandlordRegistrationConfirmIdentityFormPage(): ConfirmIdentityFormPageLandlordRegistration {
        navigate("register-as-a-landlord/confirm-identity")
        return createValidPage(page, ConfirmIdentityFormPageLandlordRegistration::class)
    }

    fun goToLandlordRegistrationEmailFormPage(): EmailFormPageLandlordRegistration {
        val dateOfBirthFormPage = goToLandlordRegistrationDateOfBirthFormPage()
        dateOfBirthFormPage.dayInput.fill("8")
        dateOfBirthFormPage.monthInput.fill("6")
        dateOfBirthFormPage.yearInput.fill("2000")
        dateOfBirthFormPage.form.submit()
        val emailFormPage = createValidPage(page, EmailFormPageLandlordRegistration::class)
        return emailFormPage
    }

    fun goToLandlordRegistrationPhoneNumberFormPage(): PhoneNumberFormPageLandlordRegistration {
        val emailFormPage = goToLandlordRegistrationEmailFormPage()
        emailFormPage.emailInput.fill("test@example.com")
        emailFormPage.form.submit()
        val phoneNumberPage = createValidPage(page, PhoneNumberFormPageLandlordRegistration::class)
        return phoneNumberPage
    }

    fun goToLandlordRegistrationCountryOfResidencePage(): CountryOfResidenceFormPageLandlordRegistration {
        val phoneNumberPage = goToLandlordRegistrationPhoneNumberFormPage()
        phoneNumberPage.phoneNumberInput.fill("07456097576")
        phoneNumberPage.form.submit()
        return createValidPage(page, CountryOfResidenceFormPageLandlordRegistration::class)
    }

    fun goToLandlordRegistrationLookupAddressPage(): LookupAddressFormPageLandlordRegistration {
        val countryOfResidencePage = goToLandlordRegistrationCountryOfResidencePage()
        countryOfResidencePage.radios.selectValue("true")
        countryOfResidencePage.form.submit()
        return createValidPage(page, LookupAddressFormPageLandlordRegistration::class)
    }

    fun goToLandlordRegistrationSelectAddressPage(): SelectAddressFormPageLandlordRegistration {
        val lookupAddressPage = goToLandlordRegistrationLookupAddressPage()
        lookupAddressPage.postcodeInput.fill("EG")
        lookupAddressPage.houseNameOrNumberInput.fill("5")
        lookupAddressPage.form.submit()
        return createValidPage(page, SelectAddressFormPageLandlordRegistration::class)
    }

    fun goToLandlordRegistrationManualAddressPage(): ManualAddressFormPageLandlordRegistration {
        val selectAddressPage = goToLandlordRegistrationSelectAddressPage()
        selectAddressPage.radios.selectValue(MANUAL_ADDRESS_CHOSEN)
        selectAddressPage.form.submit()
        return createValidPage(page, ManualAddressFormPageLandlordRegistration::class)
    }

    fun goToLandlordRegistrationInternationalAddressPage(): InternationalAddressFormPageLandlordRegistration {
        val countryOfResidencePage = goToLandlordRegistrationCountryOfResidencePage()
        countryOfResidencePage.radios.selectValue("false")
        countryOfResidencePage.select.autocompleteInput.fill("France")
        countryOfResidencePage.select.selectValue("France")
        countryOfResidencePage.form.submit()
        return createValidPage(page, InternationalAddressFormPageLandlordRegistration::class)
    }

    fun goToLandlordRegistrationLookupContactAddressPage(): LookupContactAddressFormPageLandlordRegistration {
        val internationalAddressPage = goToLandlordRegistrationInternationalAddressPage()
        internationalAddressPage.textAreaInput.fill("address")
        internationalAddressPage.form.submit()
        return createValidPage(page, LookupContactAddressFormPageLandlordRegistration::class)
    }

    fun goToLandlordRegistrationSelectContactAddressPage(): SelectContactAddressFormPageLandlordRegistration {
        val lookupContactAddressPage = goToLandlordRegistrationLookupContactAddressPage()
        lookupContactAddressPage.postcodeInput.fill("EG")
        lookupContactAddressPage.houseNameOrNumberInput.fill("5")
        lookupContactAddressPage.form.submit()
        return createValidPage(page, SelectContactAddressFormPageLandlordRegistration::class)
    }

    fun goToLandlordRegistrationManualContactAddressPage(): ManualContactAddressFormPageLandlordRegistration {
        val selectAddressPage = goToLandlordRegistrationSelectContactAddressPage()
        selectAddressPage.radios.selectValue(MANUAL_ADDRESS_CHOSEN)
        selectAddressPage.form.submit()
        return createValidPage(page, ManualContactAddressFormPageLandlordRegistration::class)
    }

    fun goToLaUserRegistrationLandingPage(): LandingPageLaUserRegistration {
        navigate("register-local-authority-user/landing-page")
        return createValidPage(page, LandingPageLaUserRegistration::class)
    }

    fun goToLaUserRegistrationNameFormPage(): NameFormPageLaUserRegistration {
        val landingPage = goToLaUserRegistrationLandingPage()
        landingPage.clickBeginButton()
        val namePage = createValidPage(page, NameFormPageLaUserRegistration::class)
        return namePage
    }

    fun goToLaUserRegistrationEmailFormPage(): EmailFormPageLaUserRegistration {
        val namePage = goToLaUserRegistrationNameFormPage()
        namePage.nameInput.fill("Test user")
        namePage.form.submit()
        val emailPage = createValidPage(page, EmailFormPageLaUserRegistration::class)
        return emailPage
    }

    fun goToLaUserRegistrationCheckAnswersPage(): SummaryPageLaUserRegistration {
        val emailPage = goToLaUserRegistrationEmailFormPage()
        emailPage.emailInput.fill("test.user@example.com")
        emailPage.form.submit()
        val checkAnswersPage = createValidPage(page, SummaryPageLaUserRegistration::class)
        return checkAnswersPage
    }

    fun goToLaUserRegistrationSuccessPage(): SuccessPageLaUserRegistration {
        val checkAnswersPage = goToLaUserRegistrationCheckAnswersPage()
        checkAnswersPage.submit()
        val successPage = createValidPage(page, SuccessPageLaUserRegistration::class)
        return successPage
    }

    fun skipToLaUserRegistrationSuccessPage(): SuccessPageLaUserRegistration {
        navigate("register-local-authority-user/success")
        return createValidPage(page, SuccessPageLaUserRegistration::class)
    }

    fun goToPropertyRegistrationStartPage(): RegisterPropertyStartPage {
        navigate("register-property")
        return createValidPage(page, RegisterPropertyStartPage::class)
    }

    fun goToPropertyRegistrationPropertyTypePage(): PropertyTypeFormPagePropertyRegistration {
        navigate("register-property/property-type")
        return createValidPage(page, PropertyTypeFormPagePropertyRegistration::class)
    }

    fun goToPropertyRegistrationOwnershipTypePage(): OwnershipTypeFormPagePropertyRegistration {
        val propertyTypePage = goToPropertyRegistrationPropertyTypePage()
        propertyTypePage.form.getRadios().selectValue(PropertyType.DETACHED_HOUSE)
        propertyTypePage.form.submit()
        return createValidPage(page, OwnershipTypeFormPagePropertyRegistration::class)
    }

    private fun navigate(path: String): Response? = page.navigate("http://localhost:$port/$path")
}
