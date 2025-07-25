package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
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
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.OccupancyFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.OccupancyNumberOfHouseholdsFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.OccupancyNumberOfPeopleFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.OwnershipTypeFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.SelectiveLicenceFormPagePropertyDetailsUpdate
import kotlin.test.assertContains

class PropertyDetailsUpdateJourneyTests : JourneyTestWithSeedData("data-local.sql") {
    private val propertyOwnershipId = 1L
    private val urlArguments = mapOf("propertyOwnershipId" to propertyOwnershipId.toString())

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
            propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingRow.clickActionLinkAndWait()
            val updateLicensingTypePage = assertPageIs(page, LicensingTypeFormPagePropertyDetailsUpdate::class, urlArguments)

            // Update licence to selective
            updateLicensingTypePage.submitLicensingType(LicensingType.SELECTIVE_LICENCE)
            val updateLicenceNumberPage = assertPageIs(page, SelectiveLicenceFormPagePropertyDetailsUpdate::class, urlArguments)

            // Update licence number
            updateLicenceNumberPage.submitLicenseNumber(newLicenceNumber)
            val checkLicensingAnswersPage = assertPageIs(page, CheckLicensingAnswersPagePropertyDetailsUpdate::class, urlArguments)

            // Check licensing answers
            assertContains(checkLicensingAnswersPage.form.summaryName.getText(), "You have updated the property licence")
            assertThat(checkLicensingAnswersPage.form.summaryList.licensingRow.value).containsText("Selective licence")
            assertThat(checkLicensingAnswersPage.form.summaryList.licensingRow.value).containsText(newLicenceNumber)
            checkLicensingAnswersPage.confirm()
            propertyDetailsUpdatePage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)

            // Check changes have occurred
            assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingRow.value).containsText("Selective licence")
            assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingRow.value).containsText(newLicenceNumber)
        }

        @Test
        fun `A property can have its licensing updated to a HMO Mandatory licence`(page: Page) {
            val newLicenceNumber = "MAND123"

            // Details page
            var propertyDetailsUpdatePage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
            propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingRow.clickActionLinkAndWait()
            val updateLicensingTypePage = assertPageIs(page, LicensingTypeFormPagePropertyDetailsUpdate::class, urlArguments)

            // Update licence to HMO mandatory
            updateLicensingTypePage.submitLicensingType(LicensingType.HMO_MANDATORY_LICENCE)
            val updateLicenceNumberPage = assertPageIs(page, HmoMandatoryLicenceFormPagePropertyDetailsUpdate::class, urlArguments)

            // Update licence number
            updateLicenceNumberPage.submitLicenseNumber(newLicenceNumber)
            val checkLicensingAnswersPage = assertPageIs(page, CheckLicensingAnswersPagePropertyDetailsUpdate::class, urlArguments)

            // Check licensing answers
            assertContains(checkLicensingAnswersPage.form.summaryName.getText(), "You have updated the property licence")
            assertThat(checkLicensingAnswersPage.form.summaryList.licensingRow.value).containsText("HMO mandatory licence")
            assertThat(checkLicensingAnswersPage.form.summaryList.licensingRow.value).containsText(newLicenceNumber)
            checkLicensingAnswersPage.confirm()
            propertyDetailsUpdatePage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)

            // Check changes have occurred
            assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingRow.value).containsText("HMO mandatory licence")
            assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingRow.value).containsText(newLicenceNumber)
        }

        @Test
        fun `A property can have its licensing updated to a HMO additional licence`(page: Page) {
            val newLicenceNumber = "ADD123"

            // Details page
            var propertyDetailsUpdatePage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
            propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingRow.clickActionLinkAndWait()
            val updateLicensingTypePage = assertPageIs(page, LicensingTypeFormPagePropertyDetailsUpdate::class, urlArguments)

            // Update licence to HMO additional
            updateLicensingTypePage.submitLicensingType(LicensingType.HMO_ADDITIONAL_LICENCE)
            val updateLicenceNumberPage = assertPageIs(page, HmoAdditionalLicenceFormPagePropertyDetailsUpdate::class, urlArguments)

            // Update licence number
            updateLicenceNumberPage.submitLicenseNumber(newLicenceNumber)
            val checkLicensingAnswersPage = assertPageIs(page, CheckLicensingAnswersPagePropertyDetailsUpdate::class, urlArguments)

            // Check licensing answers
            assertContains(checkLicensingAnswersPage.form.summaryName.getText(), "You have updated the property licence")
            assertThat(checkLicensingAnswersPage.form.summaryList.licensingRow.value).containsText("HMO additional licence")
            assertThat(checkLicensingAnswersPage.form.summaryList.licensingRow.value).containsText(newLicenceNumber)
            checkLicensingAnswersPage.confirm()
            propertyDetailsUpdatePage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)

            // Check changes have occurred
            assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingRow.value).containsText("HMO additional licence")
            assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingRow.value).containsText(newLicenceNumber)
        }

        @Test
        fun `A property can have its licensing removed`(page: Page) {
            // Details page
            var propertyDetailsUpdatePage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
            propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingRow.clickActionLinkAndWait()
            val updateLicensingTypePage = assertPageIs(page, LicensingTypeFormPagePropertyDetailsUpdate::class, urlArguments)

            // Update licence to no licensing
            updateLicensingTypePage.submitLicensingType(LicensingType.NO_LICENSING)
            val checkLicensingAnswersPage = assertPageIs(page, CheckLicensingAnswersPagePropertyDetailsUpdate::class, urlArguments)

            // Check licensing answers
            assertContains(checkLicensingAnswersPage.form.summaryName.getText(), "You have removed this property’s licence")
            assertThat(checkLicensingAnswersPage.form.summaryList.licensingRow.value).containsText("None")
            checkLicensingAnswersPage.confirm()
            propertyDetailsUpdatePage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)

            // Check changes have occurred
            assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingRow.value).containsText("None")
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
            assertThat(updateOccupancyPage.form.fieldsetHeading).containsText("Is your property still occupied by tenants?")
            updateOccupancyPage.submitIsVacant()
            val checkOccupancyAnswersPage =
                assertPageIs(page, CheckOccupancyAnswersPagePropertyDetailsUpdate::class, occupiedPropertyUrlArguments)

            // Check occupancy answers
            assertThat(checkOccupancyAnswersPage.form.summaryList.occupancyRow).containsText("No")
            assertThat(checkOccupancyAnswersPage.form.summaryList.numberOfHouseholdsRow).isHidden()
            assertThat(checkOccupancyAnswersPage.form.summaryList.numberOfPeopleRow).isHidden()
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
            assertThat(updateOccupancyPage.form.fieldsetHeading).containsText("Is your property occupied by tenants?")
            updateOccupancyPage.submitIsOccupied()
            val updateNumberOfHouseholdsPage =
                assertPageIs(page, OccupancyNumberOfHouseholdsFormPagePropertyDetailsUpdate::class, vacantPropertyUrlArguments)

            // Update number of households
            val newNumberOfHouseholds = 1
            assertThat(updateNumberOfHouseholdsPage.form.fieldsetHeading).containsText("How many households live in your property?")
            updateNumberOfHouseholdsPage.submitNumberOfHouseholds(newNumberOfHouseholds)
            val updateNumberOfPeoplePage =
                assertPageIs(page, OccupancyNumberOfPeopleFormPagePropertyDetailsUpdate::class, vacantPropertyUrlArguments)

            // Update number of people
            val newNumberOfPeople = 3
            assertThat(updateNumberOfPeoplePage.form.fieldsetHeading).containsText("How many people live in your property?")
            updateNumberOfPeoplePage.submitNumOfPeople(newNumberOfPeople)
            val checkOccupancyAnswersPage =
                assertPageIs(page, CheckOccupancyAnswersPagePropertyDetailsUpdate::class, vacantPropertyUrlArguments)

            // Check occupancy answers
            assertThat(checkOccupancyAnswersPage.form.summaryList.occupancyRow).containsText("Yes")
            assertThat(checkOccupancyAnswersPage.form.summaryList.numberOfHouseholdsRow).containsText(newNumberOfHouseholds.toString())
            assertThat(checkOccupancyAnswersPage.form.summaryList.numberOfPeopleRow).containsText(newNumberOfPeople.toString())
            checkOccupancyAnswersPage.confirm()
            propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, vacantPropertyUrlArguments)

            // Check changes have occurred
            assertThat(propertyDetailsPage.propertyDetailsSummaryList.occupancyRow.value).containsText("Yes")
            assertThat(propertyDetailsPage.propertyDetailsSummaryList.numberOfHouseholdsRow.value)
                .containsText(newNumberOfHouseholds.toString())
            assertThat(propertyDetailsPage.propertyDetailsSummaryList.numberOfPeopleRow.value)
                .containsText(newNumberOfPeople.toString())
        }

        @Test
        fun `A property can have just their number of households and people updated`(page: Page) {
            // Details page
            var propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(occupiedPropertyOwnershipId)
            propertyDetailsPage.propertyDetailsSummaryList.numberOfHouseholdsRow.clickActionLinkAndWait()
            val updateNumberOfHouseholdsPage =
                assertPageIs(page, NumberOfHouseholdsFormPagePropertyDetailsUpdate::class, occupiedPropertyUrlArguments)

            // Update number of households
            val newNumberOfHouseholds = 1
            assertThat(updateNumberOfHouseholdsPage.form.fieldsetHeading).containsText("Update the number of households in the property")
            updateNumberOfHouseholdsPage.submitNumberOfHouseholds(newNumberOfHouseholds)
            val updateNumberOfPeoplePage =
                assertPageIs(page, HouseholdsNumberOfPeopleFormPagePropertyDetailsUpdate::class, occupiedPropertyUrlArguments)

            // Update number of people
            val newNumberOfPeople = 3
            assertThat(updateNumberOfPeoplePage.form.fieldsetHeading).containsText("Update how many people live in your property")
            updateNumberOfPeoplePage.submitNumOfPeople(newNumberOfPeople)
            val checkOccupancyAnswersPage =
                assertPageIs(page, CheckHouseholdsAnswersPagePropertyDetailsUpdate::class, occupiedPropertyUrlArguments)

            // Check occupancy answers
            assertThat(checkOccupancyAnswersPage.form.summaryList.numberOfHouseholdsRow).containsText(newNumberOfHouseholds.toString())
            assertThat(checkOccupancyAnswersPage.form.summaryList.numberOfPeopleRow).containsText(newNumberOfPeople.toString())
            checkOccupancyAnswersPage.confirm()
            propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, occupiedPropertyUrlArguments)

            // Check changes have occurred
            assertThat(propertyDetailsPage.propertyDetailsSummaryList.numberOfHouseholdsRow.value)
                .containsText(newNumberOfHouseholds.toString())
            assertThat(propertyDetailsPage.propertyDetailsSummaryList.numberOfPeopleRow.value)
                .containsText(newNumberOfPeople.toString())
        }

        @Test
        fun `A property can have just their number of people updated`(page: Page) {
            // Details page
            var propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(occupiedPropertyOwnershipId)
            propertyDetailsPage.propertyDetailsSummaryList.numberOfPeopleRow.clickActionLinkAndWait()
            val updateNumberOfPeoplePage =
                assertPageIs(page, NumberOfPeopleFormPagePropertyDetailsUpdate::class, occupiedPropertyUrlArguments)

            // Update number of people
            val newNumberOfPeople = 3
            assertThat(updateNumberOfPeoplePage.form.fieldsetHeading).containsText("Update how many people live in your property")
            updateNumberOfPeoplePage.submitNumOfPeople(newNumberOfPeople)
            val checkOccupancyAnswersPage =
                assertPageIs(page, CheckPeopleAnswersPagePropertyDetailsUpdate::class, occupiedPropertyUrlArguments)

            // Check occupancy answers
            assertThat(checkOccupancyAnswersPage.form.summaryList.numberOfPeopleRow).containsText(newNumberOfPeople.toString())
            checkOccupancyAnswersPage.confirm()
            propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, occupiedPropertyUrlArguments)

            // Check changes have occurred
            assertThat(propertyDetailsPage.propertyDetailsSummaryList.numberOfPeopleRow.value)
                .containsText(newNumberOfPeople.toString())
        }

        @Test
        fun `Simultaneous updates are isolated`(browserContext: BrowserContext) {
            // Create two pages
            val (page1, navigator1) = createPageAndNavigator(browserContext)
            val (page2, navigator2) = createPageAndNavigator(browserContext)

            // Start updating occupancy to vacant on page1
            val updateOccupancyPage1 = navigator1.goToPropertyDetailsUpdateOccupancy(occupiedPropertyOwnershipId)
            updateOccupancyPage1.submitIsVacant()
            val checkOccupancyAnswersPage1 =
                assertPageIs(page1, CheckOccupancyAnswersPagePropertyDetailsUpdate::class, occupiedPropertyUrlArguments)

            // Simultaneously start updating number of people on page2
            val newNumberOfPeople = "3"
            val updateNumberOfPeoplePage2 = navigator2.goToPropertyDetailsUpdateNumberOfPeoplePage(occupiedPropertyOwnershipId)
            updateNumberOfPeoplePage2.submitNumOfPeople(newNumberOfPeople)
            val checkPeopleAnswersPage2 =
                assertPageIs(page2, CheckPeopleAnswersPagePropertyDetailsUpdate::class, occupiedPropertyUrlArguments)

            assertThat(checkPeopleAnswersPage2.form.summaryList.occupancyRow).containsText("Yes")
            assertThat(checkPeopleAnswersPage2.form.summaryList.numberOfPeopleRow).containsText(newNumberOfPeople)

            // Finish updating occupancy to vacant on page1
            assertThat(checkOccupancyAnswersPage1.form.summaryList.occupancyRow).containsText("No")
            checkOccupancyAnswersPage1.confirm()
            val propertyDetailsPage1 = assertPageIs(page1, PropertyDetailsPageLandlordView::class, occupiedPropertyUrlArguments)

            // Check changes have occurred on page1
            assertThat(propertyDetailsPage1.propertyDetailsSummaryList.occupancyRow.value).containsText("No")
        }

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
            assertThat(checkPeopleAnswersPage.form.summaryList.numberOfHouseholdsRow).containsText(newNumberOfHouseholds)
            assertThat(checkPeopleAnswersPage.form.summaryList.numberOfPeopleRow).containsText(newNumberOfPeople)
            checkPeopleAnswersPage.form.submit()
            propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, occupiedPropertyUrlArguments)

            assertThat(propertyDetailsPage.propertyDetailsSummaryList.numberOfHouseholdsRow).containsText(newNumberOfHouseholds)
            assertThat(propertyDetailsPage.propertyDetailsSummaryList.numberOfPeopleRow).containsText(newNumberOfPeople)
        }
    }
}
