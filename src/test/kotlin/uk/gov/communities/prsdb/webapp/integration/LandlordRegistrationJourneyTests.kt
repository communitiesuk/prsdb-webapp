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
import uk.gov.communities.prsdb.webapp.constants.INDIVIDUAL_LANDLORD_REGISTRATION_SURVEY_URL
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.ORGANISATION_LANDLORD_REGISTRATION
import uk.gov.communities.prsdb.webapp.constants.ORG_LANDLORD_REGISTRATION_SURVEY_URL
import uk.gov.communities.prsdb.webapp.constants.enums.CharityRegulator
import uk.gov.communities.prsdb.webapp.constants.enums.GoverningBodyMemberType
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordType
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordUserRepository
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.CheckAnswersPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.CompaniesHouseInterruptionPageLandlordRegistration
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
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgCharityNumberEnglandAndWalesFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgCharityRegisteredWithFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgCheckAnswersPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgEmailFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgGovBodyDetailsFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgGovBodyMemberDobFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgGovBodyMemberListFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgGovBodyMemberLookupAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgGovBodyMemberManualAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgGovBodyMemberNameFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgGovBodyMemberSelectAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgGovBodyWhoToProvideFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgIsRegisteredCharityFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgIsRegisteredCompanyFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgMainContactFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgNameFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgPhoneNumberFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgSelectAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgTypeFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.OrgTypeTrustInterruptionPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.PhoneNumberFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.PrivacyNoticePageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.SelectAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.organisationLandlordRegistrationJourneyPages.OrgCompanyNumberFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.VerifiedIdentityDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LandlordRegistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.OrganisationalLandlordRegistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService
import uk.gov.communities.prsdb.webapp.testHelpers.builders.LandlordStateSessionBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.extensions.getFormattedUkPhoneNumber
import java.net.URI
import java.time.LocalDate
import kotlin.test.assertNotNull

class LandlordRegistrationJourneyTests : IntegrationTestWithMutableData("data-mockuser-not-landlord.sql") {
    private val phoneNumberUtil = PhoneNumberUtil.getInstance()
    private val absoluteLandlordUrl = "www.prsd.gov.uk/landlord"

    @Autowired
    private lateinit var userToLandlordService: UserToLandlordService

    @Autowired
    private lateinit var organisationalLandlordUserRepository: OrganisationalLandlordUserRepository

    @MockitoBean
    private lateinit var confirmationEmailSender: EmailNotificationService<LandlordRegistrationConfirmationEmail>

    @MockitoBean
    private lateinit var orgConfirmationEmailSender: EmailNotificationService<OrganisationalLandlordRegistrationConfirmationEmail>

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

        val createdLandlord = assertNotNull(userToLandlordService.getLandlordForBaseUserId("urn:fdc:gov.uk:2022:UVWXY"))
        val createdLandlordRegNum =
            RegistrationNumberDataModel.fromRegistrationNumber(createdLandlord.registrationNumber)

        verify(confirmationEmailSender).sendEmail(
            "test@example.com",
            LandlordRegistrationConfirmationEmail(createdLandlordRegNum.toString(), absoluteLandlordUrl),
        )

        val confirmationPage = assertPageIs(page, ConfirmationPageLandlordRegistration::class)
        assertEquals(createdLandlordRegNum.toString(), confirmationPage.confirmationBanner.registrationNumberText)
        assertThat(confirmationPage.surveyLink).isVisible()
        assertThat(confirmationPage.surveyLink).hasAttribute("href", INDIVIDUAL_LANDLORD_REGISTRATION_SURVEY_URL)
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

        val createdLandlord = assertNotNull(userToLandlordService.getLandlordForBaseUserId("urn:fdc:gov.uk:2022:UVWXY"))
        val createdLandlordRegNum =
            RegistrationNumberDataModel.fromRegistrationNumber(createdLandlord.registrationNumber)

        verify(confirmationEmailSender).sendEmail(
            "test@example.com",
            LandlordRegistrationConfirmationEmail(createdLandlordRegNum.toString(), absoluteLandlordUrl),
        )

