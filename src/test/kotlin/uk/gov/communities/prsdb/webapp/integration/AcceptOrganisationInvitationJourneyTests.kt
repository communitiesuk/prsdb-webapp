package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import uk.gov.communities.prsdb.webapp.constants.MULTI_USER_ORGANISATIONS
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordInvitationStatus
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordUserRepository
import uk.gov.communities.prsdb.webapp.integration.IntegrationTestWithMutableData.NestedIntegrationTestWithMutableData
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.CheckAnswersPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.ConfirmationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.EmailAddressPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.FullNamePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.InvalidLinkPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.JoinOrganisationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.UserIsAlreadyLandlordPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordUserService
import uk.gov.communities.prsdb.webapp.services.UserRolesService
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

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

    @MockitoSpyBean
    private lateinit var userRolesService: UserRolesService

    @BeforeEach
    fun enableFeatureFlag() {
        featureFlagManager.enable(MULTI_USER_ORGANISATIONS)
    }

    @Nested
    inner class UserIsNotYetAnOrganisationalUser :
        NestedIntegrationTestWithMutableData(
            listOf("data-local.sql", "data-overlay-mockuser-is-not-a-landlord.sql"),
        ) {
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

        @Test
        fun `Invitees are redirected to the invalid link page if their invitation is cancelled before they submit`(page: Page) {
            val checkAnswersPage = completeStepsUpToCheckAnswers(page, validToken)

            // The invitation is cancelled after the invitee reaches check answers but before they submit
            invitationRepository.delete(invitationRepository.findByToken(UUID.fromString(validToken))!!)

            checkAnswersPage.form.submit()

            assertPageIs(page, InvalidLinkPage::class)
            assertTrue(organisationalLandlordUserRepository.findByBaseUser_Id(INVITATION_TEST_USER_ID).isEmpty())
        }

        @Test
        fun `Invitees are redirected to the already landlord page if they become a landlord before they submit`(page: Page) {
            val checkAnswersPage = completeStepsUpToCheckAnswers(page, validToken)

            // The invitee becomes a landlord after reaching check answers but before they submit
            doReturn(true).whenever(userRolesService).getUserHasLandlordRole(INVITATION_TEST_USER_ID)

            checkAnswersPage.form.submit()

            assertPageIs(page, UserIsAlreadyLandlordPage::class)
            assertTrue(organisationalLandlordUserRepository.findByBaseUser_Id(INVITATION_TEST_USER_ID).isEmpty())
            // The invitation is untouched, so the correct account can still accept it
            val invitation = invitationRepository.findByToken(UUID.fromString(validToken))!!
            assertEquals(OrganisationalLandlordInvitationStatus.PENDING, invitation.status)
        }
    }

    @Nested
    inner class UserIsAlreadyAnIndividualLandlord : NestedIntegrationTestWithMutableData("data-local.sql") {
        @Test
        fun `Invitees who are already an individual landlord are shown the already landlord page`(page: Page) {
            // Act
            val userIsAlreadyLandlordPage =
                navigator.goToAcceptOrganisationalLandlordInvitationJourneyAsExistingLandlord(validToken)

            // Assert
            assertPageIs(page, UserIsAlreadyLandlordPage::class)
            BaseComponent
                .assertThat(userIsAlreadyLandlordPage.heading)
                .containsText("You need to use a different GOV.UK One Login to accept this invitation")
            BaseComponent.assertThat(userIsAlreadyLandlordPage.signOutLink).isVisible()

            // The invitation is untouched, so the correct account can still accept it
            val invitation = invitationRepository.findByToken(UUID.fromString(validToken))!!
            assertEquals(OrganisationalLandlordInvitationStatus.PENDING, invitation.status)
        }
    }

    @Nested
    inner class UserIsAlreadyAnOrganisationalLandlordUser :
        NestedIntegrationTestWithMutableData(
            listOf(
                "data-local.sql",
                "data-overlay-mockuser-is-not-a-landlord.sql",
                "data-overlay-mockuser-is-org-landlord-user.sql",
            ),
        ) {
        @Test
        fun `Invitees who are already an organisational landlord user are shown the already landlord page`(page: Page) {
            // Act
            val userIsAlreadyLandlordPage =
                navigator.goToAcceptOrganisationalLandlordInvitationJourneyAsExistingLandlord(validToken)

            // Assert
            assertPageIs(page, UserIsAlreadyLandlordPage::class)
            BaseComponent
                .assertThat(userIsAlreadyLandlordPage.heading)
                .containsText("You need to use a different GOV.UK One Login to accept this invitation")
        }
    }

    private fun completeAcceptanceJourneyAndAssertResult(
        page: Page,
        token: String,
        expectedRole: OrganisationalLandlordUserRole,
    ) {
        // 1-4. Complete the journey up to check answers
        val checkAnswersPage = completeStepsUpToCheckAnswers(page, token)
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

    private fun completeStepsUpToCheckAnswers(
        page: Page,
        token: String,
    ): CheckAnswersPage {
        // 1. Join organisation page - a valid token for a non-landlord redirects past the validate token and check
        // user is landlord steps
        val joinOrganisationPage = navigator.goToAcceptOrganisationalLandlordInvitationJourneyWithValidToken(token)
        assertPageIs(page, JoinOrganisationPage::class)
        assertEquals("Join your organisation", joinOrganisationPage.heading.getText())
        assertEquals(
            true,
            joinOrganisationPage.introductionText.textContent()?.contains("Local Organisation Landlord."),
        )
        joinOrganisationPage.form.submit()

        // 2. Full name page
        val fullNamePage = assertPageIs(page, FullNamePage::class)
        fullNamePage.submitName("Jane Smith")

        // 3. Email address page
        val emailAddressPage = assertPageIs(page, EmailAddressPage::class)
        emailAddressPage.submitEmail("invitee@example.com")

        // 4. Check answers page
        return assertPageIs(page, CheckAnswersPage::class)
    }

    companion object {
        private const val INVITATION_TEST_USER_ID = "urn:fdc:gov.uk:2022:UVWXY"
    }
}
