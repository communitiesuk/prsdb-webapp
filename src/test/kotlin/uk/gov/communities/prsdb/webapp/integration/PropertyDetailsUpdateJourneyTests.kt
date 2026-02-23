package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.CheckHouseholdsAnswersPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.CheckLicensingAnswersPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.CheckOccupancyAnswersPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.CheckPeopleAnswersPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.HmoAdditionalLicenceFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.HmoMandatoryLicenceFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.HouseholdsNumberOfPeopleFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.LicensingTypeFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.NumberOfHouseholdsFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.NumberOfPeopleFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.OccupancyBillsIncludedFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.OccupancyFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.OccupancyFurnishedStatusFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.OccupancyNumberOfBedroomsFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.OccupancyNumberOfHouseholdsFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.OccupancyNumberOfPeopleFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.OccupancyRentAmountFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.OccupancyRentFrequencyFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.OccupancyRentIncludesBillsFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.OwnershipTypeFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.SelectiveLicenceFormPagePropertyDetailsUpdate
import java.net.URI
import kotlin.test.assertContains

class PropertyDetailsUpdateJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    private val propertyOwnershipId = 1L
    private val urlArguments = mapOf("propertyOwnershipId" to propertyOwnershipId.toString())

    @BeforeEach
    fun setUp() {
        whenever(absoluteUrlProvider.buildLandlordDashboardUri())
            .thenReturn(URI("example.com"))
    }

    // TODO PDJB-105 update this test to have occupancy be the first to be updated
    @Disabled
    @Test
    fun `A property update does not affect prior sections updated in parallel`(page: Page) {
        // Ensure ownership type starts as freehold
        navigator
            .goToPropertyDetailsUpdateOwnershipTypePage(propertyOwnershipId)
            .submitOwnershipType(OwnershipType.FREEHOLD)

        // Start updating ownership type to create isolated session
        navigator.skipToPropertyDetailsUpdateCheckOccupancyToOccupiedAnswersPage(propertyOwnershipId)

        // Update ownership type to leasehold
        navigator
            .goToPropertyDetailsUpdateOwnershipTypePage(propertyOwnershipId)
            .submitOwnershipType(OwnershipType.LEASEHOLD)

        navigator
            .skipToPropertyDetailsUpdateCheckOccupancyToOccupiedAnswersPage(propertyOwnershipId)
            .form
            .submit()

        val propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)

        assertThat(propertyDetailsPage.propertyDetailsSummaryList.ownershipTypeRow.value).containsText("Leasehold")
    }

    @Nested
    inner class OwnershipTypeUpdates {
        @Test
        fun `A property can have its ownership type updated`(page: Page) {
            // Details page
            var propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
            propertyDetailsPage.propertyDetailsSummaryList.ownershipTypeRow.clickActionLinkAndWait()
            val updateOwnershipTypePage = assertPageIs(page, OwnershipTypeFormPagePropertyDetailsUpdate::class, urlArguments)

            // Update Ownership Type page
            updateOwnershipTypePage.submitOwnershipType(OwnershipType.LEASEHOLD)
            propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)

            // Check changes have occurred
            assertThat(propertyDetailsPage.propertyDetailsSummaryList.ownershipTypeRow.value).containsText("Leasehold")
        }
    }

    @Nested
    inner class LicenceUpdates {
        @Test
        fun `A property can have its licensing updated to a selective licence`(page: Page) {
            val newLicenceNumber = "SL123"

            // Details page
            var propertyDetailsUpdatePage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
            propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingTypeRow.clickActionLinkAndWait()
            val updateLicensingTypePage = assertPageIs(page, LicensingTypeFormPagePropertyDetailsUpdate::class, urlArguments)

            // Update licence to selective
            updateLicensingTypePage.submitLicensingType(LicensingType.SELECTIVE_LICENCE)
            val updateLicenceNumberPage = assertPageIs(page, SelectiveLicenceFormPagePropertyDetailsUpdate::class, urlArguments)

            // Update licence number
            updateLicenceNumberPage.submitLicenseNumber(newLicenceNumber)
            val checkLicensingAnswersPage = assertPageIs(page, CheckLicensingAnswersPagePropertyDetailsUpdate::class, urlArguments)

            // Check licensing answers
            assertContains(checkLicensingAnswersPage.summaryName.getText(), "You have updated the property licence")
            assertThat(checkLicensingAnswersPage.summaryList.licensingTypeRow.value).containsText("Selective licence")
            assertThat(checkLicensingAnswersPage.summaryList.licensingNumberRow.value).containsText(newLicenceNumber)
            checkLicensingAnswersPage.confirm()
            propertyDetailsUpdatePage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)

            // Check changes have occurred
            assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingTypeRow.value).containsText("Selective licence")
            assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingNumberRow.value).containsText(newLicenceNumber)
        }

        @Test
        fun `A property can have its licensing updated to a HMO Mandatory licence`(page: Page) {
            val newLicenceNumber = "MAND123"

            // Details page
            var propertyDetailsUpdatePage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
            propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingTypeRow.clickActionLinkAndWait()
            val updateLicensingTypePage = assertPageIs(page, LicensingTypeFormPagePropertyDetailsUpdate::class, urlArguments)

            // Update licence to HMO mandatory
            updateLicensingTypePage.submitLicensingType(LicensingType.HMO_MANDATORY_LICENCE)
            val updateLicenceNumberPage = assertPageIs(page, HmoMandatoryLicenceFormPagePropertyDetailsUpdate::class, urlArguments)

            // Update licence number
            updateLicenceNumberPage.submitLicenseNumber(newLicenceNumber)
            val checkLicensingAnswersPage = assertPageIs(page, CheckLicensingAnswersPagePropertyDetailsUpdate::class, urlArguments)

            // Check licensing answers
            assertContains(checkLicensingAnswersPage.summaryName.getText(), "You have updated the property licence")
            assertThat(checkLicensingAnswersPage.summaryList.licensingTypeRow.value).containsText("HMO mandatory licence")
            assertThat(checkLicensingAnswersPage.summaryList.licensingNumberRow.value).containsText(newLicenceNumber)
            checkLicensingAnswersPage.confirm()
            propertyDetailsUpdatePage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)

            // Check changes have occurred
            assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingTypeRow.value).containsText("HMO mandatory licence")
            assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingNumberRow.value).containsText(newLicenceNumber)
        }

        @Test
        fun `A property can have its licensing updated to a HMO additional licence`(page: Page) {
            val newLicenceNumber = "ADD123"

            // Details page
            var propertyDetailsUpdatePage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
            propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingTypeRow.clickActionLinkAndWait()
            val updateLicensingTypePage = assertPageIs(page, LicensingTypeFormPagePropertyDetailsUpdate::class, urlArguments)

            // Update licence to HMO additional
            updateLicensingTypePage.submitLicensingType(LicensingType.HMO_ADDITIONAL_LICENCE)
            val updateLicenceNumberPage = assertPageIs(page, HmoAdditionalLicenceFormPagePropertyDetailsUpdate::class, urlArguments)

            // Update licence number
            updateLicenceNumberPage.submitLicenseNumber(newLicenceNumber)
            val checkLicensingAnswersPage = assertPageIs(page, CheckLicensingAnswersPagePropertyDetailsUpdate::class, urlArguments)

            // Check licensing answers
            assertContains(checkLicensingAnswersPage.summaryName.getText(), "You have updated the property licence")
            assertThat(checkLicensingAnswersPage.summaryList.licensingTypeRow.value).containsText("HMO additional licence")
            assertThat(checkLicensingAnswersPage.summaryList.licensingNumberRow.value).containsText(newLicenceNumber)
            checkLicensingAnswersPage.confirm()
            propertyDetailsUpdatePage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)

            // Check changes have occurred
            assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingTypeRow.value).containsText("HMO additional licence")
            assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingNumberRow.value).containsText(newLicenceNumber)
        }

        @Test
        fun `A property can have its licensing removed`(page: Page) {
            // A property ownership with an existing licence
            val propertyOwnershipId = 7L
            val urlArguments = mapOf("propertyOwnershipId" to propertyOwnershipId.toString())

            // Details page
            var propertyDetailsUpdatePage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
            propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingTypeRow.clickActionLinkAndWait()
            val updateLicensingTypePage = assertPageIs(page, LicensingTypeFormPagePropertyDetailsUpdate::class, urlArguments)

            // Update licence to no licensing
            updateLicensingTypePage.submitLicensingType(LicensingType.NO_LICENSING)
            val checkLicensingAnswersPage = assertPageIs(page, CheckLicensingAnswersPagePropertyDetailsUpdate::class, urlArguments)

            // Check licensing answers
            assertContains(checkLicensingAnswersPage.summaryName.getText(), "You have removed this property’s licence")
            assertThat(checkLicensingAnswersPage.summaryList.licensingTypeRow.value).containsText("None")
            checkLicensingAnswersPage.confirm()
            propertyDetailsUpdatePage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)

            // Check changes have occurred
            assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingTypeRow.value).containsText("None")
        }

        @Test
        fun `A property can have its licensing number updated again from the check licensing answers page`(page: Page) {
            val firstNewLicenceNumber = "SL456"
            val secondNewLicenceNumber = "SL789"

            // Details page
            var propertyDetailsUpdatePage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
            propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingTypeRow.clickActionLinkAndWait()
            val updateLicensingTypePage = assertPageIs(page, LicensingTypeFormPagePropertyDetailsUpdate::class, urlArguments)

            // Update licence to selective
            updateLicensingTypePage.submitLicensingType(LicensingType.SELECTIVE_LICENCE)
            var updateLicenceNumberPage = assertPageIs(page, SelectiveLicenceFormPagePropertyDetailsUpdate::class, urlArguments)

            // Update licence number
            updateLicenceNumberPage.submitLicenseNumber(firstNewLicenceNumber)
            var checkLicensingAnswersPage = assertPageIs(page, CheckLicensingAnswersPagePropertyDetailsUpdate::class, urlArguments)

            // Click change link for Licensing Number
            checkLicensingAnswersPage.summaryList.licensingNumberRow
                .clickActionLinkAndWait()
            updateLicenceNumberPage = assertPageIs(page, SelectiveLicenceFormPagePropertyDetailsUpdate::class, urlArguments)

            // Update licence number
            updateLicenceNumberPage.submitLicenseNumber(secondNewLicenceNumber)
            checkLicensingAnswersPage =
                assertPageIs(page, CheckLicensingAnswersPagePropertyDetailsUpdate::class, urlArguments)

            // Check licensing answers
            assertContains(checkLicensingAnswersPage.summaryName.getText(), "You have updated the property licence")
            assertThat(checkLicensingAnswersPage.summaryList.licensingTypeRow.value).containsText("Selective licence")
            assertThat(checkLicensingAnswersPage.summaryList.licensingNumberRow.value).containsText(secondNewLicenceNumber)
            checkLicensingAnswersPage.confirm()
            propertyDetailsUpdatePage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)

            // Check changes have occurred
            assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingTypeRow.value).containsText("Selective licence")
            assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingNumberRow.value).containsText(secondNewLicenceNumber)
        }
    }

    @Nested
    inner class OccupancyUpdates {
        private val occupiedPropertyOwnershipId = 1L
        private val occupiedPropertyUrlArguments = mapOf("propertyOwnershipId" to occupiedPropertyOwnershipId.toString())

        private val vacantPropertyOwnershipId = 7L
        private val vacantPropertyUrlArguments = mapOf("propertyOwnershipId" to vacantPropertyOwnershipId.toString())

        @Test
        fun `A property can have its occupancy updated from occupied to vacant`(page: Page) {
            // Details page
            var propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(occupiedPropertyOwnershipId)
            propertyDetailsPage.propertyDetailsSummaryList.occupancyRow.clickActionLinkAndWait()
            val updateOccupancyPage = assertPageIs(page, OccupancyFormPagePropertyDetailsUpdate::class, occupiedPropertyUrlArguments)

            // Update occupancy to vacant
            assertThat(updateOccupancyPage.form.fieldsetHeading).containsText("Update whether your property is occupied by tenants")
            updateOccupancyPage.submitIsVacant()
            val checkOccupancyAnswersPage =
                assertPageIs(page, CheckOccupancyAnswersPagePropertyDetailsUpdate::class, occupiedPropertyUrlArguments)

            // Check occupancy answers
            assertThat(checkOccupancyAnswersPage.summaryList.occupancyRow).containsText("No")
            assertThat(checkOccupancyAnswersPage.summaryList.numberOfHouseholdsRow).isHidden()
            assertThat(checkOccupancyAnswersPage.summaryList.numberOfPeopleRow).isHidden()
            checkOccupancyAnswersPage.confirm()
            propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, occupiedPropertyUrlArguments)

            // Check changes have occurred
            assertThat(propertyDetailsPage.propertyDetailsSummaryList.occupancyRow.value).containsText("No")
        }

        @Test
        fun `A property can have its occupancy updated from vacant to occupied`(page: Page) {
            // Details page
            var propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(vacantPropertyOwnershipId)
            propertyDetailsPage.propertyDetailsSummaryList.occupancyRow.clickActionLinkAndWait()
            val updateOccupancyPage = assertPageIs(page, OccupancyFormPagePropertyDetailsUpdate::class, vacantPropertyUrlArguments)

            // Update occupancy to occupied
            assertThat(updateOccupancyPage.form.fieldsetHeading).containsText("Update whether your property is occupied by tenants")
            updateOccupancyPage.submitIsOccupied()
            val updateNumberOfHouseholdsPage =
                assertPageIs(page, OccupancyNumberOfHouseholdsFormPagePropertyDetailsUpdate::class, vacantPropertyUrlArguments)

            // Update number of households
            val newNumberOfHouseholds = 1
            assertThat(updateNumberOfHouseholdsPage.header).containsText("Update how many households live in your property")
            updateNumberOfHouseholdsPage.submitNumberOfHouseholds(newNumberOfHouseholds)
            val updateNumberOfPeoplePage =
                assertPageIs(page, OccupancyNumberOfPeopleFormPagePropertyDetailsUpdate::class, vacantPropertyUrlArguments)

            // Update number of people
            val newNumberOfPeople = 3
            assertThat(updateNumberOfPeoplePage.header).containsText("Update how many people live in your property")
            updateNumberOfPeoplePage.submitNumOfPeople(newNumberOfPeople)
            val bedroomsPage = assertPageIs(page, OccupancyNumberOfBedroomsFormPagePropertyDetailsUpdate::class, vacantPropertyUrlArguments)

            // Update number of bedrooms
            val newNumberOfBedrooms = 3
            assertThat(bedroomsPage.header).containsText("Update how many bedrooms are in your property")
            bedroomsPage.submitNumOfBedrooms(newNumberOfBedrooms)
            val rentIncludesBillsPage =
                assertPageIs(page, OccupancyRentIncludesBillsFormPagePropertyDetailsUpdate::class, vacantPropertyUrlArguments)

            // Update rent include bills
            assertThat(rentIncludesBillsPage.form.fieldsetHeading).containsText("Update whether the rent includes bills")
            rentIncludesBillsPage.submitIsIncluded()
            val billsIncludedPage =
                assertPageIs(page, OccupancyBillsIncludedFormPagePropertyDetailsUpdate::class, vacantPropertyUrlArguments)

            // Update bills included
            val expectedBillsIncluded = "Gas, Electricity, Water"
            assertThat(billsIncludedPage.form.fieldsetHeading).containsText("Update which of these you include in the rent")
            billsIncludedPage.selectGasElectricityWater()
            billsIncludedPage.form.submit()
            val furnishedPage = assertPageIs(page, OccupancyFurnishedStatusFormPagePropertyDetailsUpdate::class, vacantPropertyUrlArguments)

            // Update furnished status
            val expectedFurnishedStatus = "Furnished"
            assertThat(
                furnishedPage.form.fieldsetHeading,
            ).containsText("Update whether the property is furnished, partly furnished or unfurnished")
            furnishedPage.submitFurnishedStatus(FurnishedStatus.FURNISHED)
            val rentFrequencyPage =
                assertPageIs(page, OccupancyRentFrequencyFormPagePropertyDetailsUpdate::class, vacantPropertyUrlArguments)

            // Update rent frequency
            val expectedRentFrequency = "Weekly"
            assertThat(rentFrequencyPage.header).containsText("Update when you charge rent")
            rentFrequencyPage.selectRentFrequency(RentFrequency.WEEKLY)
            rentFrequencyPage.form.submit()
            val rentAmountPage = assertPageIs(page, OccupancyRentAmountFormPagePropertyDetailsUpdate::class, vacantPropertyUrlArguments)

            // Update rent amount
            val expectedRentAmount = "£400"
            assertThat(rentAmountPage.header).containsText("Update how much the weekly rent is for your property")
            rentAmountPage.submitRentAmount("400")
            val checkOccupancyAnswersPage =
                assertPageIs(page, CheckOccupancyAnswersPagePropertyDetailsUpdate::class, vacantPropertyUrlArguments)
            // Check occupancy answers
            assertThat(checkOccupancyAnswersPage.summaryList.occupancyRow).containsText("Yes")
            assertThat(checkOccupancyAnswersPage.summaryList.numberOfHouseholdsRow).containsText(newNumberOfHouseholds.toString())
            assertThat(checkOccupancyAnswersPage.summaryList.numberOfPeopleRow).containsText(newNumberOfPeople.toString())
            assertThat(checkOccupancyAnswersPage.summaryList.numberOfBedroomsRow).containsText(newNumberOfBedrooms.toString())
            assertThat(checkOccupancyAnswersPage.summaryList.rentIncludesBillsRow).containsText("Yes")
            assertThat(checkOccupancyAnswersPage.summaryList.billsIncludedRow).containsText(expectedBillsIncluded)
            assertThat(checkOccupancyAnswersPage.summaryList.furnishedStatusRow).containsText(expectedFurnishedStatus)
            assertThat(checkOccupancyAnswersPage.summaryList.rentFrequencyRow).containsText(expectedRentFrequency)
            assertThat(checkOccupancyAnswersPage.summaryList.rentAmountRow).containsText(expectedRentAmount)
            checkOccupancyAnswersPage.confirm()
            propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, vacantPropertyUrlArguments)

            // Check changes have occurred
            assertThat(propertyDetailsPage.propertyDetailsSummaryList.occupancyRow.value).containsText("Yes")
            assertThat(propertyDetailsPage.propertyDetailsSummaryList.numberOfHouseholdsRow.value)
                .containsText(newNumberOfHouseholds.toString())
            assertThat(propertyDetailsPage.propertyDetailsSummaryList.numberOfPeopleRow.value)
                .containsText(newNumberOfPeople.toString())
            assertThat(propertyDetailsPage.propertyDetailsSummaryList.numberOfBedroomsRow.value)
                .containsText(newNumberOfBedrooms.toString())
            assertThat(propertyDetailsPage.propertyDetailsSummaryList.rentIncludesBillsRow.value).containsText("Yes")
            assertThat(propertyDetailsPage.propertyDetailsSummaryList.billsIncludedRow.value).containsText(expectedBillsIncluded)
            assertThat(propertyDetailsPage.propertyDetailsSummaryList.furnishedStatusRow.value).containsText(expectedFurnishedStatus)
            assertThat(propertyDetailsPage.propertyDetailsSummaryList.rentFrequencyRow.value).containsText(expectedRentFrequency)
            assertThat(propertyDetailsPage.propertyDetailsSummaryList.rentAmountRow.value).containsText(expectedRentAmount)
        }

        // TODO PDJB-105: re-enable and update tests once rent level updates have been added
        @Disabled
        @Test
        fun `A property can have just their number of households and people updated`(page: Page) {
            // Details page
            var propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(occupiedPropertyOwnershipId)
            propertyDetailsPage.propertyDetailsSummaryList.numberOfHouseholdsRow.clickActionLinkAndWait()
            val updateNumberOfHouseholdsPage =
                assertPageIs(page, NumberOfHouseholdsFormPagePropertyDetailsUpdate::class, occupiedPropertyUrlArguments)

            // Update number of households
            val newNumberOfHouseholds = 1
            assertThat(updateNumberOfHouseholdsPage.header).containsText("Update how many households live in your property")
            updateNumberOfHouseholdsPage.submitNumberOfHouseholds(newNumberOfHouseholds)
            val updateNumberOfPeoplePage =
                assertPageIs(page, HouseholdsNumberOfPeopleFormPagePropertyDetailsUpdate::class, occupiedPropertyUrlArguments)

            // Update number of people
            val newNumberOfPeople = 3
            assertThat(updateNumberOfPeoplePage.header).containsText("Update how many people live in your property")
            updateNumberOfPeoplePage.submitNumOfPeople(newNumberOfPeople)
            val checkOccupancyAnswersPage =
                assertPageIs(page, CheckHouseholdsAnswersPagePropertyDetailsUpdate::class, occupiedPropertyUrlArguments)

            // Check occupancy answers
            assertThat(checkOccupancyAnswersPage.summaryList.numberOfHouseholdsRow).containsText(newNumberOfHouseholds.toString())
            assertThat(checkOccupancyAnswersPage.summaryList.numberOfPeopleRow).containsText(newNumberOfPeople.toString())
            checkOccupancyAnswersPage.confirm()
            propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, occupiedPropertyUrlArguments)

            // Check changes have occurred
            assertThat(propertyDetailsPage.propertyDetailsSummaryList.numberOfHouseholdsRow.value)
                .containsText(newNumberOfHouseholds.toString())
            assertThat(propertyDetailsPage.propertyDetailsSummaryList.numberOfPeopleRow.value)
                .containsText(newNumberOfPeople.toString())
        }

        // TODO PDJB-105: re-enable and update tests once rent level updates have been added
        @Disabled
        @Test
        fun `A property can have just their number of people updated`(page: Page) {
            // Details page
            var propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(occupiedPropertyOwnershipId)
            propertyDetailsPage.propertyDetailsSummaryList.numberOfPeopleRow.clickActionLinkAndWait()
            val updateNumberOfPeoplePage =
                assertPageIs(page, NumberOfPeopleFormPagePropertyDetailsUpdate::class, occupiedPropertyUrlArguments)

            // Update number of people
            val newNumberOfPeople = 3
            assertThat(updateNumberOfPeoplePage.header).containsText("Update how many people live in your property")
            updateNumberOfPeoplePage.submitNumOfPeople(newNumberOfPeople)
            val checkOccupancyAnswersPage =
                assertPageIs(page, CheckPeopleAnswersPagePropertyDetailsUpdate::class, occupiedPropertyUrlArguments)

            // Check occupancy answers
            assertThat(checkOccupancyAnswersPage.summaryList.numberOfPeopleRow).containsText(newNumberOfPeople.toString())
            checkOccupancyAnswersPage.confirm()
            propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, occupiedPropertyUrlArguments)

            // Check changes have occurred
            assertThat(propertyDetailsPage.propertyDetailsSummaryList.numberOfPeopleRow.value)
                .containsText(newNumberOfPeople.toString())
        }

        // TODO PDJB-105: check if this is still needed - the state is being cleared
        @Disabled
        @Test
        fun `Submitting an occupancy update clears the journey context for all the occupancy sub-journeys`(page: Page) {
            // Details page - start a people update
            var propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
            propertyDetailsPage.propertyDetailsSummaryList.numberOfPeopleRow.clickActionLinkAndWait()
            var updatePeoplePage = assertPageIs(page, NumberOfPeopleFormPagePropertyDetailsUpdate::class, occupiedPropertyUrlArguments)

            // Update people page - go back to the details page and complete a households update instead
            updatePeoplePage.backLink.clickAndWait()
            propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, occupiedPropertyUrlArguments)

            propertyDetailsPage.propertyDetailsSummaryList.numberOfHouseholdsRow.clickActionLinkAndWait()
            val updateHouseholdsPage =
                assertPageIs(page, NumberOfHouseholdsFormPagePropertyDetailsUpdate::class, occupiedPropertyUrlArguments)

            val newNumberOfHouseholds = "1"
            updateHouseholdsPage.submitNumberOfHouseholds(newNumberOfHouseholds)
            val updateHouseholdsPeoplePage =
                assertPageIs(page, HouseholdsNumberOfPeopleFormPagePropertyDetailsUpdate::class, occupiedPropertyUrlArguments)

            updateHouseholdsPeoplePage.submitNumOfPeople(newNumberOfHouseholds)
            val checkHouseholdsAnswersPage =
                assertPageIs(page, CheckHouseholdsAnswersPagePropertyDetailsUpdate::class, occupiedPropertyUrlArguments)

            checkHouseholdsAnswersPage.form.submit()
            propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, occupiedPropertyUrlArguments)

            // Details page - start another people update
            propertyDetailsPage.propertyDetailsSummaryList.numberOfPeopleRow.clickActionLinkAndWait()
            updatePeoplePage = assertPageIs(page, NumberOfPeopleFormPagePropertyDetailsUpdate::class, occupiedPropertyUrlArguments)

            val newNumberOfPeople = "3"
            updatePeoplePage.submitNumOfPeople(newNumberOfPeople)
            val checkPeopleAnswersPage =
                assertPageIs(page, CheckPeopleAnswersPagePropertyDetailsUpdate::class, occupiedPropertyUrlArguments)

            // Check people answers page - check summary data includes newNumberOfHouseholds then complete update
            assertThat(checkPeopleAnswersPage.summaryList.numberOfHouseholdsRow).containsText(newNumberOfHouseholds)
            assertThat(checkPeopleAnswersPage.summaryList.numberOfPeopleRow).containsText(newNumberOfPeople)
            checkPeopleAnswersPage.form.submit()
            propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, occupiedPropertyUrlArguments)

            assertThat(propertyDetailsPage.propertyDetailsSummaryList.numberOfHouseholdsRow).containsText(newNumberOfHouseholds)
            assertThat(propertyDetailsPage.propertyDetailsSummaryList.numberOfPeopleRow).containsText(newNumberOfPeople)
        }
    }
}
