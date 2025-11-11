package uk.gov.communities.prsdb.webapp.forms.journeys

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncil
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncilInvitation
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncilUser
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLocalCouncilUserStepId
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.LocalCouncilDataService
import uk.gov.communities.prsdb.webapp.services.LocalCouncilInvitationService
import uk.gov.communities.prsdb.webapp.services.SecurityContextService
import uk.gov.communities.prsdb.webapp.testHelpers.JourneyTestHelper
import uk.gov.communities.prsdb.webapp.testHelpers.builders.JourneyDataBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalAuthorityData.Companion.createLocalAuthority
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockOneLoginUserData.Companion.createOneLoginUser
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class LaUserRegistrationJourneyTests {
    @Mock
    private lateinit var mockJourneyDataService: JourneyDataService

    @Mock
    private lateinit var mockInvitationService: LocalCouncilInvitationService

    @Mock
    private lateinit var mockLocalCouncilDataService: LocalCouncilDataService

    @Mock
    private lateinit var mockSecurityContextService: SecurityContextService

    private lateinit var invitation: LocalCouncilInvitation

    val alwaysTrueValidator: AlwaysTrueValidator = AlwaysTrueValidator()

    @Test
    fun `handleSubmitAndRedirect registers the new user and adds their id to the session`() {
        val name = "Test user"
        val email = "test.user@example.com"
        val localAuthority = createLocalAuthority()
        val invitedAsAdmin = true
        val baseUserId = "test-base-user-id"

        val expectedLaUser = setupInvitationAndLAUserMocks(name, email, localAuthority, invitedAsAdmin, baseUserId)

        // Act
        completeHandleSubmitAndRedirect()

        // Assert
        verify(mockLocalCouncilDataService).registerUserAndReturnID(baseUserId, localAuthority, name, email, invitedAsAdmin, true)
        verify(mockLocalCouncilDataService).setLastUserIdRegisteredThisSession(expectedLaUser.id)
    }

    @Test
    fun `handleSubmitAndRedirect deletes the invitation from the database`() {
        setupInvitationAndLAUserMocks()

        // Act
        completeHandleSubmitAndRedirect()

        // Assert
        verify(mockInvitationService).deleteInvitation(invitation)
    }

    @Test
    fun `handleSubmitAndRedirect updates user roles`() {
        setupInvitationAndLAUserMocks()

        // Act
        completeHandleSubmitAndRedirect()

        // Assert
        verify(mockSecurityContextService).refreshContext()
    }

    @Test
    fun `handleSubmitAndRedirect clears data from the session`() {
        setupInvitationAndLAUserMocks()

        // Act
        completeHandleSubmitAndRedirect()

        // Assert
        verify(mockInvitationService).clearTokenFromSession()
        verify(mockJourneyDataService).removeJourneyDataAndContextIdFromSession()
    }

    private fun completeHandleSubmitAndRedirect() {
        val testJourney =
            LocalCouncilUserRegistrationJourney(
                validator = alwaysTrueValidator,
                journeyDataService = mockJourneyDataService,
                invitationService = mockInvitationService,
                localCouncilDataService = mockLocalCouncilDataService,
                invitation = invitation,
                securityContextService = mockSecurityContextService,
            )

        testJourney.completeStep(
            stepPathSegment = RegisterLocalCouncilUserStepId.CheckAnswers.urlPathSegment,
            formData = mapOf(),
            subPageNumber = null,
            principal = mock(),
        )
    }

    private fun setupInvitationAndLAUserMocks(
        name: String = "Test user",
        email: String = "test.user@example.com",
        localCouncil: LocalCouncil = createLocalAuthority(),
        invitedAsAdmin: Boolean = false,
        baseUserId: String = "test-base-user-id",
    ): LocalCouncilUser {
        createLocalAuthority()

        val journeyData = JourneyDataBuilder.forLaUser(name, email).build()
        whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

        invitation =
            LocalCouncilInvitation(
                token = UUID.randomUUID(),
                email = email,
                invitingAuthority = localCouncil,
                invitedAsAdmin = invitedAsAdmin,
            )

        val newLaUser =
            LocalCouncilUser(
                baseUser = createOneLoginUser(baseUserId),
                isManager = invitedAsAdmin,
                localCouncil = localCouncil,
                name = name,
                email = email,
                hasAcceptedPrivacyNotice = true,
            )

        whenever(
            mockLocalCouncilDataService.registerUserAndReturnID(
                baseUserId = anyOrNull(),
                localCouncil = eq(invitation.invitingAuthority),
                name = eq(name),
                email = eq(email),
                invitedAsAdmin = eq(invitedAsAdmin),
                hasAcceptedPrivacyNotice = eq(true),
            ),
        ).thenReturn(newLaUser.id)

        JourneyTestHelper.setMockUser(baseUserId)

        return newLaUser
    }
}
