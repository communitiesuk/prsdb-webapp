package uk.gov.communities.prsdb.webapp.integration

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.microsoft.playwright.Page
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.ORGANISATION_LANDLORD_REGISTRATION
import uk.gov.communities.prsdb.webapp.constants.enums.CharityRegulator
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.CheckAnswersPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.ConfirmIdentityFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.ConfirmationPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.CountryOfResidenceFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.DateOfBirthFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.EmailFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.IdentityNotVerifiedFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.LandlordTypeFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.LeadTrusteeAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.LeadTrusteeDobFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.LeadTrusteeEmailFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.LeadTrusteeNameFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.LeadTrusteePhoneFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.LookupAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.ManualAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.NameFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgCharityFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgCharityNumberEnglandAndWalesFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgCharityRegisteredWithFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgCompaniesHouseFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgDirectorsFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgEmailFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgLandlordCyaPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgMainContactFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgNameFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgPhoneNumberFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgTrusteesFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgTypeFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.PhoneNumberFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.PrivacyNoticePageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.SelectAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.organisationLandlordRegistrationJourneyPages.OrgCompanyNumberFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.organisationLandlordRegistrationJourneyPages.YourDetailsPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.VerifiedIdentityDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LandlordRegistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.testHelpers.extensions.getFormattedUkPhoneNumber
import java.net.URI
import java.time.LocalDate
import kotlin.test.assertNotNull

class LandlordRegistrationJourneyTests : IntegrationTestWithMutableData("data-mockuser-not-landlord.sql") {
    private val phoneNumberUtil = PhoneNumberUtil.getInstance()
    private val absoluteLandlordUrl = "www.prsd.gov.uk/landlord"

    @Autowired
    private lateinit var landlordService: LandlordService

    @MockitoBean
    private lateinit var confirmationEmailSender: EmailNotificationService<LandlordRegistrationConfirmationEmail>

    @MockitoBean
    private lateinit var absoluteUrlProvider: AbsoluteUrlProvider

    @BeforeEach
    fun setup() {
        whenever(absoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI(absoluteLandlordUrl))
        featureFlagManager.disable(ORGANISATION_LANDLORD_REGISTRATION)
    }

    @Test
    fun `User can navigate the whole journey if pages are correctly filled in (verified, England or Wales, selected address)`(page: Page) {
        // Set up mock One Login data
        val verifiedIdentity = VerifiedIdentityDataModel("name", LocalDate.now())
        whenever(identityService.getVerifiedIdentityData(any())).thenReturn(verifiedIdentity)

        val landlordRegistrationStartPage = navigator.goToLandlordRegistrationServiceInformationStartPage()
        landlordRegistrationStartPage.startButton.clickAndWait()

        val privacyNoticePage = assertPageIs(page, PrivacyNoticePageLandlordRegistration::class)
        privacyNoticePage.agreeAndSubmit()

        val confirmIdentityPage = assertPageIs(page, ConfirmIdentityFormPageLandlordRegistration::class)
        confirmIdentityPage.confirm()

        val emailPage = assertPageIs(page, EmailFormPageLandlordRegistration::class)
        emailPage.submitEmail("test@example.com")

        val phoneNumPage = assertPageIs(page, PhoneNumberFormPageLandlordRegistration::class)
        phoneNumPage.submitPhoneNumber("07123456789")

        val countryOfResidencePage = assertPageIs(page, CountryOfResidenceFormPageLandlordRegistration::class)
        countryOfResidencePage.submitUk()

        val lookupAddressPage = assertPageIs(page, LookupAddressFormPageLandlordRegistration::class)
        lookupAddressPage.submitPostcodeAndBuildingNameOrNumber("EG1 2AA", "1")

        val selectAddressPage = assertPageIs(page, SelectAddressFormPageLandlordRegistration::class)
        selectAddressPage.selectAddressAndSubmit("1 PRSDB Square, EG1 2AA")

        val checkAnswersPage = assertPageIs(page, CheckAnswersPageLandlordRegistration::class)
        checkAnswersPage.confirmAndSubmit()

        val createdLandlord = assertNotNull(landlordService.retrieveLandlordByBaseUserId("urn:fdc:gov.uk:2022:UVWXY"))
        val createdLandlordRegNum =
            RegistrationNumberDataModel.fromRegistrationNumber(createdLandlord.registrationNumber)

        verify(confirmationEmailSender).sendEmail(
            "test@example.com",
            LandlordRegistrationConfirmationEmail(createdLandlordRegNum.toString(), absoluteLandlordUrl),
        )

        val confirmationPage = assertPageIs(page, ConfirmationPageLandlordRegistration::class)
        assertEquals(createdLandlordRegNum.toString(), confirmationPage.confirmationBanner.registrationNumberText)
        confirmationPage.goToDashboardLink.clickAndWait()
        val dashboard = assertPageIs(page, LandlordDashboardPage::class)

        assertThat(dashboard.dashboardBannerSubHeading).containsText("Landlord registration number")
    }

