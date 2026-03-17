package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
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
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.BillsIncludedFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.CheckHouseholdsAnswersPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.CheckLicensingAnswersPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.CheckOccupancyAnswersPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.CheckRentFrequencyAndAmountAnswersPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.CheckRentIncludesBillsAnswersPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.FurnishedStatusFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.HmoAdditionalLicenceFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.HmoMandatoryLicenceFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.HouseholdsNumberOfPeopleFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.LicensingTypeFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.NumberOfBedroomsFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.NumberOfHouseholdsFormPagePropertyDetailsUpdate
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
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.RentAmountFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.RentFrequencyFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.RentIncludesBillsFormPagePropertyDetailsUpdate
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

    @Nested
    inner class OwnershipTypeUpdates {
        @Test
        fun `A property can have its ownership type updated`(page: Page) {
            // Details page
            var propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
            propertyDetailsPage.propertyDetailsSummaryList.ownershipTypeRow.clickFirstActionLinkAndWait()
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
            propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingTypeRow.clickFirstActionLinkAndWait()
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
            propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingTypeRow.clickFirstActionLinkAndWait()
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
            propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingTypeRow.clickFirstActionLinkAndWait()
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
            propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingTypeRow.clickFirstActionLinkAndWait()
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
            propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingTypeRow.clickFirstActionLinkAndWait()
            val updateLicensingTypePage = assertPageIs(page, LicensingTypeFormPagePropertyDetailsUpdate::class, urlArguments)

            // Update licence to selective
            updateLicensingTypePage.submitLicensingType(LicensingType.SELECTIVE_LICENCE)
            var updateLicenceNumberPage = assertPageIs(page, SelectiveLicenceFormPagePropertyDetailsUpdate::class, urlArguments)

            // Update licence number
            updateLicenceNumberPage.submitLicenseNumber(firstNewLicenceNumber)
            var checkLicensingAnswersPage = assertPageIs(page, CheckLicensingAnswersPagePropertyDetailsUpdate::class, urlArguments)

            // Click change link for Licensing Number
            checkLicensingAnswersPage.summaryList.licensingNumberRow
                .clickFirstActionLinkAndWait()
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
    inner class TenancyAndRentalInformation {
        private val occupiedPropertyOwnershipId = 1L
        private val occupiedPropertyUrlArguments = mapOf("propertyOwnershipId" to occupiedPropertyOwnershipId.toString())

        private val vacantPropertyOwnershipId = 7L
        private val vacantPropertyUrlArguments = mapOf("propertyOwnershipId" to vacantPropertyOwnershipId.toString())

        @Nested
        inner class OccupancyUpdates {
            @Test
            fun `A property can have its occupancy updated from occupied to vacant`(page: Page) {
                // Details page
                var propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(occupiedPropertyOwnershipId)
                propertyDetailsPage.propertyDetailsSummaryList.occupancyRow.clickFirstActionLinkAndWait()
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
                propertyDetailsPage.propertyDetailsSummaryList.occupancyRow.clickFirstActionLinkAndWait()
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
                val bedroomsPage =
                    assertPageIs(page, OccupancyNumberOfBedroomsFormPagePropertyDetailsUpdate::class, vacantPropertyUrlArguments)

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
                val furnishedPage =
                    assertPageIs(page, OccupancyFurnishedStatusFormPagePropertyDetailsUpdate::class, vacantPropertyUrlArguments)

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
        }

        @Nested
        inner class HouseholdsAndTenantsUpdates {
            @Test
            fun `A property can have just their number of households and people updated`(page: Page) {
                // Details page
                var propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(occupiedPropertyOwnershipId)
                propertyDetailsPage.propertyDetailsSummaryList.numberOfHouseholdsRow.clickFirstActionLinkAndWait()
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
        }

        @Nested
        inner class NumberOfBedroomsUpdates {
            @Test
            fun `A property can have just its number of bedrooms updated`(page: Page) {
                val newNumberOfBedrooms = 4
                // Details page
                var propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(occupiedPropertyOwnershipId)
                // Assert initial number of bedrooms is not 4
                assertThat(propertyDetailsPage.propertyDetailsSummaryList.numberOfBedroomsRow.value)
                    .not().containsText(newNumberOfBedrooms.toString())
                propertyDetailsPage.propertyDetailsSummaryList.numberOfBedroomsRow.clickFirstActionLinkAndWait()
                val updateNumberOfBedroomsPage =
                    assertPageIs(page, NumberOfBedroomsFormPagePropertyDetailsUpdate::class, occupiedPropertyUrlArguments)

                // Update number of bedrooms
                assertThat(updateNumberOfBedroomsPage.header).containsText("Update how many bedrooms are in your property")
                updateNumberOfBedroomsPage.submitNumOfBedrooms(newNumberOfBedrooms)
                propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, occupiedPropertyUrlArguments)

                // Check change has occurred
                assertThat(propertyDetailsPage.propertyDetailsSummaryList.numberOfBedroomsRow.value)
                    .containsText(newNumberOfBedrooms.toString())
            }
        }

        @Nested
        inner class RentIncludesBills {
            @Test
            fun `A property can have its rent includes bills status updated`(page: Page) {
                // Details page
                var propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(occupiedPropertyOwnershipId)
                // Assert initial rent includes bills status is not Yes
                assertThat(propertyDetailsPage.propertyDetailsSummaryList.rentIncludesBillsRow.value)
                    .not().containsText("Yes")
                propertyDetailsPage.propertyDetailsSummaryList.rentIncludesBillsRow.clickFirstActionLinkAndWait()
                val updateRentIncludesBillsPage =
                    assertPageIs(page, RentIncludesBillsFormPagePropertyDetailsUpdate::class, occupiedPropertyUrlArguments)

                // Update rent includes bills to yes
                assertThat(updateRentIncludesBillsPage.form.fieldsetHeading).containsText("Update whether the rent includes bills")
                updateRentIncludesBillsPage.submitIsIncluded()
                val billsIncludedPage =
                    assertPageIs(page, BillsIncludedFormPagePropertyDetailsUpdate::class, occupiedPropertyUrlArguments)

                // Update bills included
                val expectedBillsIncluded = "Gas, Electricity, Water"
                assertThat(billsIncludedPage.form.fieldsetHeading).containsText("Update which of these you include in the rent")
                billsIncludedPage.selectGasElectricityWater()
                billsIncludedPage.form.submit()
                val checkYourAnswersPage =
                    assertPageIs(page, CheckRentIncludesBillsAnswersPagePropertyDetailsUpdate::class, occupiedPropertyUrlArguments)

                // Check answers
                assertThat(checkYourAnswersPage.summaryList.rentIncludesBillsRow).containsText("Yes")
                assertThat(checkYourAnswersPage.summaryList.billsIncludedRow).containsText(expectedBillsIncluded)
                checkYourAnswersPage.confirm()
                propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, occupiedPropertyUrlArguments)

                assertThat(propertyDetailsPage.propertyDetailsSummaryList.rentIncludesBillsRow.value)
                    .containsText("Yes")
                assertThat(propertyDetailsPage.propertyDetailsSummaryList.billsIncludedRow).containsText(expectedBillsIncluded)
            }

            @Test
            fun `Changing the rent includes bills status from the CYA page updates the property with the correct values`(page: Page) {
                // start update journey
                var propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(occupiedPropertyOwnershipId)
                propertyDetailsPage.propertyDetailsSummaryList.rentIncludesBillsRow.clickFirstActionLinkAndWait()
                var updateRentIncludesBillsPage =
                    assertPageIs(page, RentIncludesBillsFormPagePropertyDetailsUpdate::class, occupiedPropertyUrlArguments)
                // Select yes for rent includes bills
                updateRentIncludesBillsPage.submitIsIncluded()
                val billsIncludedPage =
                    assertPageIs(page, BillsIncludedFormPagePropertyDetailsUpdate::class, occupiedPropertyUrlArguments)
                // Select bills included and submit
                billsIncludedPage.selectGasElectricityWater()
                billsIncludedPage.form.submit()
                var checkYourAnswersPage =
                    assertPageIs(page, CheckRentIncludesBillsAnswersPagePropertyDetailsUpdate::class, occupiedPropertyUrlArguments)

                // Change rent includes bills answer to no
                checkYourAnswersPage.summaryList.rentIncludesBillsRow.clickFirstActionLinkAndWait()
                updateRentIncludesBillsPage =
                    assertPageIs(page, RentIncludesBillsFormPagePropertyDetailsUpdate::class, occupiedPropertyUrlArguments)
                updateRentIncludesBillsPage.submitIsNotIncluded()
                checkYourAnswersPage =
                    assertPageIs(page, CheckRentIncludesBillsAnswersPagePropertyDetailsUpdate::class, occupiedPropertyUrlArguments)

                // Confirm answers
                assertThat(checkYourAnswersPage.summaryList.rentIncludesBillsRow).containsText("No")
                assertThat(checkYourAnswersPage.summaryList.billsIncludedRow).isHidden()
                checkYourAnswersPage.confirm()
                propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, occupiedPropertyUrlArguments)

                // Check update is correct
                assertThat(propertyDetailsPage.propertyDetailsSummaryList.rentIncludesBillsRow.value)
                    .containsText("No")
                assertThat(propertyDetailsPage.propertyDetailsSummaryList.billsIncludedRow).isHidden()
            }
        }

        @Nested
        inner class FurnishedStatusUpdates {
            @Test
            fun `A property can have just its furniture status updated`(page: Page) {
                val newFurnishedStatusValue = "Partly furnished"
                // Details page
                var propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(occupiedPropertyOwnershipId)
                // Assert initial furnished status is not FurnishedStatus.PART_FURNISHED
                assertThat(propertyDetailsPage.propertyDetailsSummaryList.furnishedStatusRow.value)
                    .not().containsText(newFurnishedStatusValue)
                propertyDetailsPage.propertyDetailsSummaryList.furnishedStatusRow.clickFirstActionLinkAndWait()
                val updateFurnishedStatusPage =
                    assertPageIs(page, FurnishedStatusFormPagePropertyDetailsUpdate::class, occupiedPropertyUrlArguments)

                // Update furnished status
                val newFurnishedStatus = FurnishedStatus.PART_FURNISHED
                assertThat(updateFurnishedStatusPage.form.fieldsetHeading)
                    .containsText("Update whether the property is furnished, partly furnished or unfurnished")
                updateFurnishedStatusPage.submitFurnishedStatus(newFurnishedStatus)
                propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, occupiedPropertyUrlArguments)

                // Check change has occurred
                assertThat(propertyDetailsPage.propertyDetailsSummaryList.furnishedStatusRow.value)
                    .containsText(newFurnishedStatusValue)
            }
        }

        @Nested
        inner class RentFrequencyAndAmountUpdates {
            @Test
            fun `A property can have its rentFrequency and amount updated`(page: Page) {
                val newRentFrequency = RentFrequency.WEEKLY
                val newRentFrequencyDisplayName = "Weekly"
                val newRentAmount = "200"
                // Details page
                var propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(occupiedPropertyOwnershipId)
                // Assert initial rent frequency is not newRentFrequency
                assertThat(propertyDetailsPage.propertyDetailsSummaryList.rentFrequencyRow.value)
                    .not().containsText(newRentFrequencyDisplayName)
                // Assert initial rent amount is not newRentAmount
                assertThat(propertyDetailsPage.propertyDetailsSummaryList.rentAmountRow.value)
                    .not().containsText(newRentAmount)
                propertyDetailsPage.propertyDetailsSummaryList.rentFrequencyRow.clickFirstActionLinkAndWait()
                val rentFrequencyPage =
                    assertPageIs(page, RentFrequencyFormPagePropertyDetailsUpdate::class, occupiedPropertyUrlArguments)

                // Update rent frequency
                assertThat(rentFrequencyPage.header).containsText("Update when you charge rent")
                rentFrequencyPage.selectRentFrequency(newRentFrequency)
                rentFrequencyPage.form.submit()
                val rentAmountPage = assertPageIs(page, RentAmountFormPagePropertyDetailsUpdate::class, occupiedPropertyUrlArguments)

                // Update rent amount
                assertThat(rentAmountPage.header).containsText("Update how much the weekly rent is for your property")
                rentAmountPage.submitRentAmount(newRentAmount)
                val checkYourAnswersPage =
                    assertPageIs(page, CheckRentFrequencyAndAmountAnswersPagePropertyDetailsUpdate::class, occupiedPropertyUrlArguments)

                // Check answers
                assertThat(checkYourAnswersPage.summaryList.rentFrequencyRow).containsText(newRentFrequencyDisplayName)
                assertThat(checkYourAnswersPage.summaryList.rentAmountRow).containsText(newRentAmount)
                checkYourAnswersPage.confirm()
                propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, occupiedPropertyUrlArguments)

                assertThat(propertyDetailsPage.propertyDetailsSummaryList.rentFrequencyRow).containsText(newRentFrequencyDisplayName)
                assertThat(propertyDetailsPage.propertyDetailsSummaryList.rentAmountRow).containsText(newRentAmount)
            }
        }
    }
}
