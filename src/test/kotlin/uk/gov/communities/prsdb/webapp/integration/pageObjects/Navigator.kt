package uk.gov.communities.prsdb.webapp.integration.pageObjects

import com.microsoft.playwright.Page
import com.microsoft.playwright.Response
import com.microsoft.playwright.options.RequestOptions
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.CONTEXT_ID_URL_PARAMETER
import uk.gov.communities.prsdb.webapp.constants.DELETE_INCOMPLETE_PROPERTY_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.TASK_LIST_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.TOKEN
import uk.gov.communities.prsdb.webapp.controllers.BetaFeedbackController
import uk.gov.communities.prsdb.webapp.controllers.CookiesController.Companion.COOKIES_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.DeregisterLandlordController
import uk.gov.communities.prsdb.webapp.controllers.DeregisterPropertyController
import uk.gov.communities.prsdb.webapp.controllers.GeneratePasscodeController.Companion.GENERATE_PASSCODE_URL
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.COMPLIANCE_ACTIONS_URL
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.INCOMPLETE_PROPERTIES_URL
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.controllers.LandlordPrivacyNoticeController.Companion.LANDLORD_PRIVACY_NOTICE_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.LocalAuthorityDashboardController.Companion.LOCAL_AUTHORITY_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalAuthorityAdminsController
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalAuthorityUsersController.Companion.getLaInviteNewUserRoute
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalAuthorityUsersController.Companion.getLaManageUsersRoute
import uk.gov.communities.prsdb.webapp.controllers.PasscodeEntryController.Companion.INVALID_PASSCODE_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.PasscodeEntryController.Companion.PASSCODE_ENTRY_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.controllers.RegisterLAUserController
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.controllers.SearchRegisterController
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.LaUserRegistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.LandlordDetailsUpdateJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.LandlordRegistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.PropertyComplianceJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.PropertyDeregistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.PropertyDetailsUpdateJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.PropertyRegistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterLandlordStepId
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterPropertyStepId
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordDetailsUpdateStepId
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLaUserStepId
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ComplianceActionsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.CookiesPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.DeleteIncompletePropertyRegistrationAreYouSurePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.GeneratePasscodePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.InviteLaAdminPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.InviteNewLaUserPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordIncompletePropertiesPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordPrivacyNoticePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LocalAuthorityDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LocalAuthorityViewLandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LookupAddressFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLaAdminsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLaUsersPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PasscodeEntryPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLocalAuthorityView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.SearchLandlordRegisterPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.SearchPropertyRegisterPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.SelectAddressFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.createValidPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.betaFeedbackPages.LandlordBetaFeedbackPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.betaFeedbackPages.LocalCouncilBetaFeedbackPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages.CheckAnswersPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages.EmailFormPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages.NameFormPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages.PrivacyNoticePageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordDeregistrationJourneyPages.AreYouSureFormPageLandlordDeregistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.CheckAnswersPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.CountryOfResidenceFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.DateOfBirthFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.EmailFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.LookupAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.ManualAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.NameFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.PhoneNumberFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.PrivacyNoticePageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.SelectAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.ServiceInformationStartPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.WhatYouNeedToRegisterStartPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.CheckAndSubmitPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.CheckAutoMatchedEpcPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.CheckMatchedEpcPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EicrExemptionOtherReasonPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EicrExemptionPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EicrExemptionReasonPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EicrIssueDatePagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EicrPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EicrUploadPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EpcExemptionReasonPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EpcExpiredPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EpcExpiryCheckPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EpcLookupPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EpcPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.FireSafetyDeclarationPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafeEngineerNumPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyExemptionOtherReasonPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyExemptionPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyExemptionReasonPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyIssueDatePagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyUploadPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.KeepPropertySafePagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.LowEnergyRatingPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.MeesExemptionCheckPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.MeesExemptionReasonPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.ResponsibilityToTenantsPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.StartPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.UpdateEicrPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.UpdateEpcPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.UpdateGasSafetyPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDeregistrationJourneyPages.AreYouSureFormPagePropertyDeregistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDeregistrationJourneyPages.ReasonPagePropertyDeregistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.CheckHouseholdsAnswersPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.CheckOccupancyAnswersPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.CheckPeopleAnswersPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.NumberOfPeopleFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.OccupancyFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.OwnershipTypeFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CheckAnswersPagePropertyRegistration
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
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.testHelpers.api.controllers.SessionController
import uk.gov.communities.prsdb.webapp.testHelpers.api.requestModels.SetJourneyDataRequestModel
import uk.gov.communities.prsdb.webapp.testHelpers.api.requestModels.StoreInvitationTokenRequestModel
import uk.gov.communities.prsdb.webapp.testHelpers.builders.JourneyDataBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.builders.JourneyPageDataBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData
import java.util.UUID
import kotlin.test.assertTrue

