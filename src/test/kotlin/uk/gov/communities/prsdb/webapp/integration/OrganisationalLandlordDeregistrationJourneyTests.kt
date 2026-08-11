package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.constants.ORGANISATION_LANDLORD_REGISTRATION
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.organisationalLandlordDeregistrationJourneyPages.AreYouSureFormPageOrganisationalLandlordDeregistration
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
        assertPageIs(page, AreYouSureFormPageOrganisationalLandlordDeregistration::class)

        // TODO: PDJB-1482 - Replace with proper page object interaction once the are you sure page is built
        page.locator("button:has-text('Continue')").click()
        page.waitForLoadState()

        // TODO: PDJB-1484 - Assert the "Organisation deleted" confirmation page content and that the dashboard is no longer accessible

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
    }
}
