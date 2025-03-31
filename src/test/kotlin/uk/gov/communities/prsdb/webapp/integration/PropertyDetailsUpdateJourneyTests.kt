package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.HmoAdditionalLicenceFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.HmoMandatoryLicenceFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.LicensingTypeFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.NumberOfHouseholdsFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.NumberOfPeopleFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.OccupancyFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.OwnershipTypeFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.PropertyDetailsUpdatePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.SelectiveLicenceFormPagePropertyDetailsUpdate

@Sql("/data-local.sql")
class PropertyDetailsUpdateJourneyTests : IntegrationTest() {
    private val propertyOwnershipId = 1L
    private val urlArguments = mapOf("propertyOwnershipId" to propertyOwnershipId.toString())

    @Test
    fun `A property's details can all be updated in one session`(page: Page) {
        // Update details page
        var propertyDetailsUpdatePage = navigator.goToPropertyDetailsUpdatePage(propertyOwnershipId)
        assertThat(propertyDetailsUpdatePage.heading).containsText("1, Example Road, EG")

        val newOwnershipType = OwnershipType.LEASEHOLD
        propertyDetailsUpdatePage = updateOwnershipTypeAndReturn(propertyDetailsUpdatePage, newOwnershipType)

        propertyDetailsUpdatePage = updateLicensingTypeToNoneAndReturn(propertyDetailsUpdatePage)

        propertyDetailsUpdatePage = updateOccupancyToVacantAndReturn(propertyDetailsUpdatePage)

        // Submit changes TODO PRSD-355 add proper submit button and declaration page
        propertyDetailsUpdatePage.submitButton.clickAndWait()
        propertyDetailsUpdatePage = assertPageIs(page, PropertyDetailsUpdatePage::class, urlArguments)

        // Check changes have occurred
        assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.ownershipTypeRow.value).containsText("Leasehold")
        assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.occupancyRow.value).containsText("No")
        assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingRow.value).containsText("None")
    }

    @Test
    fun `A property can have just their ownership type updated`(page: Page) {
        // Update details page
        var propertyDetailsUpdatePage = navigator.goToPropertyDetailsUpdatePage(propertyOwnershipId)
        assertThat(propertyDetailsUpdatePage.heading).containsText("1, Example Road, EG")

        val newOwnershipType = OwnershipType.LEASEHOLD
        propertyDetailsUpdatePage = updateOwnershipTypeAndReturn(propertyDetailsUpdatePage, newOwnershipType)

        // Submit changes TODO PRSD-355 add proper submit button and declaration page
        propertyDetailsUpdatePage.submitButton.clickAndWait()
        propertyDetailsUpdatePage = assertPageIs(page, PropertyDetailsUpdatePage::class, urlArguments)

        // Check changes have occurred
        assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.ownershipTypeRow.value).containsText("Leasehold")
    }

    @Test
    fun `A property can have just their number of households and people updated`(page: Page) {
        // Update details page
        var propertyDetailsUpdatePage = navigator.goToPropertyDetailsUpdatePage(propertyOwnershipId)
        assertThat(propertyDetailsUpdatePage.heading).containsText("1, Example Road, EG")

        val newNumberOfHouseholds = 3
        val newNumberOfPeople = 5
        propertyDetailsUpdatePage =
            updateNumberOfHouseholdsAndPeopleAndReturn(propertyDetailsUpdatePage, newNumberOfHouseholds, newNumberOfPeople)

        // Submit changes TODO PRSD-355 add proper submit button and declaration page
        propertyDetailsUpdatePage.submitButton.clickAndWait()
        propertyDetailsUpdatePage = assertPageIs(page, PropertyDetailsUpdatePage::class, urlArguments)

        // Check changes have occurred
        assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.numberOfHouseholdsRow.value).containsText(
            newNumberOfHouseholds.toString(),
        )
        assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.numberOfPeopleRow.value).containsText(
            newNumberOfPeople.toString(),
        )
    }

    private fun updateOwnershipTypeAndReturn(
        detailsPage: PropertyDetailsUpdatePage,
        newOwnershipType: OwnershipType,
    ): PropertyDetailsUpdatePage {
        val page = detailsPage.page
        detailsPage.propertyDetailsSummaryList.ownershipTypeRow.clickActionLinkAndWait()

        val updateOwnershipTypePage = assertPageIs(page, OwnershipTypeFormPagePropertyDetailsUpdate::class, urlArguments)
        updateOwnershipTypePage.submitOwnershipType(newOwnershipType)

        return assertPageIs(page, PropertyDetailsUpdatePage::class, urlArguments)
    }

    private fun updateLicensingTypeToNoneAndReturn(detailsPage: PropertyDetailsUpdatePage): PropertyDetailsUpdatePage {
        val page = detailsPage.page
        detailsPage.propertyDetailsSummaryList.licensingRow.clickActionLinkAndWait()

        val updateLicensingType = assertPageIs(page, LicensingTypeFormPagePropertyDetailsUpdate::class, urlArguments)
        updateLicensingType.submitLicensingType(LicensingType.NO_LICENSING)

        return assertPageIs(page, PropertyDetailsUpdatePage::class, urlArguments)
    }

    private fun updateOccupancyToVacantAndReturn(detailsPage: PropertyDetailsUpdatePage): PropertyDetailsUpdatePage {
        val page = detailsPage.page
        detailsPage.propertyDetailsSummaryList.occupancyRow.clickActionLinkAndWait()

        val updateOccupancyPage = assertPageIs(page, OccupancyFormPagePropertyDetailsUpdate::class, urlArguments)
        updateOccupancyPage.submitIsVacant()

        return assertPageIs(page, PropertyDetailsUpdatePage::class, urlArguments)
    }

    private fun updateNumberOfHouseholdsAndPeopleAndReturn(
        detailsPage: PropertyDetailsUpdatePage,
        newNumberOfHouseholds: Int,
        newNumberOfPeople: Int,
    ): PropertyDetailsUpdatePage {
        val page = detailsPage.page
        detailsPage.propertyDetailsSummaryList.numberOfHouseholdsRow.clickActionLinkAndWait()

        val updateNumberOfHouseholdsPage = assertPageIs(page, NumberOfHouseholdsFormPagePropertyDetailsUpdate::class, urlArguments)
        updateNumberOfHouseholdsPage.submitNumberOfHouseholds(newNumberOfHouseholds)

        val updateNumberOfPeoplePage = assertPageIs(page, NumberOfPeopleFormPagePropertyDetailsUpdate::class, urlArguments)
        updateNumberOfPeoplePage.submitNumOfPeople(newNumberOfPeople)

        return assertPageIs(page, PropertyDetailsUpdatePage::class, urlArguments)
    }

    @Nested
    inner class OccupancyUpdates {
        private val occupiedPropertyOwnershipId = 1L
        private val occupiedPropertyUrlArguments = mapOf("propertyOwnershipId" to occupiedPropertyOwnershipId.toString())

        private val vacantPropertyOwnershipId = 7L
        private val vacantPropertyUrlArguments = mapOf("propertyOwnershipId" to vacantPropertyOwnershipId.toString())

        @Test
        fun `Step access and fieldset headings work correctly when a property is updated from occupied to vacant`(page: Page) {
            // Update details page
            var propertyDetailsUpdatePage = navigator.goToPropertyDetailsUpdatePage(occupiedPropertyOwnershipId)
            assertThat(propertyDetailsUpdatePage.heading).containsText("1, Example Road, EG")

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
            propertyDetailsUpdatePage = navigator.goToPropertyDetailsUpdatePage(occupiedPropertyOwnershipId)
            propertyDetailsUpdatePage.propertyDetailsSummaryList.occupancyRow.clickActionLinkAndWait()
            val updateOccupancyPage = assertPageIs(page, OccupancyFormPagePropertyDetailsUpdate::class, occupiedPropertyUrlArguments)
            assertThat(updateOccupancyPage.form.fieldsetHeading).containsText("Is your property still occupied by tenants?")
            updateOccupancyPage.submitIsVacant()
            assertPageIs(page, PropertyDetailsUpdatePage::class, occupiedPropertyUrlArguments)

            // Check number of households/people pages can't be reached
            navigator.skipToPropertyDetailsUpdateNumberOfHouseholdsPage(occupiedPropertyOwnershipId)
            assertPageIs(page, PropertyDetailsUpdatePage::class, occupiedPropertyUrlArguments)

            navigator.skipToPropertyDetailsUpdateNumberOfPeoplePage(occupiedPropertyOwnershipId)
            propertyDetailsUpdatePage = assertPageIs(page, PropertyDetailsUpdatePage::class, occupiedPropertyUrlArguments)

            // Submit changes TODO PRSD-355 add proper submit button and declaration page
            propertyDetailsUpdatePage.submitButton.clickAndWait()
            propertyDetailsUpdatePage = assertPageIs(page, PropertyDetailsUpdatePage::class, occupiedPropertyUrlArguments)

            // Check changes have occurred
            assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.occupancyRow.value).containsText("No")
        }

        @Test
        fun `Step access and fieldset headings work correctly when a property is updated from vacant to occupied`(page: Page) {
            // Update details page
            var propertyDetailsUpdatePage = navigator.goToPropertyDetailsUpdatePage(vacantPropertyOwnershipId)
            assertThat(propertyDetailsUpdatePage.heading).containsText("6 Mythical Place")

            // Check number of households/people pages can't be reached
            navigator.skipToPropertyDetailsUpdateNumberOfHouseholdsPage(vacantPropertyOwnershipId)
            assertPageIs(page, PropertyDetailsUpdatePage::class, vacantPropertyUrlArguments)

            navigator.skipToPropertyDetailsUpdateNumberOfPeoplePage(vacantPropertyOwnershipId)
            propertyDetailsUpdatePage = assertPageIs(page, PropertyDetailsUpdatePage::class, vacantPropertyUrlArguments)

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

            // Submit changes TODO PRSD-355 add proper submit button and declaration page
            propertyDetailsUpdatePage.submitButton.clickAndWait()
            propertyDetailsUpdatePage = assertPageIs(page, PropertyDetailsUpdatePage::class, vacantPropertyUrlArguments)

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

            // Update details page
            var propertyDetailsUpdatePage = navigator.goToPropertyDetailsUpdatePage(propertyOwnershipId)
            assertThat(propertyDetailsUpdatePage.heading).containsText("1, Example Road, EG")

            propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingRow.clickActionLinkAndWait()

            // Update licence to selective
            val updateLicensingTypePage = assertPageIs(page, LicensingTypeFormPagePropertyDetailsUpdate::class, urlArguments)
            assertThat(updateLicensingTypePage.form.fieldsetHeading).containsText("Update the type of licensing you have for your property")
            updateLicensingTypePage.submitLicensingType(newLicensingType)

            // Update licence number
            val updateLicenceNumberPage = assertPageIs(page, SelectiveLicenceFormPagePropertyDetailsUpdate::class, urlArguments)
            assertThat(updateLicenceNumberPage.form.fieldsetHeading).containsText("What is your selective licence number?")
            updateLicenceNumberPage.submitLicenseNumber(newLicenceNumber)

            // Submit changes TODO PRSD-355 add proper submit button and declaration page
            propertyDetailsUpdatePage.submitButton.clickAndWait()
            propertyDetailsUpdatePage = assertPageIs(page, PropertyDetailsUpdatePage::class, urlArguments)

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

            // Update details page
            var propertyDetailsUpdatePage = navigator.goToPropertyDetailsUpdatePage(propertyOwnershipId)
            assertThat(propertyDetailsUpdatePage.heading).containsText("1, Example Road, EG")

            propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingRow.clickActionLinkAndWait()

            // Update licence to hmp mandatory
            val updateLicensingTypePage = assertPageIs(page, LicensingTypeFormPagePropertyDetailsUpdate::class, urlArguments)
            assertThat(updateLicensingTypePage.form.fieldsetHeading).containsText("Update the type of licensing you have for your property")
            updateLicensingTypePage.submitLicensingType(newLicensingType)

            val updateLicenceNumberPage = assertPageIs(page, HmoMandatoryLicenceFormPagePropertyDetailsUpdate::class, urlArguments)
            assertThat(updateLicenceNumberPage.form.fieldsetHeading).containsText("What is your HMO mandatory licence number?")
            updateLicenceNumberPage.submitLicenseNumber(newLicenceNumber)

            // Submit changes TODO PRSD-355 add proper submit button and declaration page
            propertyDetailsUpdatePage.submitButton.clickAndWait()
            propertyDetailsUpdatePage = assertPageIs(page, PropertyDetailsUpdatePage::class, urlArguments)

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

            // Update details page
            var propertyDetailsUpdatePage = navigator.goToPropertyDetailsUpdatePage(propertyOwnershipId)
            assertThat(propertyDetailsUpdatePage.heading).containsText("1, Example Road, EG")

            propertyDetailsUpdatePage.propertyDetailsSummaryList.licensingRow.clickActionLinkAndWait()

            // Update licence to selective
            val updateLicensingTypePage = assertPageIs(page, LicensingTypeFormPagePropertyDetailsUpdate::class, urlArguments)
            assertThat(updateLicensingTypePage.form.fieldsetHeading).containsText("Update the type of licensing you have for your property")
            updateLicensingTypePage.submitLicensingType(newLicensingType)

            val updateLicenceNumberPage = assertPageIs(page, HmoAdditionalLicenceFormPagePropertyDetailsUpdate::class, urlArguments)
            assertThat(updateLicenceNumberPage.form.fieldsetHeading).containsText("What is your HMO additional licence number?")
            updateLicenceNumberPage.submitLicenseNumber(newLicenceNumber)

            // Submit changes TODO PRSD-355 add proper submit button and declaration page
            propertyDetailsUpdatePage.submitButton.clickAndWait()
            propertyDetailsUpdatePage = assertPageIs(page, PropertyDetailsUpdatePage::class, urlArguments)

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
