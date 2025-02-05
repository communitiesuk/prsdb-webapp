package uk.gov.communities.prsdb.webapp.integration.pageObjects

import com.microsoft.playwright.Page
import com.microsoft.playwright.Response
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.REGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordType
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ErrorPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.InviteNewLaUserPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LocalAuthorityViewLandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLaUsersPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLocalAuthorityView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.SearchLandlordRegisterPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.createValidPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages.CheckAnswersPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages.EmailFormPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages.LandingPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages.NameFormPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages.SuccessPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.CheckAnswersPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.ConfirmIdentityFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.CountryOfResidenceFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.DateOfBirthFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.DeclarationFormPageLandlordRegistration
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
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CheckAnswersPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.DeclarationFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.HmoAdditionalLicenceFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.HmoMandatoryLicenceFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.HouseholdsFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.LandlordTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.LicensingTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.LookupAddressFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.ManualAddressFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.OccupancyFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.OwnershipTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.PeopleFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.PropertyTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.RegisterPropertyStartPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SelectAddressFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SelectLocalAuthorityFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SelectiveLicenceFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.models.formModels.VerifiedIdentityModel
import uk.gov.communities.prsdb.webapp.services.OneLoginIdentityService
import java.time.LocalDate
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.CONFIRMATION_PAGE_PATH_SEGMENT as LANDLORD_CONFIRMATION
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController.Companion.CONFIRMATION_PAGE_PATH_SEGMENT as PROPERTY_CONFIRMATION

