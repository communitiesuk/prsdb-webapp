package uk.gov.communities.prsdb.webapp.integration

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.microsoft.playwright.Page
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.eq
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.CheckAnswersPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.ConfirmationPageLandlordRegistration
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
import uk.gov.communities.prsdb.webapp.local.api.MockOSPlacesAPIResponses
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LandlordRegistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.testHelpers.extensions.getFormattedUkPhoneNumber
import java.net.URI
import kotlin.test.assertNotNull

class LandlordRegistrationJourneyTests : IntegrationTestWithMutableData("data-mockuser-not-landlord.sql") {
    private val phoneNumberUtil = PhoneNumberUtil.getInstance()
    private val absoluteLandlordUrl = "www.prsd.gov.uk/landlord"

    @Autowired
    private lateinit var landlordService: LandlordService

    @MockitoBean
    private lateinit var confirmationEmailSender: EmailNotificationService<LandlordRegistrationConfirmationEmail>

    @BeforeEach
    fun setup() {
        whenever(osPlacesClient.search(any(), any(), eq(false))).thenReturn(
            MockOSPlacesAPIResponses.createResponse(AddressDataModel("1, Example Road, EG1 2AB")),
        )

        whenever(absoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI(absoluteLandlordUrl))
    }

    @Test
    fun `User can navigate the whole journey if pages are correctly filled in (verified, England or Wales, selected address)`(page: Page) {
        val confirmIdentityPage = navigator.skipToLandlordRegistrationConfirmIdentityPage()
        assertThat(confirmIdentityPage.form.sectionHeader).containsText("Section 2 of 3 \u2014 Register your details")
        confirmIdentityPage.confirm()

        val emailPage = assertPageIs(page, EmailFormPageLandlordRegistration::class)
        assertThat(emailPage.form.sectionHeader).containsText("Section 2 of 3 \u2014 Register your details")
        emailPage.submitEmail("test@example.com")

        val phoneNumPage = assertPageIs(page, PhoneNumberFormPageLandlordRegistration::class)
        assertThat(phoneNumPage.form.sectionHeader).containsText("Section 2 of 3 \u2014 Register your details")
        phoneNumPage.submitPhoneNumber("07123456789")

        val countryOfResidencePage = assertPageIs(page, CountryOfResidenceFormPageLandlordRegistration::class)
        assertThat(countryOfResidencePage.form.sectionHeader).containsText("Section 2 of 3 \u2014 Register your details")
        countryOfResidencePage.submitUk()

        val lookupAddressPage = assertPageIs(page, LookupAddressFormPageLandlordRegistration::class)
        assertThat(lookupAddressPage.form.sectionHeader).containsText("Section 2 of 3 \u2014 Register your details")
        lookupAddressPage.submitPostcodeAndBuildingNameOrNumber("EG1 2AB", "1")

        val selectAddressPage = assertPageIs(page, SelectAddressFormPageLandlordRegistration::class)
        assertThat(selectAddressPage.form.sectionHeader).containsText("Section 2 of 3 \u2014 Register your details")
        selectAddressPage.selectAddressAndSubmit("1, Example Road, EG1 2AB")

        val checkAnswersPage = assertPageIs(page, CheckAnswersPageLandlordRegistration::class)
        assertThat(checkAnswersPage.form.sectionHeader).containsText("Section 3 of 3 \u2014 Check and submit registration")
        checkAnswersPage.confirm()

        val declarationPage = assertPageIs(page, DeclarationFormPageLandlordRegistration::class)
        assertThat(declarationPage.form.sectionHeader).containsText("Section 3 of 3 \u2014 Check and submit registration")
        declarationPage.agreeAndSubmit()

        val createdLandlord = assertNotNull(landlordService.retrieveLandlordByBaseUserId("urn:fdc:gov.uk:2022:UVWXY"))
        val createdLandlordRegNum =
            RegistrationNumberDataModel.fromRegistrationNumber(createdLandlord.registrationNumber)

        verify(confirmationEmailSender).sendEmail(
            "test@example.com",
            LandlordRegistrationConfirmationEmail(createdLandlordRegNum.toString(), absoluteLandlordUrl),
        )

        val confirmationPage = assertPageIs(page, ConfirmationPageLandlordRegistration::class)
        assertEquals(createdLandlordRegNum.toString(), confirmationPage.confirmationBanner.registrationNumberText)
        confirmationPage.goToDashboardButton.clickAndWait()
        val dashboard = assertPageIs(page, LandlordDashboardPage::class)

        assertThat(dashboard.dashboardBannerSubHeading).containsText("Landlord registration number")
    }

