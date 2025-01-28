package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor.captor
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.test.context.jdbc.Sql
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordType
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.AlreadyRegisteredFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CheckAnswersPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.ConfirmationPagePropertyRegistration
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
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SelectAddressFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SelectLocalAuthorityFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SelectiveLicenceFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.PropertyRegistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import java.net.URI

@Sql("/data-local.sql")
class PropertyRegistrationJourneyTests : IntegrationTest() {
    @SpyBean
    private lateinit var propertyOwnershipRepository: PropertyOwnershipRepository

    @SpyBean
    private lateinit var confirmationEmailSender: EmailNotificationService<PropertyRegistrationConfirmationEmail>

    @BeforeEach
    fun setup() {
        whenever(
            osPlacesClient.search(any(), any()),
        ).thenReturn(
            """
            {
              "results": [
                {
                  "DPA": {
                    "ADDRESS": "1, Example Road, EG1 2AB",
                    "LOCAL_CUSTODIAN_CODE": 28,
                    "UPRN": "1",
                    "BUILDING_NUMBER": 1,
                    "POSTCODE": "EG1 2AB"
                  }
                },
                {
                  "DPA": {
                    "ADDRESS": "already registered address",
                    "LOCAL_CUSTODIAN_CODE": 28,
                    "UPRN": "1123456",
                    "BUILDING_NUMBER": 1,
                    "POSTCODE": "EG1 3CD"
                  }
                }
              ]
            }
            """.trimIndent(),
        )
    }

