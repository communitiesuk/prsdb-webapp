package uk.gov.communities.prsdb.webapp.integration.pageObjects

import com.microsoft.playwright.Page
import com.microsoft.playwright.Response
import com.microsoft.playwright.options.RequestOptions
import uk.gov.communities.prsdb.webapp.constants.CANCEL_INVITATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.CONTEXT_ID_URL_PARAMETER
import uk.gov.communities.prsdb.webapp.constants.DELETE_ADMIN_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.DELETE_INCOMPLETE_PROPERTY_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.EDIT_ADMIN_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.TASK_LIST_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.TOKEN
import uk.gov.communities.prsdb.webapp.controllers.BetaFeedbackController
import uk.gov.communities.prsdb.webapp.controllers.CookiesController.Companion.COOKIES_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.DeregisterLandlordController
import uk.gov.communities.prsdb.webapp.controllers.DeregisterPropertyController
import uk.gov.communities.prsdb.webapp.controllers.ExampleFeatureFlagTestController
import uk.gov.communities.prsdb.webapp.controllers.GeneratePasscodeController.Companion.GENERATE_PASSCODE_URL
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.COMPLIANCE_ACTIONS_URL
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.INCOMPLETE_PROPERTIES_URL
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.controllers.LandlordPrivacyNoticeController.Companion.LANDLORD_PRIVACY_NOTICE_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.LocalCouncilDashboardController.Companion.LOCAL_COUNCIL_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalCouncilAdminsController
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalCouncilAdminsController.Companion.SYSTEM_OPERATOR_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalCouncilUsersController.Companion.getLocalCouncilInviteNewUserRoute
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalCouncilUsersController.Companion.getLocalCouncilManageUsersRoute
import uk.gov.communities.prsdb.webapp.controllers.NewRegisterPropertyController
import uk.gov.communities.prsdb.webapp.controllers.PasscodeEntryController.Companion.INVALID_PASSCODE_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.PasscodeEntryController.Companion.PASSCODE_ENTRY_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.controllers.RegisterLocalCouncilUserController
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.controllers.SearchRegisterController
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.LandlordDetailsUpdateJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.LandlordRegistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.LocalCouncilUserRegistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.PropertyComplianceJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.PropertyDeregistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.PropertyDetailsUpdateJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterLandlordStepId
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterPropertyStepId
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordDetailsUpdateStepId
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLocalCouncilUserStepId
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.CancelLocalCouncilAdminInvitationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ComplianceActionsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.CookiesPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.DeleteIncompletePropertyRegistrationAreYouSurePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.DeleteLocalCouncilAdminPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.EditLocalCouncilAdminPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.GeneratePasscodePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.InviteLocalCouncilAdminPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.InviteNewLocalCouncilUserPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordIncompletePropertiesPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordPrivacyNoticePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LocalCouncilDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LocalCouncilViewLandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LookupAddressFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLocalCouncilAdminsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLocalCouncilUsersPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PasscodeEntryPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLocalCouncilView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.SearchLandlordRegisterPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.SearchPropertyRegisterPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.SelectAddressFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.createValidPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.betaFeedbackPages.LandlordBetaFeedbackPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.betaFeedbackPages.LocalCouncilBetaFeedbackPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.featureFlaggedExamplePages.FeatureFlaggedServiceTestPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.featureFlaggedExamplePages.FeatureFourDisabledPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.featureFlaggedExamplePages.FeatureFourEnabledPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.featureFlaggedExamplePages.FeatureThreeDisabledPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.featureFlaggedExamplePages.FeatureThreeEnabledPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.featureFlaggedExamplePages.FeatureTwoDisabledPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.featureFlaggedExamplePages.FeatureTwoEnabledPage
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
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.localCouncilUserRegistrationJourneyPages.CheckAnswersPageLocalCouncilUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.localCouncilUserRegistrationJourneyPages.EmailFormPageLocalCouncilUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.localCouncilUserRegistrationJourneyPages.NameFormPageLocalCouncilUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.localCouncilUserRegistrationJourneyPages.PrivacyNoticePageLocalCouncilUserRegistration
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
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.BillsIncludedFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CheckAnswersPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.HmoAdditionalLicenceFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.HmoMandatoryLicenceFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.LicensingTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.LookupAddressFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.ManualAddressFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.NumberOfBedroomsFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.NumberOfHouseholdsFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.NumberOfPeopleFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.OccupancyFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.OwnershipTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.PropertyTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.RegisterPropertyStartPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.RentIncludesBillsFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SelectAddressFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SelectLocalCouncilFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SelectiveLicenceFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.TaskListPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.testHelpers.api.controllers.SessionController
import uk.gov.communities.prsdb.webapp.testHelpers.api.requestModels.SetJourneyDataRequestModel
import uk.gov.communities.prsdb.webapp.testHelpers.api.requestModels.SetJourneyStateRequestModel
import uk.gov.communities.prsdb.webapp.testHelpers.api.requestModels.StoreInvitationTokenRequestModel
import uk.gov.communities.prsdb.webapp.testHelpers.builders.JourneyDataBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.builders.JourneyPageDataBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyStateSessionBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData
import java.util.UUID
import kotlin.test.assertTrue

