package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.HasEpc
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.EpcState
import uk.gov.communities.prsdb.webapp.services.EpcLookupService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

@ExtendWith(MockitoExtension::class)
class EpcQuestionStepConfigTests {
    @Mock
    lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    lateinit var mockEpcLookupService: EpcLookupService

    @Mock
    lateinit var mockEpcState: EpcState

    val routeSegment = EpcQuestionStep.ROUTE_SEGMENT
    val propertyId = 123L
    val uprn = 456L

    @Test
    fun `mode returns null when form model is not present`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockEpcState.getStepData(routeSegment)).thenReturn(null)

        // Act
        val result = stepConfig.mode(mockEpcState)

        // Assert
        assertNull(result)
    }

    @Test
    fun `mode returns null when hasCert is null`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockEpcState.getStepData(routeSegment)).thenReturn(emptyMap())

        // Act
        val result = stepConfig.mode(mockEpcState)

        // Assert
        assertNull(result)
    }

    @Test
    fun `mode returns AUTOMATCHED when hasCert is YES and automatchedEpc is not null`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val epcData = MockEpcData.createEpcDataModel()
        whenever(mockEpcState.getStepData(routeSegment)).thenReturn(mapOf("hasCert" to HasEpc.YES))
        whenever(mockEpcState.automatchedEpc).thenReturn(epcData)

        // Act
        val result = stepConfig.mode(mockEpcState)

        // Assert
        assertEquals(EpcStatusMode.AUTOMATCHED, result)
    }

    @Test
    fun `mode returns NOT_AUTOMATCHED when hasCert is YES and automatchedEpc is null`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockEpcState.getStepData(routeSegment)).thenReturn(mapOf("hasCert" to HasEpc.YES))
        whenever(mockEpcState.automatchedEpc).thenReturn(null)

        // Act
        val result = stepConfig.mode(mockEpcState)

        // Assert
        assertEquals(EpcStatusMode.NOT_AUTOMATCHED, result)
    }

    @Test
    fun `mode returns NO_EPC when hasCert is NO`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockEpcState.getStepData(routeSegment)).thenReturn(mapOf("hasCert" to HasEpc.NO))

        // Act
        val result = stepConfig.mode(mockEpcState)

        // Assert
        assertEquals(EpcStatusMode.NO_EPC, result)
    }

    @Test
    fun `mode returns EPC_NOT_REQUIRED when hasCert is NOT_REQUIRED`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockEpcState.getStepData(routeSegment)).thenReturn(mapOf("hasCert" to HasEpc.NOT_REQUIRED))

        // Act
        val result = stepConfig.mode(mockEpcState)

        // Assert
        assertEquals(EpcStatusMode.EPC_NOT_REQUIRED, result)
    }

    @Test
    fun `afterStepDataIsAdded looks up EPC by UPRN when UPRN is present`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockEpcState.getStepData(routeSegment)).thenReturn(mapOf("hasCert" to HasEpc.YES))
        val epcData = MockEpcData.createEpcDataModel()
        val address = MockLandlordData.createAddress(uprn = uprn)
        val propertyOwnership = MockLandlordData.createPropertyOwnership(address = address, id = propertyId)

        whenever(mockEpcState.propertyId).thenReturn(propertyId)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(propertyOwnership)
        whenever(mockEpcLookupService.getEpcByUprn(uprn)).thenReturn(epcData)

        // Act
        stepConfig.afterStepDataIsAdded(mockEpcState)

        // Assert
        verify(mockPropertyOwnershipService).getPropertyOwnership(propertyId)
        verify(mockEpcLookupService).getEpcByUprn(uprn)
        verify(mockEpcState).automatchedEpc = epcData
    }

    @Test
    fun `afterStepDataIsAdded does not look up EPC when UPRN is null`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockEpcState.getStepData(routeSegment)).thenReturn(mapOf("hasCert" to HasEpc.YES))
        val address = MockLandlordData.createAddress(uprn = null)
        val propertyOwnership = MockLandlordData.createPropertyOwnership(address = address, id = propertyId)

        whenever(mockEpcState.propertyId).thenReturn(propertyId)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(propertyOwnership)

        // Act
        stepConfig.afterStepDataIsAdded(mockEpcState)

        // Assert
        verify(mockPropertyOwnershipService).getPropertyOwnership(propertyId)
        verify(mockEpcLookupService, never()).getEpcByUprn(any())
    }

    @Test
    fun `afterStepDataIsAdded sets automatchedEpc to null when EPC lookup returns null`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockEpcState.getStepData(routeSegment)).thenReturn(mapOf("hasCert" to HasEpc.YES))
        val address = MockLandlordData.createAddress(uprn = uprn)
        val propertyOwnership = MockLandlordData.createPropertyOwnership(address = address, id = propertyId)

        whenever(mockEpcState.propertyId).thenReturn(propertyId)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(propertyOwnership)
        whenever(mockEpcLookupService.getEpcByUprn(uprn)).thenReturn(null)

        // Act
        stepConfig.afterStepDataIsAdded(mockEpcState)

        // Assert
        verify(mockPropertyOwnershipService).getPropertyOwnership(propertyId)
        verify(mockEpcLookupService).getEpcByUprn(uprn)
        verify(mockEpcState).automatchedEpc = null
    }

    @Test
    fun `afterStepDataIsAdded does not look up EPC when hasCert is not YES`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockEpcState.getStepData(routeSegment)).thenReturn(mapOf("hasCert" to HasEpc.NO))

        // Act
        stepConfig.afterStepDataIsAdded(mockEpcState)

        // Assert
        verify(mockPropertyOwnershipService, never()).getPropertyOwnership(any())
        verify(mockEpcLookupService, never()).getEpcByUprn(any())
    }

    private fun setupStepConfig(): EpcQuestionStepConfig {
        val stepConfig = EpcQuestionStepConfig(mockPropertyOwnershipService, mockEpcLookupService)
        stepConfig.routeSegment = routeSegment
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
