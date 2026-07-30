package uk.gov.communities.prsdb.webapp.integration

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
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
import uk.gov.communities.prsdb.webapp.constants.enums.GoverningBodyMemberType
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationLandlordUserRepository
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
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
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.LeadTrusteeSelectAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.LookupAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.ManualAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.NameFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgCharityFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgCharityNumberEnglandAndWalesFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgCharityRegisteredWithFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgCheckAnswersPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgCompaniesHouseFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgEmailFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgGovBodyDetailsFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgGovBodyMemberDobFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgGovBodyMemberListFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgGovBodyMemberLookupAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgGovBodyMemberNameFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgGovBodyMemberSelectAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgGovBodyWhoToProvideFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgMainContactFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgNameFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgPhoneNumberFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgTypeFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.PhoneNumberFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.PrivacyNoticePageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.SelectAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.organisationLandlordRegistrationJourneyPages.OrgCompanyNumberFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.VerifiedIdentityDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LandlordRegistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.OrganisationLandlordRegistrationConfirmationEmail
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

    @Autowired
    private lateinit var organisationLandlordUserRepository: OrganisationLandlordUserRepository

    @MockitoBean
    private lateinit var confirmationEmailSender: EmailNotificationService<LandlordRegistrationConfirmationEmail>

    @MockitoBean
    private lateinit var orgConfirmationEmailSender: EmailNotificationService<OrganisationLandlordRegistrationConfirmationEmail>

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
        assertThat(confirmationPage.surveyLink).isVisible()
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
        assertThat(confirmationPage.surveyLink).isVisible()
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

        val emailPage = assertPageIs(page, EmailFormPageLandlordRegistration::class)
        emailPage.submitEmail("test@example.com")

        val phoneNumPage = assertPageIs(page, PhoneNumberFormPageLandlordRegistration::class)
        phoneNumPage.submitPhoneNumber("07123456789")

        val landlordTypePage = assertPageIs(page, LandlordTypeFormPageLandlordRegistration::class)
        landlordTypePage.submitIndividual()

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
        assertThat(confirmationPage.surveyLink).isVisible()
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

        val emailPage = assertPageIs(page, EmailFormPageLandlordRegistration::class)
        emailPage.submitEmail("registrant@example.com")

        val phoneNumPage = assertPageIs(page, PhoneNumberFormPageLandlordRegistration::class)
        phoneNumPage.submitPhoneNumber("07123456789")

        val landlordTypePage = assertPageIs(page, LandlordTypeFormPageLandlordRegistration::class)
        landlordTypePage.submitOrganisation()

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

        val orgCharityPage = assertPageIs(page, OrgCharityFormPageLandlordRegistration::class)
        orgCharityPage.submitYes()

        val orgCharityRegisteredWithPage = assertPageIs(page, OrgCharityRegisteredWithFormPageLandlordRegistration::class)
        orgCharityRegisteredWithPage.submitCharityRegisteredWith(CharityRegulator.ENGLAND_AND_WALES)

        val orgCharityNumberPage = assertPageIs(page, OrgCharityNumberEnglandAndWalesFormPageLandlordRegistration::class)
        orgCharityNumberPage.submitCharityNumber("1234567")

        val orgCompaniesHousePage = assertPageIs(page, OrgCompaniesHouseFormPageLandlordRegistration::class)
        orgCompaniesHousePage.submitYes()

        val orgCompanyNumberPage = assertPageIs(page, OrgCompanyNumberFormPageLandlordRegistration::class)
        orgCompanyNumberPage.submitCompanyNumber("12345678")

        val orgMainContactPage = assertPageIs(page, OrgMainContactFormPageLandlordRegistration::class)
        orgMainContactPage.submit("Test Contact", "contact@example.com", "07123456789")

        val checkAnswersPage = assertPageIs(page, OrgCheckAnswersPageLandlordRegistration::class)
        assertThat(checkAnswersPage.yourDetailsCard.title).hasText("Your details")
        assertThat(checkAnswersPage.yourDetailsCard).containsText("registrant@example.com")
        assertThat(checkAnswersPage.yourDetailsCard).containsText("07123456789")
        assertThat(checkAnswersPage.landlordDetails.landlordTypeRow).containsText("Organisation")
        assertThat(checkAnswersPage.landlordDetails.organisationNameRow).containsText("Test Organisation Name")
        assertThat(checkAnswersPage.landlordDetails.organisationTypeRow).containsText("Company")
        assertThat(checkAnswersPage.mainContactCard.title).hasText("Main contact")

        checkAnswersPage.confirmAndSubmit()

        val createdOrgLandlord =
            assertNotNull(organisationLandlordUserRepository.findByBaseUser_Id("urn:fdc:gov.uk:2022:UVWXY").singleOrNull())
                .organisationLandlord
        val createdOrgLandlordRegNum = RegistrationNumberDataModel.fromRegistrationNumber(createdOrgLandlord.registrationNumber)

        verify(orgConfirmationEmailSender).sendEmail(
            "registrant@example.com",
            OrganisationLandlordRegistrationConfirmationEmail(
                registrantName = "name",
                organisationName = "Test Organisation Name",
                lrn = createdOrgLandlordRegNum.toString(),
                prsdURL = absoluteLandlordUrl,
            ),
        )

        // TODO: PDJB-1180: assert the confirmation page renders here. It currently errors for org landlords because
        //  RegisterLandlordController.getConfirmation looks the landlord up via retrieveLandlordByBaseUserId, which
        //  only finds IndividualLandlords.
    }

    @Test
    fun `Unverified identity with feature flag enabled asks for email and phone before landlord type for individual flow`(page: Page) {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)
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
        phoneNumPage.submitPhoneNumber("07123456789")

        val landlordTypePage = assertPageIs(page, LandlordTypeFormPageLandlordRegistration::class)
        landlordTypePage.submitIndividual()

        assertPageIs(page, CountryOfResidenceFormPageLandlordRegistration::class)
    }

    @Test
    fun `Unverified identity with feature flag enabled asks for email and phone before landlord type for organisation flow`(page: Page) {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)
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
        emailPage.submitEmail("registrant@example.com")

        val phoneNumPage = assertPageIs(page, PhoneNumberFormPageLandlordRegistration::class)
        phoneNumPage.submitPhoneNumber("07123456789")

        val landlordTypePage = assertPageIs(page, LandlordTypeFormPageLandlordRegistration::class)
        landlordTypePage.submitOrganisation()

        assertPageIs(page, OrgNameFormPageLandlordRegistration::class)
    }

    @Test
    fun `Selecting trust on org type shows lead trustee questions before proceeding to charity`(page: Page) {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

        val orgTypePage = navigator.skipToLandlordRegistrationOrganisationTypePage()
        orgTypePage.selectTrust()
        orgTypePage.form.submit()

        val leadTrusteeNamePage = assertPageIs(page, LeadTrusteeNameFormPageLandlordRegistration::class)
        leadTrusteeNamePage.submitName("Test Lead Trustee Name")

        val leadTrusteeDobPage = assertPageIs(page, LeadTrusteeDobFormPageLandlordRegistration::class)
        leadTrusteeDobPage.submitDate("15", "6", "1980")

        val leadTrusteeEmailPage = assertPageIs(page, LeadTrusteeEmailFormPageLandlordRegistration::class)
        leadTrusteeEmailPage.submitEmail("trustee@test.com")

        val leadTrusteePhonePage = assertPageIs(page, LeadTrusteePhoneFormPageLandlordRegistration::class)
        leadTrusteePhonePage.submitPhoneNumber("07123456789")

        val leadTrusteeLookupAddressPage = assertPageIs(page, LeadTrusteeAddressFormPageLandlordRegistration::class)
        leadTrusteeLookupAddressPage.submitPostcodeAndBuildingNameOrNumber("EG1 2AA", "1")

        val leadTrusteeSelectAddressPage =
            assertPageIs(page, LeadTrusteeSelectAddressFormPageLandlordRegistration::class)
        leadTrusteeSelectAddressPage.selectAddressAndSubmit("1 PRSDB Square, EG1 2AA")

        assertPageIs(page, OrgCharityFormPageLandlordRegistration::class)
    }

    @Test
    fun `Selecting no on companies house skips the company number question and goes to the governing body journey`(page: Page) {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

        navigator.skipToLandlordRegistrationOrganisationCompaniesHousePage()
        val companiesHousePage = assertPageIs(page, OrgCompaniesHouseFormPageLandlordRegistration::class)
        companiesHousePage.submitNo()

        val govBodyDetailsPage = assertPageIs(page, OrgGovBodyDetailsFormPageLandlordRegistration::class)
        govBodyDetailsPage.submitHasDetails()

        val whoToProvidePage = assertPageIs(page, OrgGovBodyWhoToProvideFormPageLandlordRegistration::class)
        whoToProvidePage.submitWhoToProvide(GoverningBodyMemberType.TRUSTEE)

        val namePage = assertPageIs(page, OrgGovBodyMemberNameFormPageLandlordRegistration::class)
        namePage.submitName("Alice Smith")

        val dobPage = assertPageIs(page, OrgGovBodyMemberDobFormPageLandlordRegistration::class)
        dobPage.submitDate("10", "3", "1985")

        val lookupAddressPage = assertPageIs(page, OrgGovBodyMemberLookupAddressFormPageLandlordRegistration::class)
        lookupAddressPage.submitPostcodeAndBuildingNameOrNumber("EG1 2AA", "1")

        val selectAddressPage = assertPageIs(page, OrgGovBodyMemberSelectAddressFormPageLandlordRegistration::class)
        selectAddressPage.selectAddressAndSubmit("1 PRSDB Square, EG1 2AA")

        val memberListPage = assertPageIs(page, OrgGovBodyMemberListFormPageLandlordRegistration::class)
        assertThat(memberListPage.heading).containsText("added 1 person")
        memberListPage.addAnotherButton.click()

        val whoToProvidePage2 = assertPageIs(page, OrgGovBodyWhoToProvideFormPageLandlordRegistration::class)
        whoToProvidePage2.submitWhoToProvide(GoverningBodyMemberType.DIRECTOR)

        val namePage2 = assertPageIs(page, OrgGovBodyMemberNameFormPageLandlordRegistration::class)
        namePage2.submitName("Bob Jones")

        val dobPage2 = assertPageIs(page, OrgGovBodyMemberDobFormPageLandlordRegistration::class)
        dobPage2.submitDate("15", "6", "1975")

        val lookupAddressPage2 = assertPageIs(page, OrgGovBodyMemberLookupAddressFormPageLandlordRegistration::class)
        lookupAddressPage2.submitPostcodeAndBuildingNameOrNumber("EG1 2AA", "1")

        val selectAddressPage2 = assertPageIs(page, OrgGovBodyMemberSelectAddressFormPageLandlordRegistration::class)
        selectAddressPage2.selectAddressAndSubmit("1 PRSDB Square, EG1 2AA")

        val updatedListPage = assertPageIs(page, OrgGovBodyMemberListFormPageLandlordRegistration::class)
        assertThat(updatedListPage.heading).containsText("added 2 people")
        updatedListPage.form.submit()

        assertPageIs(page, OrgMainContactFormPageLandlordRegistration::class)
    }

    @Test
    fun `Selecting no on charity skips the charity questions and goes to the companies house page`(page: Page) {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

        navigator.skipToOrgLandlordRegistrationCharityPage()
        val orgCharityPage = assertPageIs(page, OrgCharityFormPageLandlordRegistration::class)
        orgCharityPage.submitNo()

        assertPageIs(page, OrgCompaniesHouseFormPageLandlordRegistration::class)
    }

    @Test
    fun `Selecting no regulator on charity registered with skips the charity number question and goes to the companies house page`(
        page: Page,
    ) {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

        navigator.skipToOrgLandlordRegistrationCharityRegisteredWithPage()
        val charityRegisteredWithPage = assertPageIs(page, OrgCharityRegisteredWithFormPageLandlordRegistration::class)
        charityRegisteredWithPage.submitCharityRegisteredWith(CharityRegulator.NONE)

        assertPageIs(page, OrgCompaniesHouseFormPageLandlordRegistration::class)
    }

    @Test
    fun `adding another governing body member from the list page completes the flow and shows both members`(page: Page) {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

        val memberListPage =
            navigator.skipToOrgLandlordRegistrationGovBodyMemberListPage(
                mapOf(1 to createTestGovBodyMember("Alice Smith")),
            )

        memberListPage.addAnotherButton.click()

        val whoToProvidePage = assertPageIs(page, OrgGovBodyWhoToProvideFormPageLandlordRegistration::class)
        whoToProvidePage.submitWhoToProvide(GoverningBodyMemberType.TRUSTEE)

        val namePage = assertPageIs(page, OrgGovBodyMemberNameFormPageLandlordRegistration::class)
        namePage.submitName("Bob Jones")

        val dobPage = assertPageIs(page, OrgGovBodyMemberDobFormPageLandlordRegistration::class)
        dobPage.submitDate("10", "3", "1975")

        val lookupAddressPage = assertPageIs(page, OrgGovBodyMemberLookupAddressFormPageLandlordRegistration::class)
        lookupAddressPage.submitPostcodeAndBuildingNameOrNumber("EG1 2AA", "1")

        val selectAddressPage = assertPageIs(page, OrgGovBodyMemberSelectAddressFormPageLandlordRegistration::class)
        selectAddressPage.selectAddressAndSubmit("1 PRSDB Square, EG1 2AA")

        val updatedListPage = assertPageIs(page, OrgGovBodyMemberListFormPageLandlordRegistration::class)
        assertThat(updatedListPage.heading).containsText("added 2 people")
        assertThat(updatedListPage.summaryList.getRowByIndex(0).value).containsText("Alice Smith")
        assertThat(updatedListPage.summaryList.getRowByIndex(1).value).containsText("Bob Jones")
    }

    @Test
    fun `changing a governing body member updates their details in the list`(page: Page) {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

        val memberListPage =
            navigator.skipToOrgLandlordRegistrationGovBodyMemberListPage(
                mapOf(
                    1 to createTestGovBodyMember("Alice Smith"),
                    2 to createTestGovBodyMember("Bob Jones"),
                ),
            )

        memberListPage.summaryList
            .getRowByIndex(0)
            .actions
            .getActionLink("Change")
            .clickAndWait()

        val whoToProvidePage = assertPageIs(page, OrgGovBodyWhoToProvideFormPageLandlordRegistration::class)
        whoToProvidePage.submitWhoToProvide(GoverningBodyMemberType.DIRECTOR)

        val namePage = assertPageIs(page, OrgGovBodyMemberNameFormPageLandlordRegistration::class)
        namePage.submitName("Alice Johnson")

        val dobPage = assertPageIs(page, OrgGovBodyMemberDobFormPageLandlordRegistration::class)
        dobPage.submitDate("15", "6", "1980")

        val lookupAddressPage = assertPageIs(page, OrgGovBodyMemberLookupAddressFormPageLandlordRegistration::class)
        lookupAddressPage.submitPostcodeAndBuildingNameOrNumber("EG1 2AA", "1")

        val selectAddressPage = assertPageIs(page, OrgGovBodyMemberSelectAddressFormPageLandlordRegistration::class)
        selectAddressPage.selectAddressAndSubmit("1 PRSDB Square, EG1 2AA")

        val updatedListPage = assertPageIs(page, OrgGovBodyMemberListFormPageLandlordRegistration::class)
        assertThat(updatedListPage.heading).containsText("added 2 people")
        assertThat(updatedListPage.summaryList.getRowByIndex(0).value).containsText("Alice Johnson")
        assertThat(updatedListPage.summaryList.getRowByIndex(1).value).containsText("Bob Jones")
    }

    @Test
    fun `removing governing body members one by one updates the list correctly`(page: Page) {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

        val memberListPage =
            navigator.skipToOrgLandlordRegistrationGovBodyMemberListPage(
                mapOf(
                    1 to createTestGovBodyMember("Alice Smith"),
                    2 to createTestGovBodyMember("Bob Jones"),
                ),
            )

        assertThat(memberListPage.heading).containsText("added 2 people")

        memberListPage.summaryList
            .getRowByIndex(1)
            .actions
            .getActionLink("Remove")
            .clickAndWait()

        val afterFirstRemoval = assertPageIs(page, OrgGovBodyMemberListFormPageLandlordRegistration::class)
        assertThat(afterFirstRemoval.heading).containsText("added 1 person")
        assertThat(afterFirstRemoval.summaryList.getRowByIndex(0).value).containsText("Alice Smith")

        afterFirstRemoval.summaryList
            .getRowByIndex(0)
            .actions
            .getActionLink("Remove")
            .clickAndWait()

        assertPageIs(page, OrgGovBodyDetailsFormPageLandlordRegistration::class)
    }

    @Test
    fun `pressing back after starting to edit resets editing state and allows adding a new member`(page: Page) {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

        val memberListPage =
            navigator.skipToOrgLandlordRegistrationGovBodyMemberListPage(
                mapOf(1 to createTestGovBodyMember("Alice Smith")),
            )

        memberListPage.summaryList
            .getRowByIndex(0)
            .actions
            .getActionLink("Change")
            .clickAndWait()

        assertPageIs(page, OrgGovBodyWhoToProvideFormPageLandlordRegistration::class)
        BackLink.default(page).clickAndWait()

        val returnedListPage = assertPageIs(page, OrgGovBodyMemberListFormPageLandlordRegistration::class)
        returnedListPage.addAnotherButton.click()

        val whoToProvidePage = assertPageIs(page, OrgGovBodyWhoToProvideFormPageLandlordRegistration::class)
        whoToProvidePage.submitWhoToProvide(GoverningBodyMemberType.TRUSTEE)

        val namePage = assertPageIs(page, OrgGovBodyMemberNameFormPageLandlordRegistration::class)
        namePage.submitName("Bob Jones")

        val dobPage = assertPageIs(page, OrgGovBodyMemberDobFormPageLandlordRegistration::class)
        dobPage.submitDate("10", "3", "1975")

        val lookupAddressPage = assertPageIs(page, OrgGovBodyMemberLookupAddressFormPageLandlordRegistration::class)
        lookupAddressPage.submitPostcodeAndBuildingNameOrNumber("EG1 2AA", "1")

        val selectAddressPage = assertPageIs(page, OrgGovBodyMemberSelectAddressFormPageLandlordRegistration::class)
        selectAddressPage.selectAddressAndSubmit("1 PRSDB Square, EG1 2AA")

        val updatedListPage = assertPageIs(page, OrgGovBodyMemberListFormPageLandlordRegistration::class)
        assertThat(updatedListPage.heading).containsText("added 2 people")
        assertThat(updatedListPage.summaryList.getRowByIndex(0).value).containsText("Alice Smith")
        assertThat(updatedListPage.summaryList.getRowByIndex(1).value).containsText("Bob Jones")
    }

    private fun createTestGovBodyMember(name: String) =
        uk.gov.communities.prsdb.webapp.models.dataModels.GoverningBodyMemberDataModel(
            name = name,
            type = GoverningBodyMemberType.DIRECTOR,
            dateOfBirth = kotlinx.datetime.LocalDate(1970, 1, 1),
            address =
                uk.gov.communities.prsdb.webapp.models.dataModels
                    .AddressDataModel(singleLineAddress = "Test Address"),
        )
}