class Navigator(
    private val page: Page,
    private val port: Int,
) {
    fun goToManageLaUsers(authorityId: Int): ManageLaUsersPage {
        navigate(getLaManageUsersRoute(authorityId))
        return createValidPage(page, ManageLaUsersPage::class)
    }

    fun goToInviteNewLaUser(authorityId: Int): InviteNewLaUserPage {
        navigate(getLaInviteNewUserRoute(authorityId))
        return createValidPage(page, InviteNewLaUserPage::class)
    }

    fun goToLandlordSearchPage(): SearchLandlordRegisterPage {
        navigate(SearchRegisterController.SEARCH_LANDLORD_URL)
        return createValidPage(page, SearchLandlordRegisterPage::class)
    }

    fun goToPropertySearchPage(): SearchPropertyRegisterPage {
        navigate(SearchRegisterController.SEARCH_PROPERTY_URL)
        return createValidPage(page, SearchPropertyRegisterPage::class)
    }

    fun goToPasscodeEntryPage(): PasscodeEntryPage {
        navigate(PASSCODE_ENTRY_ROUTE)
        return createValidPage(page, PasscodeEntryPage::class)
    }

    fun navigateToLandlordRegistrationStartPage() {
        navigate(RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE)
    }

    fun goToLandlordRegistrationServiceInformationStartPage(): ServiceInformationStartPageLandlordRegistration {
        navigate(RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE)
        return createValidPage(page, ServiceInformationStartPageLandlordRegistration::class)
    }

    fun goToLandlordRegistrationWhatYouNeedToRegisterStartPage(): WhatYouNeedToRegisterStartPageLandlordRegistration {
        navigate(RegisterLandlordController.LANDLORD_REGISTRATION_START_PAGE_ROUTE)
        return createValidPage(page, WhatYouNeedToRegisterStartPageLandlordRegistration::class)
    }

    fun goToLandlordRegistrationPrivacyNoticePage(): PrivacyNoticePageLandlordRegistration {
        navigate(RegisterLandlordController.LANDLORD_REGISTRATION_PRIVACY_NOTICE_ROUTE)
        return createValidPage(page, PrivacyNoticePageLandlordRegistration::class)
    }

    fun navigateToLandlordRegistrationPrivacyNoticePage() {
        navigate(RegisterLandlordController.LANDLORD_REGISTRATION_PRIVACY_NOTICE_ROUTE)
    }

    fun skipToLandlordRegistrationNamePage(): NameFormPageLandlordRegistration {
        setJourneyDataInSession(
            LandlordRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyPageDataBuilder.beforeLandlordRegistrationName().build(),
        )
        navigate("${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/${LandlordRegistrationStepId.Name.urlPathSegment}")
        return createValidPage(page, NameFormPageLandlordRegistration::class)
    }

    fun skipToLandlordRegistrationDateOfBirthPage(): DateOfBirthFormPageLandlordRegistration {
        setJourneyDataInSession(
            LandlordRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyPageDataBuilder.beforeLandlordRegistrationDob().build(),
        )
        navigate("${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/${LandlordRegistrationStepId.DateOfBirth.urlPathSegment}")
        return createValidPage(page, DateOfBirthFormPageLandlordRegistration::class)
    }

    fun skipToLandlordRegistrationEmailPage(): EmailFormPageLandlordRegistration {
        setJourneyDataInSession(
            LandlordRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyPageDataBuilder.beforeLandlordRegistrationEmail().build(),
        )
        navigate("${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/${LandlordRegistrationStepId.Email.urlPathSegment}")
        return createValidPage(page, EmailFormPageLandlordRegistration::class)
    }

    fun skipToLandlordRegistrationPhoneNumberPage(): PhoneNumberFormPageLandlordRegistration {
        setJourneyDataInSession(
            LandlordRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyPageDataBuilder.beforeLandlordRegistrationPhoneNumber().build(),
        )
        navigate("${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/${LandlordRegistrationStepId.PhoneNumber.urlPathSegment}")
        return createValidPage(page, PhoneNumberFormPageLandlordRegistration::class)
    }

    fun skipToLandlordRegistrationCountryOfResidencePage(): CountryOfResidenceFormPageLandlordRegistration {
        setJourneyDataInSession(
            LandlordRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyPageDataBuilder.beforeLandlordRegistrationCountryOfResidence().build(),
        )
        navigate(
            "${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/${LandlordRegistrationStepId.CountryOfResidence.urlPathSegment}",
        )
        return createValidPage(page, CountryOfResidenceFormPageLandlordRegistration::class)
    }

    fun skipToLandlordRegistrationLookupAddressPage(): LookupAddressFormPageLandlordRegistration {
        setJourneyDataInSession(
            LandlordRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyPageDataBuilder.beforeLandlordRegistrationLookupAddress().build(),
        )
        navigate("${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/${LandlordRegistrationStepId.LookupAddress.urlPathSegment}")
        return createValidPage(page, LookupAddressFormPageLandlordRegistration::class)
    }

    fun skipToLandlordRegistrationSelectAddressPage(): SelectAddressFormPageLandlordRegistration {
        setJourneyDataInSession(
            LandlordRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyPageDataBuilder.beforeLandlordRegistrationSelectAddress().build(),
        )
        navigate("${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/${LandlordRegistrationStepId.SelectAddress.urlPathSegment}")
        return createValidPage(page, SelectAddressFormPageLandlordRegistration::class)
    }

    fun skipToLandlordRegistrationManualAddressPage(): ManualAddressFormPageLandlordRegistration {
        setJourneyDataInSession(
            LandlordRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyPageDataBuilder.beforeLandlordRegistrationManualAddress().build(),
        )
        navigate("${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/${LandlordRegistrationStepId.ManualAddress.urlPathSegment}")
        return createValidPage(page, ManualAddressFormPageLandlordRegistration::class)
    }

    fun skipToLandlordRegistrationCheckAnswersPage(): CheckAnswersPageLandlordRegistration {
        setJourneyDataInSession(
            LandlordRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyPageDataBuilder.beforeLandlordRegistrationCheckAnswers().build(),
        )
        navigate("${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/${LandlordRegistrationStepId.CheckAnswers.urlPathSegment}")
        return createValidPage(page, CheckAnswersPageLandlordRegistration::class)
    }

    fun navigateToLandlordRegistrationConfirmationPage() {
        navigate("${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/$CONFIRMATION_PATH_SEGMENT")
    }

    fun navigateToLaUserRegistrationAcceptInvitationRoute(token: String) {
        navigate("${RegisterLAUserController.LA_USER_REGISTRATION_ROUTE}?$TOKEN=$token")
    }

    fun navigateToLaUserRegistrationLandingPage(token: UUID) {
        storeInvitationTokenInSession(token)
        navigate("${RegisterLAUserController.LA_USER_REGISTRATION_ROUTE}/${RegisterLaUserStepId.LandingPage.urlPathSegment}")
    }

    fun skipToLaUserRegistrationPrivacyNoticePage(token: UUID): PrivacyNoticePageLaUserRegistration {
        storeInvitationTokenInSession(token)
        setJourneyDataInSession(
            LaUserRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyDataBuilder().withLandingPageReached().build(),
        )
        navigate("${RegisterLAUserController.LA_USER_REGISTRATION_ROUTE}/${RegisterLaUserStepId.PrivacyNotice.urlPathSegment}")
        return createValidPage(page, PrivacyNoticePageLaUserRegistration::class)
    }

    fun skipToLaUserRegistrationNameFormPage(token: UUID): NameFormPageLaUserRegistration {
        storeInvitationTokenInSession(token)
        setJourneyDataInSession(
            LaUserRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyPageDataBuilder.beforeLaUserRegistrationName().build(),
        )
        navigate("${RegisterLAUserController.LA_USER_REGISTRATION_ROUTE}/${RegisterLaUserStepId.Name.urlPathSegment}")
        return createValidPage(page, NameFormPageLaUserRegistration::class)
    }

    fun skipToLaUserRegistrationEmailFormPage(token: UUID): EmailFormPageLaUserRegistration {
        storeInvitationTokenInSession(token)
        setJourneyDataInSession(
            LaUserRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyPageDataBuilder.beforeLaUserRegistrationEmail().build(),
        )
        navigate("${RegisterLAUserController.LA_USER_REGISTRATION_ROUTE}/${RegisterLaUserStepId.Email.urlPathSegment}")
        return createValidPage(page, EmailFormPageLaUserRegistration::class)
    }

    fun skipToLaUserRegistrationCheckAnswersPage(token: UUID): CheckAnswersPageLaUserRegistration {
        storeInvitationTokenInSession(token)
        setJourneyDataInSession(
            LaUserRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyPageDataBuilder.beforeLaUserRegistrationCheckAnswers().build(),
        )
        navigate("${RegisterLAUserController.LA_USER_REGISTRATION_ROUTE}/${RegisterLaUserStepId.CheckAnswers.urlPathSegment}")
        return createValidPage(page, CheckAnswersPageLaUserRegistration::class)
    }

    fun navigateToLaUserRegistrationConfirmationPage() {
        navigate("${RegisterLAUserController.LA_USER_REGISTRATION_ROUTE}/$CONFIRMATION_PATH_SEGMENT")
    }

    fun goToPropertyRegistrationStartPage(): RegisterPropertyStartPage {
        navigate(RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE)
        return createValidPage(page, RegisterPropertyStartPage::class)
    }

    fun goToPropertyRegistrationTaskList(): TaskListPagePropertyRegistration {
        navigate("${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/$TASK_LIST_PATH_SEGMENT")
        return createValidPage(page, TaskListPagePropertyRegistration::class)
    }

    fun goToPropertyRegistrationLookupAddressPage(): LookupAddressFormPagePropertyRegistration {
        navigate("${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${RegisterPropertyStepId.LookupAddress.urlPathSegment}")
        return createValidPage(page, LookupAddressFormPagePropertyRegistration::class)
    }

    fun skipToPropertyRegistrationSelectAddressPage(
        customLookedUpAddresses: List<AddressDataModel>? = null,
    ): SelectAddressFormPagePropertyRegistration {
        setJourneyDataInSession(
            PropertyRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyPageDataBuilder.beforePropertyRegistrationSelectAddress(customLookedUpAddresses).build(),
        )
        navigate("${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${RegisterPropertyStepId.SelectAddress.urlPathSegment}")
        return createValidPage(page, SelectAddressFormPagePropertyRegistration::class)
    }

    fun skipToPropertyRegistrationManualAddressPage(): ManualAddressFormPagePropertyRegistration {
        setJourneyDataInSession(
            PropertyRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyPageDataBuilder.beforePropertyRegistrationManualAddress().build(),
        )
        navigate("${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${RegisterPropertyStepId.ManualAddress.urlPathSegment}")
        return createValidPage(page, ManualAddressFormPagePropertyRegistration::class)
    }

    fun skipToPropertyRegistrationSelectLocalAuthorityPage(): SelectLocalAuthorityFormPagePropertyRegistration {
        setJourneyDataInSession(
            PropertyRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyPageDataBuilder.beforePropertyRegistrationSelectLocalAuthority().build(),
        )
        navigate("${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${RegisterPropertyStepId.LocalAuthority.urlPathSegment}")
        return createValidPage(page, SelectLocalAuthorityFormPagePropertyRegistration::class)
    }

    fun skipToPropertyRegistrationPropertyTypePage(): PropertyTypeFormPagePropertyRegistration {
        setJourneyDataInSession(
            PropertyRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyPageDataBuilder.beforePropertyRegistrationPropertyType().build(),
        )
        navigate("${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${RegisterPropertyStepId.PropertyType.urlPathSegment}")
        return createValidPage(page, PropertyTypeFormPagePropertyRegistration::class)
    }

    fun skipToPropertyRegistrationOwnershipTypePage(): OwnershipTypeFormPagePropertyRegistration {
        setJourneyDataInSession(
            PropertyRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyPageDataBuilder.beforePropertyRegistrationOwnershipType().build(),
        )
        navigate("${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${RegisterPropertyStepId.OwnershipType.urlPathSegment}")
        return createValidPage(page, OwnershipTypeFormPagePropertyRegistration::class)
    }

    fun skipToPropertyRegistrationLicensingTypePage(): LicensingTypeFormPagePropertyRegistration {
        setJourneyDataInSession(
            PropertyRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyPageDataBuilder.beforePropertyRegistrationLicensingType().build(),
        )
        navigate("${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${RegisterPropertyStepId.LicensingType.urlPathSegment}")
        return createValidPage(page, LicensingTypeFormPagePropertyRegistration::class)
    }

    fun skipToPropertyRegistrationSelectiveLicencePage(): SelectiveLicenceFormPagePropertyRegistration {
        setJourneyDataInSession(
            PropertyRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyPageDataBuilder.beforePropertyRegistrationSelectiveLicence().build(),
        )
        navigate("${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${RegisterPropertyStepId.SelectiveLicence.urlPathSegment}")
        return createValidPage(page, SelectiveLicenceFormPagePropertyRegistration::class)
    }

    fun skipToPropertyRegistrationHmoMandatoryLicencePage(): HmoMandatoryLicenceFormPagePropertyRegistration {
        setJourneyDataInSession(
            PropertyRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyPageDataBuilder.beforePropertyRegistrationHmoMandatoryLicence().build(),
        )
        navigate("${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${RegisterPropertyStepId.HmoMandatoryLicence.urlPathSegment}")
        return createValidPage(page, HmoMandatoryLicenceFormPagePropertyRegistration::class)
    }

    fun skipToPropertyRegistrationHmoAdditionalLicencePage(): HmoAdditionalLicenceFormPagePropertyRegistration {
        setJourneyDataInSession(
            PropertyRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyPageDataBuilder.beforePropertyRegistrationHmoAdditionalLicence().build(),
        )
        navigate("${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${RegisterPropertyStepId.HmoAdditionalLicence.urlPathSegment}")
        return createValidPage(page, HmoAdditionalLicenceFormPagePropertyRegistration::class)
    }

    fun skipToPropertyRegistrationOccupancyPage(): OccupancyFormPagePropertyRegistration {
        setJourneyDataInSession(
            PropertyRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyPageDataBuilder.beforePropertyRegistrationOccupancy().build(),
        )
        navigate("${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${RegisterPropertyStepId.Occupancy.urlPathSegment}")
        return createValidPage(page, OccupancyFormPagePropertyRegistration::class)
    }

    fun skipToPropertyRegistrationHouseholdsPage(): NumberOfHouseholdsFormPagePropertyRegistration {
        setJourneyDataInSession(
            PropertyRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyPageDataBuilder.beforePropertyRegistrationHouseholds().build(),
        )
        navigate("${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${RegisterPropertyStepId.NumberOfHouseholds.urlPathSegment}")
        return createValidPage(page, NumberOfHouseholdsFormPagePropertyRegistration::class)
    }

    fun skipToPropertyRegistrationPeoplePage(): NumberOfPeopleFormPagePropertyRegistration {
        setJourneyDataInSession(
            PropertyRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyPageDataBuilder.beforePropertyRegistrationPeople().build(),
        )
        navigate("${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${RegisterPropertyStepId.NumberOfPeople.urlPathSegment}")
        return createValidPage(page, NumberOfPeopleFormPagePropertyRegistration::class)
    }

    fun skipToPropertyRegistrationCheckAnswersPage(): CheckAnswersPagePropertyRegistration {
        setJourneyDataInSession(
            PropertyRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyPageDataBuilder.beforePropertyRegistrationCheckAnswers().build(),
        )
        navigate("${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${RegisterPropertyStepId.CheckAnswers.urlPathSegment}")
        return createValidPage(page, CheckAnswersPagePropertyRegistration::class)
    }

    fun navigateToPropertyRegistrationConfirmationPage() {
        navigate("${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/$CONFIRMATION_PATH_SEGMENT")
    }

    fun goToPropertyComplianceStartPage(propertyOwnershipId: Long): StartPagePropertyCompliance {
        navigate(PropertyComplianceController.getPropertyCompliancePath(propertyOwnershipId))
        return createValidPage(
            page,
            StartPagePropertyCompliance::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun goToPropertyComplianceGasSafetyPage(propertyOwnershipId: Long): GasSafetyPagePropertyCompliance {
        navigate(
            PropertyComplianceController.getPropertyCompliancePath(propertyOwnershipId) +
                "/${PropertyComplianceStepId.GasSafety.urlPathSegment}",
        )
        return createValidPage(
            page,
            GasSafetyPagePropertyCompliance::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun skipToPropertyComplianceGasSafetyIssueDatePage(propertyOwnershipId: Long): GasSafetyIssueDatePagePropertyCompliance {
        setJourneyDataInSession(
            PropertyComplianceJourneyFactory.getJourneyDataKey(propertyOwnershipId),
            JourneyPageDataBuilder.beforePropertyComplianceGasSafetyIssueDate().build(),
        )
        navigate(
            PropertyComplianceController.getPropertyCompliancePath(propertyOwnershipId) +
                "/${PropertyComplianceStepId.GasSafetyIssueDate.urlPathSegment}",
        )
        return createValidPage(
            page,
            GasSafetyIssueDatePagePropertyCompliance::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun skipToPropertyComplianceGasSafetyEngineerNumPage(propertyOwnershipId: Long): GasSafeEngineerNumPagePropertyCompliance {
        setJourneyDataInSession(
            PropertyComplianceJourneyFactory.getJourneyDataKey(propertyOwnershipId),
            JourneyPageDataBuilder.beforePropertyComplianceGasSafetyEngineerNum().build(),
        )
        navigate(
            PropertyComplianceController.getPropertyCompliancePath(propertyOwnershipId) +
                "/${PropertyComplianceStepId.GasSafetyEngineerNum.urlPathSegment}",
        )
        return createValidPage(
            page,
            GasSafeEngineerNumPagePropertyCompliance::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun skipToPropertyComplianceGasSafetyUploadPage(propertyOwnershipId: Long): GasSafetyUploadPagePropertyCompliance {
        setJourneyDataInSession(
            PropertyComplianceJourneyFactory.getJourneyDataKey(propertyOwnershipId),
            JourneyPageDataBuilder.beforePropertyComplianceGasSafetyUpload().build(),
        )
        navigate(
            PropertyComplianceController.getPropertyCompliancePath(propertyOwnershipId) +
                "/${PropertyComplianceStepId.GasSafetyUpload.urlPathSegment}",
        )
        return createValidPage(
            page,
            GasSafetyUploadPagePropertyCompliance::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun skipToPropertyComplianceGasSafetyExemptionPage(propertyOwnershipId: Long): GasSafetyExemptionPagePropertyCompliance {
        setJourneyDataInSession(
            PropertyComplianceJourneyFactory.getJourneyDataKey(propertyOwnershipId),
            JourneyPageDataBuilder.beforePropertyComplianceGasSafetyExemption().build(),
        )
        navigate(
            PropertyComplianceController.getPropertyCompliancePath(propertyOwnershipId) +
                "/${PropertyComplianceStepId.GasSafetyExemption.urlPathSegment}",
        )
        return createValidPage(
            page,
            GasSafetyExemptionPagePropertyCompliance::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun skipToPropertyComplianceGasSafetyExemptionReasonPage(propertyOwnershipId: Long): GasSafetyExemptionReasonPagePropertyCompliance {
        setJourneyDataInSession(
            PropertyComplianceJourneyFactory.getJourneyDataKey(propertyOwnershipId),
            JourneyPageDataBuilder.beforePropertyComplianceGasSafetyExemptionReason().build(),
        )
        navigate(
            PropertyComplianceController.getPropertyCompliancePath(propertyOwnershipId) +
                "/${PropertyComplianceStepId.GasSafetyExemptionReason.urlPathSegment}",
        )
        return createValidPage(
            page,
            GasSafetyExemptionReasonPagePropertyCompliance::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun skipToPropertyComplianceGasSafetyExemptionOtherReasonPage(
        propertyOwnershipId: Long,
    ): GasSafetyExemptionOtherReasonPagePropertyCompliance {
        setJourneyDataInSession(
            PropertyComplianceJourneyFactory.getJourneyDataKey(propertyOwnershipId),
            JourneyPageDataBuilder.beforePropertyComplianceGasSafetyExemptionOtherReason().build(),
        )
        navigate(
            PropertyComplianceController.getPropertyCompliancePath(propertyOwnershipId) +
                "/${PropertyComplianceStepId.GasSafetyExemptionOtherReason.urlPathSegment}",
        )
        return createValidPage(
            page,
            GasSafetyExemptionOtherReasonPagePropertyCompliance::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun skipToPropertyComplianceEicrPage(propertyOwnershipId: Long): EicrPagePropertyCompliance {
        setJourneyDataInSession(
            PropertyComplianceJourneyFactory.getJourneyDataKey(propertyOwnershipId),
            JourneyPageDataBuilder.beforePropertyComplianceEicr().build(),
        )
        navigate(
            PropertyComplianceController.getPropertyCompliancePath(propertyOwnershipId) +
                "/${PropertyComplianceStepId.EICR.urlPathSegment}",
        )
        return createValidPage(
            page,
            EicrPagePropertyCompliance::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun skipToPropertyComplianceEicrIssueDatePage(propertyOwnershipId: Long): EicrIssueDatePagePropertyCompliance {
        setJourneyDataInSession(
            PropertyComplianceJourneyFactory.getJourneyDataKey(propertyOwnershipId),
            JourneyPageDataBuilder.beforePropertyComplianceEicrIssueDate().build(),
        )
        navigate(
            PropertyComplianceController.getPropertyCompliancePath(propertyOwnershipId) +
                "/${PropertyComplianceStepId.EicrIssueDate.urlPathSegment}",
        )
        return createValidPage(
            page,
            EicrIssueDatePagePropertyCompliance::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun skipToPropertyComplianceEicrUploadPage(propertyOwnershipId: Long): EicrUploadPagePropertyCompliance {
        setJourneyDataInSession(
            PropertyComplianceJourneyFactory.getJourneyDataKey(propertyOwnershipId),
            JourneyPageDataBuilder.beforePropertyComplianceEicrUpload().build(),
        )
        navigate(
            PropertyComplianceController.getPropertyCompliancePath(propertyOwnershipId) +
                "/${PropertyComplianceStepId.EicrUpload.urlPathSegment}",
        )
        return createValidPage(
            page,
            EicrUploadPagePropertyCompliance::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun skipToPropertyComplianceEicrExemptionPage(propertyOwnershipId: Long): EicrExemptionPagePropertyCompliance {
        setJourneyDataInSession(
            PropertyComplianceJourneyFactory.getJourneyDataKey(propertyOwnershipId),
            JourneyPageDataBuilder.beforePropertyComplianceEicrExemption().build(),
        )
        navigate(
            PropertyComplianceController.getPropertyCompliancePath(propertyOwnershipId) +
                "/${PropertyComplianceStepId.EicrExemption.urlPathSegment}",
        )
        return createValidPage(
            page,
            EicrExemptionPagePropertyCompliance::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun skipToPropertyComplianceEicrExemptionReasonPage(propertyOwnershipId: Long): EicrExemptionReasonPagePropertyCompliance {
        setJourneyDataInSession(
            PropertyComplianceJourneyFactory.getJourneyDataKey(propertyOwnershipId),
            JourneyPageDataBuilder.beforePropertyComplianceEicrExemptionReason().build(),
        )
        navigate(
            PropertyComplianceController.getPropertyCompliancePath(propertyOwnershipId) +
                "/${PropertyComplianceStepId.EicrExemptionReason.urlPathSegment}",
        )
        return createValidPage(
            page,
            EicrExemptionReasonPagePropertyCompliance::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun skipToPropertyComplianceEicrExemptionOtherReasonPage(propertyOwnershipId: Long): EicrExemptionOtherReasonPagePropertyCompliance {
        setJourneyDataInSession(
            PropertyComplianceJourneyFactory.getJourneyDataKey(propertyOwnershipId),
            JourneyPageDataBuilder.beforePropertyComplianceEicrExemptionOtherReason().build(),
        )
        navigate(
            PropertyComplianceController.getPropertyCompliancePath(propertyOwnershipId) +
                "/${PropertyComplianceStepId.EicrExemptionOtherReason.urlPathSegment}",
        )
        return createValidPage(
            page,
            EicrExemptionOtherReasonPagePropertyCompliance::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun skipToPropertyComplianceEpcPage(propertyOwnershipId: Long): EpcPagePropertyCompliance {
        setJourneyDataInSession(
            PropertyComplianceJourneyFactory.getJourneyDataKey(propertyOwnershipId),
            JourneyPageDataBuilder.beforePropertyComplianceEpc().build(),
        )
        navigate(
            PropertyComplianceController.getPropertyCompliancePath(propertyOwnershipId) +
                "/${PropertyComplianceStepId.EPC.urlPathSegment}",
        )
        return createValidPage(
            page,
            EpcPagePropertyCompliance::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun skipToPropertyComplianceEpcExemptionReasonPage(propertyOwnershipId: Long): EpcExemptionReasonPagePropertyCompliance {
        setJourneyDataInSession(
            PropertyComplianceJourneyFactory.getJourneyDataKey(propertyOwnershipId),
            JourneyPageDataBuilder.beforePropertyComplianceEpcExemptionReason().build(),
        )
        navigate(
            PropertyComplianceController.getPropertyCompliancePath(propertyOwnershipId) +
                "/${PropertyComplianceStepId.EpcExemptionReason.urlPathSegment}",
        )
        return createValidPage(
            page,
            EpcExemptionReasonPagePropertyCompliance::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun skipToPropertyComplianceCheckAutoMatchedEpcPage(
        propertyOwnershipId: Long,
        epcDetails: EpcDataModel = MockEpcData.createEpcDataModel(),
    ): CheckAutoMatchedEpcPagePropertyCompliance {
        setJourneyDataInSession(
            PropertyComplianceJourneyFactory.getJourneyDataKey(propertyOwnershipId),
            JourneyPageDataBuilder.beforePropertyComplianceCheckAutoMatchedEpc(epcDetails).build(),
        )
        navigate(
            PropertyComplianceController.getPropertyCompliancePath(propertyOwnershipId) +
                "/${PropertyComplianceStepId.CheckAutoMatchedEpc.urlPathSegment}",
        )
        return createValidPage(
            page,
            CheckAutoMatchedEpcPagePropertyCompliance::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun skipToPropertyComplianceCheckMatchedEpcPage(
        propertyOwnershipId: Long,
        epcDetails: EpcDataModel = MockEpcData.createEpcDataModel(),
    ): CheckMatchedEpcPagePropertyCompliance {
        setJourneyDataInSession(
            PropertyComplianceJourneyFactory.getJourneyDataKey(propertyOwnershipId),
            JourneyPageDataBuilder.beforePropertyComplianceCheckMatchedEpc(epcDetails).build(),
        )
        navigate(
            PropertyComplianceController.getPropertyCompliancePath(propertyOwnershipId) +
                "/${PropertyComplianceStepId.CheckMatchedEpc.urlPathSegment}",
        )
        return createValidPage(
            page,
            CheckMatchedEpcPagePropertyCompliance::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun skipToPropertyComplianceEpcLookupPage(propertyOwnershipId: Long): EpcLookupPagePropertyCompliance {
        setJourneyDataInSession(
            PropertyComplianceJourneyFactory.getJourneyDataKey(propertyOwnershipId),
            JourneyPageDataBuilder.beforePropertyComplianceEpcLookup().build(),
        )
        navigate(
            PropertyComplianceController.getPropertyCompliancePath(propertyOwnershipId) +
                "/${PropertyComplianceStepId.EpcLookup.urlPathSegment}",
        )
        return createValidPage(
            page,
            EpcLookupPagePropertyCompliance::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun skipToPropertyComplianceEpcExpiryCheckPage(
        propertyOwnershipId: Long,
        epcRating: String = "C",
    ): EpcExpiryCheckPagePropertyCompliance {
        setJourneyDataInSession(
            PropertyComplianceJourneyFactory.getJourneyDataKey(propertyOwnershipId),
            JourneyPageDataBuilder.beforePropertyComplianceEpcExpiryCheck(epcRating).build(),
        )
        navigate(
            PropertyComplianceController.getPropertyCompliancePath(propertyOwnershipId) +
                "/${PropertyComplianceStepId.EpcExpiryCheck.urlPathSegment}",
        )
        return createValidPage(
            page,
            EpcExpiryCheckPagePropertyCompliance::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun skipToPropertyComplianceEpcExpiredPage(
        propertyOwnershipId: Long,
        epcRating: String = "C",
    ): EpcExpiredPagePropertyCompliance {
        setJourneyDataInSession(
            PropertyComplianceJourneyFactory.getJourneyDataKey(propertyOwnershipId),
            JourneyPageDataBuilder.beforePropertyComplianceEpcExpired(epcRating).build(),
        )
        navigate(
            PropertyComplianceController.getPropertyCompliancePath(propertyOwnershipId) +
                "/${PropertyComplianceStepId.EpcExpired.urlPathSegment}",
        )
        return createValidPage(
            page,
            EpcExpiredPagePropertyCompliance::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun skipToPropertyComplianceMeesExemptionCheckPage(propertyOwnershipId: Long): MeesExemptionCheckPagePropertyCompliance {
        setJourneyDataInSession(
            PropertyComplianceJourneyFactory.getJourneyDataKey(propertyOwnershipId),
            JourneyPageDataBuilder.beforePropertyComplianceMeesExemptionCheck().build(),
        )
        navigate(
            PropertyComplianceController.getPropertyCompliancePath(propertyOwnershipId) +
                "/${PropertyComplianceStepId.MeesExemptionCheck.urlPathSegment}",
        )
        return createValidPage(
            page,
            MeesExemptionCheckPagePropertyCompliance::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun skipToPropertyComplianceMeesExemptionReasonPage(propertyOwnershipId: Long): MeesExemptionReasonPagePropertyCompliance {
        setJourneyDataInSession(
            PropertyComplianceJourneyFactory.getJourneyDataKey(propertyOwnershipId),
            JourneyPageDataBuilder.beforePropertyComplianceMeesExemptionReason().build(),
        )
        navigate(
            PropertyComplianceController.getPropertyCompliancePath(propertyOwnershipId) +
                "/${PropertyComplianceStepId.MeesExemptionReason.urlPathSegment}",
        )
        return createValidPage(
            page,
            MeesExemptionReasonPagePropertyCompliance::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun skipToPropertyComplianceLowEnergyRatingPage(propertyOwnershipId: Long): LowEnergyRatingPagePropertyCompliance {
        setJourneyDataInSession(
            PropertyComplianceJourneyFactory.getJourneyDataKey(propertyOwnershipId),
            JourneyPageDataBuilder.beforePropertyComplianceLowEnergyRating().build(),
        )
        navigate(
            PropertyComplianceController.getPropertyCompliancePath(propertyOwnershipId) +
                "/${PropertyComplianceStepId.LowEnergyRating.urlPathSegment}",
        )
        return createValidPage(
            page,
            LowEnergyRatingPagePropertyCompliance::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun skipToPropertyComplianceFireSafetyDeclarationPage(propertyOwnershipId: Long): FireSafetyDeclarationPagePropertyCompliance {
        setJourneyDataInSession(
            PropertyComplianceJourneyFactory.getJourneyDataKey(propertyOwnershipId),
            JourneyPageDataBuilder.beforePropertyComplianceFireSafetyDeclaration().build(),
        )
        navigate(
            PropertyComplianceController.getPropertyCompliancePath(propertyOwnershipId) +
                "/${PropertyComplianceStepId.FireSafetyDeclaration.urlPathSegment}",
        )
        return createValidPage(
            page,
            FireSafetyDeclarationPagePropertyCompliance::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun skipToPropertyComplianceKeepPropertySafePage(propertyOwnershipId: Long): KeepPropertySafePagePropertyCompliance {
        setJourneyDataInSession(
            PropertyComplianceJourneyFactory.getJourneyDataKey(propertyOwnershipId),
            JourneyPageDataBuilder.beforePropertyComplianceKeepPropertySafe().build(),
        )
        navigate(
            PropertyComplianceController.getPropertyCompliancePath(propertyOwnershipId) +
                "/${PropertyComplianceStepId.KeepPropertySafe.urlPathSegment}",
        )
        return createValidPage(
            page,
            KeepPropertySafePagePropertyCompliance::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun skipToPropertyComplianceResponsibilityToTenantsPage(propertyOwnershipId: Long): ResponsibilityToTenantsPagePropertyCompliance {
        setJourneyDataInSession(
            PropertyComplianceJourneyFactory.getJourneyDataKey(propertyOwnershipId),
            JourneyPageDataBuilder.beforePropertyComplianceResponsibilityToTenants().build(),
        )
        navigate(
            PropertyComplianceController.getPropertyCompliancePath(propertyOwnershipId) +
                "/${PropertyComplianceStepId.ResponsibilityToTenants.urlPathSegment}",
        )
        return createValidPage(
            page,
            ResponsibilityToTenantsPagePropertyCompliance::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun skipToPropertyComplianceCheckAnswersPage(propertyOwnershipId: Long): CheckAndSubmitPagePropertyCompliance {
        setJourneyDataInSession(
            PropertyComplianceJourneyFactory.getJourneyDataKey(propertyOwnershipId),
            JourneyPageDataBuilder
                .beforePropertyComplianceCheckAnswers()
                .withResponsibilityToTenantsDeclaration()
                .build(),
        )

        navigate(
            PropertyComplianceController.getPropertyCompliancePath(propertyOwnershipId) +
                "/${PropertyComplianceStepId.CheckAndSubmit.urlPathSegment}",
        )
        return createValidPage(
            page,
            CheckAndSubmitPagePropertyCompliance::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun goToPropertyComplianceUpdateUpdateGasSafetyPage(propertyOwnershipId: Long): UpdateGasSafetyPagePropertyComplianceUpdate {
        navigate(
            PropertyComplianceController.getUpdatePropertyComplianceStepPath(
                propertyOwnershipId,
                PropertyComplianceStepId.UpdateGasSafety,
            ),
        )
        return createValidPage(
            page,
            UpdateGasSafetyPagePropertyComplianceUpdate::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun goToPropertyComplianceUpdateUpdateEicrPage(propertyOwnershipId: Long): UpdateEicrPagePropertyComplianceUpdate {
        navigate(
            PropertyComplianceController.getUpdatePropertyComplianceStepPath(
                propertyOwnershipId,
                PropertyComplianceStepId.UpdateEICR,
            ),
        )
        return createValidPage(
            page,
            UpdateEicrPagePropertyComplianceUpdate::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun goToPropertyComplianceUpdateUpdateEpcPage(propertyOwnershipId: Long): UpdateEpcPagePropertyComplianceUpdate {
        navigate(
            PropertyComplianceController.getUpdatePropertyComplianceStepPath(
                propertyOwnershipId,
                PropertyComplianceStepId.UpdateEpc,
            ),
        )
        return createValidPage(
            page,
            UpdateEpcPagePropertyComplianceUpdate::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun goToLandlordDetails(): LandlordDetailsPage {
        navigate(LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE)
        return createValidPage(page, LandlordDetailsPage::class)
    }

    fun goToLandlordDetailsAsALocalAuthorityUser(id: Long): LocalAuthorityViewLandlordDetailsPage {
        navigate(LandlordDetailsController.getLandlordDetailsForLaUserPath(id))
        return createValidPage(page, LocalAuthorityViewLandlordDetailsPage::class, mapOf("id" to id.toString()))
    }

    fun goToUpdateLandlordDetailsUpdateLookupAddressPage(): LookupAddressFormPageUpdateLandlordDetails {
        navigate("${LandlordDetailsController.UPDATE_ROUTE}/${LandlordDetailsUpdateStepId.LookupEnglandAndWalesAddress.urlPathSegment}")
        return createValidPage(page, LookupAddressFormPageUpdateLandlordDetails::class)
    }

    fun skipToLandlordDetailsUpdateSelectAddressPage(): SelectAddressFormPageUpdateLandlordDetails {
        setJourneyDataInSession(
            LandlordDetailsUpdateJourneyFactory.getJourneyDataKey(LandlordDetailsUpdateStepId.SelectEnglandAndWalesAddress.urlPathSegment),
            JourneyPageDataBuilder.beforeLandlordDetailsUpdateSelectAddress().build(),
        )
        navigate("${LandlordDetailsController.UPDATE_ROUTE}/${LandlordDetailsUpdateStepId.SelectEnglandAndWalesAddress.urlPathSegment}")
        return createValidPage(page, SelectAddressFormPageUpdateLandlordDetails::class)
    }

    fun navigateToLandlordDetailsUpdateNamePage() {
        navigate("${LandlordDetailsController.UPDATE_ROUTE}/${LandlordDetailsUpdateStepId.UpdateName.urlPathSegment}")
    }

    fun navigateToLandlordDetailsUpdateDateOfBirthPage() {
        navigate("${LandlordDetailsController.UPDATE_ROUTE}/${LandlordDetailsUpdateStepId.UpdateDateOfBirth.urlPathSegment}")
    }

    fun goToPropertyDetailsLandlordView(id: Long): PropertyDetailsPageLandlordView {
        navigate(PropertyDetailsController.getPropertyDetailsPath(id, isLaView = false))
        return createValidPage(
            page,
            PropertyDetailsPageLandlordView::class,
            mapOf("propertyOwnershipId" to id.toString()),
        )
    }

    fun goToPropertyDetailsLocalAuthorityView(id: Long): PropertyDetailsPageLocalAuthorityView {
        navigate(PropertyDetailsController.getPropertyDetailsPath(id, isLaView = true))
        return createValidPage(
            page,
            PropertyDetailsPageLocalAuthorityView::class,
            mapOf("propertyOwnershipId" to id.toString()),
        )
    }

    fun goToPropertyDetailsUpdateOwnershipTypePage(propertyOwnershipId: Long): OwnershipTypeFormPagePropertyDetailsUpdate {
        navigate(
            PropertyDetailsController.getUpdatePropertyDetailsPath(propertyOwnershipId) +
                "/${UpdatePropertyDetailsStepId.UpdateOwnershipType.urlPathSegment}",
        )
        return createValidPage(
            page,
            OwnershipTypeFormPagePropertyDetailsUpdate::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun skipToPropertyDetailsUpdateCheckOccupancyToOccupiedAnswersPage(
        propertyOwnershipId: Long,
    ): CheckOccupancyAnswersPagePropertyDetailsUpdate {
        setJourneyDataInSession(
            PropertyDetailsUpdateJourneyFactory.getJourneyDataKey(
                propertyOwnershipId,
                UpdatePropertyDetailsStepId.CheckYourOccupancyAnswers.urlPathSegment,
            ),
            JourneyDataBuilder()
                .withNewOccupants()
                .build(),
        )
        return goToPropertyDetailsUpdateCheckOccupancyAnswersPage(propertyOwnershipId)
    }

    fun skipToPropertyDetailsUpdateCheckOccupancyToVacantAnswersPage(
        propertyOwnershipId: Long,
    ): CheckOccupancyAnswersPagePropertyDetailsUpdate {
        setJourneyDataInSession(
            PropertyDetailsUpdateJourneyFactory.getJourneyDataKey(
                propertyOwnershipId,
                UpdatePropertyDetailsStepId.CheckYourOccupancyAnswers.urlPathSegment,
            ),
            JourneyDataBuilder()
                .withIsOccupiedUpdate(false)
                .build(),
        )
        return goToPropertyDetailsUpdateCheckOccupancyAnswersPage(propertyOwnershipId)
    }

    fun goToPropertyDetailsUpdateOccupancy(propertyOwnershipId: Long): OccupancyFormPagePropertyDetailsUpdate {
        navigate(
            PropertyDetailsController.getUpdatePropertyDetailsPath(propertyOwnershipId) +
                "/${UpdatePropertyDetailsStepId.UpdateOccupancy.urlPathSegment}",
        )
        return createValidPage(
            page,
            OccupancyFormPagePropertyDetailsUpdate::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun goToPropertyDetailsUpdateCheckOccupancyAnswersPage(propertyOwnershipId: Long): CheckOccupancyAnswersPagePropertyDetailsUpdate {
        navigate(
            PropertyDetailsController.getUpdatePropertyDetailsPath(propertyOwnershipId) +
                "/${UpdatePropertyDetailsStepId.CheckYourOccupancyAnswers.urlPathSegment}",
        )
        return createValidPage(
            page,
            CheckOccupancyAnswersPagePropertyDetailsUpdate::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun skipToPropertyDetailsUpdateCheckHouseholdAnswersPage(propertyOwnershipId: Long): CheckHouseholdsAnswersPagePropertyDetailsUpdate {
        setJourneyDataInSession(
            PropertyDetailsUpdateJourneyFactory.getJourneyDataKey(
                propertyOwnershipId,
                UpdatePropertyDetailsStepId.CheckYourHouseholdsAnswers.urlPathSegment,
            ),
            JourneyDataBuilder()
                .withNumberOfHouseholdsUpdate(1)
                .withNumberOfHouseholdsPeopleUpdate(3)
                .build(),
        )
        navigate(
            PropertyDetailsController.getUpdatePropertyDetailsPath(propertyOwnershipId) +
                "/${UpdatePropertyDetailsStepId.CheckYourHouseholdsAnswers.urlPathSegment}",
        )
        return createValidPage(
            page,
            CheckHouseholdsAnswersPagePropertyDetailsUpdate::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun skipToPropertyDetailsUpdateCheckPeopleAnswersPage(propertyOwnershipId: Long): CheckPeopleAnswersPagePropertyDetailsUpdate {
        setJourneyDataInSession(
            PropertyDetailsUpdateJourneyFactory.getJourneyDataKey(
                propertyOwnershipId,
                UpdatePropertyDetailsStepId.CheckYourPeopleAnswers.urlPathSegment,
            ),
            JourneyDataBuilder()
                .withNumberOfPeopleUpdate(3)
                .build(),
        )
        navigate(
            PropertyDetailsController.getUpdatePropertyDetailsPath(propertyOwnershipId) +
                "/${UpdatePropertyDetailsStepId.CheckYourPeopleAnswers.urlPathSegment}",
        )
        return createValidPage(
            page,
            CheckPeopleAnswersPagePropertyDetailsUpdate::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun goToPropertyDetailsUpdateNumberOfPeoplePage(propertyOwnershipId: Long): NumberOfPeopleFormPagePropertyDetailsUpdate {
        navigate(
            PropertyDetailsController.getUpdatePropertyDetailsPath(propertyOwnershipId) +
                "/${UpdatePropertyDetailsStepId.UpdateNumberOfPeople.urlPathSegment}",
        )
        return createValidPage(
            page,
            NumberOfPeopleFormPagePropertyDetailsUpdate::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun goToPropertyDeregistrationAreYouSurePage(propertyOwnershipId: Long): AreYouSureFormPagePropertyDeregistration {
        navigate(DeregisterPropertyController.getPropertyDeregistrationPath(propertyOwnershipId))
        return createValidPage(
            page,
            AreYouSureFormPagePropertyDeregistration::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun skipToPropertyDeregistrationReasonPage(propertyOwnershipId: Long): ReasonPagePropertyDeregistration {
        setJourneyDataInSession(
            PropertyDeregistrationJourneyFactory.getJourneyKey(propertyOwnershipId),
            JourneyPageDataBuilder.beforePropertyDeregistrationReason().build(),
        )
        navigate(
            DeregisterPropertyController.getPropertyDeregistrationBasePath(propertyOwnershipId) +
                "/${DeregisterPropertyStepId.Reason.urlPathSegment}",
        )
        return createValidPage(
            page,
            ReasonPagePropertyDeregistration::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    fun goToLandlordDeregistrationAreYouSurePage(): AreYouSureFormPageLandlordDeregistration {
        navigate("${DeregisterLandlordController.LANDLORD_DEREGISTRATION_ROUTE}/${DeregisterLandlordStepId.AreYouSure.urlPathSegment}")
        return createValidPage(page, AreYouSureFormPageLandlordDeregistration::class)
    }

    fun goToLocalAuthorityDashboard(): LocalAuthorityDashboardPage {
        navigate(LOCAL_AUTHORITY_DASHBOARD_URL)
        return createValidPage(page, LocalAuthorityDashboardPage::class)
    }

    fun goToGeneratePasscodePage(): GeneratePasscodePage {
        navigate(GENERATE_PASSCODE_URL)
        return createValidPage(page, GeneratePasscodePage::class)
    }

    fun navigateToPasscodeEntryPage() {
        navigate(PASSCODE_ENTRY_ROUTE)
    }

    fun navigateToInvalidPasscodePage() {
        navigate(INVALID_PASSCODE_ROUTE)
    }

    fun navigateToLandlordDashboard() {
        navigate(LANDLORD_DASHBOARD_URL)
    }

    fun goToLandlordDashboard(): LandlordDashboardPage {
        navigate(LANDLORD_DASHBOARD_URL)
        return createValidPage(page, LandlordDashboardPage::class)
    }

    fun goToLandlordPrivacyNoticePage(): LandlordPrivacyNoticePage {
        navigate(LANDLORD_PRIVACY_NOTICE_ROUTE)
        return createValidPage(page, LandlordPrivacyNoticePage::class)
    }

    fun goToLandlordIncompleteProperties(): LandlordIncompletePropertiesPage {
        navigate(INCOMPLETE_PROPERTIES_URL)
        return createValidPage(page, LandlordIncompletePropertiesPage::class)
    }

    fun goToComplianceActions(): ComplianceActionsPage {
        navigate(COMPLIANCE_ACTIONS_URL)
        return createValidPage(page, ComplianceActionsPage::class)
    }

    fun goToDeleteIncompletePropertyRegistrationAreYouSurePage(contextId: String): DeleteIncompletePropertyRegistrationAreYouSurePage {
        navigate(
            "/$LANDLORD_PATH_SEGMENT/$DELETE_INCOMPLETE_PROPERTY_PATH_SEGMENT" +
                "?$CONTEXT_ID_URL_PARAMETER=$contextId",
        )
        return createValidPage(
            page,
            DeleteIncompletePropertyRegistrationAreYouSurePage::class,
            mapOf("contextId" to contextId),
        )
    }

    fun goToInviteLaAdmin(): InviteLaAdminPage {
        navigate(ManageLocalAuthorityAdminsController.INVITE_LA_ADMIN_ROUTE)
        return createValidPage(page, InviteLaAdminPage::class)
    }

    fun goToManageLaAdminsPage(): ManageLaAdminsPage {
        navigate(ManageLocalAuthorityAdminsController.MANAGE_LA_ADMINS_ROUTE)
        return createValidPage(page, ManageLaAdminsPage::class)
    }

    fun goToCookiesPage(): CookiesPage {
        navigate(COOKIES_ROUTE)
        return createValidPage(page, CookiesPage::class)
    }

    fun goToLandlordBetaFeedbackPage(): LandlordBetaFeedbackPage {
        navigate(BetaFeedbackController.LANDLORD_FEEDBACK_URL)
        return createValidPage(page, LandlordBetaFeedbackPage::class)
    }

    fun goToLocalCouncilBetaFeedbackPage(): LocalCouncilBetaFeedbackPage {
        navigate(BetaFeedbackController.LOCAL_AUTHORITY_FEEDBACK_URL)
        return createValidPage(page, LocalCouncilBetaFeedbackPage::class)
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
