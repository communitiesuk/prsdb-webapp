package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.ORGANISATION_LANDLORD_REGISTRATION
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.organisationalLandlordDeregistrationJourneyPages.AreYouSureFormPageOrganisationalLandlordDeregistration

@WithOrgLandlordProfile
class OrganisationalLandlordDeregistrationJourneyTests : IntegrationTestWithImmutableData("data-local.sql") {
    @BeforeEach
    fun enableOrgLandlordFlag() {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)
    }

    @Test
    fun `an organisational landlord can start the deregistration journey from their details page`(page: Page) {
        val detailsPage = navigator.goToOrgLandlordDetails()
        detailsPage.deleteOrganisationLink.clickAndWait()

        val areYouSurePage = assertPageIs(page, AreYouSureFormPageOrganisationalLandlordDeregistration::class)

        areYouSurePage.submitYesDelete()

        // TODO: PDJB-1483 - Assert the organisational landlord and its related records are deleted after submitting
        // TODO: PDJB-1484 - Assert the "Organisation deleted" confirmation page content and that the dashboard is no longer accessible
    }
}
