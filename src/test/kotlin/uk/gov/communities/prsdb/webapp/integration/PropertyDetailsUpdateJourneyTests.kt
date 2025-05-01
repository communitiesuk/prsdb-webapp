package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.HmoAdditionalLicenceFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.HmoMandatoryLicenceFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.LicensingTypeFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.NumberOfHouseholdsFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.NumberOfPeopleFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.OccupancyFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.OwnershipTypeFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.SelectiveLicenceFormPagePropertyDetailsUpdate

@Sql("/data-local.sql")
// TODO PRSD-1106 - re-enable tests and update them to match new flow
// TODO PRSD-1107 - re-enable tests and update them to match new flow
// TODO PRSD-1108 - re-enable tests and update them to match new flow
// TODO PRSD-1109 - re-enable tests and update them to match new flow
// All above can be removed once all tests are re-enabled and re-written
@Disabled
class PropertyDetailsUpdateJourneyTests : IntegrationTest() {
    private val propertyOwnershipId = 1L
    private val urlArguments = mapOf("propertyOwnershipId" to propertyOwnershipId.toString())

    @Test
    fun `A property's details can all be updated in one session`(page: Page) {
        // Details page
        var propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)

        val newOwnershipType = OwnershipType.LEASEHOLD
        propertyDetailsPage = updateOwnershipTypeAndReturn(propertyDetailsPage, newOwnershipType)

        propertyDetailsPage = updateLicensingTypeToNoneAndReturn(propertyDetailsPage)

        propertyDetailsPage = updateOccupancyToVacantAndReturn(propertyDetailsPage)

