package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import uk.gov.communities.prsdb.webapp.constants.MULTI_USER_ORGANISATIONS
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrganisationalLandlordUserInvitationController.Companion.ACCEPT_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordUserRepository
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.CheckAnswersPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.CheckUserIsLandlordPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.ConfirmationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.EmailAddressPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.FullNamePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.InvalidLinkPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.JoinOrganisationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordUserService
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@TestPropertySource(properties = ["local.one-login-user-id=urn:fdc:gov.uk:2022:INVITEE"])
class AcceptOrganisationInvitationJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    private val validToken = "1234abcd-5678-abcd-1234-567abcd2222a"

    private val expiredToken = "1234abcd-5678-abcd-1234-567abcd2222d"

    private val unrecognisedToken = "1234abcd-5678-abcd-1234-567abcd9999a"

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
        )
    }

    @Test
    fun `Invitees accepting an editor invitation get an editor organisational user`(page: Page) {
        completeAcceptanceJourneyAndAssertResult(
            page,
            "1234abcd-5678-abcd-1234-567abcd2222b",
            OrganisationalLandlordUserRole.EDITOR,
        )
    }

    private fun completeAcceptanceJourneyAndAssertResult(
        page: Page,
        token: String,
        expectedRole: OrganisationalLandlordUserRole,
    ) {
        removeExistingOrganisationalLandlordUser()

        // 1. Go to the start of accept invitation journey - a valid token redirects past the validate token step
        // TODO PDJB-1829: CheckUserIsLandlordStep is a placeholder, so step 1 currently stops here. Once it decides whether the
        // invitee is already a landlord user, a valid token should arrive at the join organisation page and this step can go.
        val checkUserIsLandlordPage =
            navigator.goToAcceptOrganisationalLandlordInvitationJourneyWithValidToken(token)
        assertPageIs(page, CheckUserIsLandlordPage::class)
        checkUserIsLandlordPage.form.submit()

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
    }

    private fun removeExistingOrganisationalLandlordUser() {
        organisationalLandlordUserRepository
            .findByBaseUser_Id(INVITATION_TEST_USER_ID)
            .forEach(organisationalLandlordUserRepository::delete)
    }

    @Test
    fun `Invitees are redirected to the invalid link page when their invitation has expired`(page: Page) {
        // Go to the start of accept invitation journey with an expired invitation token
        val invalidLinkPage =
            navigator.goToAcceptOrganisationalLandlordInvitationJourneyWithInvalidToken(expiredToken)

        assertPageIs(page, InvalidLinkPage::class)
        BaseComponent
            .assertThat(invalidLinkPage.heading)
            .containsText("There was a problem with this invitation link")
        BaseComponent.assertThat(invalidLinkPage.signInLink).hasAttribute("href", LANDLORD_DASHBOARD_URL)
    }

    @Test
    fun `Invitees are redirected to the invalid link page when their invitation token is not recognised`(page: Page) {
        // Go to the start of accept invitation journey with a token that matches no invitation
        val invalidLinkPage =
            navigator.goToAcceptOrganisationalLandlordInvitationJourneyWithInvalidToken(unrecognisedToken)

        assertPageIs(page, InvalidLinkPage::class)
        BaseComponent
            .assertThat(invalidLinkPage.heading)
            .containsText("There was a problem with this invitation link")
    }

    @Test
    fun `Validate token page is unavailable when multi user organisations is disabled`(page: Page) {
        featureFlagManager.disable(MULTI_USER_ORGANISATIONS)

        val response = page.navigate("http://localhost:$port$ACCEPT_INVITATION_ROUTE?token=$validToken")
        assertEquals(404, response?.status())
    }

    companion object {
        private const val INVITATION_TEST_USER_ID = "urn:fdc:gov.uk:2022:INVITEE"
    }
}
