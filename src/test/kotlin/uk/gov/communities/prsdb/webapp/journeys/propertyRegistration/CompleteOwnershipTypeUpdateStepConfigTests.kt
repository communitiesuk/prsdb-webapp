package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.ownershipType.CompleteOwnershipTypeUpdateStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.ownershipType.UpdateOwnershipTypeJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OwnershipTypeFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyUpdateConfirmation
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.net.URI

@ExtendWith(MockitoExtension::class)
class CompleteOwnershipTypeUpdateStepConfigTests {
    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockEmailNotificationService: EmailNotificationService<PropertyUpdateConfirmation>

    @Mock
    private lateinit var mockAbsoluteUrlProvider: AbsoluteUrlProvider

    @Mock
    private lateinit var mockState: UpdateOwnershipTypeJourneyState

    @Mock
    private lateinit var mockOwnershipTypeStep: OwnershipTypeStep

    @Mock
    private lateinit var mockOwnershipTypeFormModel: OwnershipTypeFormModel

    private val propertyId = 123L

    @Mock
    private lateinit var stepConfig: CompleteOwnershipTypeUpdateStepConfig

    @BeforeEach
    fun setUp() {
        stepConfig =
            CompleteOwnershipTypeUpdateStepConfig(
                propertyOwnershipService = mockPropertyOwnershipService,
                updateConfirmationEmailService = mockEmailNotificationService,
                absoluteUrlProvider = mockAbsoluteUrlProvider,
            )
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockState.ownershipTypeStep).thenReturn(mockOwnershipTypeStep)
        whenever(mockOwnershipTypeStep.formModel).thenReturn(mockOwnershipTypeFormModel)
        whenever(mockOwnershipTypeFormModel.ownershipType).thenReturn(OwnershipType.FREEHOLD)
    }

    @Test
    fun `afterStepIsReached sends confirmation email to primary landlord`() {
        val landlordEmail = "landlord@example.com"
        val landlord = MockLandlordData.createLandlord(email = landlordEmail)
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyId, primaryLandlord = landlord)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(propertyOwnership)
        whenever(mockAbsoluteUrlProvider.buildComplianceInformationUri(propertyOwnership.id)).thenReturn(URI("http://example.com"))

        stepConfig.afterStepIsReached(mockState)

        verify(mockEmailNotificationService).sendEmail(eq(landlordEmail), any<PropertyUpdateConfirmation>())
    }

    @Test
    fun `afterStepIsReached sends confirmation email with correct updated items`() {
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyId)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(propertyOwnership)
        whenever(mockAbsoluteUrlProvider.buildComplianceInformationUri(propertyOwnership.id)).thenReturn(URI("http://example.com"))

        stepConfig.afterStepIsReached(mockState)

        verify(mockEmailNotificationService).sendEmail(
            any(),
            argThat<PropertyUpdateConfirmation> { this.updatedItems == "The ownership type" },
        )
    }
}
