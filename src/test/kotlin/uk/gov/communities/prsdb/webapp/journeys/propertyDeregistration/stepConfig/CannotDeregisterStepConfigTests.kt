package uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.controllers.LeavePropertyController
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.PropertyDeregistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

@ExtendWith(MockitoExtension::class)
class CannotDeregisterStepConfigTests {
    @Mock
    lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    lateinit var mockAbsoluteUrlProvider: AbsoluteUrlProvider

    @Mock
    lateinit var mockState: PropertyDeregistrationJourneyState

    val propertyOwnershipId = 123L

    @Test
    fun `mode returns null when form model is not present in state`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(CannotDeregisterStep.ROUTE_SEGMENT)).thenReturn(null)

        val result = stepConfig.mode(mockState)

        assertNull(result)
    }

    @Test
    fun `mode returns COMPLETE when form model is present in state`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(CannotDeregisterStep.ROUTE_SEGMENT)).thenReturn(emptyMap())

        val result = stepConfig.mode(mockState)

        assertEquals(Complete.COMPLETE, result)
    }

    @Test
    fun `chooseTemplate returns cannotDeregisterPropertyJointLandlords`() {
        val stepConfig = setupStepConfig()

        val result = stepConfig.chooseTemplate(mockState)

        assertEquals("cannotDeregisterPropertyJointLandlords", result)
    }

    @Test
    fun `getStepSpecificContent provides the property address lines and a no longer a landlord url`() {
        val stepConfig = setupStepConfig()
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyOwnershipId)
        whenever(mockState.propertyOwnershipId).thenReturn(propertyOwnershipId)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)

        val result = stepConfig.getStepSpecificContent(mockState)

        assertEquals(propertyOwnership.address.toMultiLineAddress().split("\n"), result["addressLines"])
        assertEquals(LeavePropertyController.getLeavePropertyPath(propertyOwnershipId), result["leavePropertyUrl"])
    }

    private fun setupStepConfig(): CannotDeregisterStepConfig {
        val stepConfig = CannotDeregisterStepConfig(mockPropertyOwnershipService, mockAbsoluteUrlProvider)
        stepConfig.routeSegment = CannotDeregisterStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
