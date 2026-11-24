package uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.ReactivationTaskType
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.PropertyReactivationJourneyState
import uk.gov.communities.prsdb.webapp.services.AddressAvailabilityService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

@ExtendWith(MockitoExtension::class)
class ReactivationInfoStepConfigTests {
    @Mock
    lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    lateinit var mockAddressAvailabilityService: AddressAvailabilityService

    @Mock
    lateinit var mockState: PropertyReactivationJourneyState

    private val propertyOwnershipId = 123L

    @Test
    fun `chooseTemplate returns the reactivate property info form`() {
        val stepConfig = setupStepConfig()

        val result = stepConfig.chooseTemplate(mockState)

        assertEquals("forms/reactivatePropertyInfoForm", result)
    }

    @Test
    fun `getStepSpecificContent provides the inactive property's address lines`() {
        val stepConfig = setupStepConfig()
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyOwnershipId, isActive = false)
        whenever(mockState.propertyOwnershipId).thenReturn(propertyOwnershipId)
        whenever(mockPropertyOwnershipService.getInactivePropertyOwnershipForCurrentLandlord(propertyOwnershipId))
            .thenReturn(propertyOwnership)

        val result = stepConfig.getStepSpecificContent(mockState)

        assertEquals(propertyOwnership.address.toMultiLineAddress().split("\n"), result["addressLines"])
    }

    @Test
    fun `getStepSpecificContent marks the button as a transaction when there are no details to provide and the address is available`() {
        val stepConfig = setupStepConfig()
        mockInactivePropertyWithUprn(uprn = 456L)
        whenever(mockState.requiredTaskTypes).thenReturn(emptyList())
        whenever(mockAddressAvailabilityService.isAddressOwned(456L)).thenReturn(false)

        val result = stepConfig.getStepSpecificContent(mockState)

        assertEquals(true, result["isTransaction"])
    }

    @Test
    fun `getStepSpecificContent does not mark the button as a transaction when there are details to provide`() {
        val stepConfig = setupStepConfig()
        mockInactivePropertyWithUprn(uprn = 456L)
        whenever(mockState.requiredTaskTypes).thenReturn(listOf(ReactivationTaskType.GAS_SAFETY))

        val result = stepConfig.getStepSpecificContent(mockState)

        assertEquals(false, result["isTransaction"])
        verifyNoInteractions(mockAddressAvailabilityService)
    }

    @Test
    fun `getStepSpecificContent does not mark the button as a transaction when the address is already registered`() {
        val stepConfig = setupStepConfig()
        mockInactivePropertyWithUprn(uprn = 456L)
        whenever(mockState.requiredTaskTypes).thenReturn(emptyList())
        whenever(mockAddressAvailabilityService.isAddressOwned(456L)).thenReturn(true)

        val result = stepConfig.getStepSpecificContent(mockState)

        assertEquals(false, result["isTransaction"])
    }

    @Test
    fun `mode returns null when the info step has not been submitted`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(ReactivationInfoStep.ROUTE_SEGMENT)).thenReturn(null)

        assertNull(stepConfig.mode(mockState))
    }

    @Test
    fun `mode returns PROPERTY_ALREADY_REGISTERED when the property is already registered`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(ReactivationInfoStep.ROUTE_SEGMENT)).thenReturn(mapOf())
        whenever(mockState.isPropertyAlreadyRegistered).thenReturn(true)

        assertEquals(ReactivationInfoMode.PROPERTY_ALREADY_REGISTERED, stepConfig.mode(mockState))
    }

    @Test
    fun `mode returns PROPERTY_AVAILABLE when the property is not already registered`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(ReactivationInfoStep.ROUTE_SEGMENT)).thenReturn(mapOf())
        whenever(mockState.isPropertyAlreadyRegistered).thenReturn(false)

        assertEquals(ReactivationInfoMode.PROPERTY_AVAILABLE, stepConfig.mode(mockState))
    }

    @Test
    fun `afterStepDataIsAdded records that the property is already registered when its UPRN is actively owned`() {
        val stepConfig = setupStepConfig()
        mockInactivePropertyWithUprn(uprn = 456L)
        whenever(mockAddressAvailabilityService.isAddressOwned(456L)).thenReturn(true)

        stepConfig.afterStepDataIsAdded(mockState)

        verify(mockState).isPropertyAlreadyRegistered = true
    }

    @Test
    fun `afterStepDataIsAdded records that the property is not already registered when its UPRN is not actively owned`() {
        val stepConfig = setupStepConfig()
        mockInactivePropertyWithUprn(uprn = 456L)
        whenever(mockAddressAvailabilityService.isAddressOwned(456L)).thenReturn(false)

        stepConfig.afterStepDataIsAdded(mockState)

        verify(mockState).isPropertyAlreadyRegistered = false
    }

    @Test
    fun `afterStepDataIsAdded records that the property is not already registered when it has no UPRN`() {
        val stepConfig = setupStepConfig()
        mockInactivePropertyWithUprn(uprn = null)

        stepConfig.afterStepDataIsAdded(mockState)

        verify(mockState).isPropertyAlreadyRegistered = false
        verifyNoInteractions(mockAddressAvailabilityService)
    }

    private fun mockInactivePropertyWithUprn(uprn: Long?) {
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                id = propertyOwnershipId,
                isActive = false,
                address = MockLandlordData.createAddress(uprn = uprn),
            )
        whenever(mockState.propertyOwnershipId).thenReturn(propertyOwnershipId)
        whenever(mockPropertyOwnershipService.getInactivePropertyOwnershipForCurrentLandlord(propertyOwnershipId))
            .thenReturn(propertyOwnership)
    }

    private fun setupStepConfig(): ReactivationInfoStepConfig {
        val stepConfig = ReactivationInfoStepConfig(mockPropertyOwnershipService, mockAddressAvailabilityService)
        stepConfig.urlPath = ReactivationInfoStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
