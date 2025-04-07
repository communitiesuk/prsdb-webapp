package uk.gov.communities.prsdb.webapp.integration

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.jdbc.Sql
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
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
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.NonEnglandOrWalesAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.PhoneNumberFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.SelectAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.SelectContactAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LandlordRegistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.testHelpers.extensions.getFormattedInternationalPhoneNumber
import uk.gov.communities.prsdb.webapp.testHelpers.extensions.getFormattedUkPhoneNumber
import java.net.URI
import kotlin.test.assertNotNull

@Sql("/data-mockuser-not-landlord.sql")
class LandlordRegistrationJourneyTests : IntegrationTest() {
    private val phoneNumberUtil = PhoneNumberUtil.getInstance()
    private val absoluteLandlordUrl = "www.prsd.gov.uk/landlord"

    @Autowired
    private lateinit var landlordService: LandlordService

    @MockitoBean
    private lateinit var confirmationEmailSender: EmailNotificationService<LandlordRegistrationConfirmationEmail>

    @BeforeEach
    fun setup() {
        whenever(
            osPlacesClient.search(any(), any()),
        ).thenReturn(
            "{'results':[{'DPA':{'ADDRESS':'1, Example Road, EG1 2AB'," +
                "'LOCAL_CUSTODIAN_CODE':28,'UPRN':'1','BUILDING_NUMBER':1,'POSTCODE':'EG1 2AB'}}]}",
        )

        whenever(absoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI(absoluteLandlordUrl))
    }

    @Test
    fun `User can navigate the whole journey if pages are correctly filled in (verified, England or Wales, selected address)`(page: Page) {
        val confirmIdentityPage = navigator.goToLandlordRegistrationConfirmIdentityFormPage()
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

        assertThat(dashboard.bannerSubHeading).containsText("Landlord registration number")
    }

