package uk.gov.communities.prsdb.webapp.integration.pageObjects

import com.microsoft.playwright.Page
import com.microsoft.playwright.Response
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.DETAILS_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LA_USER_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.REGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ErrorPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.InviteNewLaUserPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordUpdateDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LocalAuthorityViewLandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLaUsersPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLocalAuthorityView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.SearchLandlordRegisterPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.SearchPropertyRegisterPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.createValidPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages.CheckAnswersPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages.EmailFormPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages.LandingPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages.NameFormPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.CheckAnswersPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.ConfirmIdentityFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.CountryOfResidenceFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.DateOfBirthFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.DeclarationFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.EmailFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.LookupAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.LookupContactAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.ManualAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.ManualContactAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.NameFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.NonEnglandOrWalesAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.PhoneNumberFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.SelectAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.SelectContactAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CheckAnswersPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.DeclarationFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.HmoAdditionalLicenceFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.HmoMandatoryLicenceFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.HouseholdsFormPagePropertyRegistration
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
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.TaskListPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.VerifiedIdentityModel
import uk.gov.communities.prsdb.webapp.services.OneLoginIdentityService
import java.time.LocalDate
import uk.gov.communities.prsdb.webapp.controllers.RegisterLAUserController.Companion.CONFIRMATION_PAGE_PATH_SEGMENT as LA_CONFIRMATION
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.CONFIRMATION_PAGE_PATH_SEGMENT as LANDLORD_CONFIRMATION
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController.Companion.CONFIRMATION_PAGE_PATH_SEGMENT as PROPERTY_CONFIRMATION

