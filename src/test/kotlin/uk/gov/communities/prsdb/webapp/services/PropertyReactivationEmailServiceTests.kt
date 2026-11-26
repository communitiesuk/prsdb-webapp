package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.util.ReflectionTestUtils
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordPropertyReactivatedNotification
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyReactivationConfirmationEmail
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.net.URI

@ExtendWith(MockitoExtension::class)
class PropertyReactivationEmailServiceTests {
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

    @Mock
    private lateinit var mockJointLandlordEmailSender: EmailNotificationService<JointLandlordPropertyReactivatedNotification>

    private lateinit var jointLandlordOwnership: PropertyOwnership

    private val propertyReactivationEmailService by lazy {
        PropertyReactivationEmailService(
            mockPropertyOwnershipService,
            mockCurrentUserService,
            mockUserToLandlordService,
            mockAbsoluteUrlProvider,
            mockConfirmationEmailSender,
            mockJointLandlordEmailSender,
        )
    }

    private val propertyOwnershipId = 1L
    private val currentLandlord = createLandlordWithId(1L)
    private val propertyOwnership =
        MockLandlordData.createPropertyOwnership(id = propertyOwnershipId, landlords = mutableSetOf(currentLandlord))

    @Test
    fun `sendConfirmationEmail emails the current user a reactivation confirmation`() {
        // Arrange
        arrangeConfirmationEmail()

        // Act
        propertyReactivationEmailService.sendConfirmationEmail(propertyOwnershipId, landlordReactivated = false)

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
    fun `sendConfirmationEmail includes the joint landlords section in the email when the property has other landlords`() {
        // Arrange
        val propertyOwnershipWithJointLandlord =
            MockLandlordData.createPropertyOwnership(
                id = propertyOwnershipId,
                landlords = mutableSetOf(currentLandlord, createLandlordWithId(2L)),
            )
        arrangeConfirmationEmail(propertyOwnershipWithJointLandlord)

        // Act
        propertyReactivationEmailService.sendConfirmationEmail(propertyOwnershipId, landlordReactivated = false)

        // Assert
        verify(mockConfirmationEmailSender).sendEmail(
            eq("james@example.com"),
            argThat<PropertyReactivationConfirmationEmail> { hasJointLandlords },
        )
    }

    @Test
    fun `sendConfirmationEmail includes the landlord account section in the email when the landlord was reactivated`() {
        // Arrange
        arrangeConfirmationEmail()

        // Act
        propertyReactivationEmailService.sendConfirmationEmail(propertyOwnershipId, landlordReactivated = true)

        // Assert
        verify(mockConfirmationEmailSender).sendEmail(
            eq("james@example.com"),
            argThat<PropertyReactivationConfirmationEmail> { landlordReactivated },
        )
    }

    @Test
    fun `sendJointLandlordNotifications emails the other landlords on the property with the property details`() {
        // Arrange
        val jointLandlord = createLandlordWithId(2L)
        arrangeJointLandlordNotifications(mutableSetOf(currentLandlord, jointLandlord))

        // Act
        propertyReactivationEmailService.sendJointLandlordNotifications(propertyOwnershipId)

        // Assert
        verify(mockJointLandlordEmailSender).sendEmailToLandlord(
            eq(jointLandlord),
            eq(
                JointLandlordPropertyReactivatedNotification(
                    recipientName = jointLandlord.name,
                    multiLineAddress = jointLandlordOwnership.address.toMultiLineAddress(),
                    registrationNumber =
                        RegistrationNumberDataModel.fromRegistrationNumber(jointLandlordOwnership.registrationNumber).toString(),
                    propertyRecordUrl = "https://example.com/property",
                ),
            ),
        )
    }

    @Test
    fun `sendJointLandlordNotifications does not email the landlord who reactivated the property`() {
        // Arrange
        arrangeJointLandlordNotifications(mutableSetOf(currentLandlord, createLandlordWithId(2L)))

        // Act
        propertyReactivationEmailService.sendJointLandlordNotifications(propertyOwnershipId)

        // Assert
        verify(mockJointLandlordEmailSender, never()).sendEmailToLandlord(eq(currentLandlord), any())
    }

    @Test
    fun `sendJointLandlordNotifications sends nothing when the property has no other landlords`() {
        // Arrange
        arrangeJointLandlordNotifications(mutableSetOf(currentLandlord))

        // Act
        propertyReactivationEmailService.sendJointLandlordNotifications(propertyOwnershipId)

        // Assert
        verifyNoInteractions(mockJointLandlordEmailSender)
    }

    @Test
    fun `sendJointLandlordNotifications still emails the remaining landlords when one email fails`() {
        // Arrange
        val failingLandlord = createLandlordWithId(2L)
        val otherLandlord = createLandlordWithId(3L)
        arrangeJointLandlordNotifications(linkedSetOf(currentLandlord, failingLandlord, otherLandlord))
        whenever(mockJointLandlordEmailSender.sendEmailToLandlord(eq(failingLandlord), any())).thenThrow(RuntimeException("Notify is down"))

        // Act
        propertyReactivationEmailService.sendJointLandlordNotifications(propertyOwnershipId)

        // Assert
        verify(mockJointLandlordEmailSender).sendEmailToLandlord(eq(otherLandlord), any())
    }

    private fun arrangeJointLandlordNotifications(landlords: MutableSet<Landlord>) {
        jointLandlordOwnership = MockLandlordData.createPropertyOwnership(id = propertyOwnershipId, landlords = landlords)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(jointLandlordOwnership)
        whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(currentLandlord)
        whenever(mockAbsoluteUrlProvider.buildPropertyDetailsUri(propertyOwnershipId)).thenReturn(URI("https://example.com/property"))
    }

    private fun arrangeConfirmationEmail(propertyOwnership: PropertyOwnership = this.propertyOwnership) {
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)
        whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(currentLandlord)
        whenever(mockCurrentUserService.getRegisteredUserDetails())
            .thenReturn(CurrentUserService.RegisteredUserDetails(name = "James", email = "james@example.com"))
        whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI("https://example.com/dashboard"))
    }

    private fun createLandlordWithId(id: Long) =
        MockLandlordData.createIndividualLandlord().also { ReflectionTestUtils.setField(it, "id", id) }
}