    @Test
    fun `User can navigate the whole journey if pages are correctly filled in (verified, England or Wales, manual address)`(page: Page) {
        val confirmIdentityPage = navigator.skipToLandlordRegistrationConfirmIdentityPage()
        confirmIdentityPage.confirm()

        val emailPage = assertPageIs(page, EmailFormPageLandlordRegistration::class)
        emailPage.submitEmail("test@example.com")

        val phoneNumPage = assertPageIs(page, PhoneNumberFormPageLandlordRegistration::class)
        phoneNumPage.submitPhoneNumber(phoneNumberUtil.getFormattedUkPhoneNumber())

        val countryOfResidencePage = assertPageIs(page, CountryOfResidenceFormPageLandlordRegistration::class)
        countryOfResidencePage.submitUk()

        val lookupAddressPage = assertPageIs(page, LookupAddressFormPageLandlordRegistration::class)
        lookupAddressPage.submitPostcodeAndBuildingNameOrNumber("EG1 2AB", "1")

        val selectAddressPage = assertPageIs(page, SelectAddressFormPageLandlordRegistration::class)
        selectAddressPage.selectAddressAndSubmit(MANUAL_ADDRESS_CHOSEN)

        val manualAddressPage = assertPageIs(page, ManualAddressFormPageLandlordRegistration::class)
        assertThat(manualAddressPage.form.sectionHeader).containsText("Section 2 of 3 \u2014 Register your details")
        manualAddressPage.submitAddress(
            addressLineOne = "1 Example Road",
            townOrCity = "Townville",
            postcode = "EG1 2AB",
        )

        val checkAnswersPage = assertPageIs(page, CheckAnswersPageLandlordRegistration::class)
        checkAnswersPage.confirm()

        val declarationPage = assertPageIs(page, DeclarationFormPageLandlordRegistration::class)
        declarationPage.agreeAndSubmit()

        val createdLandlord = assertNotNull(landlordService.retrieveLandlordByBaseUserId("urn:fdc:gov.uk:2022:UVWXY"))
        val createdLandlordRegNum =
            RegistrationNumberDataModel.fromRegistrationNumber(createdLandlord.registrationNumber)

        verify(confirmationEmailSender).sendEmail(
            "test@example.com",
            LandlordRegistrationConfirmationEmail(createdLandlordRegNum.toString(), absoluteLandlordUrl),
        )

        val confirmationPage = assertPageIs(page, ConfirmationPageLandlordRegistration::class)
        assertEquals(createdLandlordRegNum.toString(), confirmationPage.confirmationBanner.registrationNumberText)
        confirmationPage.goToDashboardButton.clickAndWait()
        assertPageIs(page, LandlordDashboardPage::class)
    }