        // Check changes have occurred
        assertThat(propertyDetailsPage.propertyDetailsSummaryList.ownershipTypeRow.value).containsText("Leasehold")
        assertThat(propertyDetailsPage.propertyDetailsSummaryList.occupancyRow.value).containsText("No")
        assertThat(propertyDetailsPage.propertyDetailsSummaryList.licensingRow.value).containsText("None")
    }

    @Test
    fun `A property can have just their ownership type updated`(page: Page) {
        // Details page
        var propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)

        val newOwnershipType = OwnershipType.LEASEHOLD
        propertyDetailsPage = updateOwnershipTypeAndReturn(propertyDetailsPage, newOwnershipType)

        // Check changes have occurred
        assertThat(propertyDetailsPage.propertyDetailsSummaryList.ownershipTypeRow.value).containsText("Leasehold")
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

    private fun updateOwnershipTypeAndReturn(
        detailsPage: PropertyDetailsPageLandlordView,
        newOwnershipType: OwnershipType,
    ): PropertyDetailsPageLandlordView {
        val page = detailsPage.page
        detailsPage.propertyDetailsSummaryList.ownershipTypeRow.clickActionLinkAndWait()

        val updateOwnershipTypePage = assertPageIs(page, OwnershipTypeFormPagePropertyDetailsUpdate::class, urlArguments)
        updateOwnershipTypePage.submitOwnershipType(newOwnershipType)

        return assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
    }

    private fun updateLicensingTypeToNoneAndReturn(detailsPage: PropertyDetailsPageLandlordView): PropertyDetailsPageLandlordView {
        val page = detailsPage.page
        detailsPage.propertyDetailsSummaryList.licensingRow.clickActionLinkAndWait()

        val updateLicensingType = assertPageIs(page, LicensingTypeFormPagePropertyDetailsUpdate::class, urlArguments)
        updateLicensingType.submitLicensingType(LicensingType.NO_LICENSING)

        return assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
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
            navigator.skipToPropertyDetailsUpdateNumberOfHouseholdsPage(occupiedPropertyOwnershipId)
            val updateNumberOfHouseholdsPage =
                assertPageIs(page, NumberOfHouseholdsFormPagePropertyDetailsUpdate::class, occupiedPropertyUrlArguments)
            assertThat(updateNumberOfHouseholdsPage.form.fieldsetHeading).containsText("Update the number of households in the property")

            navigator.skipToPropertyDetailsUpdateNumberOfPeoplePage(occupiedPropertyOwnershipId)
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
            navigator.skipToPropertyDetailsUpdateNumberOfHouseholdsPage(occupiedPropertyOwnershipId)
            assertPageIs(page, PropertyDetailsPageLandlordView::class, occupiedPropertyUrlArguments)

            navigator.skipToPropertyDetailsUpdateNumberOfPeoplePage(occupiedPropertyOwnershipId)
            propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, occupiedPropertyUrlArguments)

            // Check changes have occurred
            assertThat(propertyDetailsPage.propertyDetailsSummaryList.occupancyRow.value).containsText("No")
        }

        @Test
        fun `Step access and fieldset headings work correctly when a property is updated from vacant to occupied`(page: Page) {
            // Check number of households/people pages can't be reached
            navigator.skipToPropertyDetailsUpdateNumberOfHouseholdsPage(vacantPropertyOwnershipId)
            assertPageIs(page, PropertyDetailsPageLandlordView::class, vacantPropertyUrlArguments)

            navigator.skipToPropertyDetailsUpdateNumberOfPeoplePage(vacantPropertyOwnershipId)
            var propertyDetailsUpdatePage = assertPageIs(page, PropertyDetailsPageLandlordView::class, vacantPropertyUrlArguments)

            // Update occupancy to occupied
            propertyDetailsUpdatePage.propertyDetailsSummaryList.occupancyRow.clickActionLinkAndWait()
            val updateOccupancyPage = assertPageIs(page, OccupancyFormPagePropertyDetailsUpdate::class, vacantPropertyUrlArguments)
            assertThat(updateOccupancyPage.form.fieldsetHeading).containsText("Is your property occupied by tenants?")
            updateOccupancyPage.submitIsOccupied()
            assertPageIs(page, NumberOfHouseholdsFormPagePropertyDetailsUpdate::class, vacantPropertyUrlArguments)

            // Check number of people page can't be reached
            navigator.skipToPropertyDetailsUpdateNumberOfPeoplePage(vacantPropertyOwnershipId)
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
    }

    @Nested
    inner class LicenceUpdates {
        @Test
        fun `A property can have their licence type and number updated for selective licence`(page: Page) {
            val newLicensingType = LicensingType.SELECTIVE_LICENCE
            val newLicenceNumber = "SL123"

            // Details page
            var propertyDetailsUpdatePage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)

            propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingRow.clickActionLinkAndWait()

            // Update licence to selective
            val updateLicensingTypePage = assertPageIs(page, LicensingTypeFormPagePropertyDetailsUpdate::class, urlArguments)
            assertThat(updateLicensingTypePage.form.fieldsetHeading).containsText("Update the type of licensing you have for your property")
            updateLicensingTypePage.submitLicensingType(newLicensingType)

            // Update licence number
            val updateLicenceNumberPage = assertPageIs(page, SelectiveLicenceFormPagePropertyDetailsUpdate::class, urlArguments)
            assertThat(updateLicenceNumberPage.form.fieldsetHeading).containsText("What is your selective licence number?")
            updateLicenceNumberPage.submitLicenseNumber(newLicenceNumber)

            propertyDetailsUpdatePage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)

            // Check changes have occurred
            assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingRow.value).containsText(
                "Selective licence",
            )
            assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingRow.value).containsText(
                newLicenceNumber,
            )
        }

        @Test
        fun `A property can have their licence type and number updated for HMO Mandatory licence`(page: Page) {
            val newLicensingType = LicensingType.HMO_MANDATORY_LICENCE
            val newLicenceNumber = "MAND123"

            // Details page
            var propertyDetailsUpdatePage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)

            propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingRow.clickActionLinkAndWait()

            // Update licence to hmp mandatory
            val updateLicensingTypePage = assertPageIs(page, LicensingTypeFormPagePropertyDetailsUpdate::class, urlArguments)
            assertThat(updateLicensingTypePage.form.fieldsetHeading).containsText("Update the type of licensing you have for your property")
            updateLicensingTypePage.submitLicensingType(newLicensingType)

            val updateLicenceNumberPage = assertPageIs(page, HmoMandatoryLicenceFormPagePropertyDetailsUpdate::class, urlArguments)
            assertThat(updateLicenceNumberPage.form.fieldsetHeading).containsText("What is your HMO mandatory licence number?")
            updateLicenceNumberPage.submitLicenseNumber(newLicenceNumber)

            propertyDetailsUpdatePage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)

            // Check changes have occurred
            assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingRow.value).containsText(
                "HMO mandatory licence",
            )
            assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingRow.value).containsText(
                newLicenceNumber,
            )
        }

        @Test
        fun `A property can have their licence type and number updated for HMO additional licence`(page: Page) {
            val newLicensingType = LicensingType.HMO_ADDITIONAL_LICENCE
            val newLicenceNumber = "ADD123"

            // Details page
            var propertyDetailsUpdatePage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)

            propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingRow.clickActionLinkAndWait()

            // Update licence to selective
            val updateLicensingTypePage = assertPageIs(page, LicensingTypeFormPagePropertyDetailsUpdate::class, urlArguments)
            assertThat(updateLicensingTypePage.form.fieldsetHeading).containsText("Update the type of licensing you have for your property")
            updateLicensingTypePage.submitLicensingType(newLicensingType)

            val updateLicenceNumberPage = assertPageIs(page, HmoAdditionalLicenceFormPagePropertyDetailsUpdate::class, urlArguments)
            assertThat(updateLicenceNumberPage.form.fieldsetHeading).containsText("What is your HMO additional licence number?")
            updateLicenceNumberPage.submitLicenseNumber(newLicenceNumber)

            propertyDetailsUpdatePage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)

            // Check changes have occurred
            assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingRow.value).containsText(
                "HMO additional licence",
            )
            assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingRow.value).containsText(
                newLicenceNumber,
            )
        }
    }
}
