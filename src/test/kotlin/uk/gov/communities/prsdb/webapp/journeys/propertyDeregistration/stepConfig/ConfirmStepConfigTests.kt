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
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyDeregistrationEmailDetails
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyDeregistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.PropertyDeregistrationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class ConfirmStepConfigTests {
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
        val stepConfig = setupStepConfig()
        whenever(mockState.propertyOwnershipId).thenReturn(propertyOwnershipId)
        whenever(mockPropertyDeregistrationService.deregisterProperty(propertyOwnershipId))
            .thenReturn(emailDetails())

        stepConfig.afterStepDataIsAdded(mockState)

        verify(mockPropertyDeregistrationService).deregisterProperty(propertyOwnershipId)
    }

    @Test
    fun `afterStepDataIsAdded adds deregistered property ownership id to session`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.propertyOwnershipId).thenReturn(propertyOwnershipId)
        whenever(mockPropertyDeregistrationService.deregisterProperty(propertyOwnershipId))
            .thenReturn(emailDetails())

        stepConfig.afterStepDataIsAdded(mockState)

        verify(mockPropertyDeregistrationService).addDeregisteredPropertyOwnershipIdToSession(propertyOwnershipId)
    }

    @Test
    fun `afterStepDataIsAdded sends confirmation email to each landlord`() {
        val stepConfig = setupStepConfig()
        val landlordEmail = "landlord@example.com"
        whenever(mockState.propertyOwnershipId).thenReturn(propertyOwnershipId)
        whenever(mockPropertyDeregistrationService.deregisterProperty(propertyOwnershipId))
            .thenReturn(emailDetails(landlordEmailAddresses = listOf(landlordEmail)))

        stepConfig.afterStepDataIsAdded(mockState)

        verify(mockConfirmationEmailSender).sendEmail(eq(landlordEmail), any<PropertyDeregistrationConfirmationEmail>())
    }

    @Test
    fun `afterStepDataIsAdded sends confirmation email with correct property address`() {
        val stepConfig = setupStepConfig()
        val propertyAddress = "123 Test Street, AB1 2CD"
        whenever(mockState.propertyOwnershipId).thenReturn(propertyOwnershipId)
        whenever(mockPropertyDeregistrationService.deregisterProperty(propertyOwnershipId))
            .thenReturn(emailDetails(singleLineAddress = propertyAddress))

        stepConfig.afterStepDataIsAdded(mockState)

        verify(mockConfirmationEmailSender).sendEmail(
            any(),
            argThat<PropertyDeregistrationConfirmationEmail> { this.singleLineAddress == propertyAddress },
        )
    }

    private fun emailDetails(
        landlordEmailAddresses: List<String> = listOf("landlord@example.com"),
        prn: String = "P1234",
        singleLineAddress: String = "123 Test Street, AB1 2CD",
    ) = PropertyDeregistrationEmailDetails(landlordEmailAddresses, prn, singleLineAddress)

    private fun setupStepConfig(): ConfirmStepConfig {
        val stepConfig =
            ConfirmStepConfig(
                mockPropertyOwnershipService,
                mockPropertyDeregistrationService,
                mockConfirmationEmailSender,
            )
        stepConfig.routeSegment = ConfirmStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
