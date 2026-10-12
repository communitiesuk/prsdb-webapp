package uk.gov.communities.prsdb.webapp.journeys.organisationalLandlordDeregistration.stepConfig

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.util.ReflectionTestUtils
import uk.gov.communities.prsdb.webapp.journeys.organisationalLandlordDeregistration.OrganisationalLandlordDeregistrationJourneyState
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.OrganisationalLandlordDeregistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.CurrentUserService
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.LandlordDeregistrationService
import uk.gov.communities.prsdb.webapp.services.SecurityContextService
import uk.gov.communities.prsdb.webapp.services.SwapToIndividualNudgeEmailService
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class DeregisterStepConfigTests {
    @Mock
    lateinit var mockLandlordDeregistrationService: LandlordDeregistrationService

    @Mock
    lateinit var mockUserToLandlordService: UserToLandlordService

    @Mock
    lateinit var mockSecurityContextService: SecurityContextService

    @Mock
    lateinit var mockSwapToIndividualNudgeEmailService: SwapToIndividualNudgeEmailService

    @Mock
    lateinit var mockCurrentUserService: CurrentUserService

    @Mock
    lateinit var mockConfirmationEmailSender: EmailNotificationService<OrganisationalLandlordDeregistrationConfirmationEmail>

    @Mock
    lateinit var mockState: OrganisationalLandlordDeregistrationJourneyState

    @Test
    fun `afterStepIsReached deregisters the organisational landlord`() {
        val stepConfig = setupStepConfig()
        val orgLandlord = setupMocks()

        stepConfig.afterStepIsReached(mockState)

        verify(mockLandlordDeregistrationService).deregisterOrganisationalLandlord(orgLandlord)
    }

    @Test
    fun `afterStepIsReached refreshes security context`() {
        val stepConfig = setupStepConfig()
        setupMocks()

        stepConfig.afterStepIsReached(mockState)

        verify(mockSecurityContextService).refreshContext()
    }

    @Test
    fun `afterStepIsReached calls nudge email service for jointly owned properties`() {
        val stepConfig = setupStepConfig()
        val orgLandlord = MockLandlordData.createOrgLandlord()
        ReflectionTestUtils.setField(orgLandlord, "id", 1L)
        val coLandlord = MockLandlordData.createIndividualLandlord()
        ReflectionTestUtils.setField(coLandlord, "id", 2L)
        val jointProperty = MockLandlordData.createPropertyOwnership(landlords = mutableSetOf(orgLandlord, coLandlord))
        whenever(mockUserToLandlordService.getCurrentOrganisationLandlordForUser()).thenReturn(orgLandlord)
        setupCurrentUser()

        stepConfig.afterStepIsReached(mockState)

        verify(mockSwapToIndividualNudgeEmailService).sendNudgeEmailIfApplicable(jointProperty)
    }

    @Test
    fun `afterStepIsReached sends the confirmation email to the current user`() {
        val stepConfig = setupStepConfig()
        val landlord =
            MockLandlordData.createOrgLandlord(
                name = "Example Housing Association",
                email = "organisation@example.com",
            )
        whenever(mockUserToLandlordService.getCurrentOrganisationLandlordForUser()).thenReturn(landlord)
        setupCurrentUser()

        stepConfig.afterStepIsReached(mockState)

        val emailCaptor = argumentCaptor<OrganisationalLandlordDeregistrationConfirmationEmail>()
        verify(mockConfirmationEmailSender).sendEmail(eq(currentUserEmail), emailCaptor.capture())

        assertEquals(
            OrganisationalLandlordDeregistrationConfirmationEmail(
                registrantName = currentUserName,
                organisationName = "Example Housing Association",
            ),
            emailCaptor.firstValue,
        )
    }

    @Test
    fun `afterStepIsReached stores the organisation name in the session`() {
        val stepConfig = setupStepConfig()
        val organisationName = "Keystone Living Group"
        val orgLandlord = MockLandlordData.createOrgLandlord(name = organisationName)
        whenever(mockUserToLandlordService.getCurrentOrganisationLandlordForUser()).thenReturn(orgLandlord)
        setupCurrentUser()

        stepConfig.afterStepIsReached(mockState)

        verify(mockLandlordDeregistrationService).addDeregisteredOrganisationNameToSession(organisationName)
    }

    @Test
    fun `afterStepIsReached does not store the organisation name if deregistration fails`() {
        val stepConfig = setupStepConfig()
        val orgLandlord = setupMocks()
        whenever(mockLandlordDeregistrationService.deregisterOrganisationalLandlord(orgLandlord))
            .thenThrow(RuntimeException("deregistration failed"))

        assertThrows<RuntimeException> { stepConfig.afterStepIsReached(mockState) }

        verify(mockLandlordDeregistrationService, never()).addDeregisteredOrganisationNameToSession(any())
    }

    private fun setupMocks() =
        MockLandlordData.createOrgLandlord().also { orgLandlord ->
            whenever(mockUserToLandlordService.getCurrentOrganisationLandlordForUser()).thenReturn(orgLandlord)
            setupCurrentUser()
        }

    private fun setupCurrentUser() {
        whenever(mockCurrentUserService.getCurrentUserDetails())
            .thenReturn(CurrentUserService.CurrentUserDetails(name = currentUserName, email = currentUserEmail))
    }

    private fun setupStepConfig() =
        DeregisterStepConfig(
            mockLandlordDeregistrationService,
            mockUserToLandlordService,
            mockSecurityContextService,
            mockCurrentUserService,
            mockSwapToIndividualNudgeEmailService,
            mockConfirmationEmailSender,
        )

    companion object {
        private const val currentUserName = "Sam Smith"
        private const val currentUserEmail = "sam.smith@example.com"
    }
}