class Navigator(
    private val page: Page,
    private val port: Int,
) {
    fun goToManageLocalCouncilUsers(councilId: Int): ManageLocalCouncilUsersPage {
        navigate(getLocalCouncilManageUsersRoute(councilId))
        return createValidPage(page, ManageLocalCouncilUsersPage::class)
    }

    fun goToInviteNewLocalCouncilUser(councilId: Int): InviteNewLocalCouncilUserPage {
        navigate(getLocalCouncilInviteNewUserRoute(councilId))
        return createValidPage(page, InviteNewLocalCouncilUserPage::class)
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

    fun navigateToLocalCouncilUserRegistrationAcceptInvitationRoute(token: String) {
        navigate("${RegisterLocalCouncilUserController.LOCAL_COUNCIL_USER_REGISTRATION_ROUTE}?$TOKEN=$token")
    }

    fun navigateToLocalCouncilUserRegistrationLandingPage(token: UUID) {
        storeInvitationTokenInSession(token)
        navigate(
            "${RegisterLocalCouncilUserController.LOCAL_COUNCIL_USER_REGISTRATION_ROUTE}" +
                "/${RegisterLocalCouncilUserStepId.LandingPage.urlPathSegment}",
        )
    }

    fun skipToLocalCouncilUserRegistrationPrivacyNoticePage(token: UUID): PrivacyNoticePageLocalCouncilUserRegistration {
        storeInvitationTokenInSession(token)
        setJourneyDataInSession(
            LocalCouncilUserRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyDataBuilder().withLandingPageReached().build(),
        )
        navigate(
            "${
                RegisterLocalCouncilUserController.LOCAL_COUNCIL_USER_REGISTRATION_ROUTE}" +
                "/${RegisterLocalCouncilUserStepId.PrivacyNotice.urlPathSegment
                }",
        )
        return createValidPage(page, PrivacyNoticePageLocalCouncilUserRegistration::class)
    }

    fun skipToLocalCouncilUserRegistrationNameFormPage(token: UUID): NameFormPageLocalCouncilUserRegistration {
        storeInvitationTokenInSession(token)
        setJourneyDataInSession(
            LocalCouncilUserRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyPageDataBuilder.beforeLocalCouncilUserRegistrationName().build(),
        )
        navigate(
            "${RegisterLocalCouncilUserController.LOCAL_COUNCIL_USER_REGISTRATION_ROUTE}" +
                "/${RegisterLocalCouncilUserStepId.Name.urlPathSegment}",
        )
        return createValidPage(page, NameFormPageLocalCouncilUserRegistration::class)
    }

    fun skipToLocalCouncilUserRegistrationEmailFormPage(token: UUID): EmailFormPageLocalCouncilUserRegistration {
        storeInvitationTokenInSession(token)
        setJourneyDataInSession(
            LocalCouncilUserRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyPageDataBuilder.beforeLocalCouncilUserRegistrationEmail().build(),
        )
        navigate(
            "${RegisterLocalCouncilUserController.LOCAL_COUNCIL_USER_REGISTRATION_ROUTE}" +
                "/${RegisterLocalCouncilUserStepId.Email.urlPathSegment}",
        )
        return createValidPage(page, EmailFormPageLocalCouncilUserRegistration::class)
    }

    fun skipToLocalCouncilUserRegistrationCheckAnswersPage(token: UUID): CheckAnswersPageLocalCouncilUserRegistration {
        storeInvitationTokenInSession(token)
        setJourneyDataInSession(
            LocalCouncilUserRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            JourneyPageDataBuilder.beforeLocalCouncilUserRegistrationCheckAnswers().build(),
        )
        navigate(
            "${
                RegisterLocalCouncilUserController.LOCAL_COUNCIL_USER_REGISTRATION_ROUTE}" +
                "/${RegisterLocalCouncilUserStepId.CheckAnswers.urlPathSegment
                }",
        )
        return createValidPage(page, CheckAnswersPageLocalCouncilUserRegistration::class)
    }

    fun navigateToLocalCouncilUserRegistrationConfirmationPage() {
        navigate("${RegisterLocalCouncilUserController.LOCAL_COUNCIL_USER_REGISTRATION_ROUTE}/$CONFIRMATION_PATH_SEGMENT")
    }

    fun goToPropertyRegistrationStartPage(): RegisterPropertyStartPage {
        navigate(NewRegisterPropertyController.PROPERTY_REGISTRATION_ROUTE)
        return createValidPage(page, RegisterPropertyStartPage::class)
    }

    fun goToPropertyRegistrationTaskList(): TaskListPagePropertyRegistration {
        navigate(
            "${NewRegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/" +
                "$TASK_LIST_PATH_SEGMENT?" +
                "journeyId=${TEST_JOURNEY_ID}",
        )
        return createValidPage(page, TaskListPagePropertyRegistration::class)
    }

    fun goToPropertyRegistrationLookupAddressPage(): LookupAddressFormPagePropertyRegistration {
        navigate("${NewRegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${RegisterPropertyStepId.LookupAddress.urlPathSegment}")
        return createValidPage(page, LookupAddressFormPagePropertyRegistration::class)
    }

    fun skipToPropertyRegistrationSelectAddressPage(
        customLookedUpAddresses: List<AddressDataModel>? = null,
    ): SelectAddressFormPagePropertyRegistration {
        setJourneyStateInSession(
            PropertyStateSessionBuilder.beforePropertyRegistrationSelectAddress(customLookedUpAddresses).build(),
        )
        navigateToPropertyRegistrationJourneyStep(RegisterPropertyStepId.SelectAddress.urlPathSegment)
        return createValidPage(page, SelectAddressFormPagePropertyRegistration::class)
    }

    fun skipToPropertyRegistrationManualAddressPage(): ManualAddressFormPagePropertyRegistration {
        setJourneyStateInSession(
            PropertyStateSessionBuilder.beforePropertyRegistrationManualAddress().build(),
        )
        navigateToPropertyRegistrationJourneyStep(RegisterPropertyStepId.ManualAddress.urlPathSegment)
        return createValidPage(page, ManualAddressFormPagePropertyRegistration::class)
    }

    fun skipToPropertyRegistrationSelectLocalCouncilPage(): SelectLocalCouncilFormPagePropertyRegistration {
        setJourneyStateInSession(
            PropertyStateSessionBuilder.beforePropertyRegistrationSelectLocalCouncil().build(),
        )
        navigateToPropertyRegistrationJourneyStep(RegisterPropertyStepId.LocalCouncil.urlPathSegment)
        return createValidPage(page, SelectLocalCouncilFormPagePropertyRegistration::class)
    }

    fun skipToPropertyRegistrationPropertyTypePage(): PropertyTypeFormPagePropertyRegistration {
        setJourneyStateInSession(
            PropertyStateSessionBuilder.beforePropertyRegistrationPropertyType().build(),
        )
        navigateToPropertyRegistrationJourneyStep(RegisterPropertyStepId.PropertyType.urlPathSegment)
        return createValidPage(page, PropertyTypeFormPagePropertyRegistration::class)
    }

    fun skipToPropertyRegistrationOwnershipTypePage(): OwnershipTypeFormPagePropertyRegistration {
        setJourneyStateInSession(
            PropertyStateSessionBuilder.beforePropertyRegistrationOwnershipType().build(),
        )
        navigateToPropertyRegistrationJourneyStep(RegisterPropertyStepId.OwnershipType.urlPathSegment)
        return createValidPage(page, OwnershipTypeFormPagePropertyRegistration::class)
    }

    fun skipToPropertyRegistrationLicensingTypePage(): LicensingTypeFormPagePropertyRegistration {
        setJourneyStateInSession(
            PropertyStateSessionBuilder.beforePropertyRegistrationLicensingType().build(),
        )
        navigateToPropertyRegistrationJourneyStep(RegisterPropertyStepId.LicensingType.urlPathSegment)
        return createValidPage(page, LicensingTypeFormPagePropertyRegistration::class)
    }

    fun skipToPropertyRegistrationSelectiveLicencePage(): SelectiveLicenceFormPagePropertyRegistration {
        setJourneyStateInSession(
            PropertyStateSessionBuilder.beforePropertyRegistrationSelectiveLicence().build(),
        )
        navigateToPropertyRegistrationJourneyStep(RegisterPropertyStepId.SelectiveLicence.urlPathSegment)
        return createValidPage(page, SelectiveLicenceFormPagePropertyRegistration::class)
    }

    fun skipToPropertyRegistrationHmoMandatoryLicencePage(): HmoMandatoryLicenceFormPagePropertyRegistration {
        setJourneyStateInSession(
            PropertyStateSessionBuilder.beforePropertyRegistrationHmoMandatoryLicence().build(),
        )
        navigateToPropertyRegistrationJourneyStep(RegisterPropertyStepId.HmoMandatoryLicence.urlPathSegment)
        return createValidPage(page, HmoMandatoryLicenceFormPagePropertyRegistration::class)
    }

    fun skipToPropertyRegistrationHmoAdditionalLicencePage(): HmoAdditionalLicenceFormPagePropertyRegistration {
        setJourneyStateInSession(
            PropertyStateSessionBuilder.beforePropertyRegistrationHmoAdditionalLicence().build(),
        )
        navigateToPropertyRegistrationJourneyStep(RegisterPropertyStepId.HmoAdditionalLicence.urlPathSegment)
        return createValidPage(page, HmoAdditionalLicenceFormPagePropertyRegistration::class)
    }

    fun skipToPropertyRegistrationOccupancyPage(): OccupancyFormPagePropertyRegistration {
        setJourneyStateInSession(
            PropertyStateSessionBuilder.beforePropertyRegistrationOccupancy().build(),
        )
        navigateToPropertyRegistrationJourneyStep(RegisterPropertyStepId.Occupancy.urlPathSegment)
        return createValidPage(page, OccupancyFormPagePropertyRegistration::class)
    }

    fun skipToPropertyRegistrationHouseholdsPage(): NumberOfHouseholdsFormPagePropertyRegistration {
        setJourneyStateInSession(
            PropertyStateSessionBuilder.beforePropertyRegistrationHouseholds().build(),
        )
        navigateToPropertyRegistrationJourneyStep(RegisterPropertyStepId.NumberOfHouseholds.urlPathSegment)
        return createValidPage(page, NumberOfHouseholdsFormPagePropertyRegistration::class)
    }

    fun skipToPropertyRegistrationPeoplePage(): NumberOfPeopleFormPagePropertyRegistration {
        setJourneyStateInSession(
            PropertyStateSessionBuilder.beforePropertyRegistrationPeople().build(),
        )
        navigateToPropertyRegistrationJourneyStep(RegisterPropertyStepId.NumberOfPeople.urlPathSegment)
        return createValidPage(page, NumberOfPeopleFormPagePropertyRegistration::class)
    }

    fun skipToPropertyRegistrationBedroomsPage(): NumberOfBedroomsFormPagePropertyRegistration {
        setJourneyStateInSession(
            PropertyStateSessionBuilder.beforePropertyRegistrationBedrooms().build(),
        )
        navigateToPropertyRegistrationJourneyStep(RegisterPropertyStepId.NumberOfBedrooms.urlPathSegment)
        return createValidPage(page, NumberOfBedroomsFormPagePropertyRegistration::class)
    }

    fun skipToPropertyRegistrationRentIncludesBillsPage(): RentIncludesBillsFormPagePropertyRegistration {
        setJourneyStateInSession(
            PropertyStateSessionBuilder.beforePropertyRegistrationRentIncludesBills().build(),
        )
        navigateToPropertyRegistrationJourneyStep(RegisterPropertyStepId.RentIncludesBills.urlPathSegment)
        return createValidPage(page, RentIncludesBillsFormPagePropertyRegistration::class)
    }

    fun skipToPropertyRegistrationBillsIncludedPage(): BillsIncludedFormPagePropertyRegistration {
        setJourneyStateInSession(
            PropertyStateSessionBuilder.beforePropertyRegistrationBillsIncluded().build(),
        )
        navigateToPropertyRegistrationJourneyStep(RegisterPropertyStepId.BillsIncluded.urlPathSegment)
        return createValidPage(page, BillsIncludedFormPagePropertyRegistration::class)
    }

    fun skipToPropertyRegistrationCheckAnswersPage(): CheckAnswersPagePropertyRegistration {
        setJourneyStateInSession(PropertyStateSessionBuilder.beforePropertyRegistrationCheckAnswers().build())
        navigateToPropertyRegistrationJourneyStep(RegisterPropertyStepId.CheckAnswers.urlPathSegment)
        return createValidPage(page, CheckAnswersPagePropertyRegistration::class)
    }

    private fun navigateToPropertyRegistrationJourneyStep(segment: String) =
        navigate("${NewRegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/$segment?journeyId=$TEST_JOURNEY_ID")

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

    fun goToLandlordDetailsAsALocalCouncilUser(id: Long): LocalCouncilViewLandlordDetailsPage {
        navigate(LandlordDetailsController.getLandlordDetailsForLocalCouncilUserPath(id))
        return createValidPage(page, LocalCouncilViewLandlordDetailsPage::class, mapOf("id" to id.toString()))
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
        navigate(PropertyDetailsController.getPropertyDetailsPath(id, isLocalCouncilView = false))
        return createValidPage(
            page,
            PropertyDetailsPageLandlordView::class,
            mapOf("propertyOwnershipId" to id.toString()),
        )
    }

    fun goToPropertyDetailsLocalCouncilView(id: Long): PropertyDetailsPageLocalCouncilView {
        navigate(PropertyDetailsController.getPropertyDetailsPath(id, isLocalCouncilView = true))
        return createValidPage(
            page,
            PropertyDetailsPageLocalCouncilView::class,
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

    fun goToLocalCouncilDashboard(): LocalCouncilDashboardPage {
        navigate(LOCAL_COUNCIL_DASHBOARD_URL)
        return createValidPage(page, LocalCouncilDashboardPage::class)
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

    fun goToInviteLocalCouncilAdmin(): InviteLocalCouncilAdminPage {
        navigate(ManageLocalCouncilAdminsController.INVITE_LOCAL_COUNCIL_ADMIN_ROUTE)
        return createValidPage(page, InviteLocalCouncilAdminPage::class)
    }

    fun goToManageLocalCouncilAdminsPage(): ManageLocalCouncilAdminsPage {
        navigate(ManageLocalCouncilAdminsController.MANAGE_LOCAL_COUNCIL_ADMINS_ROUTE)
        return createValidPage(page, ManageLocalCouncilAdminsPage::class)
    }

    fun goToEditAdminsPage(localCouncilAdminId: Long): EditLocalCouncilAdminPage {
        navigate("$SYSTEM_OPERATOR_ROUTE/$EDIT_ADMIN_PATH_SEGMENT/$localCouncilAdminId")
        return createValidPage(page, EditLocalCouncilAdminPage::class, mapOf("localCouncilAdminId" to localCouncilAdminId.toString()))
    }

    fun goToDeleteLocalCouncilAdminPage(localCouncilAdminId: Long): DeleteLocalCouncilAdminPage {
        navigate("$SYSTEM_OPERATOR_ROUTE/$DELETE_ADMIN_PATH_SEGMENT/$localCouncilAdminId")
        return createValidPage(page, DeleteLocalCouncilAdminPage::class, mapOf("localCouncilAdminId" to localCouncilAdminId.toString()))
    }

    fun goToCancelAdminInvitePage(invitationId: Long): CancelLocalCouncilAdminInvitationPage {
        navigate("$SYSTEM_OPERATOR_ROUTE/$CANCEL_INVITATION_PATH_SEGMENT/$invitationId")
        return createValidPage(page, CancelLocalCouncilAdminInvitationPage::class, mapOf("invitationId" to invitationId.toString()))
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
        navigate(BetaFeedbackController.LOCAL_COUNCIL_FEEDBACK_URL)
        return createValidPage(page, LocalCouncilBetaFeedbackPage::class)
    }

    // TODO PRSD-1683 - delete example feature flag implementation when no longer needed
    fun goToFeatureFlaggedServiceTestPage(): FeatureFlaggedServiceTestPage {
        navigate(ExampleFeatureFlagTestController.FEATURED_FLAGGED_SERVICE_TEST_URL_ROUTE)
        return createValidPage(page, FeatureFlaggedServiceTestPage::class)
    }

    fun goToFeatureFlagTwoEnabledPage(): FeatureTwoEnabledPage {
        navigate(ExampleFeatureFlagTestController.FEATURE_FLAGGED_GROUPED_ENDPOINT_FLAG_2_ROUTE)
        return createValidPage(page, FeatureTwoEnabledPage::class)
    }

    fun goToFeatureFlagTwoDisabledPage(): FeatureTwoDisabledPage {
        navigate(ExampleFeatureFlagTestController.INVERSE_FEATURE_FLAGGED_GROUPED_ENDPOINT_FLAG_2_ROUTE)
        return createValidPage(page, FeatureTwoDisabledPage::class)
    }

    fun goToFeatureFlagThreeEnabledPage(): FeatureThreeEnabledPage {
        navigate(ExampleFeatureFlagTestController.FEATURE_FLAGGED_GROUPED_ENDPOINT_FLAG_3_ROUTE)
        return createValidPage(page, FeatureThreeEnabledPage::class)
    }

    fun goToFeatureFlagThreeDisabledPage(): FeatureThreeDisabledPage {
        navigate(ExampleFeatureFlagTestController.INVERSE_FEATURE_FLAGGED_GROUPED_ENDPOINT_FLAG_3_ROUTE)
        return createValidPage(page, FeatureThreeDisabledPage::class)
    }

    fun goToFeatureFlagFourEnabledPage(): FeatureFourEnabledPage {
        navigate(ExampleFeatureFlagTestController.FEATURE_FLAGGED_ENDPOINT_WITH_RELEASE_DATE_ROUTE)
        return createValidPage(page, FeatureFourEnabledPage::class)
    }

    fun goToFeatureFlagFourDisabledPage(): FeatureFourDisabledPage {
        navigate(ExampleFeatureFlagTestController.INVERSE_FEATURE_FLAGGED_ENDPOINT_WITH_RELEASE_DATE_ROUTE)
        return createValidPage(page, FeatureFourDisabledPage::class)
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

    private fun setJourneyStateInSession(journeyState: Map<String, Any>) {
        val response =
            page.request().post(
                "http://localhost:$port/${SessionController.SET_JOURNEY_STATE_ROUTE}",
                RequestOptions.create().setData(SetJourneyStateRequestModel(TEST_JOURNEY_ID, journeyState)),
            )
        assertTrue(response.ok(), "Failed to set journey state. Received status code: ${response.status()}")
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

    companion object {
        const val TEST_JOURNEY_ID = "test-journey-id"
    }
}
