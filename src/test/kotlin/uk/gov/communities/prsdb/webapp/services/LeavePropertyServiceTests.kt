package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.constants.PROPERTIES_LEFT_THIS_SESSION
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplateModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordYouLeftConfirmation
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class LeavePropertyServiceTests {
    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockLandlordService: LandlordService

    @Mock
    private lateinit var mockHttpSession: HttpSession

    @Mock
    private lateinit var emailSender: EmailNotificationService<EmailTemplateModel>

    @InjectMocks
    private lateinit var leavePropertyService: LeavePropertyService

    @Test
    fun `getPropertyOwnershipIfUserCanLeave throws NOT_FOUND when the user is not a landlord on the property`() {
        val propertyOwnershipId = 1L
        val landlordOne = MockLandlordData.createLandlord(baseUser = MockLandlordData.createPrsdbUser("user-1"))
        val landlordTwo = MockLandlordData.createLandlord(baseUser = MockLandlordData.createPrsdbUser("user-2"))
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                id = propertyOwnershipId,
                landlords = mutableSetOf(landlordOne, landlordTwo),
            )
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)

        assertThrows<ResponseStatusException> {
            leavePropertyService.getPropertyOwnershipIfUserCanLeave(propertyOwnershipId, "not-a-landlord")
        }
    }

    @Test
    fun `getPropertyOwnershipIfUserCanLeave throws NOT_FOUND when the property has fewer than two landlords`() {
        val propertyOwnershipId = 1L
        val soleLandlord = MockLandlordData.createLandlord(baseUser = MockLandlordData.createPrsdbUser("user-1"))
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                id = propertyOwnershipId,
                landlords = mutableSetOf(soleLandlord),
            )
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)

        assertThrows<ResponseStatusException> {
            leavePropertyService.getPropertyOwnershipIfUserCanLeave(propertyOwnershipId, "user-1")
        }
    }

    @Test
    fun `getPropertyOwnershipIfUserCanLeave returns the property when the user is one of two or more landlords`() {
        val propertyOwnershipId = 1L
        val landlordOne = MockLandlordData.createLandlord(baseUser = MockLandlordData.createPrsdbUser("user-1"))
        val landlordTwo = MockLandlordData.createLandlord(baseUser = MockLandlordData.createPrsdbUser("user-2"))
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                id = propertyOwnershipId,
                landlords = mutableSetOf(landlordOne, landlordTwo),
            )
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)

        val result = leavePropertyService.getPropertyOwnershipIfUserCanLeave(propertyOwnershipId, "user-1")

        assertEquals(propertyOwnership, result)
    }

    @Test
    fun `leavePropertyOwnership removes the landlord and sends them a confirmation email`() {
        val address = MockLandlordData.createAddress(singleLineAddress = "10 High Street, London, SW1A 1AA")
        val landlord = MockLandlordData.createLandlord(name = "Alice", email = "alice@example.com")
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                landlords = mutableSetOf(landlord, MockLandlordData.createLandlord(name = "Bob")),
                address = address,
            )

        leavePropertyService.leavePropertyOwnership(landlord, propertyOwnership)

        verify(mockPropertyOwnershipService).removeLandlord(propertyOwnership, landlord)

        val emailCaptor = argumentCaptor<JointLandlordYouLeftConfirmation>()
        verify(emailSender).sendEmail(eq("alice@example.com"), emailCaptor.capture())
        val sentEmail = emailCaptor.firstValue
        assertEquals("Alice", sentEmail.recipientName)
        assertEquals(address.toMultiLineAddress(), sentEmail.propertyAddress)
    }

    @Test
    fun `addLeftPropertyOwnershipToSession appends the property ownership to the list stored in the session`() {
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val existingOwnerships = mutableMapOf((456L to "First address"), (789L to "Second address"))
        whenever(mockHttpSession.getAttribute(PROPERTIES_LEFT_THIS_SESSION)).thenReturn(existingOwnerships)

        leavePropertyService.addLeftPropertyOwnershipToSession(propertyOwnership)

        verify(mockHttpSession).setAttribute(
            PROPERTIES_LEFT_THIS_SESSION,
            existingOwnerships + (propertyOwnership.id to propertyOwnership.address.singleLineAddress),
        )
    }

    @Test
    fun `getLeftPropertyOwnershipsFromSession returns the list stored in the session`() {
        val leftOwnerships = mutableMapOf((456L to "First address"), (789L to "Second address"))
        whenever(mockHttpSession.getAttribute(PROPERTIES_LEFT_THIS_SESSION)).thenReturn(leftOwnerships)

        val result = leavePropertyService.getLeftPropertyOwnershipsFromSession()

        assertEquals(leftOwnerships, result)
    }

    @Test
    fun `getLeftPropertyOwnershipsFromSession returns an empty list when nothing is stored in the session`() {
        whenever(mockHttpSession.getAttribute(PROPERTIES_LEFT_THIS_SESSION)).thenReturn(null)

        val result = leavePropertyService.getLeftPropertyOwnershipsFromSession()

        assertEquals(emptyMap(), result)
    }
}
