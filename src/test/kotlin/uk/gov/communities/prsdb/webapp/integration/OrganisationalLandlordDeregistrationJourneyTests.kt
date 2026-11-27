package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.constants.ACTIVE_INACTIVE
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ErrorPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.organisationalLandlordDeregistrationJourneyPages.AreYouSureFormPageOrganisationalLandlordDeregistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.organisationalLandlordDeregistrationJourneyPages.ConfirmationPageOrganisationalLandlordDeregistration
import uk.gov.communities.prsdb.webapp.services.SwapToIndividualNudgeEmailService

// TODO PDJB-2149: Delete this test class along with the landlord deregistration journey when the ACTIVE_INACTIVE flag is removed
@WithOrgLandlordProfile
class OrganisationalLandlordDeregistrationJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    @MockitoBean
    private lateinit var swapToIndividualNudgeEmailService: SwapToIndividualNudgeEmailService

    @BeforeEach
    fun disableActiveInactive() {
        featureFlagManager.disableFeature(ACTIVE_INACTIVE)
    }

    @Test
    fun `an organisational landlord can complete the deregistration journey`(
        page: Page,
        @Autowired jdbcTemplate: JdbcTemplate,
    ) {
        val beforeDeregistrationOrgInvitationCount =
            jdbcTemplate.queryForObject(
                "SELECT count(*) FROM organisational_landlord_invitation WHERE organisation_landlord_id = 36",
                Int::class.java,
            )

        val detailsPage = navigator.goToOrgLandlordDetails()
        detailsPage.deleteOrganisationLink.clickAndWait()
        val areYouSurePage = assertPageIs(page, AreYouSureFormPageOrganisationalLandlordDeregistration::class)

        areYouSurePage.submitYesDelete()

        val confirmationPage = assertPageIs(page, ConfirmationPageOrganisationalLandlordDeregistration::class)

        assertThat(confirmationPage.confirmationBanner.title).containsText("Organisation deleted")

        val landlordCount =
            jdbcTemplate.queryForObject(
                "SELECT count(*) FROM landlord WHERE id = 36",
                Int::class.java,
            )
        val orgLandlordUserCount =
            jdbcTemplate.queryForObject(
                "SELECT count(*) FROM organisational_landlord_user WHERE organisation_landlord_id = 36",
                Int::class.java,
            )
        val afterDeregistrationOrgInvitationCount =
            jdbcTemplate.queryForObject(
                "SELECT count(*) FROM organisational_landlord_invitation WHERE organisation_landlord_id = 36",
                Int::class.java,
            )

        assertEquals(5, beforeDeregistrationOrgInvitationCount)
        assertEquals(0, landlordCount)
        assertEquals(0, orgLandlordUserCount)
        assertEquals(0, afterDeregistrationOrgInvitationCount)

        // Check they can no longer access the landlord dashboard
        navigator.navigate(LANDLORD_DASHBOARD_URL)
        val errorPage = assertPageIs(page, ErrorPage::class)
        assertThat(errorPage.heading).containsText("You do not have permission to access this page")
    }
}
