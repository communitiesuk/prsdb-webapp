package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.context.TestPropertySource
import uk.gov.communities.prsdb.webapp.constants.MULTI_USER_ORGANISATIONS
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrganisationalLandlordUserInvitationController.Companion.ACCEPT_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.TeamMembersController.Companion.TEAM_MEMBERS_ROUTE
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord
import uk.gov.communities.prsdb.webapp.database.entity.PrsdbUser
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordUserRepository
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.CheckAnswersPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.ConfirmationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.EmailAddressPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.FullNamePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.InvalidLinkPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.JoinOrganisationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.ValidateTokenPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps.CheckAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps.TokenValidity
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordUserService
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@TestPropertySource(properties = ["local.one-login-user-id=urn:fdc:gov.uk:2022:INVITEE"])
class AcceptOrganisationInvitationJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    @Autowired
    private lateinit var invitationRepository: OrganisationalLandlordInvitationRepository

    @Autowired
    private lateinit var organisationalLandlordUserRepository: OrganisationalLandlordUserRepository

    @MockitoSpyBean
    private lateinit var invitationService: OrganisationalLandlordInvitationService

    @MockitoSpyBean
    private lateinit var organisationalLandlordUserService: OrganisationalLandlordUserService

    @BeforeEach
    fun enableFeatureFlag() {
        featureFlagManager.enable(MULTI_USER_ORGANISATIONS)
    }

    @Test
    fun `Invitees accepting an admin invitation get an admin organisational user`(page: Page) {
        completeAcceptanceJourneyAndAssertResult(
            page,
            "1234abcd-5678-abcd-1234-567abcd2222a",
            OrganisationalLandlordUserRole.ADMIN,
            200,
        )
    }

    @Test
    fun `Invitees accepting an editor invitation get an editor organisational user`(page: Page) {
        completeAcceptanceJourneyAndAssertResult(
            page,
            "1234abcd-5678-abcd-1234-567abcd2222b",
            OrganisationalLandlordUserRole.EDITOR,
            403,
        )
    }

    @Test
    fun `A failed user save leaves the invitation available`(page: Page) {
        removeExistingOrganisationalLandlordUser()
        val token = "1234abcd-5678-abcd-1234-567abcd2222a"
        doThrow(IllegalStateException("User save failed"))
            .whenever(organisationalLandlordUserService)
            .createOrganisationalLandlordUser(any<OrganisationalLandlord>(), any<PrsdbUser>(), any(), any(), any())

        val validateTokenPage = navigator.goToAcceptOrganisationalLandlordInvitationJourney(token)
        validateTokenPage.form.radios.selectValue(TokenValidity.VALID)
        validateTokenPage.form.submit()
        val joinOrganisationPage = assertPageIs(page, JoinOrganisationPage::class)
        joinOrganisationPage.form.submit()
        val fullNamePage = assertPageIs(page, FullNamePage::class)
        fullNamePage.submitName("Jane Smith")
        val emailAddressPage = assertPageIs(page, EmailAddressPage::class)
        emailAddressPage.submitEmail("invitee@example.com")
        val checkAnswersPage = assertPageIs(page, CheckAnswersPage::class)

        var checkAnswersPostStatus: Int? = null
        page.onResponse { response ->
            if (response.request().method() == "POST" && response.url().contains("/${CheckAnswersStep.ROUTE_SEGMENT}")) {
                checkAnswersPostStatus = response.status()
            }
        }
        checkAnswersPage.form.submit()

        assertEquals(500, checkAnswersPostStatus)
        assertNotNull(invitationRepository.findByToken(UUID.fromString(token)))
        verify(organisationalLandlordUserService)
            .createOrganisationalLandlordUser(any<OrganisationalLandlord>(), any<PrsdbUser>(), any(), any(), any())
    }

    private fun completeAcceptanceJourneyAndAssertResult(
        page: Page,
        token: String,
        expectedRole: OrganisationalLandlordUserRole,
        expectedTeamMembersStatus: Int,
    ) {
        removeExistingOrganisationalLandlordUser()

        // 1. Go to the start of accept invitation journey (Validate Token page)
        val validateTokenPage = navigator.goToAcceptOrganisationalLandlordInvitationJourney(token)
        assertPageIs(page, ValidateTokenPage::class)
        // TODO PDJB-1822: Validate token step
        validateTokenPage.form.radios.selectValue(TokenValidity.VALID)
        validateTokenPage.form.submit()

        // 1b. Join Organisation page
        val joinOrganisationPage = assertPageIs(page, JoinOrganisationPage::class)
        assertEquals("Join your organisation", joinOrganisationPage.heading.getText())
        assertEquals(
            true,
            joinOrganisationPage.introductionText.textContent()?.contains("Local Organisation Landlord."),
        )
        joinOrganisationPage.form.submit()

        // 2. Full Name page
        val fullNamePage = assertPageIs(page, FullNamePage::class)
        fullNamePage.submitName("Jane Smith")

        // 3. Email Address page
        val emailAddressPage = assertPageIs(page, EmailAddressPage::class)
        emailAddressPage.submitEmail("invitee@example.com")

        // 4. Submit Check Answers
        val checkAnswersPage = assertPageIs(page, CheckAnswersPage::class)
        checkAnswersPage.form.submit()

        // 5. Confirmation page
        val confirmationPage = assertPageIs(page, ConfirmationPage::class)
        assertTrue(confirmationPage.heading.getText().contains("TODO PDJB-1775"))

        val user = organisationalLandlordUserRepository.findByBaseUser_Id(INVITATION_TEST_USER_ID).single()
        assertEquals("Jane Smith", user.name)
        assertEquals("invitee@example.com", user.email)
        assertEquals(36L, user.organisationalLandlord.id)
        assertEquals(expectedRole, user.role)
        assertNull(invitationRepository.findByToken(UUID.fromString(token)))
        verify(invitationService).clearJourneyIdInvitationTokenPairsForTokenFromSession(token)

        val response = page.navigate("http://localhost:$port$TEAM_MEMBERS_ROUTE")
        assertEquals(expectedTeamMembersStatus, response?.status())
    }

    private fun removeExistingOrganisationalLandlordUser() {
        organisationalLandlordUserRepository
            .findByBaseUser_Id(INVITATION_TEST_USER_ID)
            .forEach(organisationalLandlordUserRepository::delete)
    }

    // TODO PDJB-1822: Update test once validate token step is in place
    @Test
    fun `Invitees are redirected to invalid link page when token validation placeholder is invalid`(page: Page) {
        // 1. Go to the start of accept invitation journey (Validate Token page)
        val validateTokenPage = navigator.goToAcceptOrganisationalLandlordInvitationJourney("1234abcd-5678-abcd-1234-567abcd2222a")
        assertPageIs(page, ValidateTokenPage::class)
        // TODO PDJB-1822: Validate token step
        validateTokenPage.form.radios.selectValue(TokenValidity.INVALID)
        validateTokenPage.form.submit()

        // 2. Invalid link page
        val invalidLinkPage = assertPageIs(page, InvalidLinkPage::class)
        BaseComponent.assertThat(invalidLinkPage.heading)
            .containsText("There was a problem with this invitation link")
        BaseComponent.assertThat(invalidLinkPage.signInLink).hasAttribute("href", LANDLORD_DASHBOARD_URL)
    }

    @Test
    fun `Validate token page is unavailable when multi user organisations is disabled`(page: Page) {
        featureFlagManager.disable(MULTI_USER_ORGANISATIONS)

        val response = page.navigate("http://localhost:$port$ACCEPT_INVITATION_ROUTE?token=1234abcd-5678-abcd-1234-567abcd2222a")
        assertEquals(404, response?.status())
    }

    companion object {
        private const val INVITATION_TEST_USER_ID = "urn:fdc:gov.uk:2022:INVITEE"
    }
}