    @Test
    fun `User can navigate the whole journey if pages are correctly filled in (unverified, England or Wales, manual address)`(page: Page) {
        // Set up no identity data from One login
        whenever(identityService.getVerifiedIdentityData(any())).thenReturn(null)

        val landlordRegistrationStartPage = navigator.goToLandlordRegistrationServiceInformationStartPage()
        landlordRegistrationStartPage.startButton.clickAndWait()

        val privacyNoticePage = assertPageIs(page, PrivacyNoticePageLandlordRegistration::class)
        privacyNoticePage.agreeAndSubmit()

        val identityNotVerifiedPage = assertPageIs(page, IdentityNotVerifiedFormPageLandlordRegistration::class)
        identityNotVerifiedPage.clickContinue()

        val namePage = assertPageIs(page, NameFormPageLandlordRegistration::class)
        namePage.submitName("landlord name")

        val dateOfBirthPage = assertPageIs(page, DateOfBirthFormPageLandlordRegistration::class)
        dateOfBirthPage.submitDate("12", "11", "1990")

        val emailPage = assertPageIs(page, EmailFormPageLandlordRegistration::class)
        emailPage.submitEmail("test@example.com")

        val phoneNumPage = assertPageIs(page, PhoneNumberFormPageLandlordRegistration::class)
        phoneNumPage.submitPhoneNumber(phoneNumberUtil.getFormattedUkPhoneNumber())

        val countryOfResidencePage = assertPageIs(page, CountryOfResidenceFormPageLandlordRegistration::class)
        countryOfResidencePage.submitUk()

        val lookupAddressPage = assertPageIs(page, LookupAddressFormPageLandlordRegistration::class)
        lookupAddressPage.submitPostcodeAndBuildingNameOrNumber("EG1 2AA", "1")

        val selectAddressPage = assertPageIs(page, SelectAddressFormPageLandlordRegistration::class)
        selectAddressPage.selectAddressAndSubmit(MANUAL_ADDRESS_CHOSEN)

        val manualAddressPage = assertPageIs(page, ManualAddressFormPageLandlordRegistration::class)
        manualAddressPage.submitAddress(
            addressLineOne = "1 Example Road",
            townOrCity = "Townville",
            postcode = "EG1 2AA",
        )

        val checkAnswersPage = assertPageIs(page, CheckAnswersPageLandlordRegistration::class)
        checkAnswersPage.confirmAndSubmit()

        val createdLandlord = assertNotNull(landlordService.retrieveLandlordByBaseUserId("urn:fdc:gov.uk:2022:UVWXY"))
        val createdLandlordRegNum =
            RegistrationNumberDataModel.fromRegistrationNumber(createdLandlord.registrationNumber)

        verify(confirmationEmailSender).sendEmail(
            "test@example.com",
            LandlordRegistrationConfirmationEmail(createdLandlordRegNum.toString(), absoluteLandlordUrl),
        )

        val confirmationPage = assertPageIs(page, ConfirmationPageLandlordRegistration::class)
        assertEquals(createdLandlordRegNum.toString(), confirmationPage.confirmationBanner.registrationNumberText)
        confirmationPage.goToDashboardLink.clickAndWait()
        assertPageIs(page, LandlordDashboardPage::class)
    }

