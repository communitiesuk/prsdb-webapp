package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.ownership

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.lenient
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.UnrecoverableJourneyStateException
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.AnyoneInOwnershipChainStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.FreeholdersTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.OwnershipStructureTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.OwnershipTypeTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.SuperiorLandlordsTask
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.AnyoneInOwnershipChainFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyUpdateEmailService
import java.time.Instant
import kotlin.test.assertSame

@ExtendWith(MockitoExtension::class)
class CompleteOwnershipUpdateStepConfigTests {
    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockPropertyUpdateEmailService: PropertyUpdateEmailService

    @Mock
    private lateinit var mockState: UpdateOwnershipJourneyState

    @Mock
    private lateinit var mockOwnershipStructureTask: OwnershipStructureTask

    @Mock
    private lateinit var mockOwnershipTypeTask: OwnershipTypeTask

    @Mock
    private lateinit var mockFreeholdersTask: FreeholdersTask

    @Mock
    private lateinit var mockSuperiorLandlordsTask: SuperiorLandlordsTask

    @Mock
    private lateinit var mockAnyoneInOwnershipChainStep: AnyoneInOwnershipChainStep

    private lateinit var stepConfig: CompleteOwnershipUpdateStepConfig

    private val propertyId = 123L
    private val lastModifiedDate = "2024-01-01T00:00:00Z"

    private val firstFreeholder = FreeholderDataModel(detailsKnown = true, name = "First freeholder", email = "first@example.com")
    private val secondFreeholder = FreeholderDataModel(detailsKnown = false)
    private val superiorLandlord =
        SuperiorLandlordDataModel(detailsKnown = true, name = "Superior landlord", email = "superior@example.com")

    @BeforeEach
    fun setUp() {
        stepConfig = CompleteOwnershipUpdateStepConfig(mockPropertyOwnershipService, mockPropertyUpdateEmailService)
        lenient().`when`(mockState.propertyId).thenReturn(propertyId)
        lenient().`when`(mockState.lastModifiedDate).thenReturn(lastModifiedDate)
        lenient().`when`(mockState.ownershipStructureTask).thenReturn(mockOwnershipStructureTask)
        lenient().`when`(mockOwnershipStructureTask.ownershipTypeTask).thenReturn(mockOwnershipTypeTask)
        lenient().`when`(mockOwnershipStructureTask.freeholdersTask).thenReturn(mockFreeholdersTask)
        lenient().`when`(mockOwnershipStructureTask.superiorLandlordsTask).thenReturn(mockSuperiorLandlordsTask)
        lenient().`when`(mockSuperiorLandlordsTask.anyoneInOwnershipChainStep).thenReturn(mockAnyoneInOwnershipChainStep)
        lenient().`when`(mockFreeholdersTask.freeholdersMap).thenReturn(mapOf(1 to firstFreeholder, 2 to secondFreeholder))
        lenient().`when`(mockSuperiorLandlordsTask.superiorLandlordsMap).thenReturn(mapOf(1 to superiorLandlord))
        lenient()
            .`when`(mockPropertyOwnershipService.updateOwnershipStructure(any(), any(), any(), any(), any()))
            .thenReturn(Pair(emptyList(), emptyList()))
    }