        val confirmationPage = assertPageIs(page, ConfirmationPageLandlordRegistration::class)
        assertEquals(createdLandlordRegNum.toString(), confirmationPage.confirmationBanner.registrationNumberText)
        assertThat(confirmationPage.surveyLink).isVisible()
        assertThat(confirmationPage.surveyLink).hasAttribute("href", INDIVIDUAL_LANDLORD_REGISTRATION_SURVEY_URL)
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

        val createdLandlord = assertNotNull(userToLandlordService.getLandlordForBaseUserId("urn:fdc:gov.uk:2022:UVWXY"))
        val createdLandlordRegNum =
            RegistrationNumberDataModel.fromRegistrationNumber(createdLandlord.registrationNumber)

        verify(confirmationEmailSender).sendEmail(
            "test@example.com",
            LandlordRegistrationConfirmationEmail(createdLandlordRegNum.toString(), absoluteLandlordUrl),
        )

        val confirmationPage = assertPageIs(page, ConfirmationPageLandlordRegistration::class)
        assertEquals(createdLandlordRegNum.toString(), confirmationPage.confirmationBanner.registrationNumberText)
        assertThat(confirmationPage.surveyLink).isVisible()
        assertThat(confirmationPage.surveyLink).hasAttribute("href", INDIVIDUAL_LANDLORD_REGISTRATION_SURVEY_URL)
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
        orgAddressPage.submitPostcodeAndBuildingNameOrNumber("EG1 2AA", "1")

        val orgSelectAddressPage = assertPageIs(page, OrgSelectAddressFormPageLandlordRegistration::class)
        orgSelectAddressPage.selectAddressAndSubmit("1 PRSDB Square, EG1 2AA")

        val orgEmailPage = assertPageIs(page, OrgEmailFormPageLandlordRegistration::class)
        orgEmailPage.submitEmail("test.address@provider.com")

        val orgPhoneNumberPage = assertPageIs(page, OrgPhoneNumberFormPageLandlordRegistration::class)
        orgPhoneNumberPage.submitPhoneNumber("07777777777")

        val orgTypePage = assertPageIs(page, OrgTypeFormPageLandlordRegistration::class)
        orgTypePage.selectCompany()
        orgTypePage.form.submit()

        val orgIsRegisteredCharityPage = assertPageIs(page, OrgIsRegisteredCharityFormPageLandlordRegistration::class)
        orgIsRegisteredCharityPage.submitYes()

        val orgCharityRegisteredWithPage =
            assertPageIs(page, OrgCharityRegisteredWithFormPageLandlordRegistration::class)
        orgCharityRegisteredWithPage.submitCharityRegisteredWith(CharityRegulator.ENGLAND_AND_WALES)

        val orgCharityNumberPage =
            assertPageIs(page, OrgCharityNumberEnglandAndWalesFormPageLandlordRegistration::class)
        orgCharityNumberPage.submitCharityNumber("1234567")

        val orgIsRegisteredCompanyPage = assertPageIs(page, OrgIsRegisteredCompanyFormPageLandlordRegistration::class)
        orgIsRegisteredCompanyPage.submitYes()

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
            assertNotNull(
                organisationalLandlordUserRepository.findByBaseUser_Id("urn:fdc:gov.uk:2022:UVWXY").singleOrNull(),
            ).organisationalLandlord
        val createdOrgLandlordRegNum =
            RegistrationNumberDataModel.fromRegistrationNumber(createdOrgLandlord.registrationNumber)

        verify(orgConfirmationEmailSender).sendEmail(
            "registrant@example.com",
            OrganisationalLandlordRegistrationConfirmationEmail(
                registrantName = "name",
                organisationName = "Test Organisation Name",
                lrn = createdOrgLandlordRegNum.toString(),
                prsdURL = absoluteLandlordUrl,
            ),
        )

        val confirmationPage = assertPageIs(page, ConfirmationPageLandlordRegistration::class)
        assertEquals(createdOrgLandlordRegNum.toString(), confirmationPage.confirmationBanner.registrationNumberText)
        assertThat(confirmationPage.surveyLink).isVisible()
        assertThat(confirmationPage.surveyLink).hasAttribute("href", ORG_LANDLORD_REGISTRATION_SURVEY_URL)
        confirmationPage.goToDashboardLink.clickAndWait()

