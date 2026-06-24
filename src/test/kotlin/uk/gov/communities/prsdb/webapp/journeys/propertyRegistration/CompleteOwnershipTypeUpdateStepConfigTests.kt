package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.ownershipType.CompleteOwnershipTypeUpdateStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.ownershipType.UpdateOwnershipTypeJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OwnershipTypeFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyUpdateEmailService

@ExtendWith(MockitoExtension::class)
class CompleteOwnershipTypeUpdateStepConfigTests {
    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockPropertyUpdateEmailService: PropertyUpdateEmailService

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
                propertyUpdateEmailService = mockPropertyUpdateEmailService,
            )
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockState.lastModifiedDate).thenReturn("2024-01-01T00:00:00Z")
        whenever(mockState.ownershipTypeStep).thenReturn(mockOwnershipTypeStep)
        whenever(mockOwnershipTypeStep.formModel).thenReturn(mockOwnershipTypeFormModel)
        whenever(mockOwnershipTypeFormModel.ownershipType).thenReturn(OwnershipType.FREEHOLD)
    }

    @Test
    fun `afterStepIsReached sends update emails with the correct updated items`() {
        stepConfig.afterStepIsReached(mockState)

        verify(mockPropertyUpdateEmailService).sendUpdateEmails(eq(propertyId), eq(listOf("The ownership type")))
    }

    @Test
    fun `afterStepIsReached deletes the journey and rethrows when an UpdateConflictException is thrown`() {
        whenever(mockPropertyOwnershipService.updateOwnershipType(any(), any(), any())).thenThrow(UpdateConflictException::class.java)

        assertThrows<UpdateConflictException> { stepConfig.afterStepIsReached(mockState) }

        verify(mockState).deleteJourney()
    }
}