    @Test
    fun `User can navigate the whole journey selecting individual landlord type when feature flag is enabled (verified, selected address)`(
        page: Page,
    ) {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

        val verifiedIdentity = VerifiedIdentityDataModel("name", LocalDate.now())
        whenever(identityService.getVerifiedIdentityData(any())).thenReturn(verifiedIdentity)

        val landlordRegistrationStartPage = navigator.goToLandlordRegistrationServiceInformationStartPage()
        landlordRegistrationStartPage.startButton.clickAndWait()

        val privacyNoticePage = assertPageIs(page, PrivacyNoticePageLandlordRegistration::class)
        privacyNoticePage.agreeAndSubmit()

        val confirmIdentityPage = assertPageIs(page, ConfirmIdentityFormPageLandlordRegistration::class)
        confirmIdentityPage.confirm()

        val landlordTypePage = assertPageIs(page, LandlordTypeFormPageLandlordRegistration::class)
        landlordTypePage.submitIndividual()

        val emailPage = assertPageIs(page, EmailFormPageLandlordRegistration::class)
        emailPage.submitEmail("test@example.com")

        val phoneNumPage = assertPageIs(page, PhoneNumberFormPageLandlordRegistration::class)
        phoneNumPage.submitPhoneNumber("07123456789")

        val countryOfResidencePage = assertPageIs(page, CountryOfResidenceFormPageLandlordRegistration::class)
        countryOfResidencePage.submitUk()

        val lookupAddressPage = assertPageIs(page, LookupAddressFormPageLandlordRegistration::class)
        lookupAddressPage.submitPostcodeAndBuildingNameOrNumber("EG1 2AA", "1")

        val selectAddressPage = assertPageIs(page, SelectAddressFormPageLandlordRegistration::class)
        selectAddressPage.selectAddressAndSubmit("1 PRSDB Square, EG1 2AA")

        val checkAnswersPage = assertPageIs(page, CheckAnswersPageLandlordRegistration::class)
        checkAnswersPage.confirmAndSubmit()

        val createdLandlord = assertNotNull(landlordService.retrieveLandlordByBaseUserId("urn:fdc:gov.uk:2022:UVWXY"))
        val createdLandlordRegNum =
            RegistrationNumberDataModel.fromRegistrationNumber(createdLandlord.registrationNumber)

        verify(confirmationEmailSender).sendEmail(
            "test@example.com",
            LandlordRegistrationConfirmationEmail(createdLandlordRegNum.toString(), absoluteLandlordUrl),
        )

        val confirmationPage = assertPageIs(page, ConfirmationPageLandlordRegistration::class)
        assertEquals(createdLandlordRegNum.toString(), confirmationPage.confirmationBanner.registrationNumberText)
        confirmationPage.goToDashboardLink.clickAndWait()
        assertPageIs(page, LandlordDashboardPage::class)
    }

