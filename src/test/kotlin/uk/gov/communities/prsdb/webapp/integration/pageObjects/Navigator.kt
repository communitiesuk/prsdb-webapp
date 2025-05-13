package uk.gov.communities.prsdb.webapp.integration.pageObjects

import com.microsoft.playwright.Page
import com.microsoft.playwright.Response
import com.microsoft.playwright.options.RequestOptions
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.CONTEXT_ID_URL_PARAMETER
import uk.gov.communities.prsdb.webapp.constants.DELETE_INCOMPLETE_PROPERTY_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.DEREGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.DEREGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LA_USER_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.REGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.SYSTEM_OPERATOR_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.TASK_LIST_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.INCOMPLETE_PROPERTIES_URL
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.controllers.LocalAuthorityDashboardController.Companion.LOCAL_AUTHORITY_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.LaUserRegistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.LandlordRegistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterLandlordStepId
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterPropertyStepId
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordDetailsUpdateStepId
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLaUserStepId
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsStepId
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.DeleteIncompletePropertyRegistrationAreYouSurePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ErrorPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.InviteLaAdminPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.InviteNewLaUserPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordIncompletePropertiesPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LocalAuthorityDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LocalAuthorityViewLandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LookupAddressFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLaUsersPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLocalAuthorityView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.SearchLandlordRegisterPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.SearchPropertyRegisterPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.SelectAddressFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.createValidPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages.CheckAnswersPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages.EmailFormPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages.LandingPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages.NameFormPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordDeregistrationJourneyPages.AreYouSureFormPageLandlordDeregistration
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
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.StartPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EicrExemptionMissingPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EicrExemptionOtherReasonPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EicrExemptionPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EicrExemptionReasonPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EicrIssueDatePagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EicrPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EicrUploadPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EpcExemptionReasonPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EpcPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafeEngineerNumPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyExemptionMissingPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyExemptionOtherReasonPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyExemptionPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyExemptionReasonPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyIssueDatePagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyUploadPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.StartPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDeregistrationJourneyPages.AreYouSureFormPagePropertyDeregistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDeregistrationJourneyPages.ReasonPagePropertyDeregistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CheckAnswersPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.DeclarationFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.HmoAdditionalLicenceFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.HmoMandatoryLicenceFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.LicensingTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.LookupAddressFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.ManualAddressFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.NumberOfHouseholdsFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.NumberOfPeopleFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.OccupancyFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.OwnershipTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.PropertyTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.RegisterPropertyStartPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SelectAddressFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SelectLocalAuthorityFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SelectiveLicenceFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.TaskListPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.testHelpers.api.controllers.SessionController
import uk.gov.communities.prsdb.webapp.testHelpers.api.requestModels.SetJourneyDataRequestModel
import uk.gov.communities.prsdb.webapp.testHelpers.api.requestModels.StoreInvitationTokenRequestModel
import uk.gov.communities.prsdb.webapp.testHelpers.builders.JourneyDataBuilder
import java.util.UUID
import kotlin.test.assertTrue

