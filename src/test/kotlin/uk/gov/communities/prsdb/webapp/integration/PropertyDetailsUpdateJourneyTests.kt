package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.CheckLicensingAnswersPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.HmoAdditionalLicenceFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.HmoMandatoryLicenceFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.LicensingTypeFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.NumberOfHouseholdsFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.NumberOfPeopleFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.OccupancyFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.OwnershipTypeFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.SelectiveLicenceFormPagePropertyDetailsUpdate
import kotlin.test.assertContains

class PropertyDetailsUpdateJourneyTests : JourneyTestWithSeedData("data-local.sql") {
    private val propertyOwnershipId = 1L
    private val urlArguments = mapOf("propertyOwnershipId" to propertyOwnershipId.toString())

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

    // TODO PRSD-1109 - re-enable tests and update them to match new flow
    @Disabled
    @Nested
    inner class OccupancyUpdates {
        private val occupiedPropertyOwnershipId = 1L
        private val occupiedPropertyUrlArguments = mapOf("propertyOwnershipId" to occupiedPropertyOwnershipId.toString())

        private val vacantPropertyOwnershipId = 7L
        private val vacantPropertyUrlArguments = mapOf("propertyOwnershipId" to vacantPropertyOwnershipId.toString())

        @Test
        fun `Step access and fieldset headings work correctly when a property is updated from occupied to vacant`(page: Page) {
            // Details page
            var propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(occupiedPropertyOwnershipId)

            // Check number of households/people pages can be reached
            navigator.navigateToPropertyDetailsUpdateNumberOfHouseholdsPage(occupiedPropertyOwnershipId)
            val updateNumberOfHouseholdsPage =
                assertPageIs(page, NumberOfHouseholdsFormPagePropertyDetailsUpdate::class, occupiedPropertyUrlArguments)
            assertThat(updateNumberOfHouseholdsPage.form.fieldsetHeading).containsText("Update the number of households in the property")

            navigator.navigateToPropertyDetailsUpdateNumberOfPeoplePage(occupiedPropertyOwnershipId)
            val updateNumberOfPeoplePage =
                assertPageIs(page, NumberOfPeopleFormPagePropertyDetailsUpdate::class, occupiedPropertyUrlArguments)
            assertThat(updateNumberOfPeoplePage.form.fieldsetHeading).containsText("Update how many people live in your property")

            // Update occupancy to vacant
            propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(occupiedPropertyOwnershipId)
            propertyDetailsPage.propertyDetailsSummaryList.occupancyRow.clickActionLinkAndWait()
            val updateOccupancyPage = assertPageIs(page, OccupancyFormPagePropertyDetailsUpdate::class, occupiedPropertyUrlArguments)
            assertThat(updateOccupancyPage.form.fieldsetHeading).containsText("Is your property still occupied by tenants?")
            updateOccupancyPage.submitIsVacant()
            assertPageIs(page, PropertyDetailsPageLandlordView::class, occupiedPropertyUrlArguments)

            // Check number of households/people pages can't be reached
            navigator.navigateToPropertyDetailsUpdateNumberOfHouseholdsPage(occupiedPropertyOwnershipId)
            assertPageIs(page, PropertyDetailsPageLandlordView::class, occupiedPropertyUrlArguments)

            navigator.navigateToPropertyDetailsUpdateNumberOfPeoplePage(occupiedPropertyOwnershipId)
            propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, occupiedPropertyUrlArguments)

            // Check changes have occurred
            assertThat(propertyDetailsPage.propertyDetailsSummaryList.occupancyRow.value).containsText("No")
        }

