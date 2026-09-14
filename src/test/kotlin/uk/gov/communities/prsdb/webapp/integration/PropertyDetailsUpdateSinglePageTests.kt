package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.CheckHouseholdsAnswersPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.CheckLicensingAnswersPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.CheckRentFrequencyAndAmountAnswersPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.HouseholdsNumberOfPeopleFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.LicensingTypeFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.NumberOfHouseholdsFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.RentAmountFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.RentFrequencyFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.SelectiveLicenceFormPagePropertyDetailsUpdate

class PropertyDetailsUpdateSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @BeforeEach
    fun enableRestructureAndSkippingFlag() {
        featureFlagManager.enableFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)
    }

    @Test
    fun `The back link on the licensing number page returns to the check licensing answers page when reached from there`(page: Page) {
        val propertyOwnershipId = 7L
        val urlArguments = mapOf("propertyOwnershipId" to propertyOwnershipId.toString())
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.propertyDetailsSummaryList.licensingTypeRow.clickFirstActionLinkAndWait()
        val licensingTypePage = assertPageIs(page, LicensingTypeFormPagePropertyDetailsUpdate::class, urlArguments)
        licensingTypePage.submitLicensingType(LicensingType.SELECTIVE_LICENCE)
        val licenceNumberPage = assertPageIs(page, SelectiveLicenceFormPagePropertyDetailsUpdate::class, urlArguments)
        licenceNumberPage.submitLicenseNumber("SL456")
        val checkAnswersPage = assertPageIs(page, CheckLicensingAnswersPagePropertyDetailsUpdate::class, urlArguments)

        checkAnswersPage.summaryList.licensingNumberRow.clickFirstActionLinkAndWait()
        assertPageIs(page, SelectiveLicenceFormPagePropertyDetailsUpdate::class, urlArguments).backLink.clickAndWait()

        assertPageIs(page, CheckLicensingAnswersPagePropertyDetailsUpdate::class, urlArguments)
    }

    @Test
    fun `The back link on the number of tenants page returns to the check occupancy answers page when reached from there`(page: Page) {
        val propertyOwnershipId = 1L
        val urlArguments = mapOf("propertyOwnershipId" to propertyOwnershipId.toString())
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.propertyDetailsSummaryList.numberOfHouseholdsRow.clickFirstActionLinkAndWait()
        val numberOfHouseholdsPage =
            assertPageIs(page, NumberOfHouseholdsFormPagePropertyDetailsUpdate::class, urlArguments)
        numberOfHouseholdsPage.submitNumberOfHouseholds(1)
        val numberOfPeoplePage =
            assertPageIs(page, HouseholdsNumberOfPeopleFormPagePropertyDetailsUpdate::class, urlArguments)
        numberOfPeoplePage.submitNumOfPeople(3)
        val checkAnswersPage =
            assertPageIs(page, CheckHouseholdsAnswersPagePropertyDetailsUpdate::class, urlArguments)

        checkAnswersPage.summaryList.numberOfPeopleRow.clickFirstActionLinkAndWait()
        assertPageIs(page, HouseholdsNumberOfPeopleFormPagePropertyDetailsUpdate::class, urlArguments).backLink.clickAndWait()

        assertPageIs(page, CheckHouseholdsAnswersPagePropertyDetailsUpdate::class, urlArguments)
    }

    @Test
    fun `The back link on the rent amount page returns to the check rent answers page when reached from there`(page: Page) {
        val propertyOwnershipId = 8L
        val urlArguments = mapOf("propertyOwnershipId" to propertyOwnershipId.toString())
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.propertyDetailsSummaryList.rentFrequencyRow.clickFirstActionLinkAndWait()
        val rentFrequencyPage = assertPageIs(page, RentFrequencyFormPagePropertyDetailsUpdate::class, urlArguments)
        rentFrequencyPage.selectRentFrequency(RentFrequency.MONTHLY)
        rentFrequencyPage.form.submit()
        val rentAmountPage = assertPageIs(page, RentAmountFormPagePropertyDetailsUpdate::class, urlArguments)
        rentAmountPage.submitRentAmount("500")
        val checkAnswersPage =
            assertPageIs(page, CheckRentFrequencyAndAmountAnswersPagePropertyDetailsUpdate::class, urlArguments)

        checkAnswersPage.summaryList.rentAmountRow.clickFirstActionLinkAndWait()
        assertPageIs(page, RentAmountFormPagePropertyDetailsUpdate::class, urlArguments).backLink.clickAndWait()

        assertPageIs(page, CheckRentFrequencyAndAmountAnswersPagePropertyDetailsUpdate::class, urlArguments)
    }
}