class Navigator(
    private val page: Page,
    private val port: Int,
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

    fun goToLandlordRegistrationStartPage(): StartPageLandlordRegistration {
        navigate("/register-as-a-landlord")
        return createValidPage(page, StartPageLandlordRegistration::class)
    }

    fun skipToLandlordRegistrationConfirmIdentityPage(): ConfirmIdentityFormPageLandlordRegistration {
        setJourneyDataInSession(
            LandlordRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyDataBuilder().withVerifiedUser().build(),
        )
        navigate("/$REGISTER_LANDLORD_JOURNEY_URL/${LandlordRegistrationStepId.ConfirmIdentity.urlPathSegment}")
        return createValidPage(page, ConfirmIdentityFormPageLandlordRegistration::class)
    }

    fun navigateToLandlordRegistrationVerifyIdentityPage() {
        navigate("/$REGISTER_LANDLORD_JOURNEY_URL/${LandlordRegistrationStepId.VerifyIdentity.urlPathSegment}")
    }

    fun skipToLandlordRegistrationNamePage(): NameFormPageLandlordRegistration {
        setJourneyDataInSession(
            LandlordRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyDataBuilder().withUnverifiedUser(name = null).build(),
        )
        navigate("/$REGISTER_LANDLORD_JOURNEY_URL/${LandlordRegistrationStepId.Name.urlPathSegment}")
        return createValidPage(page, NameFormPageLandlordRegistration::class)
    }

    fun skipToLandlordRegistrationDateOfBirthPage(): DateOfBirthFormPageLandlordRegistration {
        setJourneyDataInSession(
            LandlordRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyDataBuilder().withUnverifiedUser(dob = null).build(),
        )
        navigate("/$REGISTER_LANDLORD_JOURNEY_URL/${LandlordRegistrationStepId.DateOfBirth.urlPathSegment}")
        return createValidPage(page, DateOfBirthFormPageLandlordRegistration::class)
    }

    fun skipToLandlordRegistrationEmailPage(): EmailFormPageLandlordRegistration {
        setJourneyDataInSession(
            LandlordRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyDataBuilder().withVerifiedUser().build(),
        )
        navigate("/$REGISTER_LANDLORD_JOURNEY_URL/${LandlordRegistrationStepId.Email.urlPathSegment}")
        return createValidPage(page, EmailFormPageLandlordRegistration::class)
    }

    fun skipToLandlordRegistrationPhoneNumberPage(): PhoneNumberFormPageLandlordRegistration {
        setJourneyDataInSession(
            LandlordRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyDataBuilder()
                .withVerifiedUser()
                .withEmailAddress()
                .build(),
        )
        navigate("/$REGISTER_LANDLORD_JOURNEY_URL/${LandlordRegistrationStepId.PhoneNumber.urlPathSegment}")
        return createValidPage(page, PhoneNumberFormPageLandlordRegistration::class)
    }

    fun skipToLandlordRegistrationCountryOfResidencePage(): CountryOfResidenceFormPageLandlordRegistration {
        setJourneyDataInSession(
            LandlordRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyDataBuilder()
                .withVerifiedUser()
                .withEmailAddress()
                .withPhoneNumber()
                .build(),
        )
        navigate("/$REGISTER_LANDLORD_JOURNEY_URL/${LandlordRegistrationStepId.CountryOfResidence.urlPathSegment}")
        return createValidPage(page, CountryOfResidenceFormPageLandlordRegistration::class)
    }

    fun skipToLandlordRegistrationLookupAddressPage(): LookupAddressFormPageLandlordRegistration {
        setJourneyDataInSession(
            LandlordRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyDataBuilder()
                .withVerifiedUser()
                .withEmailAddress()
                .withPhoneNumber()
                .withEnglandOrWalesResidence()
                .build(),
        )
        navigate("/$REGISTER_LANDLORD_JOURNEY_URL/${LandlordRegistrationStepId.LookupAddress.urlPathSegment}")
        return createValidPage(page, LookupAddressFormPageLandlordRegistration::class)
    }

    fun skipToLandlordRegistrationSelectAddressPage(): SelectAddressFormPageLandlordRegistration {
        setJourneyDataInSession(
            LandlordRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyDataBuilder()
                .withVerifiedUser()
                .withEmailAddress()
                .withPhoneNumber()
                .withEnglandOrWalesResidence()
                .withLookupAddress()
                .withLookedUpAddresses()
                .build(),
        )
        navigate("/$REGISTER_LANDLORD_JOURNEY_URL/${LandlordRegistrationStepId.SelectAddress.urlPathSegment}")
        return createValidPage(page, SelectAddressFormPageLandlordRegistration::class)
    }

    fun skipToLandlordRegistrationManualAddressPage(): ManualAddressFormPageLandlordRegistration {
        setJourneyDataInSession(
            LandlordRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyDataBuilder()
                .withVerifiedUser()
                .withEmailAddress()
                .withPhoneNumber()
                .withEnglandOrWalesResidence()
                .withLookupAddress()
                .withLookedUpAddresses()
                .withManualAddressSelected()
                .build(),
        )
        navigate("/$REGISTER_LANDLORD_JOURNEY_URL/${LandlordRegistrationStepId.ManualAddress.urlPathSegment}")
        return createValidPage(page, ManualAddressFormPageLandlordRegistration::class)
    }

    fun skipToLandlordRegistrationNonEnglandOrWalesAddressPage(): NonEnglandOrWalesAddressFormPageLandlordRegistration {
        setJourneyDataInSession(
            LandlordRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyDataBuilder()
                .withVerifiedUser()
                .withEmailAddress()
                .withPhoneNumber()
                .withNonEnglandOrWalesAddress(nonEnglandOrWalesAddress = null)
                .build(),
        )
        navigate("/$REGISTER_LANDLORD_JOURNEY_URL/${LandlordRegistrationStepId.NonEnglandOrWalesAddress.urlPathSegment}")
        return createValidPage(page, NonEnglandOrWalesAddressFormPageLandlordRegistration::class)
    }

    fun skipToLandlordRegistrationLookupContactAddressPage(): LookupContactAddressFormPageLandlordRegistration {
        setJourneyDataInSession(
            LandlordRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyDataBuilder()
                .withVerifiedUser()
                .withEmailAddress()
                .withPhoneNumber()
                .withNonEnglandOrWalesAddress()
                .build(),
        )
        navigate("/$REGISTER_LANDLORD_JOURNEY_URL/${LandlordRegistrationStepId.LookupContactAddress.urlPathSegment}")
        return createValidPage(page, LookupContactAddressFormPageLandlordRegistration::class)
    }

    fun skipToLandlordRegistrationSelectContactAddressPage(): SelectContactAddressFormPageLandlordRegistration {
        setJourneyDataInSession(
            LandlordRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyDataBuilder()
                .withVerifiedUser()
                .withEmailAddress()
                .withPhoneNumber()
                .withNonEnglandOrWalesAddress()
                .withLookupAddress(isContactAddress = true)
                .withLookedUpAddresses()
                .build(),
        )
        navigate("/$REGISTER_LANDLORD_JOURNEY_URL/${LandlordRegistrationStepId.SelectContactAddress.urlPathSegment}")
        return createValidPage(page, SelectContactAddressFormPageLandlordRegistration::class)
    }

    fun skipToLandlordRegistrationManualContactAddressPage(): ManualContactAddressFormPageLandlordRegistration {
        setJourneyDataInSession(
            LandlordRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyDataBuilder()
                .withVerifiedUser()
                .withEmailAddress()
                .withPhoneNumber()
                .withNonEnglandOrWalesAddress()
                .withLookupAddress(isContactAddress = true)
                .withLookedUpAddresses()
                .withManualAddressSelected(isContactAddress = true)
                .build(),
        )
        navigate("/$REGISTER_LANDLORD_JOURNEY_URL/${LandlordRegistrationStepId.ManualContactAddress.urlPathSegment}")
        return createValidPage(page, ManualContactAddressFormPageLandlordRegistration::class)
    }

    fun skipToLandlordRegistrationCheckAnswersPage(): CheckAnswersPageLandlordRegistration {
        setJourneyDataInSession(
            LandlordRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyDataBuilder()
                .withVerifiedUser()
                .withEmailAddress()
                .withPhoneNumber()
                .withEnglandOrWalesResidence()
                .withLookupAddress()
                .withSelectedAddress()
                .build(),
        )
        navigate("/$REGISTER_LANDLORD_JOURNEY_URL/${LandlordRegistrationStepId.CheckAnswers.urlPathSegment}")
        return createValidPage(page, CheckAnswersPageLandlordRegistration::class)
    }

    fun skipToLandlordRegistrationDeclarationPage(): DeclarationFormPageLandlordRegistration {
        setJourneyDataInSession(
            LandlordRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyDataBuilder()
                .withVerifiedUser()
                .withEmailAddress()
                .withPhoneNumber()
                .withEnglandOrWalesResidence()
                .withLookupAddress()
                .withSelectedAddress()
                .withCheckedAnswers()
                .build(),
        )
        navigate("/$REGISTER_LANDLORD_JOURNEY_URL/${LandlordRegistrationStepId.Declaration.urlPathSegment}")
        return createValidPage(page, DeclarationFormPageLandlordRegistration::class)
    }

    fun navigateToLandlordRegistrationConfirmationPage() {
        navigate("/$REGISTER_LANDLORD_JOURNEY_URL/$CONFIRMATION_PATH_SEGMENT")
    }

    fun skipToLaUserRegistrationLandingPage(token: UUID): LandingPageLaUserRegistration {
        storeInvitationTokenInSession(token)
        navigate("/$REGISTER_LA_USER_JOURNEY_URL/${RegisterLaUserStepId.LandingPage.urlPathSegment}")
        return createValidPage(page, LandingPageLaUserRegistration::class)
    }

    fun skipToLaUserRegistrationNameFormPage(token: UUID): NameFormPageLaUserRegistration {
        storeInvitationTokenInSession(token)
        setJourneyDataInSession(
            LaUserRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyDataBuilder().withLandingPageReached().build(),
        )
        navigate("/$REGISTER_LA_USER_JOURNEY_URL/${RegisterLaUserStepId.Name.urlPathSegment}")
        return createValidPage(page, NameFormPageLaUserRegistration::class)
    }

    fun skipToLaUserRegistrationEmailFormPage(token: UUID): EmailFormPageLaUserRegistration {
        storeInvitationTokenInSession(token)
        setJourneyDataInSession(
            LaUserRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyDataBuilder()
                .withLandingPageReached()
                .withName()
                .build(),
        )
        navigate("/$REGISTER_LA_USER_JOURNEY_URL/${RegisterLaUserStepId.Email.urlPathSegment}")
        return createValidPage(page, EmailFormPageLaUserRegistration::class)
    }

    fun skipToLaUserRegistrationCheckAnswersPage(token: UUID): CheckAnswersPageLaUserRegistration {
        storeInvitationTokenInSession(token)
        setJourneyDataInSession(
            LaUserRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyDataBuilder()
                .withLandingPageReached()
                .withName()
                .withEmailAddress()
                .build(),
        )
        navigate("/$REGISTER_LA_USER_JOURNEY_URL/${RegisterLaUserStepId.CheckAnswers.urlPathSegment}")
        return createValidPage(page, CheckAnswersPageLaUserRegistration::class)
    }

    fun navigateToLaUserRegistrationConfirmationPage() {
        navigate("/$REGISTER_LA_USER_JOURNEY_URL/$CONFIRMATION_PATH_SEGMENT")
    }

    fun goToPropertyRegistrationStartPage(): RegisterPropertyStartPage {
        navigate("/register-property")
        return createValidPage(page, RegisterPropertyStartPage::class)
    }

    fun goToPropertyRegistrationTaskList(): TaskListPagePropertyRegistration {
        navigate("/register-property/$TASK_LIST_PATH_SEGMENT")
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

    fun goToPropertyRegistrationHouseholdsPage(): NumberOfHouseholdsFormPagePropertyRegistration {
        val occupancyPage = goToPropertyRegistrationOccupancyPage()
        occupancyPage.submitIsOccupied()
        return createValidPage(page, NumberOfHouseholdsFormPagePropertyRegistration::class)
    }

    fun goToPropertyRegistrationPeoplePage(): NumberOfPeopleFormPagePropertyRegistration {
        val householdsPage = goToPropertyRegistrationHouseholdsPage()
        householdsPage.submitNumberOfHouseholds(2)
        return createValidPage(page, NumberOfPeopleFormPagePropertyRegistration::class)
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
        navigate("/$REGISTER_PROPERTY_JOURNEY_URL/$CONFIRMATION_PATH_SEGMENT")
        return createValidPage(page, ErrorPage::class)
    }

    fun goToPropertyComplianceStartPage(propertyOwnershipId: Long): StartPagePropertyCompliance {
        navigate(PropertyComplianceController.getPropertyCompliancePath(propertyOwnershipId))
        return createValidPage(page, StartPagePropertyCompliance::class, mapOf("propertyOwnershipId" to propertyOwnershipId.toString()))
    }

    fun goToPropertyComplianceGasSafetyPage(propertyOwnershipId: Long): GasSafetyPagePropertyCompliance {
        navigate(
            PropertyComplianceController.getPropertyCompliancePath(propertyOwnershipId) +
                "/${PropertyComplianceStepId.GasSafety.urlPathSegment}",
        )
        return createValidPage(page, GasSafetyPagePropertyCompliance::class, mapOf("propertyOwnershipId" to propertyOwnershipId.toString()))
    }

    fun goToPropertyComplianceGasSafetyIssueDatePage(propertyOwnershipId: Long): GasSafetyIssueDatePagePropertyCompliance {
        val gasSafetyPage = goToPropertyComplianceGasSafetyPage(propertyOwnershipId)
        gasSafetyPage.submitHasCert()
        return createValidPage(
            page,
            GasSafetyIssueDatePagePropertyCompliance::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun goToPropertyComplianceGasSafetyEngineerNumPage(propertyOwnershipId: Long): GasSafeEngineerNumPagePropertyCompliance {
        val gasSafetyIssueDatePage = goToPropertyComplianceGasSafetyIssueDatePage(propertyOwnershipId)
        gasSafetyIssueDatePage.submitDate(DateTimeHelper().getCurrentDateInUK())
        return createValidPage(
            page,
            GasSafeEngineerNumPagePropertyCompliance::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun goToPropertyComplianceGasSafetyUploadPage(propertyOwnershipId: Long): GasSafetyUploadPagePropertyCompliance {
        val gasSafetyEngineerNumPage = goToPropertyComplianceGasSafetyEngineerNumPage(propertyOwnershipId)
        gasSafetyEngineerNumPage.submitEngineerNum("1234567")
        return createValidPage(
            page,
            GasSafetyUploadPagePropertyCompliance::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun goToPropertyComplianceGasSafetyExemptionPage(propertyOwnershipId: Long): GasSafetyExemptionPagePropertyCompliance {
        val gasSafetyPage = goToPropertyComplianceGasSafetyPage(propertyOwnershipId)
        gasSafetyPage.submitHasNoCert()
        return createValidPage(
            page,
            GasSafetyExemptionPagePropertyCompliance::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun goToPropertyComplianceGasSafetyExemptionReasonPage(propertyOwnershipId: Long): GasSafetyExemptionReasonPagePropertyCompliance {
        val gasSafetyExemptionPage = goToPropertyComplianceGasSafetyExemptionPage(propertyOwnershipId)
        gasSafetyExemptionPage.submitHasExemption()
        return createValidPage(
            page,
            GasSafetyExemptionReasonPagePropertyCompliance::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun goToPropertyComplianceGasSafetyExemptionOtherReasonPage(
        propertyOwnershipId: Long,
    ): GasSafetyExemptionOtherReasonPagePropertyCompliance {
        val gasSafetyExemptionReasonPage = goToPropertyComplianceGasSafetyExemptionReasonPage(propertyOwnershipId)
        gasSafetyExemptionReasonPage.submitExemptionReason(GasSafetyExemptionReason.OTHER)
        return createValidPage(
            page,
            GasSafetyExemptionOtherReasonPagePropertyCompliance::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun goToPropertyComplianceEicrPage(propertyOwnershipId: Long): EicrPagePropertyCompliance {
        val gasSafetyExemptionPage = goToPropertyComplianceGasSafetyExemptionPage(propertyOwnershipId)
        gasSafetyExemptionPage.submitHasNoExemption()
        val gasSafetyExemptionMissingPage =
            createValidPage(
                page,
                GasSafetyExemptionMissingPagePropertyCompliance::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )
        gasSafetyExemptionMissingPage.saveAndContinueToEicrButton.clickAndWait()
        navigate(
            PropertyComplianceController.getPropertyCompliancePath(propertyOwnershipId) +
                "/${PropertyComplianceStepId.EICR.urlPathSegment}",
        )
        return createValidPage(page, EicrPagePropertyCompliance::class, mapOf("propertyOwnershipId" to propertyOwnershipId.toString()))
    }

    fun goToPropertyComplianceEicrIssueDatePage(propertyOwnershipId: Long): EicrIssueDatePagePropertyCompliance {
        val eicrPage = goToPropertyComplianceEicrPage(propertyOwnershipId)
        eicrPage.submitHasCert()
        return createValidPage(
            page,
            EicrIssueDatePagePropertyCompliance::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun goToPropertyComplianceEicrUploadPage(propertyOwnershipId: Long): EicrUploadPagePropertyCompliance {
        val gasSafetyIssueDatePage = goToPropertyComplianceEicrIssueDatePage(propertyOwnershipId)
        gasSafetyIssueDatePage.submitDate(DateTimeHelper().getCurrentDateInUK())
        return createValidPage(
            page,
            EicrUploadPagePropertyCompliance::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun goToPropertyComplianceEicrExemptionPage(propertyOwnershipId: Long): EicrExemptionPagePropertyCompliance {
        val eicrPage = goToPropertyComplianceEicrPage(propertyOwnershipId)
        eicrPage.submitHasNoCert()
        return createValidPage(
            page,
            EicrExemptionPagePropertyCompliance::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun goToPropertyComplianceEicrExemptionReasonPage(propertyOwnershipId: Long): EicrExemptionReasonPagePropertyCompliance {
        val eicrExemptionPage = goToPropertyComplianceEicrExemptionPage(propertyOwnershipId)
        eicrExemptionPage.submitHasExemption()
        return createValidPage(
            page,
            EicrExemptionReasonPagePropertyCompliance::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun goToPropertyComplianceEicrExemptionOtherReasonPage(propertyOwnershipId: Long): EicrExemptionOtherReasonPagePropertyCompliance {
        val eicrExemptionReasonPage = goToPropertyComplianceEicrExemptionReasonPage(propertyOwnershipId)
        eicrExemptionReasonPage.submitExemptionReason(EicrExemptionReason.OTHER)
        return createValidPage(
            page,
            EicrExemptionOtherReasonPagePropertyCompliance::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun goToPropertyComplianceEpcPage(propertyOwnershipId: Long): EpcPagePropertyCompliance {
        val eicrExemptionPage = goToPropertyComplianceEicrExemptionPage(propertyOwnershipId)
        eicrExemptionPage.submitHasNoExemption()

        val eicrExemptionMissingPage =
            createValidPage(
                page,
                EicrExemptionMissingPagePropertyCompliance::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )
        eicrExemptionMissingPage.saveAndContinueToEicrButton.clickAndWait()

        navigate(
            PropertyComplianceController.getPropertyCompliancePath(propertyOwnershipId) +
                "/${PropertyComplianceStepId.EPC.urlPathSegment}",
        )

        return createValidPage(page, EpcPagePropertyCompliance::class, mapOf("propertyOwnershipId" to propertyOwnershipId.toString()))
    }

    fun goToPropertyComplianceEpcExemptionReasonPage(propertyOwnershipId: Long): EpcExemptionReasonPagePropertyCompliance {
        val epcPage = goToPropertyComplianceEpcPage(propertyOwnershipId)
        epcPage.submitCertNotRequired()
        return createValidPage(
            page,
            EpcExemptionReasonPagePropertyCompliance::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun goToLandlordDetails(): LandlordDetailsPage {
        navigate("/landlord-details")
        return createValidPage(page, LandlordDetailsPage::class)
    }

    fun goToLandlordDetailsAsALocalAuthorityUser(id: Long): LocalAuthorityViewLandlordDetailsPage {
        navigate("/landlord-details/$id")
        return createValidPage(page, LocalAuthorityViewLandlordDetailsPage::class)
    }

    fun goToUpdateLandlordDetailsUpdateLookupAddressPage(): LookupAddressFormPageUpdateLandlordDetails {
        val detailsPage = goToLandlordDetails()
        detailsPage.personalDetailsSummaryList.addressRow.actions.actionLink
            .clickAndWait()
        return createValidPage(page, LookupAddressFormPageUpdateLandlordDetails::class)
    }

    fun goToLandlordDetailsUpdateSelectAddressPage(): SelectAddressFormPageUpdateLandlordDetails {
        val lookupAddressPage = goToUpdateLandlordDetailsUpdateLookupAddressPage()
        lookupAddressPage.submitPostcodeAndBuildingNameOrNumber("EG", "1")
        return createValidPage(page, SelectAddressFormPageUpdateLandlordDetails::class)
    }

    fun skipToLandlordDetailsUpdateNamePage() {
        navigate("${LandlordDetailsController.UPDATE_ROUTE}/${LandlordDetailsUpdateStepId.UpdateName.urlPathSegment}")
    }

    fun skipToLandlordDetailsUpdateDateOfBirthPage() {
        navigate("${LandlordDetailsController.UPDATE_ROUTE}/${LandlordDetailsUpdateStepId.UpdateDateOfBirth.urlPathSegment}")
    }

    fun goToPropertyDetailsLandlordView(id: Long): PropertyDetailsPageLandlordView {
        navigate("/property-details/$id")
        return createValidPage(
            page,
            PropertyDetailsPageLandlordView::class,
            mapOf("propertyOwnershipId" to id.toString()),
        )
    }

    fun goToPropertyDetailsLocalAuthorityView(id: Long): PropertyDetailsPageLocalAuthorityView {
        navigate("/local-authority/property-details/$id")
        return createValidPage(
            page,
            PropertyDetailsPageLocalAuthorityView::class,
            mapOf("propertyOwnershipId" to id.toString()),
        )
    }

    fun skipToPropertyDetailsUpdateNumberOfHouseholdsPage(propertyOwnershipId: Long) {
        navigate(
            PropertyDetailsController.getUpdatePropertyDetailsPath(propertyOwnershipId) +
                "/${UpdatePropertyDetailsStepId.UpdateNumberOfHouseholds.urlPathSegment}",
        )
    }

    fun skipToPropertyDetailsUpdateNumberOfPeoplePage(propertyOwnershipId: Long) {
        navigate(
            PropertyDetailsController.getUpdatePropertyDetailsPath(propertyOwnershipId) +
                "/${UpdatePropertyDetailsStepId.UpdateNumberOfPeople.urlPathSegment}",
        )
    }

    fun goToPropertyDeregistrationAreYouSurePage(propertyOwnershipId: Long): AreYouSureFormPagePropertyDeregistration {
        navigate("/$DEREGISTER_PROPERTY_JOURNEY_URL/$propertyOwnershipId/${DeregisterPropertyStepId.AreYouSure.urlPathSegment}")
        return createValidPage(
            page,
            AreYouSureFormPagePropertyDeregistration::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun goToPropertyDeregistrationReasonPage(propertyOwnershipId: Long): ReasonPagePropertyDeregistration {
        val areYouSurePage = goToPropertyDeregistrationAreYouSurePage(propertyOwnershipId)
        areYouSurePage.submitWantsToProceed()
        return createValidPage(
            page,
            ReasonPagePropertyDeregistration::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun goToLandlordDeregistrationAreYouSurePage(): AreYouSureFormPageLandlordDeregistration {
        navigate("/$DEREGISTER_LANDLORD_JOURNEY_URL/${DeregisterLandlordStepId.AreYouSure.urlPathSegment}")
        return createValidPage(page, AreYouSureFormPageLandlordDeregistration::class)
    }

    fun goToLocalAuthorityDashboard(): LocalAuthorityDashboardPage {
        navigate(LOCAL_AUTHORITY_DASHBOARD_URL)
        return createValidPage(page, LocalAuthorityDashboardPage::class)
    }

    fun goToLandlordDashboard(): LandlordDashboardPage {
        navigate(LANDLORD_DASHBOARD_URL)
        return createValidPage(page, LandlordDashboardPage::class)
    }

    fun goToLandlordIncompleteProperties(): LandlordIncompletePropertiesPage {
        navigate(INCOMPLETE_PROPERTIES_URL)
        return createValidPage(page, LandlordIncompletePropertiesPage::class)
    }

    fun goToDeleteIncompletePropertyRegistrationAreYouSurePage(contextId: String): DeleteIncompletePropertyRegistrationAreYouSurePage {
        navigate(
            "/$LANDLORD_PATH_SEGMENT/$DELETE_INCOMPLETE_PROPERTY_PATH_SEGMENT" +
                "?$CONTEXT_ID_URL_PARAMETER=$contextId",
        )
        return createValidPage(page, DeleteIncompletePropertyRegistrationAreYouSurePage::class, mapOf("contextId" to contextId))
    }

    fun goToInviteLaAdmin(): InviteLaAdminPage {
        navigate("/$SYSTEM_OPERATOR_PATH_SEGMENT/invite-la-admin")
        return createValidPage(page, InviteLaAdminPage::class)
    }

    fun navigate(path: String): Response? = page.navigate("http://localhost:$port$path")

    private fun setJourneyDataInSession(
        journeyDataKey: String,
        journeyData: JourneyData,
    ) {
        val response =
            page.request().post(
                "http://localhost:$port/${SessionController.SET_JOURNEY_DATA_ROUTE}",
                RequestOptions.create().setData(SetJourneyDataRequestModel(journeyDataKey, journeyData)),
            )
        assertTrue(response.ok(), "Failed to set journey data. Received status code: ${response.status()}")
        response.dispose()
    }

    private fun storeInvitationTokenInSession(token: UUID) {
        val response =
            page.request().post(
                "http://localhost:$port/${SessionController.STORE_INVITATION_TOKEN_ROUTE}",
                RequestOptions.create().setData(StoreInvitationTokenRequestModel(token)),
            )
        assertTrue(response.ok(), "Failed to store invitation token. Received status code: ${response.status()}")
        response.dispose()
    }
}