    @Test
    fun `afterStepIsReached passes the freeholders when the ownership type requires freeholder records`() {
        // Arrange
        whenever(mockOwnershipTypeTask.ownershipType).thenReturn(OwnershipType.JOINT_TENANTS)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockPropertyOwnershipService).updateOwnershipStructure(
            propertyId,
            OwnershipType.JOINT_TENANTS,
            listOf(firstFreeholder, secondFreeholder),
            emptyList(),
            Instant.parse(lastModifiedDate),
        )
    }

    @Test
    fun `afterStepIsReached passes the superior landlords when there is anyone in the ownership chain`() {
        // Arrange
        whenever(mockOwnershipTypeTask.ownershipType).thenReturn(OwnershipType.LEASEHOLD_ONLY)
        stubAnyoneInOwnershipChain(true)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockPropertyOwnershipService).updateOwnershipStructure(
            eq(propertyId),
            eq(OwnershipType.LEASEHOLD_ONLY),
            eq(listOf(firstFreeholder, secondFreeholder)),
            eq(listOf(superiorLandlord)),
            any(),
        )
    }

    @Test
    fun `afterStepIsReached passes no superior landlords when there is no one in the ownership chain`() {
        // Arrange
        whenever(mockOwnershipTypeTask.ownershipType).thenReturn(OwnershipType.LEASEHOLD_ONLY)
        stubAnyoneInOwnershipChain(false)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockPropertyOwnershipService).updateOwnershipStructure(
            eq(propertyId),
            eq(OwnershipType.LEASEHOLD_ONLY),
            any(),
            eq(emptyList()),
            any(),
        )
    }

    @Test
    fun `afterStepIsReached passes no freeholders or superior landlords when the ownership type requires neither`() {
        // Arrange
        whenever(mockOwnershipTypeTask.ownershipType).thenReturn(OwnershipType.SOLE_FREEHOLD)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockPropertyOwnershipService).updateOwnershipStructure(
            eq(propertyId),
            eq(OwnershipType.SOLE_FREEHOLD),
            eq(emptyList()),
            eq(emptyList()),
            any(),
        )
    }

    @Test
    fun `afterStepIsReached throws when the ownership type is missing`() {
        // Arrange
        whenever(mockOwnershipTypeTask.ownershipType).thenReturn(null)
        whenever(mockState.journeyId).thenReturn("journey-id")

        // Act & Assert
        assertThrows<UnrecoverableJourneyStateException> { stepConfig.afterStepIsReached(mockState) }
        verifyNoInteractions(mockPropertyOwnershipService, mockPropertyUpdateEmailService)
    }

    @Test
    fun `afterStepIsReached throws when the ownership type requires freeholder records but there are no freeholders`() {
        // Arrange
        whenever(mockOwnershipTypeTask.ownershipType).thenReturn(OwnershipType.JOINT_TENANTS)
        whenever(mockFreeholdersTask.freeholdersMap).thenReturn(null)
        whenever(mockState.journeyId).thenReturn("journey-id")

        // Act & Assert
        assertThrows<UnrecoverableJourneyStateException> { stepConfig.afterStepIsReached(mockState) }
        verifyNoInteractions(mockPropertyOwnershipService, mockPropertyUpdateEmailService)
    }

    @Test
    fun `afterStepIsReached throws when there is anyone in the ownership chain but there are no superior landlords`() {
        // Arrange
        whenever(mockOwnershipTypeTask.ownershipType).thenReturn(OwnershipType.SHARE_OF_SOLE_FREEHOLD)
        stubAnyoneInOwnershipChain(true)
        whenever(mockSuperiorLandlordsTask.superiorLandlordsMap).thenReturn(null)
        whenever(mockState.journeyId).thenReturn("journey-id")

        // Act & Assert
        assertThrows<UnrecoverableJourneyStateException> { stepConfig.afterStepIsReached(mockState) }
        verifyNoInteractions(mockPropertyOwnershipService, mockPropertyUpdateEmailService)
    }

    @Test
    fun `afterStepIsReached sends update emails with the ownership bullet`() {
        // Arrange
        whenever(mockOwnershipTypeTask.ownershipType).thenReturn(OwnershipType.SOLE_FREEHOLD)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockPropertyUpdateEmailService).sendUpdateEmails(propertyId, listOf("The property’s ownership"))
    }

    @Test
    fun `afterStepIsReached deletes the journey and rethrows without sending emails when an UpdateConflictException is thrown`() {
        // Arrange
        whenever(mockOwnershipTypeTask.ownershipType).thenReturn(OwnershipType.SOLE_FREEHOLD)
        whenever(mockPropertyOwnershipService.updateOwnershipStructure(any(), any(), any(), any(), any()))
            .thenThrow(UpdateConflictException::class.java)

        // Act & Assert
        assertThrows<UpdateConflictException> { stepConfig.afterStepIsReached(mockState) }
        verify(mockState).deleteJourney()
        verify(mockPropertyUpdateEmailService, never()).sendUpdateEmails(any(), any())
    }

    @Test
    fun `resolveNextDestination deletes the journey and returns the default destination`() {
        // Arrange
        val defaultDestination = Destination.Nowhere()

        // Act
        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        // Assert
        assertSame(defaultDestination, result)
        verify(mockState).deleteJourney()
    }

    private fun stubAnyoneInOwnershipChain(anyoneInOwnershipChain: Boolean) {
        whenever(mockAnyoneInOwnershipChainStep.formModelIfReachableOrNull)
            .thenReturn(AnyoneInOwnershipChainFormModel(anyoneInOwnershipChain = anyoneInOwnershipChain))
    }
}
