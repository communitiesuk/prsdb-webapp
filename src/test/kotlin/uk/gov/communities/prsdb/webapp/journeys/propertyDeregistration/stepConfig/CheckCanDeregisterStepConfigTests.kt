package uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.PropertyDeregistrationJourneyState
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

@ExtendWith(MockitoExtension::class)
class CheckCanDeregisterStepConfigTests {
    @Mock
    lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    lateinit var mockState: PropertyDeregistrationJourneyState

    val propertyOwnershipId = 123L

    @Test
    fun `mode returns SINGLE_LANDLORD when the property has a single landlord`() {
        val stepConfig = setupStepConfig()
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyOwnershipId)
        whenever(mockState.propertyOwnershipId).thenReturn(propertyOwnershipId)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)

        val result = stepConfig.mode(mockState)

        assertEquals(CanDeregisterMode.SINGLE_LANDLORD, result)
    }

    @Test
    fun `mode returns HAS_JOINT_LANDLORDS when the property has multiple landlords`() {
        val stepConfig = setupStepConfig()
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                id = propertyOwnershipId,
                landlords =
                    mutableSetOf(
                        MockLandlordData.createLandlord(name = "Landlord 1"),
                        MockLandlordData.createLandlord(name = "Landlord 2"),
                    ),
            )
        whenever(mockState.propertyOwnershipId).thenReturn(propertyOwnershipId)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)

        val result = stepConfig.mode(mockState)

        assertEquals(CanDeregisterMode.HAS_JOINT_LANDLORDS, result)
    }

    private fun setupStepConfig(): CheckCanDeregisterStepConfig {
        val stepConfig = CheckCanDeregisterStepConfig(mockPropertyOwnershipService)
        stepConfig.routeSegment = CheckCanDeregisterStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