        @Test
        fun `Step access and fieldset headings work correctly when a property is updated from vacant to occupied`(page: Page) {
            // Check number of households/people pages can't be reached
            navigator.navigateToPropertyDetailsUpdateNumberOfHouseholdsPage(vacantPropertyOwnershipId)
            assertPageIs(page, PropertyDetailsPageLandlordView::class, vacantPropertyUrlArguments)

            navigator.navigateToPropertyDetailsUpdateNumberOfPeoplePage(vacantPropertyOwnershipId)
            var propertyDetailsUpdatePage = assertPageIs(page, PropertyDetailsPageLandlordView::class, vacantPropertyUrlArguments)

            // Update occupancy to occupied
            propertyDetailsUpdatePage.propertyDetailsSummaryList.occupancyRow.clickActionLinkAndWait()
            val updateOccupancyPage = assertPageIs(page, OccupancyFormPagePropertyDetailsUpdate::class, vacantPropertyUrlArguments)
            assertThat(updateOccupancyPage.form.fieldsetHeading).containsText("Is your property occupied by tenants?")
            updateOccupancyPage.submitIsOccupied()
            assertPageIs(page, NumberOfHouseholdsFormPagePropertyDetailsUpdate::class, vacantPropertyUrlArguments)

            // Check number of people page can't be reached
            navigator.navigateToPropertyDetailsUpdateNumberOfPeoplePage(vacantPropertyOwnershipId)
            val updateNumberOfHouseholdsPage =
                assertPageIs(page, NumberOfHouseholdsFormPagePropertyDetailsUpdate::class, vacantPropertyUrlArguments)

            // Update number of households/people
            val newNumberOfHouseholds = 1
            val newNumberOfPeople = 3
            assertThat(updateNumberOfHouseholdsPage.form.fieldsetHeading).containsText("How many households live in your property?")
            updateNumberOfHouseholdsPage.submitNumberOfHouseholds(newNumberOfHouseholds)
            val updateNumberOfPeoplePage =
                assertPageIs(page, NumberOfPeopleFormPagePropertyDetailsUpdate::class, vacantPropertyUrlArguments)
            assertThat(updateNumberOfPeoplePage.form.fieldsetHeading).containsText("How many people live in your property?")
            updateNumberOfPeoplePage.submitNumOfPeople(newNumberOfPeople)

            propertyDetailsUpdatePage = assertPageIs(page, PropertyDetailsPageLandlordView::class, vacantPropertyUrlArguments)

            // Check changes have occurred
            assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.occupancyRow.value).containsText("Yes")
            assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.numberOfHouseholdsRow.value).containsText(
                newNumberOfHouseholds.toString(),
            )
            assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.numberOfPeopleRow.value).containsText(
                newNumberOfPeople.toString(),
            )
        }

        @Test
        fun `A property can have just their number of households and people updated`(page: Page) {
            // Details page
            var propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)

            val newNumberOfHouseholds = 3
            val newNumberOfPeople = 5
            propertyDetailsPage =
                updateNumberOfHouseholdsAndPeopleAndReturn(propertyDetailsPage, newNumberOfHouseholds, newNumberOfPeople)

            // Check changes have occurred
            assertThat(propertyDetailsPage.propertyDetailsSummaryList.numberOfHouseholdsRow.value).containsText(
                newNumberOfHouseholds.toString(),
            )
            assertThat(propertyDetailsPage.propertyDetailsSummaryList.numberOfPeopleRow.value).containsText(
                newNumberOfPeople.toString(),
            )
        }

        // TODO PRSD-1109 - add test for updating just number of people
    }

    private fun updateOccupancyToVacantAndReturn(detailsPage: PropertyDetailsPageLandlordView): PropertyDetailsPageLandlordView {
        val page = detailsPage.page
        detailsPage.propertyDetailsSummaryList.occupancyRow.clickActionLinkAndWait()

        val updateOccupancyPage = assertPageIs(page, OccupancyFormPagePropertyDetailsUpdate::class, urlArguments)
        updateOccupancyPage.submitIsVacant()

        return assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
    }

    private fun updateNumberOfHouseholdsAndPeopleAndReturn(
        detailsPage: PropertyDetailsPageLandlordView,
        newNumberOfHouseholds: Int,
        newNumberOfPeople: Int,
    ): PropertyDetailsPageLandlordView {
        val page = detailsPage.page
        detailsPage.propertyDetailsSummaryList.numberOfHouseholdsRow.clickActionLinkAndWait()

        val updateNumberOfHouseholdsPage = assertPageIs(page, NumberOfHouseholdsFormPagePropertyDetailsUpdate::class, urlArguments)
        updateNumberOfHouseholdsPage.submitNumberOfHouseholds(newNumberOfHouseholds)

        val updateNumberOfPeoplePage = assertPageIs(page, NumberOfPeopleFormPagePropertyDetailsUpdate::class, urlArguments)
        updateNumberOfPeoplePage.submitNumOfPeople(newNumberOfPeople)

        return assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
    }
}