        val dashboardPage = assertPageIs(page, LandlordDashboardPage::class)
        assertThat(dashboardPage.dashboardBannerHeading).hasText("Test Organisation Name")
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

        assertPageIs(page, OrgIsRegisteredCharityFormPageLandlordRegistration::class)
    }

    @Test
    fun `The organisation name change link returns to the org check answers page with the updated value`(page: Page) {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

        val checkAnswersPage = navigator.skipToLandlordRegistrationOrgCheckAnswersPage()
        checkAnswersPage.landlordDetails.organisationNameRow.clickNamedActionLinkAndWait("Change")

        val orgNamePage = assertPageIs(page, OrgNameFormPageLandlordRegistration::class)
        orgNamePage.submitName("Updated Organisation Name")

        val updatedCheckAnswersPage = assertPageIs(page, OrgCheckAnswersPageLandlordRegistration::class)
        assertThat(updatedCheckAnswersPage.landlordDetails.organisationNameRow).containsText("Updated Organisation Name")
    }

    @Test
    fun `The landlord type change link returns to the org check answers page when the organisation type is kept`(page: Page) {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

        val checkAnswersPage = navigator.skipToLandlordRegistrationOrgCheckAnswersPage()
        checkAnswersPage.landlordDetails.landlordTypeRow.clickNamedActionLinkAndWait("Change")

        val landlordTypePage = assertPageIs(page, LandlordTypeFormPageLandlordRegistration::class)
        landlordTypePage.submitOrganisation()

        assertPageIs(page, OrgCheckAnswersPageLandlordRegistration::class)
    }

    @Test
    fun `The registered charity change link re-walks the charity questions and returns to the org check answers page`(page: Page) {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

        val checkAnswersPage = navigator.skipToLandlordRegistrationOrgCheckAnswersPage()
        checkAnswersPage.landlordDetails.registeredCharityRow.clickNamedActionLinkAndWait("Change")

        val orgCharityPage = assertPageIs(page, OrgIsRegisteredCharityFormPageLandlordRegistration::class)
        orgCharityPage.submitYes()

        val orgCharityRegisteredWithPage =
            assertPageIs(page, OrgCharityRegisteredWithFormPageLandlordRegistration::class)
        orgCharityRegisteredWithPage.submitCharityRegisteredWith(CharityRegulator.ENGLAND_AND_WALES)

        val orgCharityNumberPage =
            assertPageIs(page, OrgCharityNumberEnglandAndWalesFormPageLandlordRegistration::class)
        orgCharityNumberPage.submitCharityNumber("1234567")

        val updatedCheckAnswersPage = assertPageIs(page, OrgCheckAnswersPageLandlordRegistration::class)
        assertThat(updatedCheckAnswersPage.landlordDetails.charityNumberRow).containsText("1234567")
    }

    @Test
    fun `The lead trustee card change link re-walks the trustee section and returns to the org check answers page`(page: Page) {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

        val checkAnswersPage = navigator.skipToLandlordRegistrationOrgCheckAnswersPage()
        checkAnswersPage.leadTrusteeCard
            .getAction("Change")
            .link
            .clickAndWait()

        val leadTrusteeNamePage = assertPageIs(page, LeadTrusteeNameFormPageLandlordRegistration::class)
        leadTrusteeNamePage.submitName("Updated Lead Trustee Name")

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

        assertPageIs(page, OrgCheckAnswersPageLandlordRegistration::class)
    }

    @Test
    fun `The organisation address change link returns to the org check answers page with the updated value`(page: Page) {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

        val checkAnswersPage = navigator.skipToLandlordRegistrationOrgCheckAnswersPage()
        checkAnswersPage.landlordDetails.organisationAddressRow.clickNamedActionLinkAndWait("Change")

        val orgAddressPage = assertPageIs(page, OrgAddressFormPageLandlordRegistration::class)
        orgAddressPage.submitPostcodeAndBuildingNameOrNumber("EG1 2AA", "1")

        val orgSelectAddressPage = assertPageIs(page, OrgSelectAddressFormPageLandlordRegistration::class)
        orgSelectAddressPage.selectAddressAndSubmit("1 PRSDB Square, EG1 2AA")

        val updatedCheckAnswersPage = assertPageIs(page, OrgCheckAnswersPageLandlordRegistration::class)
        assertThat(updatedCheckAnswersPage.landlordDetails.organisationAddressRow).containsText("1 PRSDB Square")
    }

    @Test
    fun `The organisation email change link returns to the org check answers page with the updated value`(page: Page) {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

        val checkAnswersPage = navigator.skipToLandlordRegistrationOrgCheckAnswersPage()
        checkAnswersPage.landlordDetails.organisationEmailRow.clickNamedActionLinkAndWait("Change")

        val orgEmailPage = assertPageIs(page, OrgEmailFormPageLandlordRegistration::class)
        orgEmailPage.submitEmail("updated.email@example.com")

        val updatedCheckAnswersPage = assertPageIs(page, OrgCheckAnswersPageLandlordRegistration::class)
        assertThat(updatedCheckAnswersPage.landlordDetails.organisationEmailRow).containsText("updated.email@example.com")
    }

    @Test
    fun `The organisation phone number change link returns to the org check answers page with the updated value`(page: Page) {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

        val checkAnswersPage = navigator.skipToLandlordRegistrationOrgCheckAnswersPage()
        checkAnswersPage.landlordDetails.organisationPhoneRow.clickNamedActionLinkAndWait("Change")

        val orgPhoneNumberPage = assertPageIs(page, OrgPhoneNumberFormPageLandlordRegistration::class)
        orgPhoneNumberPage.submitPhoneNumber("07999999999")

        val updatedCheckAnswersPage = assertPageIs(page, OrgCheckAnswersPageLandlordRegistration::class)
        assertThat(updatedCheckAnswersPage.landlordDetails.organisationPhoneRow).containsText("07999999999")
    }

    @Test
    fun `The governing body member card change link re-walks the member list and returns to the org check answers page`(page: Page) {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

        val checkAnswersPage = navigator.skipToLandlordRegistrationOrgCheckAnswersPage()
        checkAnswersPage.governingBodyMemberCard
            .getAction("Change")
            .link
            .clickAndWait()

        val memberListPage = assertPageIs(page, OrgGovBodyMemberListFormPageLandlordRegistration::class)
        memberListPage.form.submit()

        assertPageIs(page, OrgCheckAnswersPageLandlordRegistration::class)
    }

    @Test
    fun `The main contact change link returns to the org check answers page with the updated value`(page: Page) {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

        val checkAnswersPage = navigator.skipToLandlordRegistrationOrgCheckAnswersPage()
        checkAnswersPage.mainContactCard
            .getAction("Change")
            .link
            .clickAndWait()

        val orgMainContactPage = assertPageIs(page, OrgMainContactFormPageLandlordRegistration::class)
        orgMainContactPage.submit("Updated Contact Name", "updated.contact@example.com", "07888888888")

        val updatedCheckAnswersPage = assertPageIs(page, OrgCheckAnswersPageLandlordRegistration::class)
        assertThat(updatedCheckAnswersPage.mainContactCard).containsText("Updated Contact Name")
    }

    @Test
    fun `The landlord type change link routes into the individual journey when switching to individual`(page: Page) {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

        val checkAnswersPage = navigator.skipToLandlordRegistrationOrgCheckAnswersPage()
        checkAnswersPage.landlordDetails.landlordTypeRow.clickNamedActionLinkAndWait("Change")

        val landlordTypePage = assertPageIs(page, LandlordTypeFormPageLandlordRegistration::class)
        landlordTypePage.submitIndividual()

        assertPageIs(page, CountryOfResidenceFormPageLandlordRegistration::class)
    }

    @Test
    fun `The landlord type change link routes into the organisation journey when switching to organisation`(page: Page) {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

        val checkAnswersPage =
            navigator.skipToLandlordRegistrationCheckAnswersPage(
                LandlordStateSessionBuilder.beforeCheckAnswers().withLandlordType(LandlordType.INDIVIDUAL),
            )
        checkAnswersPage.summaryList.landlordTypeRow.clickNamedActionLinkAndWait("Change")

        val landlordTypePage = assertPageIs(page, LandlordTypeFormPageLandlordRegistration::class)
        landlordTypePage.submitOrganisation()

        assertPageIs(page, OrgNameFormPageLandlordRegistration::class)
    }

    @Test
    fun `The organisation type change link returns to the org check answers page with the updated value`(page: Page) {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

        val checkAnswersPage = navigator.skipToLandlordRegistrationOrgCheckAnswersPage()
        checkAnswersPage.landlordDetails.organisationTypeRow.clickNamedActionLinkAndWait("Change")

        val orgTypePage = assertPageIs(page, OrgTypeFormPageLandlordRegistration::class)
        orgTypePage.selectCharity()
        orgTypePage.selectTrust()
        orgTypePage.form.submit()

        val updatedCheckAnswersPage = assertPageIs(page, OrgCheckAnswersPageLandlordRegistration::class)
        assertThat(updatedCheckAnswersPage.landlordDetails.organisationTypeRow).containsText("Charity")
        assertThat(updatedCheckAnswersPage.landlordDetails.organisationTypeRow).containsText("Trust")
    }

    @Test
    fun `The organisation type change link shows interruption pages when trust status changes`(page: Page) {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

        var checkAnswersPage = navigator.skipToLandlordRegistrationOrgCheckAnswersPage()
        checkAnswersPage.landlordDetails.organisationTypeRow.clickNamedActionLinkAndWait("Change")

        var orgTypePage = assertPageIs(page, OrgTypeFormPageLandlordRegistration::class)
        orgTypePage.selectCharity()
        orgTypePage.form.submit()

        var interruptionPage = assertPageIs(page, OrgTypeTrustInterruptionPageLandlordRegistration::class)
        interruptionPage.submit()

        checkAnswersPage = assertPageIs(page, OrgCheckAnswersPageLandlordRegistration::class)
        assertThat(checkAnswersPage.landlordDetails.organisationTypeRow).containsText("Charity")
        assertThat(checkAnswersPage.landlordDetails.organisationTypeRow).not().containsText("Trust")
        assertThat(checkAnswersPage.leadTrusteeCard).hasCount(0)

        checkAnswersPage.landlordDetails.organisationTypeRow.clickNamedActionLinkAndWait("Change")

        orgTypePage = assertPageIs(page, OrgTypeFormPageLandlordRegistration::class)
        orgTypePage.selectCharity()
        orgTypePage.selectTrust()
        orgTypePage.form.submit()

        interruptionPage = assertPageIs(page, OrgTypeTrustInterruptionPageLandlordRegistration::class)
        interruptionPage.submit()

        val leadTrusteeNamePage = assertPageIs(page, LeadTrusteeNameFormPageLandlordRegistration::class)
        leadTrusteeNamePage.submitName("Reassigned Lead Trustee")

        val leadTrusteeDobPage = assertPageIs(page, LeadTrusteeDobFormPageLandlordRegistration::class)
        leadTrusteeDobPage.submitDate("15", "6", "1980")

        val leadTrusteeEmailPage = assertPageIs(page, LeadTrusteeEmailFormPageLandlordRegistration::class)
        leadTrusteeEmailPage.submitEmail("reassigned.trustee@test.com")

        val leadTrusteePhonePage = assertPageIs(page, LeadTrusteePhoneFormPageLandlordRegistration::class)
        leadTrusteePhonePage.submitPhoneNumber("07123456789")

        val leadTrusteeLookupAddressPage = assertPageIs(page, LeadTrusteeAddressFormPageLandlordRegistration::class)
        leadTrusteeLookupAddressPage.submitPostcodeAndBuildingNameOrNumber("EG1 2AA", "1")

        val leadTrusteeSelectAddressPage =
            assertPageIs(page, LeadTrusteeSelectAddressFormPageLandlordRegistration::class)
        leadTrusteeSelectAddressPage.selectAddressAndSubmit("1 PRSDB Square, EG1 2AA")

        checkAnswersPage = assertPageIs(page, OrgCheckAnswersPageLandlordRegistration::class)
        assertThat(checkAnswersPage.landlordDetails.organisationTypeRow).containsText("Charity")
        assertThat(checkAnswersPage.landlordDetails.organisationTypeRow).containsText("Trust")
        assertThat(checkAnswersPage.leadTrusteeCard).containsText("Reassigned Lead Trustee")
        assertThat(checkAnswersPage.leadTrusteeCard).containsText("reassigned.trustee@test.com")
    }

    @Test
    fun `The Companies House change link routes into the companies house update flow`(page: Page) {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

        val checkAnswersPage = navigator.skipToLandlordRegistrationOrgCheckAnswersPage()
        checkAnswersPage.landlordDetails.registeredWithCompaniesHouseRow.clickNamedActionLinkAndWait("Change")

        assertPageIs(page, OrgIsRegisteredCompanyFormPageLandlordRegistration::class)
    }

    @Test
    fun `The company number change link opens the company number page and returns to check answers`(page: Page) {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

        val checkAnswersPage = navigator.skipToLandlordRegistrationOrgCheckAnswersPageForRegisteredCompany()
        checkAnswersPage.landlordDetails.companiesHouseNumberRow.clickNamedActionLinkAndWait("Change")

        val companyNumberPage = assertPageIs(page, OrgCompanyNumberFormPageLandlordRegistration::class)
        companyNumberPage.submitCompanyNumber("87654321")

        assertPageIs(page, OrgCheckAnswersPageLandlordRegistration::class)
    }

    @Test
    fun `Keeping the same Companies House answer returns straight to check answers`(page: Page) {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

        val checkAnswersPage = navigator.skipToLandlordRegistrationOrgCheckAnswersPageForRegisteredCompany()
        checkAnswersPage.landlordDetails.registeredWithCompaniesHouseRow.clickNamedActionLinkAndWait("Change")

        val isRegisteredCompanyPage = assertPageIs(page, OrgIsRegisteredCompanyFormPageLandlordRegistration::class)
        isRegisteredCompanyPage.submitYes()

        assertPageIs(page, OrgCheckAnswersPageLandlordRegistration::class)
    }

    @Test
    fun `Changing the Companies House answer to no routes through the governing body member flow before returning to check answers`(
        page: Page,
    ) {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

        val checkAnswersPage = navigator.skipToLandlordRegistrationOrgCheckAnswersPageForRegisteredCompany()
        checkAnswersPage.landlordDetails.registeredWithCompaniesHouseRow.clickNamedActionLinkAndWait("Change")

        val isRegisteredCompanyPage = assertPageIs(page, OrgIsRegisteredCompanyFormPageLandlordRegistration::class)
        isRegisteredCompanyPage.submitNo()

        val interruptionPage = assertPageIs(page, CompaniesHouseInterruptionPageLandlordRegistration::class)
        interruptionPage.submit()

        // Changing to a non-company routes into the governing body member flow rather than straight back to check answers.
        val memberListPage = assertPageIs(page, OrgGovBodyMemberListFormPageLandlordRegistration::class)
        memberListPage.form.submit()

        assertPageIs(page, OrgCheckAnswersPageLandlordRegistration::class)
    }

    @Test
    fun `Selecting no on companies house skips the company number question and goes to the governing body journey`(page: Page) {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

        navigator.skipToOrgLandlordRegistrationIsRegisteredCompanyPage()
        val companiesHousePage = assertPageIs(page, OrgIsRegisteredCompanyFormPageLandlordRegistration::class)
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

        navigator.skipToOrgLandlordRegistrationIsRegisteredCharityPage()
        val orgIsRegisteredCharityPage = assertPageIs(page, OrgIsRegisteredCharityFormPageLandlordRegistration::class)
        orgIsRegisteredCharityPage.submitNo()

        assertPageIs(page, OrgIsRegisteredCompanyFormPageLandlordRegistration::class)
    }

    @Test
    fun `Selecting no regulator on charity registered with skips the charity number question and goes to the companies house page`(
        page: Page,
    ) {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

        navigator.skipToOrgLandlordRegistrationCharityRegisteredWithPage()
        val charityRegisteredWithPage = assertPageIs(page, OrgCharityRegisteredWithFormPageLandlordRegistration::class)
        charityRegisteredWithPage.submitCharityRegisteredWith(CharityRegulator.NONE)

        assertPageIs(page, OrgIsRegisteredCompanyFormPageLandlordRegistration::class)
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

    @Test
    fun `editing a governing body member pre-fills all questions including looked-up address`(page: Page) {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

        val memberListPage =
            navigator.skipToOrgLandlordRegistrationGovBodyMemberListPage(
                mapOf(
                    1 to
                        createTestGovBodyMember(
                            name = "Alice Smith",
                            type = GoverningBodyMemberType.TRUSTEE,
                            dateOfBirth = kotlinx.datetime.LocalDate(1985, 3, 15),
                            addressSearchPostcode = "EG1 2AA",
                            addressSearchHouseNameOrNumber = "1",
                            selectedAddress = "1 PRSDB Square, EG1 2AA",
                        ),
                ),
            )

        memberListPage.summaryList
            .getRowByIndex(0)
            .actions
            .getActionLink("Change")
            .clickAndWait()

        val whoToProvidePage = assertPageIs(page, OrgGovBodyWhoToProvideFormPageLandlordRegistration::class)
        assertEquals("TRUSTEE", whoToProvidePage.form.radios.selectedValue)
        whoToProvidePage.form.submit()

        val namePage = assertPageIs(page, OrgGovBodyMemberNameFormPageLandlordRegistration::class)
        assertThat(namePage.form.nameInput).hasValue("Alice Smith")
        namePage.form.submit()

        val dobPage = assertPageIs(page, OrgGovBodyMemberDobFormPageLandlordRegistration::class)
        assertThat(dobPage.form.dayInput).hasValue("15")
        assertThat(dobPage.form.monthInput).hasValue("3")
        assertThat(dobPage.form.yearInput).hasValue("1985")
        dobPage.form.submit()

        val lookupAddressPage = assertPageIs(page, OrgGovBodyMemberLookupAddressFormPageLandlordRegistration::class)
        assertThat(lookupAddressPage.form.postcodeInput).hasValue("EG1 2AA")
        assertThat(lookupAddressPage.form.houseNameOrNumberInput).hasValue("1")
        lookupAddressPage.form.submit()

        val selectAddressPage = assertPageIs(page, OrgGovBodyMemberSelectAddressFormPageLandlordRegistration::class)
        assertEquals("1 PRSDB Square, EG1 2AA", selectAddressPage.form.addressRadios.selectedValue)
        selectAddressPage.form.submit()

        val updatedListPage = assertPageIs(page, OrgGovBodyMemberListFormPageLandlordRegistration::class)
        assertThat(updatedListPage.summaryList.getRowByIndex(0).value).containsText("Alice Smith")
    }

    @Test
    fun `editing a governing body member pre-fills manual address`(page: Page) {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

        val memberListPage =
            navigator.skipToOrgLandlordRegistrationGovBodyMemberListPage(
                mapOf(
                    1 to
                        createTestGovBodyMember(
                            name = "Bob Jones",
                            type = GoverningBodyMemberType.DIRECTOR,
                            dateOfBirth = kotlinx.datetime.LocalDate(1990, 7, 20),
                            addressSearchPostcode = "EG1 2AA",
                            addressSearchHouseNameOrNumber = "1",
                            selectedAddress = MANUAL_ADDRESS_CHOSEN,
                            manualAddressLineOne = "123 Main Street",
                            manualAddressLineTwo = "Flat 4",
                            manualTownOrCity = "London",
                            manualCounty = "Greater London",
                            manualPostcode = "EG1 2AA",
                        ),
                ),
            )

        memberListPage.summaryList
            .getRowByIndex(0)
            .actions
            .getActionLink("Change")
            .clickAndWait()

        val whoToProvidePage = assertPageIs(page, OrgGovBodyWhoToProvideFormPageLandlordRegistration::class)
        whoToProvidePage.form.submit()

        val namePage = assertPageIs(page, OrgGovBodyMemberNameFormPageLandlordRegistration::class)
        namePage.form.submit()

        val dobPage = assertPageIs(page, OrgGovBodyMemberDobFormPageLandlordRegistration::class)
        dobPage.form.submit()

        val lookupAddressPage = assertPageIs(page, OrgGovBodyMemberLookupAddressFormPageLandlordRegistration::class)
        assertThat(lookupAddressPage.form.postcodeInput).hasValue("EG1 2AA")
        assertThat(lookupAddressPage.form.houseNameOrNumberInput).hasValue("1")
        lookupAddressPage.form.submit()

        val selectAddressPage = assertPageIs(page, OrgGovBodyMemberSelectAddressFormPageLandlordRegistration::class)
        assertEquals(MANUAL_ADDRESS_CHOSEN, selectAddressPage.form.addressRadios.selectedValue)
        selectAddressPage.form.submit()

        val manualAddressPage = assertPageIs(page, OrgGovBodyMemberManualAddressFormPageLandlordRegistration::class)
        assertThat(manualAddressPage.form.addressLineOneInput).hasValue("123 Main Street")
        assertThat(manualAddressPage.form.addressLineTwoInput).hasValue("Flat 4")
        assertThat(manualAddressPage.form.townOrCityInput).hasValue("London")
        assertThat(manualAddressPage.form.countyInput).hasValue("Greater London")
        assertThat(manualAddressPage.form.postcodeInput).hasValue("EG1 2AA")
        manualAddressPage.form.submit()

        val updatedListPage = assertPageIs(page, OrgGovBodyMemberListFormPageLandlordRegistration::class)
        assertThat(updatedListPage.summaryList.getRowByIndex(0).value).containsText("Bob Jones")
    }

    @Test
    fun `the back link on the org check answers page returns to the main contact page`(page: Page) {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

        navigator.skipToLandlordRegistrationOrgCheckAnswersPage()

        BackLink.default(page).clickAndWait()

        assertPageIs(page, OrgMainContactFormPageLandlordRegistration::class)
    }

    @Test
    fun `the back link on the individual check answers page returns to the select address page`(page: Page) {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

        navigator.skipToLandlordRegistrationCheckAnswersPage(
            LandlordStateSessionBuilder.beforeCheckAnswers().withLandlordType(LandlordType.INDIVIDUAL),
        )

        BackLink.default(page).clickAndWait()

        assertPageIs(page, SelectAddressFormPageLandlordRegistration::class)
    }

    @Test
    fun `the back link on the main contact page returns to the governing body member list page when not a registered company`(page: Page) {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

        navigator.skipToOrgLandlordRegistrationMainContactPage()

        BackLink.default(page).clickAndWait()

        assertPageIs(page, OrgGovBodyMemberListFormPageLandlordRegistration::class)
    }

    @Test
    fun `the back link on the main contact page returns to the company number page when a registered company`(page: Page) {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

        navigator.skipToOrgLandlordRegistrationMainContactPage(
            LandlordStateSessionBuilder.beforeOrgCompanyNumber().withOrgCompanyNumber(),
        )

        BackLink.default(page).clickAndWait()

        assertPageIs(page, OrgCompanyNumberFormPageLandlordRegistration::class)
    }

    private fun createTestGovBodyMember(
        name: String,
        type: GoverningBodyMemberType = GoverningBodyMemberType.DIRECTOR,
        dateOfBirth: kotlinx.datetime.LocalDate = kotlinx.datetime.LocalDate(1970, 1, 1),
        addressSearchPostcode: String? = null,
        addressSearchHouseNameOrNumber: String? = null,
        selectedAddress: String? = null,
        manualAddressLineOne: String? = null,
        manualAddressLineTwo: String? = null,
        manualTownOrCity: String? = null,
        manualCounty: String? = null,
        manualPostcode: String? = null,
    ) = uk.gov.communities.prsdb.webapp.models.dataModels.GoverningBodyMemberDataModel(
        name = name,
        type = type,
        dateOfBirth = dateOfBirth,
        address =
            uk.gov.communities.prsdb.webapp.models.dataModels
                .AddressDataModel(singleLineAddress = "Test Address"),
        addressSearchPostcode = addressSearchPostcode,
        addressSearchHouseNameOrNumber = addressSearchHouseNameOrNumber,
        selectedAddress = selectedAddress,
        manualAddressLineOne = manualAddressLineOne,
        manualAddressLineTwo = manualAddressLineTwo,
        manualTownOrCity = manualTownOrCity,
        manualCounty = manualCounty,
        manualPostcode = manualPostcode,
    )
}