    @Test
    fun `User can navigate the whole journey if pages are correctly filled in (verified, England or Wales, manual address)`(page: Page) {
        val confirmIdentityPage = navigator.goToLandlordRegistrationConfirmIdentityFormPage()
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
        val namePage = navigator.goToLandlordRegistrationNameFormPage()
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
        val namePage = navigator.goToLandlordRegistrationNameFormPage()
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

    @Nested
    inner class LandlordRegistrationStepName {
        @Test
        fun `Submitting an empty name returns an error`() {
            val namePage = navigator.goToLandlordRegistrationNameFormPage()
            namePage.submitName("")
            assertThat(namePage.form.getErrorMessage()).containsText("You must enter your full name")
        }
    }

    @Nested
    inner class LandlordRegistrationStepDateOfBirth {
        @ParameterizedTest
        @CsvSource(
            "'','','',Enter a date",
            "'',11,1990,You must include a day",
            "12,'',1990,You must include a month",
            "12,11,'',You must include a year",
            "'','',1990,You must include a day and a month",
            "12,'','',You must include a month and a year",
            "'',11,'',You must include a day and a year",
            "'',0,190,You must include a day",
            "0,'',190,You must include a month",
            "0,0,'',You must include a year",
            "'','',190,You must include a day and a month",
            "0,'','',You must include a month and a year",
        )
        fun `Submitting any empty fields returns an error`(
            day: String,
            month: String,
            year: String,
            expectedErrorMessage: String,
        ) {
            val dateOfBirthPage = navigator.goToLandlordRegistrationDateOfBirthFormPage()
            dateOfBirthPage.submitDate(day, month, year)
            assertThat(dateOfBirthPage.form.getErrorMessage()).containsText(expectedErrorMessage)
        }

        @ParameterizedTest
        @CsvSource(
            "32,11,1990,Day must be a whole number between 1 and 31",
            "0,11,1990,Day must be a whole number between 1 and 31",
            "ABC,11,1990,Day must be a whole number between 1 and 31",
            "12,13,1990,Month must be a whole number between 1 and 12",
            "12,0,1990,Month must be a whole number between 1 and 12",
            "12,ABC,1990,Month must be a whole number between 1 and 12",
            "12,11,190,Year must be a whole number greater than 1899",
            "12,11,ABC,Year must be a whole number greater than 1899",
            "0,0,1990,Day must be a whole number between 1 and 31. Month must be a whole number between 1 and 12",
            "0,11,190,Day must be a whole number between 1 and 31. Year must be a whole number greater than 1899",
            "1,0,190,Month must be a whole number between 1 and 12. Year must be a whole number greater than 1899",
            "0,0,190,Day must be a whole number between 1 and 31. Month must be a whole number between 1 and 12. " +
                "Year must be a whole number greater than 1899",
        )
        fun `Submitting invalid day, month, or year returns an error`(
            day: String,
            month: String,
            year: String,
            expectedErrorMessage: String,
        ) {
            val dateOfBirthPage = navigator.goToLandlordRegistrationDateOfBirthFormPage()
            dateOfBirthPage.submitDate(day, month, year)
            assertThat(dateOfBirthPage.form.getErrorMessage()).containsText(expectedErrorMessage)
        }

        @Nested
        inner class AgeValidation {
            private val dateTimeHelper = DateTimeHelper()

            private val currentDate = dateTimeHelper.getCurrentDateInUK()

            @Test
            fun `Submitting a valid date of birth for the minimum age redirects to the next page`(page: Page) {
                val date = currentDate.minus(DatePeriod(years = 18))
                val dateOfBirthPage = navigator.goToLandlordRegistrationDateOfBirthFormPage()
                dateOfBirthPage.submitDate(date)
                assertPageIs(page, EmailFormPageLandlordRegistration::class)
            }

            @Test
            fun `Submitting a valid date of birth for the maximum age redirects to the next page`(page: Page) {
                val date = currentDate.minus(DatePeriod(years = 121)).plus(DatePeriod(days = 1))
                val dateOfBirthPage = navigator.goToLandlordRegistrationDateOfBirthFormPage()
                dateOfBirthPage.submitDate(date)
                assertPageIs(page, EmailFormPageLandlordRegistration::class)
            }

            @Test
            fun `Submitting any invalid date for the minimum age returns an error`() {
                val date = currentDate.minus(DatePeriod(years = 18)).plus(DatePeriod(days = 1))
                val dateOfBirthPage = navigator.goToLandlordRegistrationDateOfBirthFormPage()
                dateOfBirthPage.submitDate(date)
                assertThat(dateOfBirthPage.form.getErrorMessage()).containsText(
                    "The minimum age to register as a landlord is 18",
                )
            }

            @Test
            fun `Submitting any invalid date for the maximum age returns an error`() {
                val date = currentDate.minus(DatePeriod(years = 121))
                val dateOfBirthPage = navigator.goToLandlordRegistrationDateOfBirthFormPage()
                dateOfBirthPage.submitDate(date)
                assertThat(dateOfBirthPage.form.getErrorMessage()).containsText("You must enter a valid date of birth")
            }
        }
    }

    @Nested
    inner class LandlordRegistrationStepEmail {
        @Test
        fun `Submitting an empty e-mail address returns an error`() {
            val emailPage = navigator.goToLandlordRegistrationEmailFormPage()
            emailPage.submitEmail("")
            assertThat(
                emailPage.form.getErrorMessage(),
            ).containsText("Enter a valid email address to continue. An email is required for contact purposes.")
        }

        @Test
        fun `Submitting an invalid e-mail address returns an error`() {
            val emailPage = navigator.goToLandlordRegistrationEmailFormPage()
            emailPage.submitEmail("")
            assertThat(
                emailPage.form.getErrorMessage(),
            ).containsText("Enter a valid email address to continue. An email is required for contact purposes.")
        }
    }

    @Nested
    inner class LandlordRegistrationStepPhoneNumber {
        @ParameterizedTest
        @ValueSource(
            strings = ["GB", "US", "ES", "SN", "AU", "VG"],
        )
        fun `Submitting correct UK and international numbers with country codes redirects to the next step`(
            regionCode: String,
            page: Page,
        ) {
            val phoneNumPage = navigator.goToLandlordRegistrationPhoneNumberFormPage()
            phoneNumPage.submitPhoneNumber(phoneNumberUtil.getFormattedInternationalPhoneNumber(regionCode))
            assertPageIs(page, CountryOfResidenceFormPageLandlordRegistration::class)
        }

        @Test
        fun `Submitting an empty phone number returns an error`() {
            val phoneNumPage = navigator.goToLandlordRegistrationPhoneNumberFormPage()
            phoneNumPage.submitPhoneNumber("")
            assertThat(phoneNumPage.form.getErrorMessage()).containsText("Enter a phone number")
        }

        @ParameterizedTest
        @ValueSource(
            strings = [
                "notaphonenumber",
                "0",
                // International phone number without a country code
                "0355501234",
            ],
        )
        fun `Submitting an invalid phone number returns an error`(invalidPhoneNumber: String) {
            val phoneNumPage = navigator.goToLandlordRegistrationPhoneNumberFormPage()
            phoneNumPage.submitPhoneNumber(invalidPhoneNumber)
            assertThat(
                phoneNumPage.form.getErrorMessage(),
            ).containsText("Enter a phone number including the country code for international numbers")
        }
    }

    @Nested
    inner class LandlordRegistrationStepCountryOfResidence {
        @Test
        fun `Submitting with no option selected returns an error`(page: Page) {
            val countryOfResidencePage = navigator.goToLandlordRegistrationCountryOfResidencePage()
            countryOfResidencePage.form.submit()
            assertThat(countryOfResidencePage.form.getErrorMessage()).containsText("Select an option")
        }

        @Test
        fun `Submitting the no radio with no country selected returns an error`(page: Page) {
            val countryOfResidencePage = navigator.goToLandlordRegistrationCountryOfResidencePage()
            countryOfResidencePage.form.selectNonUk()
            countryOfResidencePage.form.submit()
            assertThat(countryOfResidencePage.form.getErrorMessage())
                .containsText("Select the country or territory you are currently living in")
        }
    }

    @Nested
    inner class LandlordRegistrationStepLookupAddress {
        @Test
        fun `Submitting with empty data fields returns an error`(page: Page) {
            val lookupAddressPage = navigator.goToLandlordRegistrationLookupAddressPage()
            lookupAddressPage.form.submit()
            assertThat(lookupAddressPage.form.getErrorMessage("postcode")).containsText("Enter a postcode")
            assertThat(lookupAddressPage.form.getErrorMessage("houseNameOrNumber")).containsText("Enter a house name or number")
        }
    }

    @Nested
    inner class LandlordRegistrationStepSelectAddress {
        @Test
        fun `Submitting with no option selected returns an error`(page: Page) {
            val selectAddressPage = navigator.goToLandlordRegistrationSelectAddressPage()
            selectAddressPage.form.submit()
            assertThat(selectAddressPage.form.getErrorMessage()).containsText("Select an address")
        }

        @Test
        fun `Clicking Search Again navigates to the previous step`(page: Page) {
            val selectAddressPage = navigator.goToLandlordRegistrationSelectAddressPage()
            selectAddressPage.searchAgain.clickAndWait()
            assertPageIs(page, LookupAddressFormPageLandlordRegistration::class)
        }
    }

    @Nested
    inner class LandlordRegistrationStepManualAddress {
        @Test
        fun `Submitting empty data fields returns errors`(page: Page) {
            val manualAddressPage = navigator.goToLandlordRegistrationManualAddressPage()
            manualAddressPage.form.submit()
            assertThat(manualAddressPage.form.getErrorMessage("addressLineOne"))
                .containsText("Enter the first line of an address, typically the building and street")
            assertThat(manualAddressPage.form.getErrorMessage("townOrCity")).containsText("Enter town or city")
            assertThat(manualAddressPage.form.getErrorMessage("postcode")).containsText("Enter postcode")
        }
    }

    @Nested
    inner class LandlordRegistrationStepNonEnglandOrWalesAddress {
        @Test
        fun `Submitting with no address returns an error`(page: Page) {
            val nonEnglandOrWalesAddressPage = navigator.goToLandlordRegistrationNonEnglandOrWalesAddressPage()
            nonEnglandOrWalesAddressPage.form.submit()
            assertThat(nonEnglandOrWalesAddressPage.form.getErrorMessage()).containsText("You must include an address")
        }

        @Test
        fun `Submitting with a too long address returns an error`(page: Page) {
            val nonEnglandOrWalesAddressPage = navigator.goToLandlordRegistrationNonEnglandOrWalesAddressPage()
            nonEnglandOrWalesAddressPage.submitAddress("too long address".repeat(1001))
            assertThat(nonEnglandOrWalesAddressPage.form.getErrorMessage().nth(0)).containsText("Address must be 1000 characters or fewer")
        }
    }

    @Nested
    inner class LandlordRegistrationStepLookupContactAddress {
        @Test
        fun `Submitting with empty data fields returns an error`(page: Page) {
            val lookupContactAddressPage = navigator.goToLandlordRegistrationLookupContactAddressPage()
            lookupContactAddressPage.form.submit()
            assertThat(lookupContactAddressPage.form.getErrorMessage("postcode")).containsText("Enter a postcode")
            assertThat(lookupContactAddressPage.form.getErrorMessage("houseNameOrNumber")).containsText("Enter a house name or number")
        }
    }

    @Nested
    inner class LandlordRegistrationStepSelectContactAddress {
        @Test
        fun `Submitting with no option selected returns an error`(page: Page) {
            val selectContactAddressPage = navigator.goToLandlordRegistrationSelectContactAddressPage()
            selectContactAddressPage.form.submit()
            assertThat(selectContactAddressPage.form.getErrorMessage()).containsText("Select an address")
        }

        @Test
        fun `Clicking Search Again navigates to the previous step`(page: Page) {
            val selectContactAddressPage = navigator.goToLandlordRegistrationSelectContactAddressPage()
            selectContactAddressPage.searchAgain.clickAndWait()
            assertPageIs(page, LookupContactAddressFormPageLandlordRegistration::class)
        }
    }

    @Nested
    inner class LandlordRegistrationStepManualContactAddress {
        @Test
        fun `Submitting empty data fields returns errors`(page: Page) {
            val manualContactAddressPage = navigator.goToLandlordRegistrationManualContactAddressPage()
            manualContactAddressPage.form.submit()
            assertThat(manualContactAddressPage.form.getErrorMessage("addressLineOne"))
                .containsText("Enter the first line of an address, typically the building and street")
            assertThat(manualContactAddressPage.form.getErrorMessage("townOrCity")).containsText("Enter town or city")
            assertThat(manualContactAddressPage.form.getErrorMessage("postcode")).containsText("Enter postcode")
        }
    }

    @Nested
    inner class LandlordRegistrationStepDeclaration {
        @Test
        fun `Submitting without checking the checkbox returns an error`(page: Page) {
            val declarationPage = navigator.goToLandlordRegistrationDeclarationPage()
            declarationPage.form.submit()
            assertThat(declarationPage.form.getErrorMessage()).containsText("You must agree to the declaration to continue")
        }
    }

    @Nested
    inner class LandlordRegistrationConfirmation {
        @Test
        fun `Navigating here with an incomplete form returns a 500 error page`() {
            val errorPage = navigator.skipToLandlordRegistrationConfirmationPage()
            assertThat(errorPage.heading).containsText("Sorry, there is a problem with the service")
        }
    }
}
