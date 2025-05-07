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
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.context.jdbc.Sql
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.StartPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.AlreadyRegisteredFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CheckAnswersPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.ConfirmationPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.DeclarationFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.HmoAdditionalLicenceFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.HmoMandatoryLicenceFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.LicensingTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.LookupAddressFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.ManualAddressFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.NoAddressFoundFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.NumberOfHouseholdsFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.NumberOfPeopleFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.OccupancyFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.OwnershipTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.PropertyTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SelectAddressFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SelectLocalAuthorityFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SelectiveLicenceFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.TaskListPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyRegistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import java.net.URI
import kotlin.test.assertTrue

@Sql("/data-local.sql")
class PropertyRegistrationJourneyTests : IntegrationTest() {
    private val absoluteLandlordUrl = "www.prsd.gov.uk/landlord"

    @MockitoSpyBean
    private lateinit var propertyOwnershipRepository: PropertyOwnershipRepository

    @MockitoBean
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

        whenever(absoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI(absoluteLandlordUrl))
    }

    @Test
    @Suppress("ktlint:standard:max-line-length")
    fun `User can navigate the whole journey if pages are correctly filled in (select address, non-custom property type, selective license, occupied)`(
        page: Page,
    ) {
        // Start page (not a journey step, but it is how the user accesses the journey)
        val registerPropertyStartPage = navigator.goToPropertyRegistrationStartPage()
        assertThat(registerPropertyStartPage.heading).containsText("Enter your property details")
        registerPropertyStartPage.startButton.clickAndWait()
        val taskListPage = assertPageIs(page, TaskListPagePropertyRegistration::class)

        // Task list page (part of the journey to support redirects)
        taskListPage.clickRegisterTaskWithName("Add the property address")
        val addressLookupPage = assertPageIs(page, LookupAddressFormPagePropertyRegistration::class)

        // Address lookup - render page
        assertThat(addressLookupPage.form.fieldsetHeading).containsText("What is the property address?")
        assertThat(addressLookupPage.form.sectionHeader).containsText("Section 1 of 2 \u2014 Register your property details")
        // fill in and submit
        addressLookupPage.submitPostcodeAndBuildingNameOrNumber("EG1 2AB", "1")
        val selectAddressPage = assertPageIs(page, SelectAddressFormPagePropertyRegistration::class)

        // Select address - render page
        assertThat(selectAddressPage.form.fieldsetHeading).containsText("Select an address")
        assertThat(selectAddressPage.form.sectionHeader).containsText("Section 1 of 2 \u2014 Register your property details")
        // fill in and submit
        selectAddressPage.selectAddressAndSubmit("1, Example Road, EG1 2AB")
        val propertyTypePage = assertPageIs(page, PropertyTypeFormPagePropertyRegistration::class)

        // Property type selection - render page
        assertThat(propertyTypePage.form.fieldsetHeading).containsText("What type of property are you registering?")
        assertThat(propertyTypePage.form.sectionHeader).containsText("Section 1 of 2 \u2014 Register your property details")
        // fill in and submit
        propertyTypePage.submitPropertyType(PropertyType.DETACHED_HOUSE)
        val ownershipTypePage = assertPageIs(page, OwnershipTypeFormPagePropertyRegistration::class)

        // Ownership type selection - render page
        assertThat(ownershipTypePage.form.fieldsetHeading).containsText("Select the ownership type for your property")
        assertThat(ownershipTypePage.form.sectionHeader).containsText("Section 1 of 2 \u2014 Register your property details")
        // fill in and submit
        ownershipTypePage.submitOwnershipType(OwnershipType.FREEHOLD)
        val licensingTypePage = assertPageIs(page, LicensingTypeFormPagePropertyRegistration::class)

        // Licensing type - render page
        assertThat(licensingTypePage.form.fieldsetHeading).containsText("Select the type of licensing you have for your property")
        assertThat(licensingTypePage.form.sectionHeader).containsText("Section 1 of 2 \u2014 Register your property details")
        // fill in and submit
        licensingTypePage.submitLicensingType(LicensingType.SELECTIVE_LICENCE)
        val selectiveLicencePage = assertPageIs(page, SelectiveLicenceFormPagePropertyRegistration::class)

        // Selective licence - render page
        assertThat(selectiveLicencePage.form.fieldsetHeading).containsText("What is your selective licence number?")
        assertThat(selectiveLicencePage.form.sectionHeader).containsText("Section 1 of 2 \u2014 Register your property details")
        // fill in and submit
        selectiveLicencePage.submitLicenseNumber("licence number")
        val occupancyPage = assertPageIs(page, OccupancyFormPagePropertyRegistration::class)

        // Occupancy - render page
        assertThat(occupancyPage.form.fieldsetHeading).containsText("Is your property occupied by tenants?")
        assertThat(occupancyPage.form.sectionHeader).containsText("Section 1 of 2 \u2014 Register your property details")
        // fill in and submit
        occupancyPage.submitIsOccupied()
        val householdsPage = assertPageIs(page, NumberOfHouseholdsFormPagePropertyRegistration::class)

        // Number of households - render page
        assertThat(householdsPage.form.fieldsetHeading).containsText("How many households live in your property?")
        assertThat(householdsPage.form.sectionHeader).containsText("Section 1 of 2 \u2014 Register your property details")
        // fill in and submit
        householdsPage.submitNumberOfHouseholds(2)
        val peoplePage = assertPageIs(page, NumberOfPeopleFormPagePropertyRegistration::class)

        // Number of people - render page
        assertThat(peoplePage.form.fieldsetHeading).containsText("How many people live in your property?")
        assertThat(peoplePage.form.sectionHeader).containsText("Section 1 of 2 \u2014 Register your property details")
        // fill in and submit
        peoplePage.submitNumOfPeople(2)
        val checkAnswersPage = assertPageIs(page, CheckAnswersPagePropertyRegistration::class)

        // Check answers - render page
        assertThat(checkAnswersPage.form.fieldsetHeading).containsText("Check your answers for:")
        assertThat(checkAnswersPage.form.sectionHeader).containsText("Section 2 of 2 \u2014 Check and submit your property details")
        // submit
        checkAnswersPage.confirm()
        val declarationPage = assertPageIs(page, DeclarationFormPagePropertyRegistration::class)

        // Declaration - render page
        assertThat(declarationPage.form.fieldsetHeading).containsText("Declaration")
        assertThat(declarationPage.form.sectionHeader).containsText("Section 2 of 2 \u2014 Check and submit your property details")
        // fill in and submit
        declarationPage.agreeAndSubmit()
        val confirmationPage = assertPageIs(page, ConfirmationPagePropertyRegistration::class)

        // Confirmation - render page
        val propertyOwnershipCaptor = captor<PropertyOwnership>()
        verify(propertyOwnershipRepository).save(propertyOwnershipCaptor.capture())
        val expectedPropertyRegNum = RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnershipCaptor.value.registrationNumber)
        assertEquals(expectedPropertyRegNum.toString(), confirmationPage.registrationNumberText)
        assertTrue(confirmationPage.addComplianceButton.locator.isVisible)
        assertTrue(confirmationPage.goToDashboardButton.locator.isVisible)

        // Check confirmation email
        verify(confirmationEmailSender).sendEmail(
            "alex.surname@example.com",
            PropertyRegistrationConfirmationEmail(
                expectedPropertyRegNum.toString(),
                "1, Example Road, EG1 2AB",
                absoluteLandlordUrl,
            ),
        )

        // Go to compliance journey
        confirmationPage.addComplianceButton.clickAndWait()
        assertPageIs(page, StartPagePropertyCompliance::class, mapOf("propertyOwnershipId" to propertyOwnershipCaptor.value.id.toString()))
    }

    @Test
    @Suppress("ktlint:standard:max-line-length")
    fun `User can navigate the whole journey if pages are correctly filled in (manual address, custom property type, no license, unoccupied)`(
        page: Page,
    ) {
        // Start page (not a journey step, but it is how the user accesses the journey)
        val registerPropertyStartPage = navigator.goToPropertyRegistrationStartPage()
        assertThat(registerPropertyStartPage.heading).containsText("Enter your property details")
        registerPropertyStartPage.startButton.clickAndWait()
        val taskListPage = assertPageIs(page, TaskListPagePropertyRegistration::class)

        // Task list page (part of the journey to support redirects)
        taskListPage.clickRegisterTaskWithName("Add the property address")
        val addressLookupPage = assertPageIs(page, LookupAddressFormPagePropertyRegistration::class)

        // Address lookup - render page
        assertThat(addressLookupPage.form.fieldsetHeading).containsText("What is the property address?")
        assertThat(addressLookupPage.form.sectionHeader).containsText("Section 1 of 2 \u2014 Register your property details")
        // fill in and submit
        addressLookupPage.submitPostcodeAndBuildingNameOrNumber("EG1 2AB", "1")
        val selectAddressPage = assertPageIs(page, SelectAddressFormPagePropertyRegistration::class)

        // Select address - render page
        assertThat(selectAddressPage.form.fieldsetHeading).containsText("Select an address")
        assertThat(selectAddressPage.form.sectionHeader).containsText("Section 1 of 2 \u2014 Register your property details")
        // fill in and submit
        selectAddressPage.selectAddressAndSubmit(MANUAL_ADDRESS_CHOSEN)
        val manualAddressPage = assertPageIs(page, ManualAddressFormPagePropertyRegistration::class)

        // Manual address - render page
        assertThat(manualAddressPage.form.fieldsetHeading).containsText("What is the property address?")
        assertThat(manualAddressPage.form.sectionHeader).containsText("Section 1 of 2 \u2014 Register your property details")
        // fill in and submit
        manualAddressPage.submitAddress(addressLineOne = "Test address line 1", townOrCity = "Testville", postcode = "EG1 2AB")
        val selectLocalAuthorityPage = assertPageIs(page, SelectLocalAuthorityFormPagePropertyRegistration::class)

        // Select local authority - render page
        assertThat(selectLocalAuthorityPage.form.fieldsetHeading).containsText("What local authority area is your property in?")
        assertThat(selectLocalAuthorityPage.form.sectionHeader).containsText("Section 1 of 2 \u2014 Register your property details")
        // fill in and submit
        selectLocalAuthorityPage.submitLocalAuthority("ISLE OF MAN", "ISLE OF MAN")
        val propertyTypePage = assertPageIs(page, PropertyTypeFormPagePropertyRegistration::class)

        // Property type selection - render page
        assertThat(propertyTypePage.form.fieldsetHeading).containsText("What type of property are you registering?")
        assertThat(propertyTypePage.form.sectionHeader).containsText("Section 1 of 2 \u2014 Register your property details")
        // fill in and submit
        propertyTypePage.submitCustomPropertyType("End terrace house")
        val ownershipTypePage = assertPageIs(page, OwnershipTypeFormPagePropertyRegistration::class)

        // Ownership type selection - render page
        assertThat(ownershipTypePage.form.fieldsetHeading).containsText("Select the ownership type for your property")
        assertThat(ownershipTypePage.form.sectionHeader).containsText("Section 1 of 2 \u2014 Register your property details")
        // fill in and submit
        ownershipTypePage.submitOwnershipType(OwnershipType.FREEHOLD)
        val licensingTypePage = assertPageIs(page, LicensingTypeFormPagePropertyRegistration::class)

        // Licensing type - render page
        assertThat(licensingTypePage.form.fieldsetHeading).containsText("Select the type of licensing you have for your property")
        assertThat(licensingTypePage.form.sectionHeader).containsText("Section 1 of 2 \u2014 Register your property details")
        // fill in and submit
        licensingTypePage.submitLicensingType(LicensingType.NO_LICENSING)
        val occupancyPage = assertPageIs(page, OccupancyFormPagePropertyRegistration::class)

        // Occupancy - render page
        assertThat(occupancyPage.form.fieldsetHeading).containsText("Is your property occupied by tenants?")
        assertThat(occupancyPage.form.sectionHeader).containsText("Section 1 of 2 \u2014 Register your property details")
        // fill in and submit
        occupancyPage.submitIsVacant()
        val checkAnswersPage = assertPageIs(page, CheckAnswersPagePropertyRegistration::class)

        // Check answers - render page
        assertThat(checkAnswersPage.form.fieldsetHeading).containsText("Check your answers for:")
        assertThat(checkAnswersPage.form.sectionHeader).containsText("Section 2 of 2 \u2014 Check and submit your property details")
        // submit
        checkAnswersPage.confirm()
        val declarationPage = assertPageIs(page, DeclarationFormPagePropertyRegistration::class)

        // Declaration - render page
        assertThat(declarationPage.form.fieldsetHeading).containsText("Declaration")
        assertThat(declarationPage.form.sectionHeader).containsText("Section 2 of 2 \u2014 Check and submit your property details")
        // fill in and submit
        declarationPage.agreeAndSubmit()
        val confirmationPage = assertPageIs(page, ConfirmationPagePropertyRegistration::class)

        // Confirmation - render page
        val propertyOwnershipCaptor = captor<PropertyOwnership>()
        verify(propertyOwnershipRepository).save(propertyOwnershipCaptor.capture())
        val expectedPropertyRegNum = RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnershipCaptor.value.registrationNumber)
        assertEquals(expectedPropertyRegNum.toString(), confirmationPage.registrationNumberText)
        assertTrue(confirmationPage.addComplianceButton.locator.isHidden)
        assertTrue(confirmationPage.goToDashboardButton.locator.isVisible)

        // Check confirmation email
        verify(confirmationEmailSender).sendEmail(
            "alex.surname@example.com",
            PropertyRegistrationConfirmationEmail(
                expectedPropertyRegNum.toString(),
                "Test address line 1, Testville, EG1 2AB",
                absoluteLandlordUrl,
            ),
        )

        // Go to dashboard
        confirmationPage.goToDashboardButton.clickAndWait()
        assertPageIs(page, LandlordDashboardPage::class)
    }

    @Nested
    inner class TaskListStep {
        @Test
        fun `Completing preceding steps will show a task as not yet started and completed steps as complete`(page: Page) {
            navigator.goToPropertyRegistrationOccupancyPage()
            val taskListPage = navigator.goToPropertyRegistrationTaskList()
            assert(taskListPage.taskHasStatus("Add the property address", "Complete"))
            assert(taskListPage.taskHasStatus("Select the type of property", "Complete"))
            assert(taskListPage.taskHasStatus("Select the ownership type", "Complete"))
            assert(taskListPage.taskHasStatus("Add any property licensing information", "Complete"))
            assert(taskListPage.taskHasStatus("Add any tenancy and household information", "Not yet started"))
        }

        @Test
        fun `Completing first step of a task will show a task as in progress and completed steps as complete`(page: Page) {
            navigator.goToPropertyRegistrationHmoAdditionalLicencePage()
            val taskListPage = navigator.goToPropertyRegistrationTaskList()
            assert(taskListPage.taskHasStatus("Add the property address", "Complete"))
            assert(taskListPage.taskHasStatus("Select the type of property", "Complete"))
            assert(taskListPage.taskHasStatus("Select the ownership type", "Complete"))
            assert(taskListPage.taskHasStatus("Add any property licensing information", "In progress"))
            assert(taskListPage.taskHasStatus("Add any tenancy and household information", "Cannot start yet"))
        }
    }

    @Nested
    inner class LookupAddressAndNoAddressFoundSteps {
        @Test
        fun `Submitting with empty data fields returns an error`(page: Page) {
            val lookupAddressPage = navigator.goToPropertyRegistrationLookupAddressPage()
            lookupAddressPage.form.submit()
            assertThat(lookupAddressPage.form.getErrorMessage("postcode")).containsText("Enter a postcode")
            assertThat(lookupAddressPage.form.getErrorMessage("houseNameOrNumber")).containsText("Enter a house name or number")
        }

        @Test
        fun `If no addresses are returned, user can search again or enter address manually via the No Address Found step`(page: Page) {
            // Lookup address finds no results
            val houseNumber = "15"
            val postcode = "AB1 2CD"
            whenever(osPlacesClient.search(houseNumber, postcode)).thenReturn("{}")
            val lookupAddressPage = navigator.goToPropertyRegistrationLookupAddressPage()
            lookupAddressPage.submitPostcodeAndBuildingNameOrNumber(postcode, houseNumber)

            // redirect to noAddressFoundPage
            val noAddressFoundPage = assertPageIs(page, NoAddressFoundFormPagePropertyRegistration::class)
            assertThat(noAddressFoundPage.heading).containsText(houseNumber)
            assertThat(noAddressFoundPage.heading).containsText(postcode)

            // Search Again
            noAddressFoundPage.searchAgain.clickAndWait()
            val lookupAddressPageAgain = assertPageIs(page, LookupAddressFormPagePropertyRegistration::class)
            lookupAddressPageAgain.submitPostcodeAndBuildingNameOrNumber(postcode, houseNumber)

            // Submit no address found page
            val noAddressFoundPageAgain = assertPageIs(page, NoAddressFoundFormPagePropertyRegistration::class)
            noAddressFoundPageAgain.form.submit()
            assertPageIs(page, ManualAddressFormPagePropertyRegistration::class)
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
            selectAddressPage.searchAgain.clickAndWait()
            assertPageIs(page, LookupAddressFormPagePropertyRegistration::class)
        }

        @Test
        fun `Selecting an already-registered address navigates to the AlreadyRegistered step`(page: Page) {
            val selectAddressPage = navigator.goToPropertyRegistrationSelectAddressPage()
            selectAddressPage.selectAddressAndSubmit("already registered address")
            assertPageIs(page, AlreadyRegisteredFormPagePropertyRegistration::class)
        }
    }

    @Nested
    inner class ManualAddressEntryStep {
        @Test
        fun `Submitting empty data fields returns errors`(page: Page) {
            val manualAddressPage = navigator.goToPropertyRegistrationManualAddressPage()
            manualAddressPage.submitAddress()
            assertThat(manualAddressPage.form.getErrorMessage("addressLineOne"))
                .containsText("Enter the first line of an address, typically the building and street")
            assertThat(manualAddressPage.form.getErrorMessage("townOrCity")).containsText("Enter town or city")
            assertThat(manualAddressPage.form.getErrorMessage("postcode")).containsText("Enter postcode")
        }
    }

    @Nested
    inner class SelectLocalAuthorityStep {
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
        fun `Submitting with no propertyType selected returns an error`(page: Page) {
            val propertyTypePage = navigator.goToPropertyRegistrationPropertyTypePage()
            propertyTypePage.form.submit()
            assertThat(propertyTypePage.form.getErrorMessage()).containsText("Select the type of property")
        }

        @Test
        fun `Submitting with the Other propertyType selected but an empty customPropertyType field returns an error`(page: Page) {
            val propertyTypePage = navigator.goToPropertyRegistrationPropertyTypePage()
            propertyTypePage.submitCustomPropertyType("")
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
    inner class LicensingTypeStep {
        @Test
        fun `Submitting with no licensingType selected returns an error`(page: Page) {
            val licensingTypePage = navigator.goToPropertyRegistrationLicensingTypePage()
            licensingTypePage.form.submit()
            assertThat(licensingTypePage.form.getErrorMessage()).containsText("Select the type of licensing for the property")
        }

        @Test
        fun `Submitting with an HMO mandatory licence redirects to the next step`(page: Page) {
            val licensingTypePage = navigator.goToPropertyRegistrationLicensingTypePage()
            licensingTypePage.submitLicensingType(LicensingType.HMO_MANDATORY_LICENCE)
            val licenseNumberPage = assertPageIs(page, HmoMandatoryLicenceFormPagePropertyRegistration::class)
            assertThat(licenseNumberPage.form.sectionHeader).containsText("Section 1 of 2 \u2014 Register your property details")
        }

        @Test
        fun `Submitting with an HMO additional licence redirects to the next step`(page: Page) {
            val licensingTypePage = navigator.goToPropertyRegistrationLicensingTypePage()
            licensingTypePage.submitLicensingType(LicensingType.HMO_ADDITIONAL_LICENCE)
            val licenseNumberPage = assertPageIs(page, HmoAdditionalLicenceFormPagePropertyRegistration::class)
            assertThat(licenseNumberPage.form.sectionHeader).containsText("Section 1 of 2 \u2014 Register your property details")
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
            selectiveLicencePage.submitLicenseNumber(aVeryLongString)
            assertThat(selectiveLicencePage.form.getErrorMessage()).containsText("The licensing number is too long")
        }
    }

    @Nested
    inner class HmoMandatoryLicenceStep {
        @Test
        fun `Submitting with a licence number redirects to the next step`(page: Page) {
            val hmoMandatoryLicencePage = navigator.goToPropertyRegistrationHmoMandatoryLicencePage()
            hmoMandatoryLicencePage.submitLicenseNumber("licence number")
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
            hmoMandatoryLicencePage.submitLicenseNumber(aVeryLongString)
            assertThat(hmoMandatoryLicencePage.form.getErrorMessage()).containsText("The licensing number is too long")
        }
    }

    @Nested
    inner class HmoAdditionalLicenceStep {
        @Test
        fun `Submitting with a licence number redirects to the next step`(page: Page) {
            val hmoAdditionalLicencePage = navigator.goToPropertyRegistrationHmoAdditionalLicencePage()
            hmoAdditionalLicencePage.submitLicenseNumber("licence number")
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
            hmoAdditionalLicencePage.submitLicenseNumber(aVeryLongString)
            assertThat(hmoAdditionalLicencePage.form.getErrorMessage()).containsText("The licensing number is too long")
        }
    }

    @Nested
    inner class OccupancyStep {
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
            householdsPage.submitNumberOfHouseholds("not-a-number")
            assertThat(householdsPage.form.getErrorMessage())
                .containsText("Number of households in your property must be a positive, whole number, like 3")
        }

        @Test
        fun `Submitting with a non-integer number in the numberOfHouseholds field returns an error`(page: Page) {
            val householdsPage = navigator.goToPropertyRegistrationHouseholdsPage()
            householdsPage.submitNumberOfHouseholds("2.3")
            assertThat(householdsPage.form.getErrorMessage())
                .containsText("Number of households in your property must be a positive, whole number, like 3")
        }

        @Test
        fun `Submitting with a negative integer in the numberOfHouseholds field returns an error`(page: Page) {
            val householdsPage = navigator.goToPropertyRegistrationHouseholdsPage()
            householdsPage.submitNumberOfHouseholds(-2)
            assertThat(householdsPage.form.getErrorMessage())
                .containsText("Number of households in your property must be a positive, whole number, like 3")
        }

        @Test
        fun `Submitting with a zero integer in the numberOfHouseholds field returns an error`(page: Page) {
            val householdsPage = navigator.goToPropertyRegistrationHouseholdsPage()
            householdsPage.submitNumberOfHouseholds(0)
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
            peoplePage.submitNumOfPeople("not-a-number")
            assertThat(peoplePage.form.getErrorMessage())
                .containsText("Number of people in your property must be a positive, whole number, like 3")
        }

        @Test
        fun `Submitting with a non-integer number in the numberOfPeople field returns an error`(page: Page) {
            val peoplePage = navigator.goToPropertyRegistrationPeoplePage()
            peoplePage.submitNumOfPeople("2.3")
            assertThat(peoplePage.form.getErrorMessage())
                .containsText("Number of people in your property must be a positive, whole number, like 3")
        }

        @Test
        fun `Submitting with a negative integer in the numberOfPeople field returns an error`(page: Page) {
            val peoplePage = navigator.goToPropertyRegistrationPeoplePage()
            peoplePage.submitNumOfPeople("-2")
            assertThat(peoplePage.form.getErrorMessage())
                .containsText("Number of people in your property must be a positive, whole number, like 3")
        }

        @Test
        fun `Submitting with a zero integer in the numberOfPeople field returns an error`(page: Page) {
            val peoplePage = navigator.goToPropertyRegistrationPeoplePage()
            peoplePage.submitNumOfPeople(0)
            assertThat(peoplePage.form.getErrorMessage())
                .containsText("Number of people in your property must be a positive, whole number, like 3")
        }

        @Test
        fun `Submitting with an integer in the numberOfPeople field that is less than the numberOfHouseholds returns an error`(
            page: Page,
        ) {
            val householdsPage = navigator.goToPropertyRegistrationHouseholdsPage()
            householdsPage.submitNumberOfHouseholds(3)
            val peoplePage = assertPageIs(page, NumberOfPeopleFormPagePropertyRegistration::class)
            peoplePage.submitNumOfPeople(2)
            assertThat(peoplePage.form.getErrorMessage())
                .containsText(
                    "The number of people in the property must be the same as or higher than the number of households in the property",
                )
        }
    }

    @Nested
    inner class Declaration {
        @Test
        fun `Submitting without checking the checkbox returns an error`(page: Page) {
            val declarationPage = navigator.goToPropertyRegistrationDeclarationPage()
            declarationPage.form.submit()
            assertThat(declarationPage.form.getErrorMessage()).containsText("You must agree to the declaration to continue")
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
