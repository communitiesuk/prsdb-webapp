package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.ACTIVE_INACTIVE
import uk.gov.communities.prsdb.webapp.constants.PROPERTIES_LEFT_THIS_SESSION
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplateModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordYouLeftConfirmation
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordYouLeftConfirmationAccountDeactivated
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.net.URI
import java.time.LocalDate
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class LeavePropertyServiceTests {
    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockHttpSession: HttpSession

    @Mock
    private lateinit var emailSender: EmailNotificationService<EmailTemplateModel>

    @Mock
    private lateinit var mockSwapToIndividualNudgeEmailService: SwapToIndividualNudgeEmailService

    @Mock
    private lateinit var mockUserToLandlordService: UserToLandlordService

    @Mock
    private lateinit var mockCurrentUserService: CurrentUserService

    @Mock
    private lateinit var mockDeactivationService: DeactivationService

    @Mock
    private lateinit var mockFeatureFlagManager: FeatureFlagManager

    @Mock
    private lateinit var mockAbsoluteUrlProvider: AbsoluteUrlProvider

    @InjectMocks
    private lateinit var leavePropertyService: LeavePropertyService

    @Test
    fun `getPropertyOwnershipIfUserCanLeave throws NOT_FOUND when the user is not a landlord on the property`() {
        // Arrange
        val propertyOwnershipId = 1L
        val currentLandlord = MockLandlordData.createIndividualLandlord(baseUser = MockLandlordData.createPrsdbUser("current-user"))
        val landlordOne = MockLandlordData.createIndividualLandlord(baseUser = MockLandlordData.createPrsdbUser("user-1"))
        val landlordTwo = MockLandlordData.createIndividualLandlord(baseUser = MockLandlordData.createPrsdbUser("user-2"))
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                id = propertyOwnershipId,
                landlords = mutableSetOf(landlordOne, landlordTwo),
            )
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)
        whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(currentLandlord)

        // Act
        val exception =
            assertThrows<ResponseStatusException> {
                leavePropertyService.getPropertyOwnershipIfUserCanLeave(propertyOwnershipId)
            }

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
    }

    @Test
    fun `getPropertyOwnershipIfUserCanLeave throws NOT_FOUND when the property has fewer than two landlords`() {
        // Arrange
        val propertyOwnershipId = 1L
        val soleLandlord = MockLandlordData.createIndividualLandlord(baseUser = MockLandlordData.createPrsdbUser("user-1"))
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                id = propertyOwnershipId,
                landlords = mutableSetOf(soleLandlord),
            )
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)
        whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(soleLandlord)

        // Act
        val exception =
            assertThrows<ResponseStatusException> {
                leavePropertyService.getPropertyOwnershipIfUserCanLeave(propertyOwnershipId)
            }

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
    }

    @Test
    fun `getPropertyOwnershipIfUserCanLeave returns the property when the user is one of two or more landlords`() {
        // Arrange
        val propertyOwnershipId = 1L
        val landlordOne = MockLandlordData.createIndividualLandlord(baseUser = MockLandlordData.createPrsdbUser("user-1"))
        val landlordTwo = MockLandlordData.createIndividualLandlord(baseUser = MockLandlordData.createPrsdbUser("user-2"))
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                id = propertyOwnershipId,
                landlords = mutableSetOf(landlordOne, landlordTwo),
            )
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)
        whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(landlordOne)

        // Act
        val result = leavePropertyService.getPropertyOwnershipIfUserCanLeave(propertyOwnershipId)

        // Assert
        assertEquals(propertyOwnership, result)
    }

    @Test
    fun `leavePropertyOwnership removes the landlord and sends the current user a confirmation email`() {
        // Arrange
        val address = MockLandlordData.createAddress(singleLineAddress = "10 High Street, London, SW1A 1AA")
        val landlord = MockLandlordData.createIndividualLandlord(name = "Alice", email = "alice@example.com")
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                landlords = mutableSetOf(landlord, MockLandlordData.createIndividualLandlord(name = "Bob")),
                address = address,
            )
        whenever(mockCurrentUserService.getRegisteredUserDetails())
            .thenReturn(CurrentUserService.RegisteredUserDetails(name = "Alice", email = "alice@example.com"))

        // Act
        leavePropertyService.leavePropertyOwnership(landlord, propertyOwnership)

        // Assert
        verify(mockPropertyOwnershipService).removeLandlord(propertyOwnership, landlord)

        val emailCaptor = argumentCaptor<JointLandlordYouLeftConfirmation>()
        verify(emailSender).sendEmail(eq("alice@example.com"), emailCaptor.capture())
        val sentEmail = emailCaptor.firstValue
        assertEquals("Alice", sentEmail.recipientName)
        assertEquals(address.toMultiLineAddress(), sentEmail.propertyAddress)
    }

    @Test
    fun `leavePropertyOwnership removes an organisation landlord and sends the current user a confirmation email`() {
        // Arrange
        val address = MockLandlordData.createAddress(singleLineAddress = "10 High Street, London, SW1A 1AA")
        val orgLandlord = MockLandlordData.createOrgLandlord(name = "Org Landlord", registrantEmail = "org.user@example.com")
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                landlords = mutableSetOf(orgLandlord, MockLandlordData.createIndividualLandlord(name = "Bob")),
                address = address,
            )
        whenever(mockCurrentUserService.getRegisteredUserDetails())
            .thenReturn(CurrentUserService.RegisteredUserDetails(name = "Acting User", email = "acting.user@example.com"))

        // Act
        leavePropertyService.leavePropertyOwnership(orgLandlord, propertyOwnership)

        // Assert
        verify(mockPropertyOwnershipService).removeLandlord(propertyOwnership, orgLandlord)

        val emailCaptor = argumentCaptor<JointLandlordYouLeftConfirmation>()
        verify(emailSender).sendEmail(eq("acting.user@example.com"), emailCaptor.capture())
        val sentEmail = emailCaptor.firstValue
        assertEquals("Acting User", sentEmail.recipientName)
        assertEquals(address.toMultiLineAddress(), sentEmail.propertyAddress)
    }

    @Test
    fun `leavePropertyOwnership sends swap to individual nudge email`() {
        // Arrange
        val landlord = MockLandlordData.createIndividualLandlord(name = "Alice", email = "alice@example.com")
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                landlords = mutableSetOf(landlord, MockLandlordData.createIndividualLandlord(name = "Bob")),
            )
        whenever(mockCurrentUserService.getRegisteredUserDetails())
            .thenReturn(CurrentUserService.RegisteredUserDetails(name = "Alice", email = "alice@example.com"))

        // Act
        leavePropertyService.leavePropertyOwnership(landlord, propertyOwnership)

        // Assert
        verify(mockSwapToIndividualNudgeEmailService).sendNudgeEmailIfApplicable(propertyOwnership)
    }

    @Test
    fun `leavePropertyOwnership still sends the nudge email without throwing when the confirmation email fails to send`() {
        // Arrange
        val landlord = MockLandlordData.createIndividualLandlord(name = "Alice", email = "alice@example.com")
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                landlords = mutableSetOf(landlord, MockLandlordData.createIndividualLandlord(name = "Bob")),
            )
        doThrow(RuntimeException("Email failed to send")).whenever(emailSender).sendEmail(any(), any())
        whenever(mockCurrentUserService.getRegisteredUserDetails())
            .thenReturn(CurrentUserService.RegisteredUserDetails(name = "Alice", email = "alice@example.com"))

        // Act & Assert
        assertDoesNotThrow { leavePropertyService.leavePropertyOwnership(landlord, propertyOwnership) }
        verify(mockSwapToIndividualNudgeEmailService).sendNudgeEmailIfApplicable(propertyOwnership)
    }

    @Test
    fun `leavePropertyOwnership does not throw when the nudge email fails to send`() {
        // Arrange
        val landlord = MockLandlordData.createIndividualLandlord(name = "Alice", email = "alice@example.com")
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                landlords = mutableSetOf(landlord, MockLandlordData.createIndividualLandlord(name = "Bob")),
            )
        doThrow(RuntimeException("Email failed to send"))
            .whenever(mockSwapToIndividualNudgeEmailService)
            .sendNudgeEmailIfApplicable(propertyOwnership)

        // Act & Assert
        assertDoesNotThrow { leavePropertyService.leavePropertyOwnership(landlord, propertyOwnership) }
    }

    @Test
    fun `leavePropertyOwnership deactivates the landlord if they have no active properties left`() {
        val landlord = MockLandlordData.createIndividualLandlord(name = "Alice", email = "alice@example.com")
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                landlords = mutableSetOf(landlord, MockLandlordData.createIndividualLandlord(name = "Bob")),
            )
        whenever(mockCurrentUserService.getRegisteredUserDetails())
            .thenReturn(CurrentUserService.RegisteredUserDetails(name = "Alice", email = "alice@example.com"))

        leavePropertyService.leavePropertyOwnership(landlord, propertyOwnership)

        verify(mockDeactivationService).deactivateLandlordIfNoActiveProperties(landlord)
    }

    @Test
    fun `leavePropertyOwnership sends the account deactivated email when the landlord was deactivated`() {
        val address = MockLandlordData.createAddress(singleLineAddress = "10 High Street, London, SW1A 1AA")
        val landlord = MockLandlordData.createIndividualLandlord(name = "Alice", email = "alice@example.com")
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                landlords = mutableSetOf(landlord, MockLandlordData.createIndividualLandlord(name = "Bob")),
                address = address,
            )
        whenever(mockCurrentUserService.getRegisteredUserDetails())
            .thenReturn(CurrentUserService.RegisteredUserDetails(name = "Alice", email = "alice@example.com"))
        doAnswer { landlord.deactivate(LocalDate.now()) }
            .whenever(mockDeactivationService)
            .deactivateLandlordIfNoActiveProperties(landlord)
        whenever(mockFeatureFlagManager.checkFeature(ACTIVE_INACTIVE)).thenReturn(true)
        whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI(DASHBOARD_URL))

        leavePropertyService.leavePropertyOwnership(landlord, propertyOwnership)

        verify(emailSender).sendEmail(
            "alice@example.com",
            JointLandlordYouLeftConfirmationAccountDeactivated(
                recipientName = "Alice",
                propertyAddress = address.toMultiLineAddress(),
                dashboardUrl = DASHBOARD_URL,
            ),
        )
        verify(emailSender, never()).sendEmail(any(), any<JointLandlordYouLeftConfirmation>())
    }

    @Test
    fun `leavePropertyOwnership sends the standard confirmation email when the landlord was not deactivated`() {
        val landlord = MockLandlordData.createIndividualLandlord(name = "Alice", email = "alice@example.com")
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                landlords = mutableSetOf(landlord, MockLandlordData.createIndividualLandlord(name = "Bob")),
            )
        whenever(mockCurrentUserService.getRegisteredUserDetails())
            .thenReturn(CurrentUserService.RegisteredUserDetails(name = "Alice", email = "alice@example.com"))

        leavePropertyService.leavePropertyOwnership(landlord, propertyOwnership)

        verify(emailSender).sendEmail(eq("alice@example.com"), any<JointLandlordYouLeftConfirmation>())
        verify(emailSender, never()).sendEmail(any(), any<JointLandlordYouLeftConfirmationAccountDeactivated>())
    }

    // TODO PDJB-2149: Remove this test when the ACTIVE_INACTIVE flag is removed
    @Test
    fun `leavePropertyOwnership sends the standard confirmation email when ACTIVE_INACTIVE is disabled`() {
        val landlord = MockLandlordData.createIndividualLandlord(name = "Alice", email = "alice@example.com")
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                landlords = mutableSetOf(landlord, MockLandlordData.createIndividualLandlord(name = "Bob")),
            )
        whenever(mockCurrentUserService.getRegisteredUserDetails())
            .thenReturn(CurrentUserService.RegisteredUserDetails(name = "Alice", email = "alice@example.com"))
        doAnswer { landlord.deactivate(LocalDate.now()) }
            .whenever(mockDeactivationService)
            .deactivateLandlordIfNoActiveProperties(landlord)
        whenever(mockFeatureFlagManager.checkFeature(ACTIVE_INACTIVE)).thenReturn(false)

        leavePropertyService.leavePropertyOwnership(landlord, propertyOwnership)

        verify(emailSender).sendEmail(eq("alice@example.com"), any<JointLandlordYouLeftConfirmation>())
        verify(emailSender, never()).sendEmail(any(), any<JointLandlordYouLeftConfirmationAccountDeactivated>())
    }

    @Test
    fun `addLeftPropertyOwnershipToSession appends the property ownership to the list stored in the session`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val existingOwnerships = mutableMapOf((456L to "First address"), (789L to "Second address"))
        whenever(mockHttpSession.getAttribute(PROPERTIES_LEFT_THIS_SESSION)).thenReturn(existingOwnerships)

        // Act
        leavePropertyService.addLeftPropertyOwnershipToSession(propertyOwnership)

        // Assert
        verify(mockHttpSession).setAttribute(
            PROPERTIES_LEFT_THIS_SESSION,
            existingOwnerships + (propertyOwnership.id to propertyOwnership.address.singleLineAddress),
        )
    }

    @Test
    fun `getLeftPropertyOwnershipsFromSession returns the list stored in the session`() {
        // Arrange
        val leftOwnerships = mutableMapOf((456L to "First address"), (789L to "Second address"))
        whenever(mockHttpSession.getAttribute(PROPERTIES_LEFT_THIS_SESSION)).thenReturn(leftOwnerships)

        // Act
        val result = leavePropertyService.getLeftPropertyOwnershipsFromSession()

        // Assert
        assertEquals(leftOwnerships, result)
    }

    @Test
    fun `getLeftPropertyOwnershipsFromSession returns an empty list when nothing is stored in the session`() {
        // Arrange
        whenever(mockHttpSession.getAttribute(PROPERTIES_LEFT_THIS_SESSION)).thenReturn(null)

        // Act
        val result = leavePropertyService.getLeftPropertyOwnershipsFromSession()

        // Assert
        assertEquals(emptyMap(), result)
    }

    companion object {
        private const val DASHBOARD_URL = "https://example.com/landlord/dashboard"
    }
}
