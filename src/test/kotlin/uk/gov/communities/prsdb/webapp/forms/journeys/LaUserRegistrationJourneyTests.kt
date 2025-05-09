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
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityInvitation
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityUser
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLaUserStepId
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityDataService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityInvitationService
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
    private lateinit var mockInvitationService: LocalAuthorityInvitationService

    @Mock
    private lateinit var mockLocalAuthorityDataService: LocalAuthorityDataService

    @Mock
    private lateinit var mockSecurityContextService: SecurityContextService

    private lateinit var invitation: LocalAuthorityInvitation

    val alwaysTrueValidator: AlwaysTrueValidator = AlwaysTrueValidator()

    @Test
    fun `handleSubmitAndRedirect registers the new user and adds their id to the session`() {
        val name = "Test user"
        val email = "test.user@example.com"
        val localAuthority = createLocalAuthority()
        val invitedAsAdmin = true
        val baseUserId = "test-base-user-id"

        val expectedLaUser = setupInvitationAndLAUSerMocks(name, email, localAuthority, invitedAsAdmin, baseUserId)

        // Act
        completeHandleSubmitAndRedirect()

        // Assert
        verify(mockLocalAuthorityDataService).registerUserAndReturnID(baseUserId, localAuthority, name, email, invitedAsAdmin)
        verify(mockLocalAuthorityDataService).setLastUserIdRegisteredThisSession(expectedLaUser.id)
    }

    @Test
    fun `handleSubmitAndRedirect deletes the invitation from the database`() {
        setupInvitationAndLAUSerMocks()

        // Act
        completeHandleSubmitAndRedirect()

        // Assert
        verify(mockInvitationService).deleteInvitation(invitation)
    }

    @Test
    fun `handleSubmitAndRedirect updates user roles`() {
        setupInvitationAndLAUSerMocks()

        // Act
        completeHandleSubmitAndRedirect()

        // Assert
        verify(mockSecurityContextService).refreshContext()
    }

    @Test
    fun `handleSubmitAndRedirect clears data from the session`() {
        setupInvitationAndLAUSerMocks()

        // Act
        completeHandleSubmitAndRedirect()

        // Assert
        verify(mockInvitationService).clearTokenFromSession()
        verify(mockJourneyDataService).removeJourneyDataAndContextIdFromSession()
    }

    private fun completeHandleSubmitAndRedirect() {
        val testJourney =
            LaUserRegistrationJourney(
                validator = alwaysTrueValidator,
                journeyDataService = mockJourneyDataService,
                invitationService = mockInvitationService,
                localAuthorityDataService = mockLocalAuthorityDataService,
                invitation = invitation,
                securityContextService = mockSecurityContextService,
            )

        testJourney.completeStep(
            stepPathSegment = RegisterLaUserStepId.CheckAnswers.urlPathSegment,
            formData = mapOf(),
            subPageNumber = null,
            principal = mock(),
        )
    }

    private fun setupInvitationAndLAUSerMocks(
        name: String = "Test user",
        email: String = "test.user@example.com",
        localAuthority: LocalAuthority = createLocalAuthority(),
        invitedAsAdmin: Boolean = false,
        baseUserId: String = "test-base-user-id",
    ): LocalAuthorityUser {
        createLocalAuthority()

        val journeyData = JourneyDataBuilder.localAuthorityUser(name, email).build()
        whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

        invitation =
            LocalAuthorityInvitation(
                token = UUID.randomUUID(),
                email = email,
                invitingAuthority = localAuthority,
                invitedAsAdmin = invitedAsAdmin,
            )

        val newLaUser =
            LocalAuthorityUser(
                baseUser = createOneLoginUser(baseUserId),
                isManager = invitedAsAdmin,
                localAuthority = localAuthority,
                name = name,
                email = email,
            )

        whenever(
            mockLocalAuthorityDataService.registerUserAndReturnID(
                baseUserId = anyOrNull(),
                localAuthority = eq(invitation.invitingAuthority),
                name = eq(name),
                email = eq(email),
                invitedAsAdmin = eq(invitedAsAdmin),
            ),
        ).thenReturn(newLaUser.id)

        JourneyTestHelper.setMockUser(baseUserId)

        return newLaUser
    }
}
