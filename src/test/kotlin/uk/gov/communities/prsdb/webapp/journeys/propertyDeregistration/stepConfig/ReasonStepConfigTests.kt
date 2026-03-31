package uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.PropertyDeregistrationJourneyState
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyDeregistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.PropertyDeregistrationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

@ExtendWith(MockitoExtension::class)
class ReasonStepConfigTests {
    @Mock
    lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    lateinit var mockPropertyDeregistrationService: PropertyDeregistrationService

    @Mock
    lateinit var mockConfirmationEmailSender: EmailNotificationService<PropertyDeregistrationConfirmationEmail>

    @Mock
    lateinit var mockState: PropertyDeregistrationJourneyState

    val propertyOwnershipId = 123L

    @Test
    fun `afterStepDataIsAdded deregisters the property`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyOwnershipId)
        whenever(mockState.propertyOwnershipId).thenReturn(propertyOwnershipId)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)

        // Act
        stepConfig.afterStepDataIsAdded(mockState)

        // Assert
        verify(mockPropertyDeregistrationService).deregisterProperty(propertyOwnershipId)
    }

    @Test
    fun `afterStepDataIsAdded adds deregistered property ownership id to session`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyOwnershipId)
        whenever(mockState.propertyOwnershipId).thenReturn(propertyOwnershipId)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)

        // Act
        stepConfig.afterStepDataIsAdded(mockState)

        // Assert
        verify(mockPropertyDeregistrationService).addDeregisteredPropertyOwnershipIdToSession(propertyOwnershipId)
    }

    @Test
    fun `afterStepDataIsAdded sends confirmation email to primary landlord`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val landlordEmail = "landlord@example.com"
        val landlord = MockLandlordData.createLandlord(email = landlordEmail)
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyOwnershipId, primaryLandlord = landlord)
        whenever(mockState.propertyOwnershipId).thenReturn(propertyOwnershipId)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)

        // Act
        stepConfig.afterStepDataIsAdded(mockState)

        // Assert
        verify(mockConfirmationEmailSender).sendEmail(eq(landlordEmail), any<PropertyDeregistrationConfirmationEmail>())
    }

    @Test
    fun `afterStepDataIsAdded sends confirmation email with correct property address`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val propertyAddress = "123 Test Street, AB1 2CD"
        val address = MockLandlordData.createAddress(singleLineAddress = propertyAddress)
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyOwnershipId, address = address)
        whenever(mockState.propertyOwnershipId).thenReturn(propertyOwnershipId)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)

        // Act
        stepConfig.afterStepDataIsAdded(mockState)

        // Assert
        verify(mockConfirmationEmailSender).sendEmail(
            any(),
            argThat<PropertyDeregistrationConfirmationEmail> { this.singleLineAddress == propertyAddress },
        )
    }

    private fun setupStepConfig(): ReasonStepConfig {
        val stepConfig =
            ReasonStepConfig(
                mockPropertyOwnershipService,
                mockPropertyDeregistrationService,
                mockConfirmationEmailSender,
            )
        stepConfig.routeSegment = ReasonStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