class Navigator(
    private val page: Page,
    private val port: Int,
    private val identityService: OneLoginIdentityService,
) {
    fun goToManageLaUsers(authorityId: Int): ManageLaUsersPage {
        navigate("local-authority/$authorityId/manage-users")
        return createValidPage(page, ManageLaUsersPage::class)
    }

    fun goToInviteNewLaUser(authorityId: Int): InviteNewLaUserPage {
        navigate("local-authority/$authorityId/invite-new-user")
        return createValidPage(page, InviteNewLaUserPage::class)
    }

    fun goToSearchLandlordRegister(): SearchLandlordRegisterPage {
        navigate("search/landlord")
        return createValidPage(page, SearchLandlordRegisterPage::class)
    }

    fun goToLandlordRegistrationConfirmIdentityFormPage(): ConfirmIdentityFormPageLandlordRegistration {
        val verifiedIdentityMap =
            mutableMapOf<String, Any?>(
                VerifiedIdentityModel.NAME_KEY to "Arthur Dent",
                VerifiedIdentityModel.BIRTH_DATE_KEY to LocalDate.of(2000, 6, 8),
            )
        whenever(identityService.getVerifiedIdentityData(any())).thenReturn(verifiedIdentityMap)

        navigate("$REGISTER_LANDLORD_JOURNEY_URL/${LandlordRegistrationStepId.VerifyIdentity.urlPathSegment}")
        return createValidPage(page, ConfirmIdentityFormPageLandlordRegistration::class)
    }

    fun goToLandlordRegistrationNameFormPage(): NameFormPageLandlordRegistration {
        whenever(identityService.getVerifiedIdentityData(any())).thenReturn(null)

        navigate("$REGISTER_LANDLORD_JOURNEY_URL/${LandlordRegistrationStepId.Name.urlPathSegment}")
        return createValidPage(page, NameFormPageLandlordRegistration::class)
    }

    fun goToLandlordRegistrationDateOfBirthFormPage(): DateOfBirthFormPageLandlordRegistration {
        val nameFormPage = goToLandlordRegistrationNameFormPage()
        nameFormPage.nameInput.fill("Arthur Dent")
        nameFormPage.form.submit()
        val dateOfBirthFormPage = createValidPage(page, DateOfBirthFormPageLandlordRegistration::class)
        return dateOfBirthFormPage
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
        lookupAddressPage.houseNameOrNumberInput.fill("1")
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
        countryOfResidencePage.select.autocompleteInput.fill("Zimbabwe")
        countryOfResidencePage.select.selectValue("Zimbabwe")
        countryOfResidencePage.form.submit()
        return createValidPage(page, InternationalAddressFormPageLandlordRegistration::class)
    }

    fun goToLandlordRegistrationLookupContactAddressPage(): LookupContactAddressFormPageLandlordRegistration {
        val internationalAddressPage = goToLandlordRegistrationInternationalAddressPage()
        internationalAddressPage.textAreaInput.fill("international address")
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

    fun goToLandlordRegistrationCheckAnswersPage(
        livesInUK: Boolean = true,
        isManualAddressChosen: Boolean = false,
    ): CheckAnswersPageLandlordRegistration {
        if (isManualAddressChosen) {
            val manualAddressPage =
                if (livesInUK) goToLandlordRegistrationManualAddressPage() else goToLandlordRegistrationManualContactAddressPage()
            manualAddressPage.addressLineOneInput.fill("1 Example Road")
            manualAddressPage.townOrCityInput.fill("Townville")
            manualAddressPage.postcodeInput.fill("EG1 2AB")
            manualAddressPage.form.submit()
            return createValidPage(page, CheckAnswersPageLandlordRegistration::class)
        } else {
            val selectAddressPage =
                if (livesInUK) goToLandlordRegistrationSelectAddressPage() else goToLandlordRegistrationSelectContactAddressPage()
            selectAddressPage.radios.selectValue("1, Example Road, EG1 2AB")
            selectAddressPage.form.submit()
            return createValidPage(page, CheckAnswersPageLandlordRegistration::class)
        }
    }

    fun goToLandlordRegistrationDeclarationPage(): DeclarationFormPageLandlordRegistration {
        val checkAnswersPage = goToLandlordRegistrationCheckAnswersPage()
        checkAnswersPage.form.submit()
        return createValidPage(page, DeclarationFormPageLandlordRegistration::class)
    }

    fun skipToLandlordRegistrationConfirmationPage(): ErrorPage {
        navigate("$REGISTER_LANDLORD_JOURNEY_URL/$LANDLORD_CONFIRMATION")
        return createValidPage(page, ErrorPage::class)
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

    fun goToLaUserRegistrationCheckAnswersPage(): CheckAnswersPageLaUserRegistration {
        val emailPage = goToLaUserRegistrationEmailFormPage()
        emailPage.emailInput.fill("test.user@example.com")
        emailPage.form.submit()
        val checkAnswersPage = createValidPage(page, CheckAnswersPageLaUserRegistration::class)
        return checkAnswersPage
    }

    fun goToLaUserRegistrationSuccessPage(): SuccessPageLaUserRegistration {
        val checkAnswersPage = goToLaUserRegistrationCheckAnswersPage()
        checkAnswersPage.form.submit()
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

    fun goToPropertyRegistrationLookupAddressPage(): LookupAddressFormPagePropertyRegistration {
        navigate("register-property/lookup-address")
        return createValidPage(page, LookupAddressFormPagePropertyRegistration::class)
    }

    fun goToPropertyRegistrationSelectAddressPage(): SelectAddressFormPagePropertyRegistration {
        val addressLookupPage = goToPropertyRegistrationLookupAddressPage()
        addressLookupPage.postcodeInput.fill("EG1 2AB")
        addressLookupPage.houseNameOrNumberInput.fill("1")
        addressLookupPage.form.submit()
        return createValidPage(page, SelectAddressFormPagePropertyRegistration::class)
    }

    fun goToPropertyRegistrationManualAddressPage(): ManualAddressFormPagePropertyRegistration {
        val addressSelectPage = goToPropertyRegistrationSelectAddressPage()
        addressSelectPage.radios.selectValue(MANUAL_ADDRESS_CHOSEN)
        addressSelectPage.form.submit()
        return createValidPage(page, ManualAddressFormPagePropertyRegistration::class)
    }

    fun goToPropertyRegistrationSelectLocalAuthorityPage(): SelectLocalAuthorityFormPagePropertyRegistration {
        val manualAddressPage = goToPropertyRegistrationManualAddressPage()
        manualAddressPage.addressLineOneInput.fill("Test address line 1")
        manualAddressPage.townOrCityInput.fill("Testville")
        manualAddressPage.postcodeInput.fill("EG1 2AB")
        manualAddressPage.form.submit()
        return createValidPage(page, SelectLocalAuthorityFormPagePropertyRegistration::class)
    }

    fun goToPropertyRegistrationPropertyTypePage(): PropertyTypeFormPagePropertyRegistration {
        val selectAddressPage = goToPropertyRegistrationSelectAddressPage()
        selectAddressPage.radios.selectValue("1, Example Road, EG1 2AB")
        selectAddressPage.form.submit()
        return createValidPage(page, PropertyTypeFormPagePropertyRegistration::class)
    }

    fun goToPropertyRegistrationOwnershipTypePage(): OwnershipTypeFormPagePropertyRegistration {
        val propertyTypePage = goToPropertyRegistrationPropertyTypePage()
        propertyTypePage.form.getRadios().selectValue(PropertyType.DETACHED_HOUSE)
        propertyTypePage.form.submit()
        return createValidPage(page, OwnershipTypeFormPagePropertyRegistration::class)
    }

    fun goToPropertyRegistrationLicensingTypePage(): LicensingTypeFormPagePropertyRegistration {
        val ownershipTypePage = goToPropertyRegistrationOwnershipTypePage()
        ownershipTypePage.form.getRadios().selectValue(OwnershipType.FREEHOLD)
        ownershipTypePage.form.submit()
        return createValidPage(page, LicensingTypeFormPagePropertyRegistration::class)
    }

    fun goToPropertyRegistrationSelectiveLicencePage(): SelectiveLicenceFormPagePropertyRegistration {
        val licensingTypePage = goToPropertyRegistrationLicensingTypePage()
        licensingTypePage.form.getRadios().selectValue(LicensingType.SELECTIVE_LICENCE)
        licensingTypePage.form.submit()
        return createValidPage(page, SelectiveLicenceFormPagePropertyRegistration::class)
    }

    fun goToPropertyRegistrationHmoMandatoryLicencePage(): HmoMandatoryLicenceFormPagePropertyRegistration {
        val licensingTypePage = goToPropertyRegistrationLicensingTypePage()
        licensingTypePage.form.getRadios().selectValue(LicensingType.HMO_MANDATORY_LICENCE)
        licensingTypePage.form.submit()
        return createValidPage(page, HmoMandatoryLicenceFormPagePropertyRegistration::class)
    }

    fun goToPropertyRegistrationHmoAdditionalLicencePage(): HmoAdditionalLicenceFormPagePropertyRegistration {
        val licensingTypePage = goToPropertyRegistrationLicensingTypePage()
        licensingTypePage.form.getRadios().selectValue(LicensingType.HMO_ADDITIONAL_LICENCE)
        licensingTypePage.form.submit()
        return createValidPage(page, HmoAdditionalLicenceFormPagePropertyRegistration::class)
    }

    fun goToPropertyRegistrationOccupancyPage(): OccupancyFormPagePropertyRegistration {
        val licensingTypePage = goToPropertyRegistrationLicensingTypePage()
        licensingTypePage.form.getRadios().selectValue(LicensingType.NO_LICENSING)
        licensingTypePage.form.submit()
        return createValidPage(page, OccupancyFormPagePropertyRegistration::class)
    }

    fun goToPropertyRegistrationHouseholdsPage(): HouseholdsFormPagePropertyRegistration {
        val occupancyPage = goToPropertyRegistrationOccupancyPage()
        occupancyPage.form.getRadios().selectValue("true")
        occupancyPage.form.submit()
        return createValidPage(page, HouseholdsFormPagePropertyRegistration::class)
    }

    fun goToPropertyRegistrationPeoplePage(): PeopleFormPagePropertyRegistration {
        val householdsPage = goToPropertyRegistrationHouseholdsPage()
        householdsPage.householdsInput.fill("2")
        householdsPage.form.submit()
        return createValidPage(page, PeopleFormPagePropertyRegistration::class)
    }

    fun goToPropertyRegistrationLandlordTypePage(): LandlordTypeFormPagePropertyRegistration {
        val peoplePage = goToPropertyRegistrationPeoplePage()
        peoplePage.peopleInput.fill("4")
        peoplePage.form.submit()
        return createValidPage(page, LandlordTypeFormPagePropertyRegistration::class)
    }

    fun goToPropertyRegistrationCheckAnswersPage(): CheckAnswersPagePropertyRegistration {
        val landlordTypePage = goToPropertyRegistrationLandlordTypePage()
        landlordTypePage.form.getRadios().selectValue(LandlordType.SOLE)
        landlordTypePage.form.submit()
        return createValidPage(page, CheckAnswersPagePropertyRegistration::class)
    }

    fun goToPropertyRegistrationDeclarationPage(): DeclarationFormPagePropertyRegistration {
        val checkAnswersPage = goToPropertyRegistrationCheckAnswersPage()
        checkAnswersPage.form.submit()
        return createValidPage(page, DeclarationFormPagePropertyRegistration::class)
    }

    fun skipToPropertyRegistrationConfirmationPage(): ErrorPage {
        navigate("$REGISTER_PROPERTY_JOURNEY_URL/$PROPERTY_CONFIRMATION")
        return createValidPage(page, ErrorPage::class)
    }

    fun goToLandlordDetails(): LandlordDetailsPage {
        navigate("landlord-details")
        return createValidPage(page, LandlordDetailsPage::class)
    }

    fun goToLandlordDetailsAsALocalAuthorityUser(id: Long): LocalAuthorityViewLandlordDetailsPage {
        navigate("landlord-details/$id")
        return createValidPage(page, LocalAuthorityViewLandlordDetailsPage::class)
    }

    fun goToPropertyDetailsLandlordView(id: Long): PropertyDetailsPageLandlordView {
        navigate("property-details/$id")
        return createValidPage(page, PropertyDetailsPageLandlordView::class)
    }

    fun goToPropertyDetailsLocalAuthorityView(id: Long): PropertyDetailsPageLocalAuthorityView {
        navigate("local-authority/property-details/$id")
        return createValidPage(page, PropertyDetailsPageLocalAuthorityView::class)
    }

    fun navigate(path: String): Response? = page.navigate("http://localhost:$port/$path")
}