    @Test
    fun `User can navigate the whole journey to register as an organisation`(page: Page) {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

        val verifiedIdentity = VerifiedIdentityDataModel("name", LocalDate.now())
        whenever(identityService.getVerifiedIdentityData(any())).thenReturn(verifiedIdentity)

        val landlordRegistrationStartPage = navigator.goToLandlordRegistrationServiceInformationStartPage()
        landlordRegistrationStartPage.startButton.clickAndWait()

        val privacyNoticePage = assertPageIs(page, PrivacyNoticePageLandlordRegistration::class)
        privacyNoticePage.agreeAndSubmit()

        val confirmIdentityPage = assertPageIs(page, ConfirmIdentityFormPageLandlordRegistration::class)
        confirmIdentityPage.confirm()

        val landlordTypePage = assertPageIs(page, LandlordTypeFormPageLandlordRegistration::class)
        landlordTypePage.submitOrganisation()

        // TODO: PDJB-1172 - Submit real your details data once the step is implemented
        val yourDetailsPage = assertPageIs(page, YourDetailsPageLandlordRegistration::class)
        yourDetailsPage.form.submit()

        val orgNamePage = assertPageIs(page, OrgNameFormPageLandlordRegistration::class)
        orgNamePage.submitName("Test Organisation Name")

        val orgAddressPage = assertPageIs(page, OrgAddressFormPageLandlordRegistration::class)
        orgAddressPage.submitAddress(
            addressLineOne = "1 Example Street",
            townOrCity = "Exampleton",
            postcode = "EG1 2AB",
        )

        val orgEmailPage = assertPageIs(page, OrgEmailFormPageLandlordRegistration::class)
        orgEmailPage.submitEmail("test.address@provider.com")

        val orgPhoneNumberPage = assertPageIs(page, OrgPhoneNumberFormPageLandlordRegistration::class)
        orgPhoneNumberPage.submitPhoneNumber("07777777777")

        val orgTypePage = assertPageIs(page, OrgTypeFormPageLandlordRegistration::class)
        orgTypePage.selectCompany()
        orgTypePage.form.submit()

        val orgCompaniesHousePage = assertPageIs(page, OrgCompaniesHouseFormPageLandlordRegistration::class)
        orgCompaniesHousePage.submitYes()

        val orgCompanyNumberPage = assertPageIs(page, OrgCompanyNumberFormPageLandlordRegistration::class)
        orgCompanyNumberPage.submitCompanyNumber("12345678")

        val orgCharityPage = assertPageIs(page, OrgCharityFormPageLandlordRegistration::class)
        orgCharityPage.submitYes()

        val orgCharityRegisteredWithPage = assertPageIs(page, OrgCharityRegisteredWithFormPageLandlordRegistration::class)
        orgCharityRegisteredWithPage.submitCharityRegisteredWith(CharityRegulator.ENGLAND_AND_WALES)

        val orgCharityNumberPage = assertPageIs(page, OrgCharityNumberEnglandAndWalesFormPageLandlordRegistration::class)
        orgCharityNumberPage.submitCharityNumber("1234567")

        // TODO: PDJB-1173 - Submit real organisation directors data once the step is implemented
        val orgDirectorsPage = assertPageIs(page, OrgDirectorsFormPageLandlordRegistration::class)
        orgDirectorsPage.form.submit()

        // TODO: PDJB-1174 - Submit real organisation trustees data once the step is implemented
        val orgTrusteesPage = assertPageIs(page, OrgTrusteesFormPageLandlordRegistration::class)
        orgTrusteesPage.form.submit()

        // TODO: PDJB-1152 - Submit real lead trustee name data once the step is implemented
        val leadTrusteeNamePage = assertPageIs(page, LeadTrusteeNameFormPageLandlordRegistration::class)
        leadTrusteeNamePage.form.submit()

        // TODO: PDJB-1153 - Submit real lead trustee email data once the step is implemented
        val leadTrusteeEmailPage = assertPageIs(page, LeadTrusteeEmailFormPageLandlordRegistration::class)
        leadTrusteeEmailPage.form.submit()

        // TODO: PDJB-1154 - Submit real lead trustee phone data once the step is implemented
        val leadTrusteePhonePage = assertPageIs(page, LeadTrusteePhoneFormPageLandlordRegistration::class)
        leadTrusteePhonePage.form.submit()

        // TODO: PDJB-1163 - Submit real lead trustee DoB data once the step is implemented
        val leadTrusteeDobPage = assertPageIs(page, LeadTrusteeDobFormPageLandlordRegistration::class)
        leadTrusteeDobPage.form.submit()

        // TODO: PDJB-1155/PDJB-1156 - Submit real lead trustee address data once the step is implemented
        val leadTrusteeAddressPage = assertPageIs(page, LeadTrusteeAddressFormPageLandlordRegistration::class)
        leadTrusteeAddressPage.form.submit()

        val orgMainContactPage = assertPageIs(page, OrgMainContactFormPageLandlordRegistration::class)
        orgMainContactPage.submit("Test Contact", "contact@example.com", "07123456789")

        // TODO: PDJB-1168 - This should lead to the normal landlord registration CYA page not the placeholder one
        assertPageIs(page, OrgLandlordCyaPageLandlordRegistration::class)

        // TODO: PDJB-1180: Once we can save OL to the database make sure that the confirmation page shows correctly here upon submitting
    }

    @Test
    fun `Selecting no on companies house skips to the charity page without asking for company number`(page: Page) {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

        navigator.skipToLandlordRegistrationOrganisationCompaniesHousePage()
        val companiesHousePage = assertPageIs(page, OrgCompaniesHouseFormPageLandlordRegistration::class)
        companiesHousePage.submitNo()

        assertPageIs(page, OrgCharityFormPageLandlordRegistration::class)
    }

    @Test
    fun `Selecting no on charity skips the charity questions and goes to the directors page`(page: Page) {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

        navigator.skipToOrgLandlordRegistrationCharityPage()
        val orgCharityPage = assertPageIs(page, OrgCharityFormPageLandlordRegistration::class)
        orgCharityPage.submitNo()

        assertPageIs(page, OrgDirectorsFormPageLandlordRegistration::class)
    }
}
