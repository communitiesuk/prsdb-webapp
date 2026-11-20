package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityExistsException
import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.util.ReflectionTestUtils
import uk.gov.communities.prsdb.webapp.constants.REACTIVATED_PROPERTY_OWNERSHIP_ID
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyReactivationConfirmationEmail
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.net.URI

@ExtendWith(MockitoExtension::class)
class PropertyReactivationServiceTests {
    @Mock
    private lateinit var mockActiveInactiveService: ActiveInactiveService

    @Mock
    private lateinit var mockAddressAvailabilityService: AddressAvailabilityService

    @Mock
    private lateinit var mockSession: HttpSession

    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockCurrentUserService: CurrentUserService

    @Mock
    private lateinit var mockUserToLandlordService: UserToLandlordService

    @Mock
    private lateinit var mockAbsoluteUrlProvider: AbsoluteUrlProvider

    @Mock
    private lateinit var mockConfirmationEmailSender: EmailNotificationService<PropertyReactivationConfirmationEmail>

    @InjectMocks
    private lateinit var propertyReactivationService: PropertyReactivationService

    private val propertyOwnershipId = 1L
    private val uprn = 2L
    private val currentLandlord = createLandlordWithId(1L)
    private val propertyOwnership =
        MockLandlordData.createPropertyOwnership(id = propertyOwnershipId, landlords = mutableSetOf(currentLandlord))

    @Test
    fun `reactivatePropertyIfAddressAvailable reactivates the property and stores it in the session when its address is available`() {
        // Arrange
        whenever(mockAddressAvailabilityService.isAddressOwned(uprn)).thenReturn(false)
        arrangeConfirmationEmail()

        // Act
        propertyReactivationService.reactivatePropertyIfAddressAvailable(propertyOwnershipId, uprn)

        // Assert
        verify(mockActiveInactiveService).reactivateProperty(propertyOwnershipId)
        verify(mockSession).setAttribute(REACTIVATED_PROPERTY_OWNERSHIP_ID, propertyOwnershipId)
    }

    @Test
    fun `reactivatePropertyIfAddressAvailable throws an EntityExistsException without reactivating when the address is unavailable`() {
        // Arrange
        whenever(mockAddressAvailabilityService.isAddressOwned(uprn)).thenReturn(true)

        // Act & Assert
        assertThrows<EntityExistsException> { propertyReactivationService.reactivatePropertyIfAddressAvailable(propertyOwnershipId, uprn) }
        verifyNoInteractions(mockActiveInactiveService)
        verifyNoInteractions(mockSession)
        verifyNoInteractions(mockConfirmationEmailSender)
    }

    @Test
    fun `reactivatePropertyIfAddressAvailable reactivates the property without checking its address when it has no UPRN`() {
        // Arrange
        arrangeConfirmationEmail()

        // Act
        propertyReactivationService.reactivatePropertyIfAddressAvailable(propertyOwnershipId, null)

        // Assert
        verify(mockActiveInactiveService).reactivateProperty(propertyOwnershipId)
        verify(mockSession).setAttribute(REACTIVATED_PROPERTY_OWNERSHIP_ID, propertyOwnershipId)
        verifyNoInteractions(mockAddressAvailabilityService)
    }

    @Test
    fun `reactivatePropertyIfAddressAvailable emails the current user a reactivation confirmation`() {
        // Arrange
        whenever(mockAddressAvailabilityService.isAddressOwned(uprn)).thenReturn(false)
        arrangeConfirmationEmail()

        // Act
        propertyReactivationService.reactivatePropertyIfAddressAvailable(propertyOwnershipId, uprn)

        // Assert
        verify(mockConfirmationEmailSender).sendEmail(
            eq("james@example.com"),
            eq(
                PropertyReactivationConfirmationEmail(
                    recipientName = "James",
                    multiLineAddress = propertyOwnership.address.toMultiLineAddress(),
                    registrationNumber =
                        RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnership.registrationNumber).toString(),
                    dashboardUrl = "https://example.com/dashboard",
                    hasJointLandlords = false,
                    landlordReactivated = false,
                ),
            ),
        )
    }

    @Test
    fun `reactivatePropertyIfAddressAvailable includes the joint landlords section in the email when the property has other landlords`() {
        // Arrange
        whenever(mockAddressAvailabilityService.isAddressOwned(uprn)).thenReturn(false)
        val propertyOwnershipWithJointLandlord =
            MockLandlordData.createPropertyOwnership(
                id = propertyOwnershipId,
                landlords = mutableSetOf(currentLandlord, createLandlordWithId(2L)),
            )
        arrangeConfirmationEmail(propertyOwnership = propertyOwnershipWithJointLandlord)

        // Act
        propertyReactivationService.reactivatePropertyIfAddressAvailable(propertyOwnershipId, uprn)

        // Assert
        verify(mockConfirmationEmailSender).sendEmail(
            eq("james@example.com"),
            argThat<PropertyReactivationConfirmationEmail> { hasJointLandlords },
        )
    }

    @Test
    fun `reactivatePropertyIfAddressAvailable includes the landlord account section in the email when the landlord was inactive`() {
        // Arrange
        whenever(mockAddressAvailabilityService.isAddressOwned(uprn)).thenReturn(false)
        arrangeConfirmationEmail(isLandlordInactive = true)

        // Act
        propertyReactivationService.reactivatePropertyIfAddressAvailable(propertyOwnershipId, uprn)

        // Assert
        val inOrder = inOrder(mockActiveInactiveService)
        inOrder.verify(mockActiveInactiveService).isCurrentLandlordInactive()
        inOrder.verify(mockActiveInactiveService).reactivateProperty(propertyOwnershipId)
        verify(mockConfirmationEmailSender).sendEmail(
            eq("james@example.com"),
            argThat<PropertyReactivationConfirmationEmail> { landlordReactivated },
        )
    }

    @Test
    fun `reactivatePropertyIfAddressAvailable still reactivates the property when the confirmation email fails to send`() {
        // Arrange
        whenever(mockAddressAvailabilityService.isAddressOwned(uprn)).thenReturn(false)
        arrangeConfirmationEmail()
        whenever(mockConfirmationEmailSender.sendEmail(any(), any())).thenThrow(RuntimeException("Notify is down"))

        // Act & Assert
        assertDoesNotThrow { propertyReactivationService.reactivatePropertyIfAddressAvailable(propertyOwnershipId, uprn) }
        verify(mockActiveInactiveService).reactivateProperty(propertyOwnershipId)
        verify(mockSession).setAttribute(REACTIVATED_PROPERTY_OWNERSHIP_ID, propertyOwnershipId)
    }

    private fun arrangeConfirmationEmail(
        isLandlordInactive: Boolean = false,
        propertyOwnership: PropertyOwnership = this.propertyOwnership,
    ) {
        whenever(mockActiveInactiveService.isCurrentLandlordInactive()).thenReturn(isLandlordInactive)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)
        whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(currentLandlord)
        whenever(mockCurrentUserService.getRegisteredUserDetails())
            .thenReturn(CurrentUserService.RegisteredUserDetails(name = "James", email = "james@example.com"))
        whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI("https://example.com/dashboard"))
    }

    private fun createLandlordWithId(id: Long) =
        MockLandlordData.createIndividualLandlord().also { ReflectionTestUtils.setField(it, "id", id) }
}
