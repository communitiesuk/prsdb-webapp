package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.ORGANISATION_LANDLORD_REGISTRATION
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.OrgLandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.OrgNameFormPageUpdateLandlordDetails

@WithOrgLandlordProfile
class OrganisationLandlordUpdateJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    @BeforeEach
    fun setup() {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)
    }

    @Test
    fun `An organisation landlord can update organisation name from landlord details and return to details page`(page: Page) {
        val orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
        orgLandlordDetailsPage.clickOrganisationNameChangeLinkAndWait()

        val updateOrgNamePage = assertPageIs(page, OrgNameFormPageUpdateLandlordDetails::class)
        updateOrgNamePage.submitName("Updated Organisation Name")

        val updatedDetailsPage = assertPageIs(page, OrgLandlordDetailsPage::class)
        assertThat(updatedDetailsPage.mainContent).containsText("Updated Organisation Name")
    }

    @Test
    fun `Organisation name change link opens the organisation name update page`(page: Page) {
        val orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
        orgLandlordDetailsPage.clickOrganisationNameChangeLinkAndWait()

        assertPageIs(page, OrgNameFormPageUpdateLandlordDetails::class)
    }

    @Test
    fun `Submitting an empty organisation name on update shows a validation error`(page: Page) {
        val orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
        orgLandlordDetailsPage.clickOrganisationNameChangeLinkAndWait()

        val updateOrgNamePage = assertPageIs(page, OrgNameFormPageUpdateLandlordDetails::class)
        updateOrgNamePage.submitName("")
        assertThat(updateOrgNamePage.form.getErrorMessage()).containsText("Enter an organisation name")
    }
}