    @Test
    fun `User can navigate the whole journey if pages are correctly filled in`(page: Page) {
        // TODO PRSD-622: this can be added to as more steps are added to the journey

        // Start page (not a journey step, but it is how the user accesses the journey)
        val registerPropertyStartPage = navigator.goToPropertyRegistrationStartPage()
        assertThat(registerPropertyStartPage.heading).containsText("Enter your property details")
        registerPropertyStartPage.startButton.click()
        val addressLookupPage = assertPageIs(page, LookupAddressFormPagePropertyRegistration::class)

        // Address lookup step - render page
        assertThat(addressLookupPage.form.getFieldsetHeading()).containsText("What is the property address?")
        // fill in and submit
        addressLookupPage.postcodeInput.fill("EG1 2AB")
        addressLookupPage.houseNameOrNumberInput.fill("1")
        addressLookupPage.form.submit()
        // goes to the next page
        val selectAddressPage = assertPageIs(page, SelectAddressFormPagePropertyRegistration::class)

        // Select address step - render page
        assertThat(selectAddressPage.form.getFieldsetHeading()).containsText("Select an address")
        // fill in and submit
        selectAddressPage.radios.selectValue("1, Example Road, EG1 2AB")
        selectAddressPage.form.submit()
        val propertyTypePage = assertPageIs(page, PropertyTypeFormPagePropertyRegistration::class)

        // Property type selection step - render page
        assertThat(propertyTypePage.form.getFieldsetHeading()).containsText("What type of property are you registering?")
        // fill in and submit
        propertyTypePage.form.getRadios().selectValue(PropertyType.DETACHED_HOUSE)
        propertyTypePage.form.submit()
        // goes to the next page
        val ownershipTypePage = assertPageIs(page, OwnershipTypeFormPagePropertyRegistration::class)

        // Ownership type selection step - render page
        assertThat(ownershipTypePage.form.getFieldsetHeading()).containsText("Select the ownership type for your property")
        // fill in and submit
        ownershipTypePage.form.getRadios().selectValue(OwnershipType.FREEHOLD)
        ownershipTypePage.form.submit()
        val licensingTypePage = assertPageIs(page, LicensingTypeFormPagePropertyRegistration::class)

        // Licensing type - render page
        assertThat(licensingTypePage.form.getFieldsetHeading()).containsText("Select the type of licensing you have for your property")
        // fill in and submit
        licensingTypePage.form.getRadios().selectValue(LicensingType.SELECTIVE_LICENCE)
        licensingTypePage.form.submit()
        val selectiveLicencePage = assertPageIs(page, SelectiveLicenceFormPagePropertyRegistration::class)

        // Selective licence - render page
        assertThat(selectiveLicencePage.form.getFieldsetHeading()).containsText("What is your selective licence number?")
        // fill in and submit
        selectiveLicencePage.licenceNumberInput.fill("licence number")
        selectiveLicencePage.form.submit()
        val occupancyPage = assertPageIs(page, OccupancyFormPagePropertyRegistration::class)

        // Occupancy - render page
        assertThat(occupancyPage.form.getFieldsetHeading()).containsText("Is your property occupied by tenants?")
        // fill in "yes" and submit
        occupancyPage.form.getRadios().selectValue("true")
        occupancyPage.form.submit()
        val householdsPage = assertPageIs(page, HouseholdsFormPagePropertyRegistration::class)

        // Number of Households - render page
        assertThat(householdsPage.form.getFieldsetHeading()).containsText("How many households live in your property?")
        // fill in and submit
        householdsPage.householdsInput.fill("2")
        householdsPage.form.submit()
        val peoplePage = assertPageIs(page, PeopleFormPagePropertyRegistration::class)

        // Number of people - render page
        assertThat(peoplePage.form.getFieldsetHeading()).containsText("How many people live in your property?")
        // fill in and submit
        peoplePage.peopleInput.fill("2")
        peoplePage.form.submit()
        val landlordTypePage = assertPageIs(page, LandlordTypeFormPagePropertyRegistration::class)

        // Landlord type - render page
        assertThat(landlordTypePage.form.getFieldsetHeading()).containsText("How are you operating for this property?")
        // fill in and submit
        landlordTypePage.form.getRadios().selectValue(LandlordType.SOLE)
        landlordTypePage.form.submit()
        val checkAnswersPage = assertPageIs(page, CheckAnswersPagePropertyRegistration::class)

        // Check answers - render page
        assertThat(checkAnswersPage.form.getFieldsetHeading()).containsText("Check your answers for:")

        //  submit
        checkAnswersPage.form.submit()
        val propertyOwnershipCaptor = captor<PropertyOwnership>()
        verify(propertyOwnershipRepository).save(propertyOwnershipCaptor.capture())
        val expectedPropertyRegNum =
            RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnershipCaptor.value.registrationNumber)
        //  check confirmation email
        verify(confirmationEmailSender).sendEmail(
            "alex.surname@example.com",
            PropertyRegistrationConfirmationEmail(expectedPropertyRegNum.toString(), "1, Example Road, EG1 2AB", "www.example.com"),
        )

        // Confirmation - render page
        val confirmationPage = assertPageIs(page, ConfirmationPagePropertyRegistration::class)
        assertEquals(expectedPropertyRegNum.toString(), confirmationPage.registrationNumberText)

        // go to dashboard
        confirmationPage.clickGoToDashboard()