    @Test
    fun `User can navigate the whole journey if pages are correctly filled in (unverified, non England or Wales, selected address)`(
        page: Page,
    ) {
        val identityNotVerifiedPage = navigator.skipToLandlordRegistrationIdentityNotVerifiedPage()
        identityNotVerifiedPage.clickContinue()

        val namePage = assertPageIs(page, NameFormPageLandlordRegistration::class)
        namePage.submitName("landlord name")

        val dateOfBirthPage = assertPageIs(page, DateOfBirthFormPageLandlordRegistration::class)
        assertThat(dateOfBirthPage.form.sectionHeader).containsText("Section 2 of 3 \u2014 Register your details")
        dateOfBirthPage.submitDate("12", "11", "1990")

        val emailPage = assertPageIs(page, EmailFormPageLandlordRegistration::class)
        emailPage.submitEmail("test@example.com")

        val phoneNumPage = assertPageIs(page, PhoneNumberFormPageLandlordRegistration::class)
        phoneNumPage.submitPhoneNumber(phoneNumberUtil.getFormattedUkPhoneNumber())

        val countryOfResidencePage = assertPageIs(page, CountryOfResidenceFormPageLandlordRegistration::class)
        countryOfResidencePage.submitNonUkCountrySelectedByPartialName("Zi", "Zimbabwe")

        val nonEnglandOrWalesAddressPage = assertPageIs(page, NonEnglandOrWalesAddressFormPageLandlordRegistration::class)
        assertThat(nonEnglandOrWalesAddressPage.form.sectionHeader).containsText("Section 2 of 3 \u2014 Register your details")
        nonEnglandOrWalesAddressPage.submitAddress("Zimbabwe address")

        val lookupContactAddressPage = assertPageIs(page, LookupContactAddressFormPageLandlordRegistration::class)
        assertThat(lookupContactAddressPage.form.sectionHeader).containsText("Section 2 of 3 \u2014 Register your details")
        lookupContactAddressPage.submitPostcodeAndBuildingNameOrNumber("EG1 2AB", "1")

        val selectContactAddressPage = assertPageIs(page, SelectContactAddressFormPageLandlordRegistration::class)
        assertThat(selectContactAddressPage.form.sectionHeader).containsText("Section 2 of 3 \u2014 Register your details")
        selectContactAddressPage.selectAddressAndSubmit("1, Example Road, EG1 2AB")

        val checkAnswersPage = assertPageIs(page, CheckAnswersPageLandlordRegistration::class)
        checkAnswersPage.confirm()

        val declarationPage = assertPageIs(page, DeclarationFormPageLandlordRegistration::class)
        declarationPage.agreeAndSubmit()

        val createdLandlord = assertNotNull(landlordService.retrieveLandlordByBaseUserId("urn:fdc:gov.uk:2022:UVWXY"))
        val createdLandlordRegNum =
            RegistrationNumberDataModel.fromRegistrationNumber(createdLandlord.registrationNumber)

        verify(confirmationEmailSender).sendEmail(
            "test@example.com",
            LandlordRegistrationConfirmationEmail(createdLandlordRegNum.toString(), absoluteLandlordUrl),
        )

        val confirmationPage = assertPageIs(page, ConfirmationPageLandlordRegistration::class)
        assertEquals(createdLandlordRegNum.toString(), confirmationPage.confirmationBanner.registrationNumberText)
        confirmationPage.goToDashboardButton.clickAndWait()
        assertPageIs(page, LandlordDashboardPage::class)
    }

    @Test
    fun `User can navigate the whole journey if pages are correctly filled in (unverified, non England or Wales, manual address)`(
        page: Page,
    ) {
        val identityNotVerifiedPage = navigator.skipToLandlordRegistrationIdentityNotVerifiedPage()
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
        countryOfResidencePage.submitNonUkCountrySelectedByPartialName("Zi", "Zimbabwe")

        val nonEnglandOrWalesAddressPage = assertPageIs(page, NonEnglandOrWalesAddressFormPageLandlordRegistration::class)
        nonEnglandOrWalesAddressPage.submitAddress("test address")

        val lookupContactAddressPage = assertPageIs(page, LookupContactAddressFormPageLandlordRegistration::class)
        lookupContactAddressPage.submitPostcodeAndBuildingNameOrNumber("EG1 2AB", "1")

        val selectContactAddressPage = assertPageIs(page, SelectContactAddressFormPageLandlordRegistration::class)
        selectContactAddressPage.selectAddressAndSubmit(MANUAL_ADDRESS_CHOSEN)

        val manualContactAddressPage = assertPageIs(page, ManualContactAddressFormPageLandlordRegistration::class)
        assertThat(manualContactAddressPage.form.sectionHeader).containsText("Section 2 of 3 \u2014 Register your details")
        manualContactAddressPage.submitAddress(
            addressLineOne = "1 Example Road",
            townOrCity = "Townville",
            postcode = "EG1 2AB",
        )

        val checkAnswersPage = assertPageIs(page, CheckAnswersPageLandlordRegistration::class)
        checkAnswersPage.confirm()

        val declarationPage = assertPageIs(page, DeclarationFormPageLandlordRegistration::class)
        declarationPage.agreeAndSubmit()
        val createdLandlord = assertNotNull(landlordService.retrieveLandlordByBaseUserId("urn:fdc:gov.uk:2022:UVWXY"))
        val createdLandlordRegNum =
            RegistrationNumberDataModel.fromRegistrationNumber(createdLandlord.registrationNumber)

        verify(confirmationEmailSender).sendEmail(
            "test@example.com",
            LandlordRegistrationConfirmationEmail(createdLandlordRegNum.toString(), absoluteLandlordUrl),
        )

        val confirmationPage = assertPageIs(page, ConfirmationPageLandlordRegistration::class)
        assertEquals(createdLandlordRegNum.toString(), confirmationPage.confirmationBanner.registrationNumberText)
        confirmationPage.goToDashboardButton.clickAndWait()
        assertPageIs(page, LandlordDashboardPage::class)
    }
}
