package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.util.ReflectionTestUtils
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
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

    @InjectMocks
    private lateinit var propertyReactivationEmailService: PropertyReactivationEmailService

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