        // TODO PRSD-670: Replace with dashboard page
        assertEquals("/", URI(page.url()).path)
    }

    @Nested
    inner class LookupAddressStep {
        @Test
        fun `Submitting with empty data fields returns an error`(page: Page) {
            val lookupAddressPage = navigator.goToPropertyRegistrationLookupAddressPage()
            lookupAddressPage.form.submit()
            assertThat(lookupAddressPage.form.getErrorMessage("postcode")).containsText("Enter a postcode")
            assertThat(lookupAddressPage.form.getErrorMessage("houseNameOrNumber")).containsText("Enter a house name or number")
        }
    }

    @Nested
    inner class SelectAddressStep {
        @Test
        fun `Submitting with no option selected returns an error`(page: Page) {
            val selectAddressPage = navigator.goToPropertyRegistrationSelectAddressPage()
            selectAddressPage.form.submit()
            assertThat(selectAddressPage.form.getErrorMessage()).containsText("Select an address")
        }

        @Test
        fun `Clicking Search Again navigates to the previous step`(page: Page) {
            val selectAddressPage = navigator.goToPropertyRegistrationSelectAddressPage()
            selectAddressPage.searchAgain.click()
            assertPageIs(page, LookupAddressFormPagePropertyRegistration::class)
        }

        @Test
        fun `Selecting the manual option navigates to the ManualAddress step`(page: Page) {
            val selectAddressPage = navigator.goToPropertyRegistrationSelectAddressPage()
            selectAddressPage.radios.selectValue(MANUAL_ADDRESS_CHOSEN)
            selectAddressPage.form.submit()
            assertPageIs(page, ManualAddressFormPagePropertyRegistration::class)
        }

        @Test
        fun `Selecting an already-registered address navigates to the AlreadyRegistered step`(page: Page) {
            val selectAddressPage = navigator.goToPropertyRegistrationSelectAddressPage()
            selectAddressPage.radios.selectValue("already registered address")
            selectAddressPage.form.submit()
            assertPageIs(page, AlreadyRegisteredFormPagePropertyRegistration::class)
        }
    }

    @Nested
    inner class ManualAddressEntryStep {
        @Test
        fun `Submitting valid data redirects to the SelectLocalAuthority step`(page: Page) {
            val manualAddressPage = navigator.goToPropertyRegistrationManualAddressPage()
            manualAddressPage.addressLineOneInput.fill("Test address line 1")
            manualAddressPage.townOrCityInput.fill("Testville")
            manualAddressPage.postcodeInput.fill("EG1 2AB")
            manualAddressPage.form.submit()
            assertPageIs(page, SelectLocalAuthorityFormPagePropertyRegistration::class)
        }

        @Test
        fun `Submitting empty data fields returns errors`(page: Page) {
            val manualAddressPage = navigator.goToPropertyRegistrationManualAddressPage()
            manualAddressPage.form.submit()
            assertThat(manualAddressPage.form.getErrorMessage("addressLineOne"))
                .containsText("Enter the first line of an address, typically the building and street")
            assertThat(manualAddressPage.form.getErrorMessage("townOrCity")).containsText("Enter town or city")
            assertThat(manualAddressPage.form.getErrorMessage("postcode")).containsText("Enter postcode")
        }
    }

    @Nested
    inner class SelectLocalAuthorityStep {
        @Test
        fun `Submitting a local authority redirects to the next step`(page: Page) {
            val selectLocalAuthorityPage = navigator.goToPropertyRegistrationSelectLocalAuthorityPage()
            selectLocalAuthorityPage.form
                .getSelect()
                .autocompleteInput
                .fill("ISLE OF MAN")
            selectLocalAuthorityPage.form.getSelect().selectValue("ISLE OF MAN")
            selectLocalAuthorityPage.form.submit()
            assertPageIs(page, PropertyTypeFormPagePropertyRegistration::class)
        }

        @Test
        fun `Submitting without selecting an LA return an error`(page: Page) {
            val selectLocalAuthorityPage = navigator.goToPropertyRegistrationSelectLocalAuthorityPage()
            selectLocalAuthorityPage.form.submit()
            assertThat(selectLocalAuthorityPage.form.getErrorMessage("localAuthorityId"))
                .containsText("Select a local authority to continue")
        }
    }

    @Nested
    inner class PropertyTypeStep {
        @Test
        fun `Submitting with other selected and the input filled in redirects to the next step`(page: Page) {
            val propertyTypePage = navigator.goToPropertyRegistrationPropertyTypePage()
            propertyTypePage.form.getRadios().selectValue(PropertyType.OTHER)
            propertyTypePage.customPropertyTypeInput.fill("End terrace house")
            propertyTypePage.form.submit()
            assertPageIs(page, OwnershipTypeFormPagePropertyRegistration::class)
        }

        @Test
        fun `Submitting with no propertyType selected returns an error`(page: Page) {
            val propertyTypePage = navigator.goToPropertyRegistrationPropertyTypePage()
            propertyTypePage.form.submit()
            assertThat(propertyTypePage.form.getErrorMessage()).containsText("Select the type of property")
        }

        @Test
        fun `Submitting with the Other propertyType selected but an empty customPropertyType field returns an error`(page: Page) {
            val propertyTypePage = navigator.goToPropertyRegistrationPropertyTypePage()
            propertyTypePage.form.getRadios().selectValue(PropertyType.OTHER)
            propertyTypePage.form.submit()
            assertThat(propertyTypePage.form.getErrorMessage()).containsText("Enter the property type")
        }
    }

    @Nested
    inner class OwnershipTypeStep {
        @Test
        fun `Submitting with no ownershipType selected returns an error`(page: Page) {
            val ownershipTypePage = navigator.goToPropertyRegistrationOwnershipTypePage()
            ownershipTypePage.form.submit()
            assertThat(ownershipTypePage.form.getErrorMessage()).containsText("Select the ownership type")
        }
    }

    @Nested
    inner class OccupancyStep {
        @Test
        fun `Submitting with the not occupied option selected skips to the next step`(page: Page) {
            val occupancyPage = navigator.goToPropertyRegistrationOccupancyPage()
            occupancyPage.form.getRadios().selectValue("false")
            occupancyPage.form.submit()
            assertPageIs(page, LandlordTypeFormPagePropertyRegistration::class)
        }

        @Test
        fun `Submitting with no occupancy option selected returns an error`(page: Page) {
            val occupancyPage = navigator.goToPropertyRegistrationOccupancyPage()
            occupancyPage.form.submit()
            assertThat(occupancyPage.form.getErrorMessage()).containsText("Select whether the property is occupied")
        }
    }

    @Nested
    inner class NumberOfHouseholdsStep {
        @Test
        fun `Submitting with a blank numberOfHouseholds field returns an error`(page: Page) {
            val householdsPage = navigator.goToPropertyRegistrationHouseholdsPage()
            householdsPage.form.submit()
            assertThat(householdsPage.form.getErrorMessage()).containsText("Enter the number of households living in your property")
        }

        @Test
        fun `Submitting with a non-numerical value in the numberOfHouseholds field returns an error`(page: Page) {
            val householdsPage = navigator.goToPropertyRegistrationHouseholdsPage()
            householdsPage.householdsInput.fill("not-a-number")
            householdsPage.form.submit()
            assertThat(householdsPage.form.getErrorMessage())
                .containsText("Number of households in your property must be a positive, whole number, like 3")
        }

        @Test
        fun `Submitting with a non-integer number in the numberOfHouseholds field returns an error`(page: Page) {
            val householdsPage = navigator.goToPropertyRegistrationHouseholdsPage()
            householdsPage.householdsInput.fill("2.3")
            householdsPage.form.submit()
            assertThat(householdsPage.form.getErrorMessage())
                .containsText("Number of households in your property must be a positive, whole number, like 3")
        }

        @Test
        fun `Submitting with a negative integer in the numberOfHouseholds field returns an error`(page: Page) {
            val householdsPage = navigator.goToPropertyRegistrationHouseholdsPage()
            householdsPage.householdsInput.fill("-2")
            householdsPage.form.submit()
            assertThat(householdsPage.form.getErrorMessage())
                .containsText("Number of households in your property must be a positive, whole number, like 3")
        }
    }

    @Nested
    inner class NumberOfPeopleStep {
        @Test
        fun `Submitting with a blank numberOfPeople field returns an error`(page: Page) {
            val peoplePage = navigator.goToPropertyRegistrationPeoplePage()
            peoplePage.form.submit()
            assertThat(peoplePage.form.getErrorMessage()).containsText("Enter the number of people living in your property")
        }

        @Test
        fun `Submitting with a non-numerical value in the numberOfPeople field returns an error`(page: Page) {
            val peoplePage = navigator.goToPropertyRegistrationPeoplePage()
            peoplePage.peopleInput.fill("not-a-number")
            peoplePage.form.submit()
            assertThat(peoplePage.form.getErrorMessage())
                .containsText("Number of people in your property must be a positive, whole number, like 3")
        }

        @Test
        fun `Submitting with a non-integer number in the numberOfPeople field returns an error`(page: Page) {
            val peoplePage = navigator.goToPropertyRegistrationPeoplePage()
            peoplePage.peopleInput.fill("2.3")
            peoplePage.form.submit()
            assertThat(peoplePage.form.getErrorMessage())
                .containsText("Number of people in your property must be a positive, whole number, like 3")
        }

        @Test
        fun `Submitting with a negative integer in the numberOfPeople field returns an error`(page: Page) {
            val peoplePage = navigator.goToPropertyRegistrationPeoplePage()
            peoplePage.peopleInput.fill("-2")
            peoplePage.form.submit()
            assertThat(peoplePage.form.getErrorMessage())
                .containsText("Number of people in your property must be a positive, whole number, like 3")
        }
    }

    @Nested
    inner class LandlordTypeStep {
        @Test
        fun `Submitting with a blank landlordType field returns an error`(page: Page) {
            val landlordTypePage = navigator.goToPropertyRegistrationLandlordTypePage()
            landlordTypePage.form.submit()
            assertThat(landlordTypePage.form.getErrorMessage()).containsText("Select how you are operating this property")
        }
    }

    @Nested
    inner class LicensingTypeStep {
        @Test
        fun `Submitting with no licensingType selected returns an error`(page: Page) {
            val licensingTypePage = navigator.goToPropertyRegistrationLicensingTypePage()
            licensingTypePage.form.submit()
            assertThat(licensingTypePage.form.getErrorMessage()).containsText("Select the type of licensing for the property")
        }

        @Test
        fun `Submitting with no licensing for property redirects to the next step`(page: Page) {
            val licensingTypePage = navigator.goToPropertyRegistrationLicensingTypePage()
            licensingTypePage.form.getRadios().selectValue(LicensingType.NO_LICENSING)
            licensingTypePage.form.submit()
            assertPageIs(page, OccupancyFormPagePropertyRegistration::class)
        }

        @Test
        fun `Submitting with an HMO mandatory licence redirects to the next step`(page: Page) {
            val licensingTypePage = navigator.goToPropertyRegistrationLicensingTypePage()
            licensingTypePage.form.getRadios().selectValue(LicensingType.HMO_MANDATORY_LICENCE)
            licensingTypePage.form.submit()
            assertPageIs(page, HmoMandatoryLicenceFormPagePropertyRegistration::class)
        }

        @Test
        fun `Submitting with an HMO additional licence redirects to the next step`(page: Page) {
            val licensingTypePage = navigator.goToPropertyRegistrationLicensingTypePage()
            licensingTypePage.form.getRadios().selectValue(LicensingType.HMO_ADDITIONAL_LICENCE)
            licensingTypePage.form.submit()
            assertPageIs(page, HmoAdditionalLicenceFormPagePropertyRegistration::class)
        }
    }

    @Nested
    inner class SelectiveLicenceStep {
        @Test
        fun `Submitting with no licence number returns an error`(page: Page) {
            val selectiveLicencePage = navigator.goToPropertyRegistrationSelectiveLicencePage()
            selectiveLicencePage.form.submit()
            assertThat(selectiveLicencePage.form.getErrorMessage()).containsText("Enter the selective licence number")
        }

        @Test
        fun `Submitting with a very long licence number returns an error`(page: Page) {
            val selectiveLicencePage = navigator.goToPropertyRegistrationSelectiveLicencePage()
            val aVeryLongString =
                "This string is very long, so long that it is not feasible that it is a real licence number " +
                    "- therefore if it is submitted there will in fact be an error rather than a successful submission." +
                    " It is actually quite difficult for a string to be long enough to trigger this error, because the" +
                    " maximum length has been selected to be permissive of id numbers we do not expect while still having " +
                    "a cap reachable with a little effort."
            selectiveLicencePage.licenceNumberInput.fill(aVeryLongString)
            selectiveLicencePage.form.submit()
            assertThat(selectiveLicencePage.form.getErrorMessage()).containsText("The licensing number is too long")
        }
    }

    @Nested
    inner class HmoMandatoryLicenceStep {
        @Test
        fun `Submitting with a licence number redirects to the next step`(page: Page) {
            val hmoMandatoryLicencePage = navigator.goToPropertyRegistrationHmoMandatoryLicencePage()
            hmoMandatoryLicencePage.licenceNumberInput.fill("licence number")
            hmoMandatoryLicencePage.form.submit()
            assertPageIs(page, OccupancyFormPagePropertyRegistration::class)
        }

        @Test
        fun `Submitting with no licence number returns an error`(page: Page) {
            val hmoMandatoryLicencePage = navigator.goToPropertyRegistrationHmoMandatoryLicencePage()
            hmoMandatoryLicencePage.form.submit()
            assertThat(hmoMandatoryLicencePage.form.getErrorMessage()).containsText("Enter the HMO Mandatory licence number")
        }

        @Test
        fun `Submitting with a very long licence number returns an error`(page: Page) {
            val hmoMandatoryLicencePage = navigator.goToPropertyRegistrationHmoMandatoryLicencePage()
            val aVeryLongString =
                "This string is very long, so long that it is not feasible that it is a real licence number " +
                    "- therefore if it is submitted there will in fact be an error rather than a successful submission." +
                    " It is actually quite difficult for a string to be long enough to trigger this error, because the" +
                    " maximum length has been selected to be permissive of id numbers we do not expect while still having " +
                    "a cap reachable with a little effort."
            hmoMandatoryLicencePage.licenceNumberInput.fill(aVeryLongString)
            hmoMandatoryLicencePage.form.submit()
            assertThat(hmoMandatoryLicencePage.form.getErrorMessage()).containsText("The licensing number is too long")
        }
    }

    @Nested
    inner class HmoAdditionalLicenceStep {
        @Test
        fun `Submitting with a licence number redirects to the next step`(page: Page) {
            val hmoAdditionalLicencePage = navigator.goToPropertyRegistrationHmoAdditionalLicencePage()
            hmoAdditionalLicencePage.licenceNumberInput.fill("licence number")
            hmoAdditionalLicencePage.form.submit()
            assertPageIs(page, OccupancyFormPagePropertyRegistration::class)
        }

        @Test
        fun `Submitting with no licence number returns an error`(page: Page) {
            val hmoAdditionalLicencePage = navigator.goToPropertyRegistrationHmoAdditionalLicencePage()
            hmoAdditionalLicencePage.form.submit()
            assertThat(hmoAdditionalLicencePage.form.getErrorMessage()).containsText("Enter the HMO additional licence number")
        }

        @Test
        fun `Submitting with a very long licence number returns an error`(page: Page) {
            val hmoAdditionalLicencePage = navigator.goToPropertyRegistrationHmoAdditionalLicencePage()
            val aVeryLongString =
                "This string is very long, so long that it is not feasible that it is a real licence number " +
                    "- therefore if it is submitted there will in fact be an error rather than a successful submission." +
                    " It is actually quite difficult for a string to be long enough to trigger this error, because the" +
                    " maximum length has been selected to be permissive of id numbers we do not expect while still having " +
                    "a cap reachable with a little effort."
            hmoAdditionalLicencePage.licenceNumberInput.fill(aVeryLongString)
            hmoAdditionalLicencePage.form.submit()
            assertThat(hmoAdditionalLicencePage.form.getErrorMessage()).containsText("The licensing number is too long")
        }
    }

    @Nested
    inner class Confirmation {
        @Test
        fun `Navigating here with an incomplete form returns a 400 error page`() {
            val errorPage = navigator.skipToPropertyRegistrationConfirmationPage()
            assertThat(errorPage.heading).containsText("Sorry, there is a problem with the service")
        }
    }
}
