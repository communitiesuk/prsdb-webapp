package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor.captor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
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
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CheckAnswersPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.ConfirmationPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.LicensingTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.LookupAddressFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.ManualAddressFormPagePropertyRegistration
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

class PropertyRegistrationJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    private val absoluteLandlordUrl = "www.prsd.gov.uk/landlord"

    @MockitoSpyBean
    private lateinit var propertyOwnershipRepository: PropertyOwnershipRepository

    @MockitoBean
    private lateinit var confirmationEmailSender: EmailNotificationService<PropertyRegistrationConfirmationEmail>

    @BeforeEach
    fun setup() {
        whenever(absoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI(absoluteLandlordUrl))
    }

    @Test
    @Suppress("ktlint:standard:max-line-length")
    fun `User can navigate the whole journey if pages are correctly filled in (select address, non-custom property type, selective license, occupied)`(
        page: Page,
    ) {
        // Start page (not a journey step, but it is how the user accesses the journey)
        val registerPropertyStartPage = navigator.goToPropertyRegistrationStartPage()
        assertThat(registerPropertyStartPage.heading).containsText("Register a property")
        registerPropertyStartPage.startButton.clickAndWait()
        val taskListPage = assertPageIs(page, TaskListPagePropertyRegistration::class)

        // Task list page (part of the journey to support redirects)
        taskListPage.clickRegisterTaskWithName("Enter the property address")
        val addressLookupPage = assertPageIs(page, LookupAddressFormPagePropertyRegistration::class)

        // Address lookup - render page
        assertThat(addressLookupPage.form.fieldsetHeading).containsText("What is the property address?")
        assertThat(addressLookupPage.form.sectionHeader).containsText("Section 1 of 2 \u2014 Register your property details")
        // fill in and submit
        addressLookupPage.submitPostcodeAndBuildingNameOrNumber("FA1 1AA", "1")
        val selectAddressPage = assertPageIs(page, SelectAddressFormPagePropertyRegistration::class)

        // Select address - render page
        assertThat(selectAddressPage.form.fieldsetHeading).containsText("Select an address")
        assertThat(selectAddressPage.form.sectionHeader).containsText("Section 1 of 2 \u2014 Register your property details")
        // fill in and submit
        selectAddressPage.selectAddressAndSubmit("1 Fictional Road, FA1 1AA")
        val propertyTypePage = assertPageIs(page, PropertyTypeFormPagePropertyRegistration::class)

        // Property type selection - render page
        assertThat(propertyTypePage.form.fieldsetHeading).containsText("What type of property are you registering?")
        assertThat(propertyTypePage.form.sectionHeader).containsText("Section 1 of 2 \u2014 Register your property details")
        // fill in and submit
        propertyTypePage.submitPropertyType(PropertyType.DETACHED_HOUSE)
        val ownershipTypePage = assertPageIs(page, OwnershipTypeFormPagePropertyRegistration::class)

        // Ownership type selection - render page
        assertThat(ownershipTypePage.form.fieldsetHeading).containsText("Select the type of ownership you have for your property")
        assertThat(ownershipTypePage.form.sectionHeader).containsText("Section 1 of 2 \u2014 Register your property details")
        // fill in and submit
        ownershipTypePage.submitOwnershipType(OwnershipType.FREEHOLD)
        val licensingTypePage = assertPageIs(page, LicensingTypeFormPagePropertyRegistration::class)

        // Licensing type - render page
        assertThat(licensingTypePage.form.fieldsetHeading).containsText("Select the type of licence you have for your property")
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
        assertThat(householdsPage.form.fieldsetLegend).containsText("How many households are in your property?")
        assertThat(householdsPage.sectionHeader).containsText("Section 1 of 2 \u2014 Register your property details")
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
        assertThat(checkAnswersPage.heading).containsText("Check your answers for:")
        assertThat(checkAnswersPage.sectionHeader).containsText("Section 2 of 2 \u2014 Check and submit your property details")
        // submit
        checkAnswersPage.confirm()
        val confirmationPage = assertPageIs(page, ConfirmationPagePropertyRegistration::class)

        // Confirmation - render page
        val propertyOwnershipCaptor = captor<PropertyOwnership>()
        verify(propertyOwnershipRepository).save(propertyOwnershipCaptor.capture())
        val expectedPropertyRegNum = RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnershipCaptor.value.registrationNumber)
        assertEquals(expectedPropertyRegNum.toString(), confirmationPage.registrationNumberText)
        assertTrue(confirmationPage.returnToDashboardButton.locator.isHidden)
        assertTrue(confirmationPage.addComplianceButton.locator.isVisible)
        assertTrue(confirmationPage.goToDashboardButton.locator.isVisible)

        // Check confirmation email
        verify(confirmationEmailSender).sendEmail(
            "alex.surname@example.com",
            PropertyRegistrationConfirmationEmail(
                expectedPropertyRegNum.toString(),
                "1 Fictional Road, FA1 1AA",
                absoluteLandlordUrl,
                true,
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
        assertThat(registerPropertyStartPage.heading).containsText("Register a property")
        registerPropertyStartPage.startButton.clickAndWait()
        val taskListPage = assertPageIs(page, TaskListPagePropertyRegistration::class)

        // Task list page (part of the journey to support redirects)
        taskListPage.clickRegisterTaskWithName("Enter the property address")
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
        assertThat(selectLocalAuthorityPage.form.fieldsetHeading).containsText("What local council area is your property in?")
        assertThat(selectLocalAuthorityPage.form.sectionHeader).containsText("Section 1 of 2 \u2014 Register your property details")
        // fill in and submit
        selectLocalAuthorityPage.submitLocalAuthority("BATH AND NORTH EAST SOMERSET COUNCIL", "BATH AND NORTH EAST SOMERSET COUNCIL")
        val propertyTypePage = assertPageIs(page, PropertyTypeFormPagePropertyRegistration::class)

        // Property type selection - render page
        assertThat(propertyTypePage.form.fieldsetHeading).containsText("What type of property are you registering?")
        assertThat(propertyTypePage.form.sectionHeader).containsText("Section 1 of 2 \u2014 Register your property details")
        // fill in and submit
        propertyTypePage.submitCustomPropertyType("End terrace house")
        val ownershipTypePage = assertPageIs(page, OwnershipTypeFormPagePropertyRegistration::class)

        // Ownership type selection - render page
        assertThat(ownershipTypePage.form.fieldsetHeading).containsText("Select the type of ownership you have for your property")
        assertThat(ownershipTypePage.form.sectionHeader).containsText("Section 1 of 2 \u2014 Register your property details")
        // fill in and submit
        ownershipTypePage.submitOwnershipType(OwnershipType.FREEHOLD)
        val licensingTypePage = assertPageIs(page, LicensingTypeFormPagePropertyRegistration::class)

        // Licensing type - render page
        assertThat(licensingTypePage.form.fieldsetHeading).containsText("Select the type of licence you have for your property")
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
        assertThat(checkAnswersPage.heading).containsText("Check your answers for:")
        assertThat(checkAnswersPage.sectionHeader).containsText("Section 2 of 2 \u2014 Check and submit your property details")
        // submit
        checkAnswersPage.confirm()
        val confirmationPage = assertPageIs(page, ConfirmationPagePropertyRegistration::class)

        // Confirmation - render page
        val propertyOwnershipCaptor = captor<PropertyOwnership>()
        verify(propertyOwnershipRepository).save(propertyOwnershipCaptor.capture())
        val expectedPropertyRegNum = RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnershipCaptor.value.registrationNumber)
        assertEquals(expectedPropertyRegNum.toString(), confirmationPage.registrationNumberText)
        assertTrue(confirmationPage.addComplianceButton.locator.isHidden)
        assertTrue(confirmationPage.goToDashboardButton.locator.isHidden)
        assertTrue(confirmationPage.returnToDashboardButton.locator.isVisible)

        // Check confirmation email
        verify(confirmationEmailSender).sendEmail(
            "alex.surname@example.com",
            PropertyRegistrationConfirmationEmail(
                expectedPropertyRegNum.toString(),
                "Test address line 1, Testville, EG1 2AB",
                absoluteLandlordUrl,
                false,
            ),
        )

        // Go to dashboard
        confirmationPage.returnToDashboardButton.clickAndWait()
        assertPageIs(page, LandlordDashboardPage::class)
    }
}