class Navigator(
    private val page: Page,
    private val port: Int,
    private val identityService: OneLoginIdentityService,
) {
    fun goToManageLaUsers(authorityId: Int): ManageLaUsersPage {
        navigate("/local-authority/$authorityId/manage-users")
        return createValidPage(page, ManageLaUsersPage::class)
    }

    fun goToInviteNewLaUser(authorityId: Int): InviteNewLaUserPage {
        navigate("/local-authority/$authorityId/invite-new-user")
        return createValidPage(page, InviteNewLaUserPage::class)
    }

    fun goToLandlordSearchPage(): SearchLandlordRegisterPage {
        navigate("/search/landlord")
        return createValidPage(page, SearchLandlordRegisterPage::class)
    }

    fun goToPropertySearchPage(): SearchPropertyRegisterPage {
        navigate("/search/property")
        return createValidPage(page, SearchPropertyRegisterPage::class)
    }

    fun goToLandlordRegistrationConfirmIdentityFormPage(): ConfirmIdentityFormPageLandlordRegistration {
        val verifiedIdentityMap =
            mutableMapOf<String, Any?>(
                VerifiedIdentityModel.NAME_KEY to "Arthur Dent",
                VerifiedIdentityModel.BIRTH_DATE_KEY to LocalDate.of(2000, 6, 8),
            )
        whenever(identityService.getVerifiedIdentityData(any())).thenReturn(verifiedIdentityMap)

        navigate("/$REGISTER_LANDLORD_JOURNEY_URL/${LandlordRegistrationStepId.VerifyIdentity.urlPathSegment}")
        return createValidPage(page, ConfirmIdentityFormPageLandlordRegistration::class)
    }

    fun goToLandlordRegistrationNameFormPage(): NameFormPageLandlordRegistration {
        whenever(identityService.getVerifiedIdentityData(any())).thenReturn(null)

        navigate("/$REGISTER_LANDLORD_JOURNEY_URL/${LandlordRegistrationStepId.Name.urlPathSegment}")
        return createValidPage(page, NameFormPageLandlordRegistration::class)
    }

    fun goToLandlordRegistrationDateOfBirthFormPage(): DateOfBirthFormPageLandlordRegistration {
        val nameFormPage = goToLandlordRegistrationNameFormPage()
        nameFormPage.submitName("Arthur Dent")
        val dateOfBirthFormPage = createValidPage(page, DateOfBirthFormPageLandlordRegistration::class)
        return dateOfBirthFormPage
    }

    fun goToLandlordRegistrationEmailFormPage(): EmailFormPageLandlordRegistration {
        val dateOfBirthFormPage = goToLandlordRegistrationDateOfBirthFormPage()
        dateOfBirthFormPage.submitDateOfBirth("8", "6", "2000")
        val emailFormPage = createValidPage(page, EmailFormPageLandlordRegistration::class)
        return emailFormPage
    }

    fun goToLandlordRegistrationPhoneNumberFormPage(): PhoneNumberFormPageLandlordRegistration {
        val emailFormPage = goToLandlordRegistrationEmailFormPage()
        emailFormPage.submitEmail("test@example.com")
        val phoneNumberPage = createValidPage(page, PhoneNumberFormPageLandlordRegistration::class)
        return phoneNumberPage
    }

    fun goToLandlordRegistrationCountryOfResidencePage(): CountryOfResidenceFormPageLandlordRegistration {
        val phoneNumberPage = goToLandlordRegistrationPhoneNumberFormPage()
        phoneNumberPage.submitPhoneNumber("07456097576")
        return createValidPage(page, CountryOfResidenceFormPageLandlordRegistration::class)
    }

    fun goToLandlordRegistrationLookupAddressPage(): LookupAddressFormPageLandlordRegistration {
        val countryOfResidencePage = goToLandlordRegistrationCountryOfResidencePage()
        countryOfResidencePage.submitUk()
        return createValidPage(page, LookupAddressFormPageLandlordRegistration::class)
    }

    fun goToLandlordRegistrationSelectAddressPage(): SelectAddressFormPageLandlordRegistration {
        val lookupAddressPage = goToLandlordRegistrationLookupAddressPage()
        lookupAddressPage.submitPostcodeAndBuildingNameOrNumber("EG", "1")
        return createValidPage(page, SelectAddressFormPageLandlordRegistration::class)
    }

    fun goToLandlordRegistrationManualAddressPage(): ManualAddressFormPageLandlordRegistration {
        val selectAddressPage = goToLandlordRegistrationSelectAddressPage()
        selectAddressPage.selectAddressAndSubmit(MANUAL_ADDRESS_CHOSEN)
        return createValidPage(page, ManualAddressFormPageLandlordRegistration::class)
    }

    fun goToLandlordRegistrationNonEnglandOrWalesAddressPage(): NonEnglandOrWalesAddressFormPageLandlordRegistration {
        val countryOfResidencePage = goToLandlordRegistrationCountryOfResidencePage()
        countryOfResidencePage.submitNonUkFromPartial("Zimbabwe", "Zimbabwe")
        return createValidPage(page, NonEnglandOrWalesAddressFormPageLandlordRegistration::class)
    }

    fun goToLandlordRegistrationLookupContactAddressPage(): LookupContactAddressFormPageLandlordRegistration {
        val nonEnglandOrWalesAddressPage = goToLandlordRegistrationNonEnglandOrWalesAddressPage()
        nonEnglandOrWalesAddressPage.submitAddress("test address")
        return createValidPage(page, LookupContactAddressFormPageLandlordRegistration::class)
    }

    fun goToLandlordRegistrationSelectContactAddressPage(): SelectContactAddressFormPageLandlordRegistration {
        val lookupContactAddressPage = goToLandlordRegistrationLookupContactAddressPage()
        lookupContactAddressPage.submitPostcodeAndBuildingNameOrNumber("EG", "5")
        return createValidPage(page, SelectContactAddressFormPageLandlordRegistration::class)
    }

    fun goToLandlordRegistrationManualContactAddressPage(): ManualContactAddressFormPageLandlordRegistration {
        val selectAddressPage = goToLandlordRegistrationSelectContactAddressPage()
        selectAddressPage.selectAddressAndSubmit(MANUAL_ADDRESS_CHOSEN)
        return createValidPage(page, ManualContactAddressFormPageLandlordRegistration::class)
    }

    fun goToLandlordRegistrationCheckAnswersPage(
        livesInEnglandOrWales: Boolean = true,
        isManualAddressChosen: Boolean = false,
    ): CheckAnswersPageLandlordRegistration {
        if (isManualAddressChosen) {
            val manualAddressPage =
                if (livesInEnglandOrWales) {
                    goToLandlordRegistrationManualAddressPage()
                } else {
                    goToLandlordRegistrationManualContactAddressPage()
                }
            manualAddressPage.submitAddress(
                addressLineOne = "1 Example Road",
                townOrCity = "Townville",
                postcode = "EG1 2AB",
            )
            return createValidPage(page, CheckAnswersPageLandlordRegistration::class)
        } else {
            val selectAddressPage =
                if (livesInEnglandOrWales) {
                    goToLandlordRegistrationSelectAddressPage()
                } else {
                    goToLandlordRegistrationSelectContactAddressPage()
                }
            selectAddressPage.selectAddressAndSubmit("1, Example Road, EG1 2AB")
            return createValidPage(page, CheckAnswersPageLandlordRegistration::class)
        }
    }

    fun goToLandlordRegistrationDeclarationPage(): DeclarationFormPageLandlordRegistration {
        val checkAnswersPage = goToLandlordRegistrationCheckAnswersPage()
        checkAnswersPage.confirm()
        return createValidPage(page, DeclarationFormPageLandlordRegistration::class)
    }

    fun skipToLandlordRegistrationConfirmationPage(): ErrorPage {
        navigate("/$REGISTER_LANDLORD_JOURNEY_URL/$LANDLORD_CONFIRMATION")
        return createValidPage(page, ErrorPage::class)
    }

    fun goToLaUserRegistrationLandingPage(token: String): LandingPageLaUserRegistration {
        navigate("/$REGISTER_LA_USER_JOURNEY_URL?token=$token")
        return createValidPage(page, LandingPageLaUserRegistration::class)
    }

    fun goToLaUserRegistrationNameFormPage(token: String): NameFormPageLaUserRegistration {
        val landingPage = goToLaUserRegistrationLandingPage(token)
        landingPage.clickBeginButton()
        val namePage = createValidPage(page, NameFormPageLaUserRegistration::class)
        return namePage
    }

    fun goToLaUserRegistrationEmailFormPage(token: String): EmailFormPageLaUserRegistration {
        val namePage = goToLaUserRegistrationNameFormPage(token)
        namePage.submitName("Test user")
        val emailPage = createValidPage(page, EmailFormPageLaUserRegistration::class)
        return emailPage
    }

    fun goToLaUserRegistrationCheckAnswersPage(token: String): CheckAnswersPageLaUserRegistration {
        val emailPage = goToLaUserRegistrationEmailFormPage(token)
        emailPage.submitEmail("test.user@example.com")
        val checkAnswersPage = createValidPage(page, CheckAnswersPageLaUserRegistration::class)
        return checkAnswersPage
    }

    fun skipToLaUserRegistrationConfirmationPage(): ErrorPage {
        navigate("/$REGISTER_LA_USER_JOURNEY_URL/$LA_CONFIRMATION")
        return createValidPage(page, ErrorPage::class)
    }

    fun goToPropertyRegistrationStartPage(): RegisterPropertyStartPage {
        navigate("/register-property")
        return createValidPage(page, RegisterPropertyStartPage::class)
    }

    fun goToPropertyRegistrationTaskList(): TaskListPagePropertyRegistration {
        navigate("/register-property/task-list")
        return createValidPage(page, TaskListPagePropertyRegistration::class)
    }

    fun goToPropertyRegistrationLookupAddressPage(): LookupAddressFormPagePropertyRegistration {
        val taskListPage = goToPropertyRegistrationTaskList()
        taskListPage.clickRegisterTaskWithName("Add the property address")
        return createValidPage(page, LookupAddressFormPagePropertyRegistration::class)
    }

    fun goToPropertyRegistrationSelectAddressPage(): SelectAddressFormPagePropertyRegistration {
        val addressLookupPage = goToPropertyRegistrationLookupAddressPage()
        addressLookupPage.submitPostcodeAndBuildingNameOrNumber("EG1 2AB", "5")
        return createValidPage(page, SelectAddressFormPagePropertyRegistration::class)
    }

    fun goToPropertyRegistrationManualAddressPage(): ManualAddressFormPagePropertyRegistration {
        val addressSelectPage = goToPropertyRegistrationSelectAddressPage()
        addressSelectPage.selectAddressAndSubmit(MANUAL_ADDRESS_CHOSEN)
        return createValidPage(page, ManualAddressFormPagePropertyRegistration::class)
    }

    fun goToPropertyRegistrationSelectLocalAuthorityPage(): SelectLocalAuthorityFormPagePropertyRegistration {
        val manualAddressPage = goToPropertyRegistrationManualAddressPage()
        manualAddressPage.submitAddress(
            addressLineOne = "Test address line 1",
            townOrCity = "Testville",
            postcode = "EG1 2AB",
        )
        return createValidPage(page, SelectLocalAuthorityFormPagePropertyRegistration::class)
    }

    fun goToPropertyRegistrationPropertyTypePage(): PropertyTypeFormPagePropertyRegistration {
        val selectAddressPage = goToPropertyRegistrationSelectAddressPage()
        selectAddressPage.selectAddressAndSubmit("1, Example Road, EG1 2AB")
        return createValidPage(page, PropertyTypeFormPagePropertyRegistration::class)
    }

    fun goToPropertyRegistrationOwnershipTypePage(): OwnershipTypeFormPagePropertyRegistration {
        val propertyTypePage = goToPropertyRegistrationPropertyTypePage()
        propertyTypePage.submitPropertyType(PropertyType.DETACHED_HOUSE)
        return createValidPage(page, OwnershipTypeFormPagePropertyRegistration::class)
    }

    fun goToPropertyRegistrationLicensingTypePage(): LicensingTypeFormPagePropertyRegistration {
        val ownershipTypePage = goToPropertyRegistrationOwnershipTypePage()
        ownershipTypePage.submitOwnershipType(OwnershipType.FREEHOLD)
        return createValidPage(page, LicensingTypeFormPagePropertyRegistration::class)
    }

    fun goToPropertyRegistrationSelectiveLicencePage(): SelectiveLicenceFormPagePropertyRegistration {
        val licensingTypePage = goToPropertyRegistrationLicensingTypePage()
        licensingTypePage.submitLicensingType(LicensingType.SELECTIVE_LICENCE)
        return createValidPage(page, SelectiveLicenceFormPagePropertyRegistration::class)
    }

    fun goToPropertyRegistrationHmoMandatoryLicencePage(): HmoMandatoryLicenceFormPagePropertyRegistration {
        val licensingTypePage = goToPropertyRegistrationLicensingTypePage()
        licensingTypePage.submitLicensingType(LicensingType.HMO_MANDATORY_LICENCE)
        return createValidPage(page, HmoMandatoryLicenceFormPagePropertyRegistration::class)
    }

    fun goToPropertyRegistrationHmoAdditionalLicencePage(): HmoAdditionalLicenceFormPagePropertyRegistration {
        val licensingTypePage = goToPropertyRegistrationLicensingTypePage()
        licensingTypePage.submitLicensingType(LicensingType.HMO_ADDITIONAL_LICENCE)
        return createValidPage(page, HmoAdditionalLicenceFormPagePropertyRegistration::class)
    }

    fun goToPropertyRegistrationOccupancyPage(): OccupancyFormPagePropertyRegistration {
        val licensingTypePage = goToPropertyRegistrationLicensingTypePage()
        licensingTypePage.submitLicensingType(LicensingType.NO_LICENSING)
        return createValidPage(page, OccupancyFormPagePropertyRegistration::class)
    }

    fun goToPropertyRegistrationHouseholdsPage(): HouseholdsFormPagePropertyRegistration {
        val occupancyPage = goToPropertyRegistrationOccupancyPage()
        occupancyPage.submitIsOccupied()
        return createValidPage(page, HouseholdsFormPagePropertyRegistration::class)
    }

    fun goToPropertyRegistrationPeoplePage(): PeopleFormPagePropertyRegistration {
        val householdsPage = goToPropertyRegistrationHouseholdsPage()
        householdsPage.submitNumberOfHouseholds(2)
        return createValidPage(page, PeopleFormPagePropertyRegistration::class)
    }

    fun goToPropertyRegistrationCheckAnswersPage(): CheckAnswersPagePropertyRegistration {
        val peoplePage = goToPropertyRegistrationPeoplePage()
        peoplePage.submitNumOfPeople(4)
        return createValidPage(page, CheckAnswersPagePropertyRegistration::class)
    }

    fun goToPropertyRegistrationDeclarationPage(): DeclarationFormPagePropertyRegistration {
        val checkAnswersPage = goToPropertyRegistrationCheckAnswersPage()
        checkAnswersPage.confirm()
        return createValidPage(page, DeclarationFormPagePropertyRegistration::class)
    }

    fun skipToPropertyRegistrationConfirmationPage(): ErrorPage {
        navigate("/$REGISTER_PROPERTY_JOURNEY_URL/$PROPERTY_CONFIRMATION")
        return createValidPage(page, ErrorPage::class)
    }

    fun goToLandlordDetails(): LandlordDetailsPage {
        navigate("/landlord-details")
        return createValidPage(page, LandlordDetailsPage::class)
    }

    fun goToLandlordDetailsAsALocalAuthorityUser(id: Long): LocalAuthorityViewLandlordDetailsPage {
        navigate("/landlord-details/$id")
        return createValidPage(page, LocalAuthorityViewLandlordDetailsPage::class)
    }

    fun goToPropertyDetailsLandlordView(id: Long): PropertyDetailsPageLandlordView {
        navigate("/property-details/$id")
        return createValidPage(page, PropertyDetailsPageLandlordView::class)
    }

    fun goToPropertyDetailsLocalAuthorityView(id: Long): PropertyDetailsPageLocalAuthorityView {
        navigate("/local-authority/property-details/$id")
        return createValidPage(page, PropertyDetailsPageLocalAuthorityView::class)
    }

    fun goToUpdateLandlordDetailsPage(): LandlordUpdateDetailsPage {
        navigate("${LandlordDetailsController.UPDATE_ROUTE}/$DETAILS_PATH_SEGMENT")
        return createValidPage(page, LandlordUpdateDetailsPage::class)
    }

    fun navigate(path: String): Response? = page.navigate("http://localhost:$port$path")
}
