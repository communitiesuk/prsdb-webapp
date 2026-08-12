package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.constants.ORGANISATION_LANDLORD_REGISTRATION
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.organisationalLandlordDeregistrationJourneyPages.AreYouSureFormPageOrganisationalLandlordDeregistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.organisationalLandlordDeregistrationJourneyPages.ConfirmationPageOrganisationalLandlordDeregistration
import uk.gov.communities.prsdb.webapp.services.SwapToIndividualNudgeEmailService

@WithOrgLandlordProfile
class OrganisationalLandlordDeregistrationJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    @MockitoBean
    private lateinit var swapToIndividualNudgeEmailService: SwapToIndividualNudgeEmailService

    @BeforeEach
    fun enableOrgLandlordFlag() {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)
    }

    @Test
    fun `an organisational landlord can complete the deregistration journey`(
        page: Page,
        @Autowired jdbcTemplate: JdbcTemplate,
    ) {
        val detailsPage = navigator.goToOrgLandlordDetails()
        detailsPage.deleteOrganisationLink.clickAndWait()
        val areYouSurePage = assertPageIs(page, AreYouSureFormPageOrganisationalLandlordDeregistration::class)

        areYouSurePage.submitYesDelete()

        val confirmationPage = assertPageIs(page, ConfirmationPageOrganisationalLandlordDeregistration::class)

        assertThat(confirmationPage.confirmationBanner.title).containsText("Organisation deleted")
        assertThat(confirmationPage.whatHappensNextHeading).containsText("What happens next")
        assertThat(confirmationPage.bodyParagraphs.first())
            .containsText("We have sent a confirmation email to all team members.")
        assertThat(confirmationPage.bodyParagraphs.nth(1))
            .containsText("Local Organisation Landlord is no longer registered as a landlord.")
        assertThat(confirmationPage.bodyParagraphs.nth(2))
            .containsText("Your organisation and property information has been deleted.")
        assertThat(confirmationPage.bodyParagraphs.nth(3))
            .containsText("You can no longer access the service on behalf of your organisation.")

        // The user loses access to the service, so the page deliberately has no dashboard link
        assertThat(confirmationPage.links).hasCount(0)

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

        assertEquals(0, landlordCount)
        assertEquals(0, orgLandlordUserCount)

        // Check they can no longer access the landlord dashboard
        val landlordDashboard = navigator.goToLandlordDashboard()
        assertTrue(landlordDashboard.page.content().contains("You do not have permission to access this page"))
    }
}
