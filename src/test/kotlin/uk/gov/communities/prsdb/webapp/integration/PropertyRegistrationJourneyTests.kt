package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaLocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor.captor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import uk.gov.communities.prsdb.webapp.clients.EpcRegisterClient
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFETY_CERT_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.JointLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EpcLookupBasePage.Companion.CURRENT_EPC_CERTIFICATE_NUMBER
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EpcLookupBasePage.Companion.CURRENT_EXPIRED_EPC_CERTIFICATE_NUMBER
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EpcLookupBasePage.Companion.NONEXISTENT_EPC_CERTIFICATE_NUMBER
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EpcLookupBasePage.Companion.SUPERSEDED_EPC_CERTIFICATE_NUMBER
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.StartPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.BillsIncludedFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CheckAnswersPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CheckAutomatchedEpcFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CheckElectricalCertUploadsFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CheckElectricalSafetyAnswersFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CheckEpcAnswersFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CheckGasCertUploadsFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CheckGasSafetyAnswersFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CheckJointLandlordsFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.ConfirmEpcDetailsRetrievedByCertificateNumberPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.ConfirmationPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.ElectricalCertExpiredFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.ElectricalCertExpiryDateFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.ElectricalCertMissingFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.EpcExpiredFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.EpcInDateAtStartOfTenancyCheckPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.EpcMissingFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.EpcNotFoundFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.EpcSuperseededFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FindYourEpcFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FurnishedStatusFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.GasCertExpiredFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.GasCertIssueDateFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.GasCertMissingFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.HasElectricalCertFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.HasEpcFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.HasGasCertFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.HasGasSupplyFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.HasJointLandlordsFormBasePagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.InviteAnotherJointLandlordFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.InviteJointLandlordFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.IsEpcRequiredFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.LicensingTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.LookupAddressFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.LowEnergyRatingFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.ManualAddressFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.MeesExemptionFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.NumberOfBedroomsFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.NumberOfHouseholdsFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.NumberOfPeopleFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.OccupancyFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.OwnershipTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.PropertyTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.ProvideElectricalCertLaterFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.ProvideEpcLaterFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.ProvideGasCertLaterFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.RemoveElectricalCertUploadFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.RemoveGasCertUploadFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.RemoveJointLandlordAreYouSureFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.RentAmountFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.RentFrequencyFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.RentIncludesBillsFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SelectAddressFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SelectLocalCouncilFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SelectiveLicenceFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.TaskListPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.UploadElectricalCertFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.UploadGasCertFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyRegistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData
import java.net.URI
import java.nio.file.Path
import java.time.format.DateTimeFormatter
import kotlin.test.assertTrue

class PropertyRegistrationJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    private val absoluteLandlordUrl = "www.prsd.gov.uk/landlord"

    @MockitoSpyBean
    private lateinit var propertyOwnershipRepository: PropertyOwnershipRepository

    @MockitoSpyBean
    private lateinit var jointLandlordInvitationRepository: JointLandlordInvitationRepository

    @MockitoBean
    private lateinit var confirmationEmailSender: EmailNotificationService<PropertyRegistrationConfirmationEmail>

    @MockitoBean
    private lateinit var jointLandlordInvitationEmailSender: EmailNotificationService<JointLandlordInvitationEmail>

    @MockitoBean
    private lateinit var epcRegisterClient: EpcRegisterClient

    @BeforeEach
    fun setup() {
        whenever(absoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI(absoluteLandlordUrl))
        whenever(
            absoluteUrlProvider.buildJointLandlordInvitationUri(org.mockito.kotlin.any()),
        ).thenReturn(URI("http://localhost/invite/test-token"))
    }

    @Test
    @Suppress("ktlint:standard:max-line-length")
    fun `User can navigate the whole journey if pages are correctly filled in (select address, non-custom property type, selective license, occupied, compliance certificates uploaded)`(
        page: Page,
    ) {
        featureFlagManager.enableFeature(JOINT_LANDLORDS)
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
        assertThat(addressLookupPage.form.sectionHeader).containsText("Section 1 of 5 \u2014 Register your property details")
        // fill in and submit
        addressLookupPage.submitPostcodeAndBuildingNameOrNumber("FA1 1AA", "1")
        val selectAddressPage = assertPageIs(page, SelectAddressFormPagePropertyRegistration::class)

        // Select address - render page
        assertThat(selectAddressPage.form.fieldsetHeading).containsText("Select your address")
        assertThat(selectAddressPage.form.sectionHeader).containsText("Section 1 of 5 \u2014 Register your property details")
        // fill in and submit
        selectAddressPage.selectAddressAndSubmit("1 Fictional Road, FA1 1AA")
        val propertyTypePage = assertPageIs(page, PropertyTypeFormPagePropertyRegistration::class)

        // Property type selection - render page
        assertThat(propertyTypePage.form.fieldsetHeading).containsText("What type of property are you registering?")
        assertThat(propertyTypePage.form.sectionHeader).containsText("Section 1 of 5 \u2014 Register your property details")
        // fill in and submit
        propertyTypePage.submitPropertyType(PropertyType.DETACHED_HOUSE)
        val ownershipTypePage = assertPageIs(page, OwnershipTypeFormPagePropertyRegistration::class)

        // Ownership type selection - render page
        assertThat(ownershipTypePage.form.fieldsetHeading).containsText("Select the type of ownership you have for your property")
        assertThat(ownershipTypePage.form.sectionHeader).containsText("Section 1 of 5 \u2014 Register your property details")
        // fill in and submit
        ownershipTypePage.submitOwnershipType(OwnershipType.FREEHOLD)
        val licensingTypePage = assertPageIs(page, LicensingTypeFormPagePropertyRegistration::class)

        // Licensing type - render page
        assertThat(licensingTypePage.form.fieldsetHeading).containsText("Select the type of licence you have for your property")
        assertThat(licensingTypePage.form.sectionHeader).containsText("Section 1 of 5 \u2014 Register your property details")
        // fill in and submit
        licensingTypePage.submitLicensingType(LicensingType.SELECTIVE_LICENCE)
        val selectiveLicencePage = assertPageIs(page, SelectiveLicenceFormPagePropertyRegistration::class)

        // Selective licence - render page
        assertThat(selectiveLicencePage.form.fieldsetHeading).containsText("What is your selective licence number?")
        assertThat(selectiveLicencePage.form.sectionHeader).containsText("Section 1 of 5 \u2014 Register your property details")
        // fill in and submit
        selectiveLicencePage.submitLicenseNumber("licence number")
        val occupancyPage = assertPageIs(page, OccupancyFormPagePropertyRegistration::class)

        // Occupancy - render page
        assertThat(occupancyPage.form.fieldsetHeading).containsText("Is your property occupied by tenants?")
        assertThat(occupancyPage.form.sectionHeader).containsText("Section 1 of 5 \u2014 Register your property details")
        // fill in and submit
        occupancyPage.submitIsOccupied()
        val householdsPage = assertPageIs(page, NumberOfHouseholdsFormPagePropertyRegistration::class)

        // Number of households - render page
        assertThat(householdsPage.header).containsText("Households in your property")
        assertThat(householdsPage.sectionHeader).containsText("Section 1 of 5 \u2014 Register your property details")
        // fill in and submit
        householdsPage.submitNumberOfHouseholds(2)
        val peoplePage = assertPageIs(page, NumberOfPeopleFormPagePropertyRegistration::class)

        // Number of people - render page
        assertThat(peoplePage.header).containsText("How many people live in your property?")
        assertThat(peoplePage.sectionHeader).containsText("Section 1 of 5 \u2014 Register your property details")
        // fill in and submit
        peoplePage.submitNumOfPeople(2)
        val bedroomsPage = assertPageIs(page, NumberOfBedroomsFormPagePropertyRegistration::class)

        // Number of bedrooms - render page
        assertThat(bedroomsPage.header).containsText("How many bedrooms in your property?")
        assertThat(bedroomsPage.form.sectionHeader).containsText("Section 1 of 5 \u2014 Register your property details")
        // fill in and submit
        bedroomsPage.submitNumOfBedrooms(3)
        val rentIncludesBillsPage = assertPageIs(page, RentIncludesBillsFormPagePropertyRegistration::class)

        // Does the rent include bills - render page
        assertThat(rentIncludesBillsPage.form.fieldsetHeading).containsText("Does the rent include bills?")
        assertThat(rentIncludesBillsPage.form.sectionHeader).containsText("Section 1 of 5 \u2014 Register your property details")
        // fill in and submit
        rentIncludesBillsPage.submitIsIncluded()
        val billsIncludedPage = assertPageIs(page, BillsIncludedFormPagePropertyRegistration::class)

        // Bills included - render page
        assertThat(billsIncludedPage.form.fieldsetHeading).containsText("Which of these do you include in the rent?")
        assertThat(billsIncludedPage.form.sectionHeader).containsText("Section 1 of 5 \u2014 Register your property details")
        // fill in and submit
        billsIncludedPage.selectGasElectricityWater()
        billsIncludedPage.selectSomethingElseCheckbox()
        billsIncludedPage.fillCustomBills("Dog Grooming")
        billsIncludedPage.form.submit()
        val furnishedPage = assertPageIs(page, FurnishedStatusFormPagePropertyRegistration::class)

        // Furnished - render page
        assertThat(furnishedPage.form.fieldsetHeading).containsText("Is the property furnished?")
        assertThat(furnishedPage.form.sectionHeader).containsText("Section 1 of 5 \u2014 Register your property details")
        // fill in and submit
        furnishedPage.submitFurnishedStatus(FurnishedStatus.FURNISHED)
        val rentFrequencyPage = assertPageIs(page, RentFrequencyFormPagePropertyRegistration::class)

        // Rent frequency - render page
        assertThat(rentFrequencyPage.header).containsText("When you charge rent")
        assertThat(rentFrequencyPage.sectionHeader).containsText("Section 1 of 5 \u2014 Register your property details")
        // fill in and submit
        rentFrequencyPage.selectRentFrequency(RentFrequency.OTHER)
        rentFrequencyPage.fillCustomRentFrequency("Fortnightly")
        rentFrequencyPage.form.submit()
        val rentAmountPage = assertPageIs(page, RentAmountFormPagePropertyRegistration::class)

        // Rent amount - render page
        assertThat(rentAmountPage.sectionHeader).containsText("Section 1 of 5 \u2014 Register your property details")
        // fill in and submit
        rentAmountPage.submitRentAmount("400")
        val hasJointLandlordsPage = assertPageIs(page, HasJointLandlordsFormBasePagePropertyRegistration::class)

        // Has Joint Landlords - render page
        assertThat(hasJointLandlordsPage.header).containsText("Invite joint landlords")
        assertThat(hasJointLandlordsPage.sectionHeader).containsText("Section 1 of 5 \u2014 Register your property details")

        // fill in and submit
        hasJointLandlordsPage.submitHasJointLandlords()
        val inviteJointLandlordPage = assertPageIs(page, InviteJointLandlordFormPagePropertyRegistration::class)

        // Invite joint landlord - render page
        assertThat(inviteJointLandlordPage.heading).containsText("Invite a joint landlord to this property")
        assertThat(inviteJointLandlordPage.form.sectionHeader).containsText("Section 1 of 5 \u2014 Register your property details")

        // fill in and submit
        inviteJointLandlordPage.submitEmail("email@address.com")
        val checkJointLandlordsPage = assertPageIs(page, CheckJointLandlordsFormPagePropertyRegistration::class)
        assertThat(checkJointLandlordsPage.summaryList.firstRow.value).containsText("email@address.com")

        // Check joint landlords - render page
        checkJointLandlordsPage
            .form
            .addAnotherButton
            .clickAndWait()

        // Invite another joint landlord - render page
        val addAnotherPage = assertPageIs(page, InviteAnotherJointLandlordFormPagePropertyRegistration::class)
        addAnotherPage.submitEmail("email2@address.com")

        val newCheckJointLandlordsPage = assertPageIs(page, CheckJointLandlordsFormPagePropertyRegistration::class)
        newCheckJointLandlordsPage.summaryList.firstRow.clickNamedActionLinkAndWait("Remove")

        // Remove Joint Landlord - render page
        val removeJointLandlordsPage = assertPageIs(page, RemoveJointLandlordAreYouSureFormPagePropertyRegistration::class)
        removeJointLandlordsPage.submitWantsToProceed()

        val finalCheckJointLandlordsPage = assertPageIs(page, CheckJointLandlordsFormPagePropertyRegistration::class)
        finalCheckJointLandlordsPage.form.submit()

        val hasGasSupplyPage = assertPageIs(page, HasGasSupplyFormPagePropertyRegistration::class)

        // Has Gas Supply - render page
        assertThat(hasGasSupplyPage.heading).containsText("Does the property have a gas supply or any gas appliances?")
        hasGasSupplyPage.submitHasGasSupply()
        val hasGasCertPage = assertPageIs(page, HasGasCertFormPagePropertyRegistration::class)

        // Has Gas Cert - render page
        assertThat(hasGasCertPage.heading).containsText("Do you have a gas safety certificate for this property?")
        hasGasCertPage.submitHasCertificate()
        val gasCertIssueDatePage = assertPageIs(page, GasCertIssueDateFormPagePropertyRegistration::class)

        // Gas Cert Issue Date - render page
        assertThat(gasCertIssueDatePage.heading).containsText("What’s the issue date on the gas safety certificate?")
        gasCertIssueDatePage.submitDate(validGasSafetyCertIssueDate)
        var uploadGasCertPage = assertPageIs(page, UploadGasCertFormPagePropertyRegistration::class)

        // Upload Gas Cert - render page
        uploadGasCertPage.uploadGasCertificate(Path.of("src/test/resources/test-files/valid-gas-cert.png"))
        var checkGasCertUploadsPage = assertPageIs(page, CheckGasCertUploadsFormPagePropertyRegistration::class)

        // Check Gas Cert Uploads - render page
        assertThat(checkGasCertUploadsPage.table.getCell(0, 0)).containsText("valid-gas-cert.png")
        assertEquals(checkGasCertUploadsPage.table.rows.count(), 1)
        checkGasCertUploadsPage.form.addAnotherButton.clickAndWait()
        uploadGasCertPage = assertPageIs(page, UploadGasCertFormPagePropertyRegistration::class)

        uploadGasCertPage.uploadGasCertificate(Path.of("src/test/resources/test-files/valid-gas-cert.png"))
        checkGasCertUploadsPage = assertPageIs(page, CheckGasCertUploadsFormPagePropertyRegistration::class)
        assertThat(checkGasCertUploadsPage.table.getCell(0, 0)).containsText("valid-gas-cert.png")
        assertThat(checkGasCertUploadsPage.table.getCell(1, 0)).containsText("valid-gas-cert.png")
        assertEquals(checkGasCertUploadsPage.table.rows.count(), 2)

        checkGasCertUploadsPage.table
            .getClickableCell(0, 2)
            .link
            .clickAndWait()

        val removeGasCertUploadPage = assertPageIs(page, RemoveGasCertUploadFormPagePropertyRegistration::class)

        removeGasCertUploadPage.form.radios.selectValue("true")
        removeGasCertUploadPage.form.submit()

        checkGasCertUploadsPage = assertPageIs(page, CheckGasCertUploadsFormPagePropertyRegistration::class)
        assertThat(checkGasCertUploadsPage.table.getCell(0, 0)).containsText("valid-gas-cert.png")

        assertEquals(checkGasCertUploadsPage.table.rows.count(), 1)
        checkGasCertUploadsPage.form.submit()

        // Remove Gas Cert Upload - render page
        val checkGasSafetyAnswersPage = assertPageIs(page, CheckGasSafetyAnswersFormPagePropertyRegistration::class)

        // Check Gas Safety Answers - render page
        // TODO PDJB-637: Implement Check Gas Safety Answers step
        assertThat(checkGasSafetyAnswersPage.heading).containsText("TODO")
        checkGasSafetyAnswersPage.form.submit()
        val hasElectricalCertPage = assertPageIs(page, HasElectricalCertFormPagePropertyRegistration::class)

        // Has Electrical Cert - render page
        assertThat(hasElectricalCertPage.heading).containsText("Which electrical safety certificate do you have for this property?")
        hasElectricalCertPage.submitHasEic()
        val electricalCertExpiryDatePage = assertPageIs(page, ElectricalCertExpiryDateFormPagePropertyRegistration::class)

        // Electrical Cert Expiry Date - render page
        assertThat(electricalCertExpiryDatePage.heading).containsText("What’s the expiry date on the Electrical Installation Certificate?")
        electricalCertExpiryDatePage.submitDate(validExpiryDate)
        val uploadElectricalCertPage = assertPageIs(page, UploadElectricalCertFormPagePropertyRegistration::class)

        // Upload Electrical Cert - render page
        // TODO PDJB-651: Implement Upload Electrical Cert step (EIC variant)
        assertThat(uploadElectricalCertPage.heading).containsText("TODO")
        uploadElectricalCertPage.form.submit()
        val checkElectricalCertUploadsPage = assertPageIs(page, CheckElectricalCertUploadsFormPagePropertyRegistration::class)

        // Check Electrical Cert Uploads - render page
        // TODO PDJB-653: Implement Check Electrical Cert Uploads step
        assertThat(checkElectricalCertUploadsPage.heading).containsText("TODO")
        checkElectricalCertUploadsPage.form.submit()
        val removeElectricalCertUploadPage = assertPageIs(page, RemoveElectricalCertUploadFormPagePropertyRegistration::class)

        // Remove Electrical Cert Upload - render page
        // TODO PDJB-654: Implement Remove Electrical Cert Upload step
        assertThat(removeElectricalCertUploadPage.heading).containsText("TODO")
        removeElectricalCertUploadPage.form.submit()
        val checkElectricalSafetyAnswersPage = assertPageIs(page, CheckElectricalSafetyAnswersFormPagePropertyRegistration::class)

        // Setup EpcLookupByUprnStep being able to find an EPC for this property when the next step submits
        whenever(epcRegisterClient.getByUprn(uprnForSelectedAddress))
            .thenReturn(
                MockEpcData.createEpcRegisterClientEpcFoundResponse(
                    expiryDate = validExpiryDate,
                ),
            )

        // Check Electrical Safety Answers - render page
        // TODO PDJB-655: Implement Check Electrical Safety Answers step (EIC variant)
        assertThat(checkElectricalSafetyAnswersPage.heading).containsText("TODO")
        checkElectricalSafetyAnswersPage.form.submit()

        // EpcLookupByUprnStep finds the EPC, so redirects to Check Automatched EPC page
        val checkAutomatchedEpcPage = assertPageIs(page, CheckAutomatchedEpcFormPagePropertyRegistration::class)

        // Check Automatched EPC - render page
        // TODO PDJB-661: Implement Check Automatched EPC step
        checkAutomatchedEpcPage.submitEpcIncorrect()
        val hasEpcPage = assertPageIs(page, HasEpcFormPagePropertyRegistration::class)

        // Has EPC - render page
        // TODO PDJB-656: Implement Has EPC step
        hasEpcPage.submitHasEpc()
        val findYourEpcPage = assertPageIs(page, FindYourEpcFormPagePropertyRegistration::class)

        // EPC Search - render page
        whenever(epcRegisterClient.getByRrn(CURRENT_EPC_CERTIFICATE_NUMBER))
            .thenReturn(
                MockEpcData.createEpcRegisterClientEpcFoundResponse(
                    certificateNumber = CURRENT_EPC_CERTIFICATE_NUMBER,
                    latestCertificateNumberForThisProperty = CURRENT_EPC_CERTIFICATE_NUMBER,
                    expiryDate = validExpiryDate,
                ),
            )
        findYourEpcPage.submitCurrentEpcNumber()
        val confirmEpcDetailsPage = assertPageIs(page, ConfirmEpcDetailsRetrievedByCertificateNumberPagePropertyRegistration::class)

        // Check Matched EPC - render page
        val expectedExpiryDate =
            validExpiryDate
                .toJavaLocalDate()
                .format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
        assertThat(confirmEpcDetailsPage.summaryCard.summaryList.addressRow.value).containsText(MockEpcData.defaultSingleLineAddress)
        assertThat(confirmEpcDetailsPage.summaryCard.summaryList.energyEfficiencyRatingRow.value).containsText("C")
        assertThat(confirmEpcDetailsPage.summaryCard.summaryList.expiryDateRow.value).containsText(expectedExpiryDate)
        assertThat(confirmEpcDetailsPage.summaryCard.summaryList.certificateNumberRow.value).containsText(CURRENT_EPC_CERTIFICATE_NUMBER)
        confirmEpcDetailsPage.submitYes()
        val checkEpcAnswersPage = assertPageIs(page, CheckEpcAnswersFormPagePropertyRegistration::class)

        // Check EPC Answers - render page
        // TODO PDJB-670: Implement Check EPC Answers step
        assertThat(checkEpcAnswersPage.heading).containsText("TODO")
        checkEpcAnswersPage.form.submit()
        val checkAnswersPage = assertPageIs(page, CheckAnswersPagePropertyRegistration::class)

        // Check answers - render page
        assertThat(checkAnswersPage.heading).containsText("Check your answers for:")
        assertThat(checkAnswersPage.sectionHeader).containsText("Section 5 of 5 \u2014 Check and submit your property details")
        // submit
        checkAnswersPage.confirm()
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
                "1 Fictional Road, FA1 1AA",
                absoluteLandlordUrl,
                true,
                listOf("email2@address.com"),
            ),
        )

        // Go to compliance journey
        confirmationPage.addComplianceButton.clickAndWait()
        assertPageIs(page, StartPagePropertyCompliance::class, mapOf("propertyOwnershipId" to propertyOwnershipCaptor.value.id.toString()))
    }

    @Test
    @Suppress("ktlint:standard:max-line-length")
    fun `User can navigate the whole journey if pages are correctly filled in (manual address, custom property type, no license, unoccupied, no joint landlords, no certificates)`(
        page: Page,
    ) {
        featureFlagManager.enableFeature(JOINT_LANDLORDS)
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
        assertThat(addressLookupPage.form.sectionHeader).containsText("Section 1 of 5 \u2014 Register your property details")
        // fill in and submit
        addressLookupPage.submitPostcodeAndBuildingNameOrNumber("FA1 1AB", "2")
        val selectAddressPage = assertPageIs(page, SelectAddressFormPagePropertyRegistration::class)

        // Select address - render page
        assertThat(selectAddressPage.form.fieldsetHeading).containsText("Select your address")
        assertThat(selectAddressPage.form.sectionHeader).containsText("Section 1 of 5 \u2014 Register your property details")
        // fill in and submit
        selectAddressPage.selectAddressAndSubmit(MANUAL_ADDRESS_CHOSEN)
        val manualAddressPage = assertPageIs(page, ManualAddressFormPagePropertyRegistration::class)

        // Manual address - render page
        assertThat(manualAddressPage.form.fieldsetHeading).containsText("What is the property address?")
        assertThat(manualAddressPage.form.sectionHeader).containsText("Section 1 of 5 \u2014 Register your property details")
        // fill in and submit
        manualAddressPage.submitAddress(addressLineOne = "Test address line 1", townOrCity = "Testville", postcode = "EG1 2AB")
        val selectLocalCouncilPage = assertPageIs(page, SelectLocalCouncilFormPagePropertyRegistration::class)

        // Select local council - render page
        assertThat(selectLocalCouncilPage.form.fieldsetHeading).containsText("What local council area is your property in?")
        assertThat(selectLocalCouncilPage.form.sectionHeader).containsText("Section 1 of 5 \u2014 Register your property details")
        // fill in and submit
        selectLocalCouncilPage.submitLocalCouncil("BATH AND NORTH EAST SOMERSET COUNCIL", "BATH AND NORTH EAST SOMERSET COUNCIL")
        val propertyTypePage = assertPageIs(page, PropertyTypeFormPagePropertyRegistration::class)

        // Property type selection - render page
        assertThat(propertyTypePage.form.fieldsetHeading).containsText("What type of property are you registering?")
        assertThat(propertyTypePage.form.sectionHeader).containsText("Section 1 of 5 \u2014 Register your property details")
        // fill in and submit
        propertyTypePage.submitCustomPropertyType("End terrace house")
        val ownershipTypePage = assertPageIs(page, OwnershipTypeFormPagePropertyRegistration::class)

        // Ownership type selection - render page
        assertThat(ownershipTypePage.form.fieldsetHeading).containsText("Select the type of ownership you have for your property")
        assertThat(ownershipTypePage.form.sectionHeader).containsText("Section 1 of 5 \u2014 Register your property details")
        // fill in and submit
        ownershipTypePage.submitOwnershipType(OwnershipType.FREEHOLD)
        val licensingTypePage = assertPageIs(page, LicensingTypeFormPagePropertyRegistration::class)

        // Licensing type - render page
        assertThat(licensingTypePage.form.fieldsetHeading).containsText("Select the type of licence you have for your property")
        assertThat(licensingTypePage.form.sectionHeader).containsText("Section 1 of 5 \u2014 Register your property details")
        // fill in and submit
        licensingTypePage.submitLicensingType(LicensingType.NO_LICENSING)
        val occupancyPage = assertPageIs(page, OccupancyFormPagePropertyRegistration::class)

        // Occupancy - render page
        assertThat(occupancyPage.form.fieldsetHeading).containsText("Is your property occupied by tenants?")
        assertThat(occupancyPage.form.sectionHeader).containsText("Section 1 of 5 \u2014 Register your property details")
        // fill in and submit
        occupancyPage.submitIsVacant()
        val hasJointLandlordsPage = assertPageIs(page, HasJointLandlordsFormBasePagePropertyRegistration::class)

        // Has Joint Landlords - render page
        assertThat(hasJointLandlordsPage.header).containsText("Invite joint landlords")
        assertThat(hasJointLandlordsPage.sectionHeader).containsText("Section 1 of 5 \u2014 Register your property details")

        // fill in and submit
        hasJointLandlordsPage.submitHasNoJointLandlords()
        val hasGasSupplyPage = assertPageIs(page, HasGasSupplyFormPagePropertyRegistration::class)

        // Has Gas Supply - render page
        assertThat(hasGasSupplyPage.heading).containsText("Does the property have a gas supply or any gas appliances?")
        hasGasSupplyPage.submitHasGasSupply()
        val hasGasCertPage = assertPageIs(page, HasGasCertFormPagePropertyRegistration::class)

        // Has Gas Cert - render page
        assertThat(hasGasCertPage.heading).containsText("Do you have a gas safety certificate for this property?")
        hasGasCertPage.submitHasNoCertificate()
        val gasCertMissingPage = assertPageIs(page, GasCertMissingFormPagePropertyRegistration::class)

        // Gas Cert Missing - render page
        assertThat(gasCertMissingPage.heading).containsText("You must get a gas safety certificate before a tenant moves in")
        assertThat(gasCertMissingPage.warning).isHidden()
        assertThat(gasCertMissingPage.submitButton).containsText("Continue")
        gasCertMissingPage.form.submit()
        val checkGasSafetyAnswersPage = assertPageIs(page, CheckGasSafetyAnswersFormPagePropertyRegistration::class)

        // Check Gas Safety Answers - render page
        // TODO PDJB-637: Implement Check Gas Safety Answers step
        assertThat(checkGasSafetyAnswersPage.heading).containsText("TODO")
        checkGasSafetyAnswersPage.form.submit()
        val hasElectricalCertPage = assertPageIs(page, HasElectricalCertFormPagePropertyRegistration::class)

        // Has Electrical Cert - render page
        assertThat(hasElectricalCertPage.heading).containsText("Which electrical safety certificate do you have for this property?")
        hasElectricalCertPage.submitHasNoCert()
        val electricalCertMissingPage = assertPageIs(page, ElectricalCertMissingFormPagePropertyRegistration::class)

        // Electrical Cert Missing - render page
        assertThat(electricalCertMissingPage.heading).containsText("You must get an electrical safety certificate before a tenant moves in")
        assertThat(electricalCertMissingPage.warning).isHidden()
        assertThat(electricalCertMissingPage.submitButton).containsText("Continue")
        electricalCertMissingPage.form.submit()
        val checkElectricalSafetyAnswersPage = assertPageIs(page, CheckElectricalSafetyAnswersFormPagePropertyRegistration::class)

        // Check Electrical Safety Answers - render page
        // TODO PDJB-655: Implement Check Electrical Safety Answers step
        assertThat(checkElectricalSafetyAnswersPage.heading).containsText("TODO")
        checkElectricalSafetyAnswersPage.form.submit()

        // We use a manual address, uprn will be null.
        // The internal EpcLookupByUprnStep at the start of the EpcTask will not find an EPC
        val hasEpcPage = assertPageIs(page, HasEpcFormPagePropertyRegistration::class)

        // Has EPC - render page
        // TODO PDJB-656: Implement Has EPC step
        hasEpcPage.submitHasNoEpc()
        val isEpcRequiredPage = assertPageIs(page, IsEpcRequiredFormPagePropertyRegistration::class)

        // Is EPC required - render page
        assertThat(isEpcRequiredPage.heading).containsText("Is an EPC required to let this property?")
        isEpcRequiredPage.submitEpcRequired()
        val epcMissingPage = assertPageIs(page, EpcMissingFormPagePropertyRegistration::class)

        // TODO PDJB-659 - Implement EPC Missing step
        epcMissingPage.form.submit()
        val checkEpcAnswersPage = assertPageIs(page, CheckEpcAnswersFormPagePropertyRegistration::class)

        // Check EPC Answers - render page
        // TODO PDJB-670: Implement Check EPC Answers step
        assertThat(checkEpcAnswersPage.heading).containsText("TODO")
        checkEpcAnswersPage.form.submit()
        val checkAnswersPage = assertPageIs(page, CheckAnswersPagePropertyRegistration::class)

        // Check answers - render page
        assertThat(checkAnswersPage.heading).containsText("Check your answers for:")
        assertThat(checkAnswersPage.sectionHeader).containsText("Section 5 of 5 \u2014 Check and submit your property details")
        // submit
        checkAnswersPage.confirm()
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
                false,
                null,
            ),
        )

        // Go to dashboard
        confirmationPage.goToDashboardButton.clickAndWait()
        assertPageIs(page, LandlordDashboardPage::class)
    }

    @Test
    fun `User can choose to provide compliance certificates later if their property is occupied`(page: Page) {
        val hasGasSupplyPage = navigator.skipToPropertyRegistrationHasGasSupplyPage(propertyIsOccupied = true)
        hasGasSupplyPage.submitHasGasSupply()
        val hasGasCertPage = assertPageIs(page, HasGasCertFormPagePropertyRegistration::class)

        // Has Gas Cert. Submit with no option selected
        hasGasCertPage.submitProvideThisLater()
        val provideGasCertLaterPage = assertPageIs(page, ProvideGasCertLaterFormPagePropertyRegistration::class)

        // Provide Gas Cert Later - render page
        assertThat(provideGasCertLaterPage.insetText).containsText("You must upload your gas safety certificate within 28 days")
        provideGasCertLaterPage.form.submit()
        val checkGasSafetyAnswersPage = assertPageIs(page, CheckGasSafetyAnswersFormPagePropertyRegistration::class)

        // Check Gas Safety Answers - render page
        // TODO PDJB-637: Implement Check Gas Safety Answers step
        assertThat(checkGasSafetyAnswersPage.heading).containsText("TODO")
        checkGasSafetyAnswersPage.form.submit()
        val hasElectricalCertPage = assertPageIs(page, HasElectricalCertFormPagePropertyRegistration::class)

        // Has Electrical Cert - render page
        hasElectricalCertPage.submitProvideThisLater()
        val provideElectricalCertLaterPage = assertPageIs(page, ProvideElectricalCertLaterFormPagePropertyRegistration::class)

        // Provide Electrical Cert Later - render page
        assertThat(
            provideElectricalCertLaterPage.insetText,
        ).containsText("You must upload your electrical safety certificate within 28 days.")
        provideElectricalCertLaterPage.form.submit()
        val checkElectricalSafetyAnswersPage = assertPageIs(page, CheckElectricalSafetyAnswersFormPagePropertyRegistration::class)

        // Setup EpcLookupByUprnStep NOT finding an EPC for this property when the next step submits
        whenever(epcRegisterClient.getByUprn(uprnForSelectedAddress)).thenReturn(MockEpcData.epcRegisterClientEpcNotFoundResponse)

        // Check Electrical Safety Answers - render page
        // TODO PDJB-655: Implement Check Electrical Safety Answers step
        assertThat(checkElectricalSafetyAnswersPage.heading).containsText("TODO")
        checkElectricalSafetyAnswersPage.form.submit()
        // The internal EpcLookupByUprnStep at the start of the EpcTask does not find an EPC
        val hasEpcPage = assertPageIs(page, HasEpcFormPagePropertyRegistration::class)

        // Has EPC - render page
        // TODO PDJB-656: Implement Has EPC step
        hasEpcPage.submitProvideThisLater()
        val provideEpcLaterPage = assertPageIs(page, ProvideEpcLaterFormPagePropertyRegistration::class)

        // Provide EPC Later - render page
        assertThat(provideEpcLaterPage.heading).containsText("Provide your EPC details later")
        assertThat(provideEpcLaterPage.insetText).containsText(
            "To keep the property registered, we need all its compliance certificates within 28 days.",
        )
        provideEpcLaterPage.form.submit()

        val checkEpcAnswersPage = assertPageIs(page, CheckEpcAnswersFormPagePropertyRegistration::class)

        // Check EPC Answers - render page
        // TODO PDJB-670: Implement Check EPC Answers step
    }

    @Test
    fun `User can choose to provide compliance certificates later if their property is unoccupied`(page: Page) {
        val hasGasSupplyPage = navigator.skipToPropertyRegistrationHasGasSupplyPage(propertyIsOccupied = false)
        hasGasSupplyPage.submitHasGasSupply()
        val hasGasCertPage = assertPageIs(page, HasGasCertFormPagePropertyRegistration::class)

        // Has Gas Cert. Submit with no option selected
        hasGasCertPage.submitProvideThisLater()
        val provideGasCertLaterPage = assertPageIs(page, ProvideGasCertLaterFormPagePropertyRegistration::class)

        // Provide Gas Cert Later - render page
        assertThat(provideGasCertLaterPage.insetText).isHidden()
        assertTrue(
            provideGasCertLaterPage.page
                .content()
                .contains("You must get a gas safety certificate before a tenant moves in."),
        )
        provideGasCertLaterPage.form.submit()
        val checkGasSafetyAnswersPage = assertPageIs(page, CheckGasSafetyAnswersFormPagePropertyRegistration::class)

        // Check Gas Safety Answers - render page
        // TODO PDJB-637: Implement Check Gas Safety Answers step
        assertThat(checkGasSafetyAnswersPage.heading).containsText("TODO")
        checkGasSafetyAnswersPage.form.submit()
        val hasElectricalCertPage = assertPageIs(page, HasElectricalCertFormPagePropertyRegistration::class)

        // Has Electrical Cert - render page
        hasElectricalCertPage.submitProvideThisLater()
        val provideElectricalCertLaterPage = assertPageIs(page, ProvideElectricalCertLaterFormPagePropertyRegistration::class)

        // Provide Electrical Cert Later - render page (unoccupied variant)
        assertThat(provideElectricalCertLaterPage.heading).containsText("Provide your electrical safety certificate later")
        assertThat(provideElectricalCertLaterPage.insetText).isHidden()
        assertTrue(
            provideElectricalCertLaterPage.page
                .content()
                .contains("You must get an electrical safety certificate before a tenant moves in."),
        )
        provideElectricalCertLaterPage.form.submit()
        val checkElectricalSafetyAnswersPage = assertPageIs(page, CheckElectricalSafetyAnswersFormPagePropertyRegistration::class)

        // Setup EpcLookupByUprnStep NOT finding an EPC for this property when the next step submits
        whenever(epcRegisterClient.getByUprn(uprnForSelectedAddress)).thenReturn(MockEpcData.epcRegisterClientEpcNotFoundResponse)

        // Check Electrical Safety Answers - render page
        // TODO PDJB-655: Implement Check Electrical Safety Answers step
        assertThat(checkElectricalSafetyAnswersPage.heading).containsText("TODO")
        checkElectricalSafetyAnswersPage.form.submit()
        // The internal EpcLookupByUprnStep at the start of the EpcTask does not find an EPC
        val hasEpcPage = assertPageIs(page, HasEpcFormPagePropertyRegistration::class)

        // Has EPC - render page
        // TODO PDJB-656: Implement Has EPC step
        hasEpcPage.submitProvideThisLater()
        val provideEpcLaterPage = assertPageIs(page, ProvideEpcLaterFormPagePropertyRegistration::class)

        // Provide EPC Later - render page
        assertThat(provideEpcLaterPage.heading).containsText("Provide your EPC details later")
        assertThat(provideEpcLaterPage.insetText).isHidden()
        provideEpcLaterPage.form.submit()

        val checkEpcAnswersPage = assertPageIs(page, CheckEpcAnswersFormPagePropertyRegistration::class)

        // Check EPC Answers - render page
        // TODO PDJB-670: Implement Check EPC Answers step
    }

    @Test
    fun `User can complete the journey with missing compliance certificates for an occupied property`(page: Page) {
        // Gas supply page
        val hasGasSupplyPage = navigator.skipToPropertyRegistrationHasGasSupplyPage(propertyIsOccupied = true)
        hasGasSupplyPage.submitHasGasSupply()
        val hasGasCertPage = assertPageIs(page, HasGasCertFormPagePropertyRegistration::class)

        // Has Gas Cert page
        hasGasCertPage.submitHasNoCertificate()
        val gasCertMissingPage = assertPageIs(page, GasCertMissingFormPagePropertyRegistration::class)

        // Gas Cert Missing - render page
        assertThat(gasCertMissingPage.heading).containsText("You must get a valid gas safety certificate for this property")
        assertThat(gasCertMissingPage.submitButton).containsText("Continue without a valid gas safety certificate")
        assertThat(gasCertMissingPage.warning)
            .containsText("You could face prosecution if you have tenants in a property without a gas safety certificate")
        gasCertMissingPage.form.submit()
        val checkGasSafetyAnswersPage = assertPageIs(page, CheckGasSafetyAnswersFormPagePropertyRegistration::class)

        // TODO PDJB-637: Implement Check Gas Safety Answers step
        assertThat(checkGasSafetyAnswersPage.heading).containsText("TODO")
        checkGasSafetyAnswersPage.form.submit()
        val hasElectricalCertPage = assertPageIs(page, HasElectricalCertFormPagePropertyRegistration::class)

        // Has Electrical Cert - render page
        assertThat(hasElectricalCertPage.heading).containsText("Which electrical safety certificate do you have for this property?")
        hasElectricalCertPage.submitHasNoCert()
        val electricalCertMissingPage = assertPageIs(page, ElectricalCertMissingFormPagePropertyRegistration::class)

        assertThat(electricalCertMissingPage.heading).containsText("You must get a valid electrical safety certificate for this property")
        assertThat(electricalCertMissingPage.warning)
            .containsText("You could face prosecution if you have tenants in a property without an electrical safety certificate.")
        assertThat(electricalCertMissingPage.submitButton).containsText("Continue without a valid electrical safety certificate")
        electricalCertMissingPage.form.submit()
        val checkElectricalSafetyAnswersPage = assertPageIs(page, CheckElectricalSafetyAnswersFormPagePropertyRegistration::class)

        // Setup EpcLookupByUprnStep NOT finding an EPC for this property when the next step submits
        whenever(epcRegisterClient.getByUprn(uprnForSelectedAddress)).thenReturn(MockEpcData.epcRegisterClientEpcNotFoundResponse)

        // Check Electrical Safety Answers - render page
        // TODO PDJB-655: Implement Check Electrical Safety Answers step
        assertThat(checkElectricalSafetyAnswersPage.heading).containsText("TODO")
        checkElectricalSafetyAnswersPage.form.submit()
        // The internal EpcLookupByUprnStep at the start of the EpcTask does not find an EPC
        val hasEpcPage = assertPageIs(page, HasEpcFormPagePropertyRegistration::class)

        // Has EPC - render page
        // TODO PDJB-656: Implement Has EPC step
        hasEpcPage.submitHasNoEpc()
        val isEpcRequiredPage = assertPageIs(page, IsEpcRequiredFormPagePropertyRegistration::class)

        // Is EPC required - render page
        assertThat(isEpcRequiredPage.heading).containsText("Is an EPC required to let this property?")
        isEpcRequiredPage.submitEpcRequired()
        val epcMissingPage = assertPageIs(page, EpcMissingFormPagePropertyRegistration::class)

        // TODO PDJB-659 - Implement EPC Missing step
        epcMissingPage.form.submit()
        val checkEpcAnswersPage = assertPageIs(page, CheckEpcAnswersFormPagePropertyRegistration::class)

        // Check EPC Answers - render page
        // TODO PDJB-670: Implement Check EPC Answers step
        assertThat(checkEpcAnswersPage.heading).containsText("TODO")
        checkEpcAnswersPage.form.submit()
        val checkAnswersPage = assertPageIs(page, CheckAnswersPagePropertyRegistration::class)
    }

    @Test
    fun `User can complete the journey with expired compliance certificates for an occupied property (epc found by uprn)`(page: Page) {
        // Gas supply page
        val hasGasSupplyPage = navigator.skipToPropertyRegistrationHasGasSupplyPage(propertyIsOccupied = true)
        hasGasSupplyPage.submitHasGasSupply()
        val hasGasCertPage = assertPageIs(page, HasGasCertFormPagePropertyRegistration::class)

        // Has Gas Cert page
        hasGasCertPage.submitHasCertificate()
        var gasCertIssueDatePage = assertPageIs(page, GasCertIssueDateFormPagePropertyRegistration::class)

        // Gas Cert Issue Date - render page
        assertThat(gasCertIssueDatePage.heading).containsText("What’s the issue date on the gas safety certificate?")
        gasCertIssueDatePage.submitDate(expiredGasSafetyCertIssueDate)
        var gasCertExpiredPage = assertPageIs(page, GasCertExpiredFormPagePropertyRegistration::class)

        // Gas Cert Expired - render page then navigate to edit issue date
        assertThat(gasCertExpiredPage.mainHeading).containsText("This gas safety certificate has expired")
        assertThat(gasCertExpiredPage.sectionHeading).containsText("You must get a valid gas safety certificate for this property")
        assertThat(gasCertExpiredPage.warning)
            .containsText("You could face prosecution if you have tenants in a property without a gas safety certificate.")
        assertThat(gasCertExpiredPage.submitButton).containsText("Continue without a valid gas safety certificate")
        gasCertExpiredPage.changeIssueDateLink.clickAndWait()
        gasCertIssueDatePage = assertPageIs(page, GasCertIssueDateFormPagePropertyRegistration::class)

        // Gas Cert Issue Date - render page, prepopulated with previous value, then submit again
        assertThat(gasCertIssueDatePage.form.dayInput).hasValue(expiredGasSafetyCertIssueDate.dayOfMonth.toString())
        assertThat(gasCertIssueDatePage.form.monthInput).hasValue(expiredGasSafetyCertIssueDate.monthNumber.toString())
        assertThat(gasCertIssueDatePage.form.yearInput).hasValue(expiredGasSafetyCertIssueDate.year.toString())
        gasCertIssueDatePage.form.submit()
        gasCertExpiredPage = assertPageIs(page, GasCertExpiredFormPagePropertyRegistration::class)

        // Back on Gas Cert Expired page - submit
        gasCertExpiredPage.form.submit()
        val checkGasSafetyAnswersPage = assertPageIs(page, CheckGasSafetyAnswersFormPagePropertyRegistration::class)

        // Check Gas Safety Answers - render page
        // TODO PDJB-637: Implement Check Gas Safety Answers step
        assertThat(checkGasSafetyAnswersPage.heading).containsText("TODO")

        checkGasSafetyAnswersPage.form.submit()
        val hasElectricalCertPage = assertPageIs(page, HasElectricalCertFormPagePropertyRegistration::class)

        // Has Electrical Cert - render page
        assertThat(hasElectricalCertPage.heading).containsText("Which electrical safety certificate do you have for this property?")
        hasElectricalCertPage.submitHasEic()
        var electricalCertExpiryDatePage = assertPageIs(page, ElectricalCertExpiryDateFormPagePropertyRegistration::class)

        // Electrical Cert Expiry Date - render page
        electricalCertExpiryDatePage.submitDate(expiredExpiryDate)
        var electricalCertExpiredPage = assertPageIs(page, ElectricalCertExpiredFormPagePropertyRegistration::class)

        // Electrical Cert Expired - render page then check change expiry date link
        assertThat(electricalCertExpiredPage.warning)
            .containsText("You could face prosecution if you have tenants in a property without an electrical safety certificate.")
        assertThat(electricalCertExpiredPage.submitButton).containsText("Continue without a valid electrical safety certificate")
        electricalCertExpiredPage.changeExpiryDateLink.clickAndWait()
        electricalCertExpiryDatePage = assertPageIs(page, ElectricalCertExpiryDateFormPagePropertyRegistration::class)

        // Electrical Cert Expiry Date again - render page, prepopulated with previous value, then submit again
        assertThat(electricalCertExpiryDatePage.form.dayInput).hasValue(expiredExpiryDate.dayOfMonth.toString())
        assertThat(electricalCertExpiryDatePage.form.monthInput).hasValue(expiredExpiryDate.monthNumber.toString())
        assertThat(electricalCertExpiryDatePage.form.yearInput).hasValue(expiredExpiryDate.year.toString())
        electricalCertExpiryDatePage.form.submit()
        electricalCertExpiredPage = assertPageIs(page, ElectricalCertExpiredFormPagePropertyRegistration::class)

        // Back on Electrical Cert Expired page - submit
        electricalCertExpiredPage.form.submit()
        val checkElectricalSafetyAnswersPage = assertPageIs(page, CheckElectricalSafetyAnswersFormPagePropertyRegistration::class)

        // Setup EpcLookupByUprnStep being able to find an EPC for this property when the next step submits
        whenever(epcRegisterClient.getByUprn(uprnForSelectedAddress))
            .thenReturn(
                MockEpcData.createEpcRegisterClientEpcFoundResponse(
                    expiryDate = expiredExpiryDate,
                ),
            )

        // TODO PDJB-655: Implement Check Electrical Safety Answers step (EIC variant)
        // Check Electrical Safety Answers - render page
        assertThat(checkElectricalSafetyAnswersPage.heading).containsText("TODO")
        checkElectricalSafetyAnswersPage.form.submit()

        // EpcLookupByUprnStep finds the EPC, so redirects to Check Automatched EPC page
        val checkAutomatchedEpcPage = assertPageIs(page, CheckAutomatchedEpcFormPagePropertyRegistration::class)

        // Check Automatched EPC - render page
        // TODO PDJB-661: Implement Check Automatched EPC step
        checkAutomatchedEpcPage.submitEpcOlderThan10Years()
        val epcExpiryCheckPage = assertPageIs(page, EpcInDateAtStartOfTenancyCheckPagePropertyRegistration::class)

        // TODO PDJB-665 - tenants in place when epc expired - NO
        epcExpiryCheckPage.submitEpcExpired()
        val epcExpiredPage = assertPageIs(page, EpcExpiredFormPagePropertyRegistration::class)

        // TODO PDJB-666 - expired
        epcExpiredPage.form.submit()
        val checkEpcAnswersPage = assertPageIs(page, CheckEpcAnswersFormPagePropertyRegistration::class)

        // Check EPC Answers - render page
        // TODO PDJB-670: Implement Check EPC Answers step
        assertThat(checkEpcAnswersPage.heading).containsText("TODO")
        checkEpcAnswersPage.form.submit()
        val checkAnswersPage = assertPageIs(page, CheckAnswersPagePropertyRegistration::class)
    }

    @Test
    fun `User can complete the journey with expired compliance certificates for an unoccupied property (epc not found by uprn)`(
        page: Page,
    ) {
        // Gas supply page
        val hasGasSupplyPage = navigator.skipToPropertyRegistrationHasGasSupplyPage(propertyIsOccupied = false)
        hasGasSupplyPage.submitHasGasSupply()
        val hasGasCertPage = assertPageIs(page, HasGasCertFormPagePropertyRegistration::class)

        // Has Gas Cert page
        hasGasCertPage.submitHasCertificate()
        var gasCertIssueDatePage = assertPageIs(page, GasCertIssueDateFormPagePropertyRegistration::class)

        // Gas Cert Issue Date - render page
        assertThat(gasCertIssueDatePage.heading).containsText("What’s the issue date on the gas safety certificate?")
        gasCertIssueDatePage.submitDate(expiredGasSafetyCertIssueDate)
        var gasCertExpiredPage = assertPageIs(page, GasCertExpiredFormPagePropertyRegistration::class)

        // Gas Cert Expired - render page then navigate to edit issue date
        assertThat(gasCertExpiredPage.mainHeading).containsText("This gas safety certificate has expired")
        assertThat(gasCertExpiredPage.sectionHeading).containsText("What to do next")
        assertThat(gasCertExpiredPage.warning).isHidden()
        assertThat(gasCertExpiredPage.submitButton).containsText("Save and continue")
        gasCertExpiredPage.changeIssueDateLink.clickAndWait()
        gasCertIssueDatePage = assertPageIs(page, GasCertIssueDateFormPagePropertyRegistration::class)

        // Gas Cert Issue Date - render page, prepopulated with previous value, then submit again
        assertThat(gasCertIssueDatePage.form.dayInput).hasValue(expiredGasSafetyCertIssueDate.dayOfMonth.toString())
        assertThat(gasCertIssueDatePage.form.monthInput).hasValue(expiredGasSafetyCertIssueDate.monthNumber.toString())
        assertThat(gasCertIssueDatePage.form.yearInput).hasValue(expiredGasSafetyCertIssueDate.year.toString())
        gasCertIssueDatePage.form.submit()
        gasCertExpiredPage = assertPageIs(page, GasCertExpiredFormPagePropertyRegistration::class)

        // Back on Gas Cert Expired page - submit
        gasCertExpiredPage.form.submit()
        val checkGasSafetyAnswersPage = assertPageIs(page, CheckGasSafetyAnswersFormPagePropertyRegistration::class)

        // Check Gas Safety Answers - render page
        // TODO PDJB-637: Implement Check Gas Safety Answers step
        assertThat(checkGasSafetyAnswersPage.heading).containsText("TODO")

        checkGasSafetyAnswersPage.form.submit()
        val hasElectricalCertPage = assertPageIs(page, HasElectricalCertFormPagePropertyRegistration::class)

        // Has Electrical Cert - render page
        assertThat(hasElectricalCertPage.heading).containsText("Which electrical safety certificate do you have for this property?")
        hasElectricalCertPage.submitHasEic()
        var electricalCertExpiryDatePage = assertPageIs(page, ElectricalCertExpiryDateFormPagePropertyRegistration::class)

        // Electrical Cert Expiry Date - render page
        electricalCertExpiryDatePage.submitDate(expiredExpiryDate)
        var electricalCertExpiredPage = assertPageIs(page, ElectricalCertExpiredFormPagePropertyRegistration::class)

        // Electrical Cert Expired - render page then check change expiry date link
        assertThat(electricalCertExpiredPage.warning).isHidden()
        assertThat(electricalCertExpiredPage.submitButton).containsText("Save and continue")
        electricalCertExpiredPage.changeExpiryDateLink.clickAndWait()
        electricalCertExpiryDatePage = assertPageIs(page, ElectricalCertExpiryDateFormPagePropertyRegistration::class)

        // Electrical Cert Expiry Date again - render page, prepopulated with previous value, then submit again
        assertThat(electricalCertExpiryDatePage.form.dayInput).hasValue(expiredExpiryDate.dayOfMonth.toString())
        assertThat(electricalCertExpiryDatePage.form.monthInput).hasValue(expiredExpiryDate.monthNumber.toString())
        assertThat(electricalCertExpiryDatePage.form.yearInput).hasValue(expiredExpiryDate.year.toString())
        electricalCertExpiryDatePage.form.submit()
        electricalCertExpiredPage = assertPageIs(page, ElectricalCertExpiredFormPagePropertyRegistration::class)

        // Back on Electrical Cert Expired page - submit
        electricalCertExpiredPage.form.submit()
        val checkElectricalSafetyAnswersPage = assertPageIs(page, CheckElectricalSafetyAnswersFormPagePropertyRegistration::class)

        // Setup EpcLookupByUprnStep NOT finding an EPC for this property when the next step submits
        whenever(epcRegisterClient.getByUprn(uprnForSelectedAddress)).thenReturn(MockEpcData.epcRegisterClientEpcNotFoundResponse)

        // TODO PDJB-655: Implement Check Electrical Safety Answers step (EIC variant)
        // Check Electrical Safety Answers - render page
        assertThat(checkElectricalSafetyAnswersPage.heading).containsText("TODO")
        checkElectricalSafetyAnswersPage.form.submit()
        // The internal EpcLookupByUprnStep at the start of the EpcTask does not find an EPC
        val hasEpcPage = assertPageIs(page, HasEpcFormPagePropertyRegistration::class)

        // Has EPC - render page
        // TODO PDJB-656: Implement Has EPC step
        hasEpcPage.submitHasEpc()
        val findYourEpcPage = assertPageIs(page, FindYourEpcFormPagePropertyRegistration::class)

        // EPC Search - render page
        whenever(epcRegisterClient.getByRrn(CURRENT_EXPIRED_EPC_CERTIFICATE_NUMBER))
            .thenReturn(
                MockEpcData.createEpcRegisterClientEpcFoundResponse(
                    certificateNumber = CURRENT_EXPIRED_EPC_CERTIFICATE_NUMBER,
                    expiryDate = expiredExpiryDate,
                    latestCertificateNumberForThisProperty = CURRENT_EXPIRED_EPC_CERTIFICATE_NUMBER,
                ),
            )
        findYourEpcPage.submitCurrentEpcNumberWhichIsExpired()
        val confirmEpcDetailsPage = assertPageIs(page, ConfirmEpcDetailsRetrievedByCertificateNumberPagePropertyRegistration::class)

        // Check Matched EPC - render page
        val expectedExpiryDate =
            expiredExpiryDate
                .toJavaLocalDate()
                .format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
        assertThat(confirmEpcDetailsPage.summaryCard.summaryList.addressRow.value).containsText(MockEpcData.defaultSingleLineAddress)
        assertThat(confirmEpcDetailsPage.summaryCard.summaryList.energyEfficiencyRatingRow.value).containsText("C")
        assertThat(confirmEpcDetailsPage.summaryCard.summaryList.expiryDateRow.value).containsText(expectedExpiryDate)
        assertThat(
            confirmEpcDetailsPage.summaryCard.summaryList.certificateNumberRow.value,
        ).containsText(CURRENT_EXPIRED_EPC_CERTIFICATE_NUMBER)
        confirmEpcDetailsPage.submitYes()
        val epcExpiredPage = assertPageIs(page, EpcExpiredFormPagePropertyRegistration::class)

        // TODO PDJB-666 - expired
        epcExpiredPage.form.submit()
        val checkEpcAnswersPage = assertPageIs(page, CheckEpcAnswersFormPagePropertyRegistration::class)

        // Check EPC Answers - render page
        // TODO PDJB-670: Implement Check EPC Answers step
        assertThat(checkEpcAnswersPage.heading).containsText("TODO")
        checkEpcAnswersPage.form.submit()
        val checkAnswersPage = assertPageIs(page, CheckAnswersPagePropertyRegistration::class)
    }

    @Test
    fun `The Electrical Safety task can be completed by the user uploaded an eicr`(page: Page) {
        // Skip to Has Electrical Cert page and submit "Yes"
        val hasElectricalCertPage = navigator.skipToPropertyRegistrationHasElectricalCertPage()
        hasElectricalCertPage.submitHasEicr()
        val electricalCertExpiryDatePage = assertPageIs(page, ElectricalCertExpiryDateFormPagePropertyRegistration::class)

        // Electrical Cert Expiry Date - render page
        assertThat(
            electricalCertExpiryDatePage.heading,
        ).containsText("What’s the expiry date on the Electrical Installation Condition Report?")
        electricalCertExpiryDatePage.submitDate(validExpiryDate)

        // TODO PDJB-651 - Upload certificate page, make sure copy matches eicr variant

        // TODO PDJB-655 - Check Electrical Safety Answers step, make sure copy matches eicr variant
    }

    @Test
    fun `The EPC task can be completed when FindYourEpc finds a superseded epc`(page: Page) {
        // Skip to Find Your EPC page and submit "Superseded EPC Found"
        val findYourEpcPage = navigator.skipToPropertyRegistrationFindYourEpcPage()
        whenever(epcRegisterClient.getByRrn(SUPERSEDED_EPC_CERTIFICATE_NUMBER)).thenReturn(
            MockEpcData.createEpcRegisterClientEpcFoundResponse(
                certificateNumber = SUPERSEDED_EPC_CERTIFICATE_NUMBER,
                expiryDate = MockEpcData.expiryDateInThePast,
                latestCertificateNumberForThisProperty = CURRENT_EPC_CERTIFICATE_NUMBER,
            ),
        )
        findYourEpcPage.submitSupersededEpcNumber()
        val epcSupersededPage = assertPageIs(page, EpcSuperseededFormPagePropertyRegistration::class)

        // Check details of superseded and latest epc - render page
        // TODO PDJB-664: Implement superseded EPC step (might need multiple variants of this for occupied / unoccupied if content is different)
        epcSupersededPage.submitEpcCompliant()
        val checkEpcAnswersPage = assertPageIs(page, CheckEpcAnswersFormPagePropertyRegistration::class)

        // Check EPC Answers - render page
        // TODO PDJB-670: Implement Check EPC Answers step
        assertThat(checkEpcAnswersPage.heading).containsText("TODO")
        checkEpcAnswersPage.form.submit()
        val checkAnswersPage = assertPageIs(page, CheckAnswersPagePropertyRegistration::class)
    }

    @Test
    fun `The EPC task can be completed when FindYourEpc finds no epc and it is missing`(page: Page) {
        // Skip to Find Your EPC page and submit "No EPC Found"
        val findYourEpcPage = navigator.skipToPropertyRegistrationFindYourEpcPage()
        whenever(
            epcRegisterClient.getByRrn(NONEXISTENT_EPC_CERTIFICATE_NUMBER),
        ).thenReturn(MockEpcData.epcRegisterClientEpcNotFoundResponse)
        findYourEpcPage.submitNonexistentEpcNumber()
        val epcNotFoundPage = assertPageIs(page, EpcNotFoundFormPagePropertyRegistration::class)

        // EPC not found - render page
        // TODO PDJB-663: Implement EPC not found step (might need multiple variants of this for occupied / unoccupied if content is different)
        epcNotFoundPage.form.submit()
        val isEpcRequiredPage = assertPageIs(page, IsEpcRequiredFormPagePropertyRegistration::class)

        // Is EPC required - render page
        assertThat(isEpcRequiredPage.heading).containsText("Is an EPC required to let this property?")
        isEpcRequiredPage.submitEpcRequired()
        val epcMissingPage = assertPageIs(page, EpcMissingFormPagePropertyRegistration::class)

        // TODO PDJB-659 - Implement EPC Missing step
        epcMissingPage.form.submit()
        val checkEpcAnswersPage = assertPageIs(page, CheckEpcAnswersFormPagePropertyRegistration::class)

        // Check EPC Answers - render page
        // TODO PDJB-670: Implement Check EPC Answers step
        assertThat(checkEpcAnswersPage.heading).containsText("TODO")
        checkEpcAnswersPage.form.submit()
        val checkAnswersPage = assertPageIs(page, CheckAnswersPagePropertyRegistration::class)
    }

    @Test
    fun `User can navigate the MEES flow when they have a MEES exemption`(page: Page) {
        val hasMeesExemptionPage = navigator.skipToPropertyRegistrationHasMeesExemptionPage()

        // Has MEES Exemption - render page
        assertThat(hasMeesExemptionPage.heading).containsText("You need a registered energy efficiency exemption to let this property")
        hasMeesExemptionPage.submitHasMeesExemption()
        val meesExemptionPage = assertPageIs(page, MeesExemptionFormPagePropertyRegistration::class)

        // MEES Exemption - select exemption reason
        meesExemptionPage.submitExemptionReason(MeesExemptionReason.HIGH_COST)
        val checkEpcAnswersPage = assertPageIs(page, CheckEpcAnswersFormPagePropertyRegistration::class)

        // Check EPC Answers - render page
        // TODO PDJB-670: Implement Check EPC Answers step
        assertThat(checkEpcAnswersPage.heading).containsText("TODO")
    }

    @Test
    fun `User can navigate the MEES flow when they do not have a MEES exemption`(page: Page) {
        val hasMeesExemptionPage = navigator.skipToPropertyRegistrationHasMeesExemptionPage()

        // Has MEES Exemption - submit no exemption
        hasMeesExemptionPage.submitHasNoMeesExemption()
        val lowEnergyRatingPage = assertPageIs(page, LowEnergyRatingFormPagePropertyRegistration::class)

        // Low Energy Rating - render page
        assertThat(lowEnergyRatingPage.heading).containsText("This property does not meet energy efficiency requirements for letting")
        lowEnergyRatingPage.form.submit()
        val checkEpcAnswersPage = assertPageIs(page, CheckEpcAnswersFormPagePropertyRegistration::class)

        // Check EPC Answers - render page
        // TODO PDJB-670: Implement Check EPC Answers step
        assertThat(checkEpcAnswersPage.heading).containsText("TODO")
    }

    @Test
    fun `User can navigate the EPC exemption flow`(page: Page) {
        val epcExemptionPage = navigator.skipToPropertyRegistrationEpcExemptionPage()

        // EPC Exemption - select exemption reason
        epcExemptionPage.submitExemptionReason(EpcExemptionReason.PROTECTED_ARCHITECTURAL_OR_HISTORICAL_MERIT)
        val checkEpcAnswersPage = assertPageIs(page, CheckEpcAnswersFormPagePropertyRegistration::class)

        // Check EPC Answers - render page
        // TODO PDJB-670: Implement Check EPC Answers step
        assertThat(checkEpcAnswersPage.heading).containsText("TODO")
    }

    companion object {
        val validGasSafetyCertIssueDate =
            DateTimeHelper()
                .getCurrentDateInUK()
                .minus(DatePeriod(years = GAS_SAFETY_CERT_VALIDITY_YEARS))
                .plus(DatePeriod(days = 5))

        val expiredGasSafetyCertIssueDate =
            DateTimeHelper()
                .getCurrentDateInUK()
                .minus(DatePeriod(years = GAS_SAFETY_CERT_VALIDITY_YEARS, days = 5))

        val validExpiryDate =
            DateTimeHelper()
                .getCurrentDateInUK()
                .plus(DatePeriod(days = 5))

        val expiredExpiryDate =
            DateTimeHelper()
                .getCurrentDateInUK()
                .minus(DatePeriod(days = 5))

        val uprnForSelectedAddress = 1L // This matches the uprn in data-local.sql for address 1 Fictional Road, FA1 1AA
    }
}
