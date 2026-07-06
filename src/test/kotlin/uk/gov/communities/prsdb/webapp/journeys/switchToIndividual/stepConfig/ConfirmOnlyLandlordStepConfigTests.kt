package uk.gov.communities.prsdb.webapp.journeys.switchToIndividual.stepConfig

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.switchToIndividual.SwitchToIndividualJourneyState
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

@ExtendWith(MockitoExtension::class)
class ConfirmOnlyLandlordStepConfigTests {
    @Mock
    lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    lateinit var mockState: SwitchToIndividualJourneyState

    @Test
    fun `mode returns null when form model is not present in state`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(ConfirmOnlyLandlordStep.ROUTE_SEGMENT)).thenReturn(null)

        val result = stepConfig.mode(mockState)

        assertNull(result)
    }

    @Test
    fun `mode returns COMPLETE when form model is present in state`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(ConfirmOnlyLandlordStep.ROUTE_SEGMENT)).thenReturn(emptyMap())

        val result = stepConfig.mode(mockState)

        assertEquals(Complete.COMPLETE, result)
    }

    @Test
    fun `chooseTemplate returns confirmOnlyLandlordForm`() {
        val stepConfig = setupStepConfig()

        val result = stepConfig.chooseTemplate(mockState)

        assertEquals("forms/confirmOnlyLandlordForm", result)
    }

    @Test
    fun `getStepSpecificContent returns address and cancel link url`() {
        val stepConfig = setupStepConfig()
        val propertyOwnershipId = 1L
        val address = "123 Test Street, AB1 2CD"
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                id = propertyOwnershipId,
                address = MockLandlordData.createAddress(singleLineAddress = address),
            )
        whenever(mockState.propertyOwnershipId).thenReturn(propertyOwnershipId)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)

        val result = stepConfig.getStepSpecificContent(mockState)

        assertEquals(listOf("123 Test Street", "AB1 2CD"), result["addressParts"])
        assertEquals(PropertyDetailsController.getPropertyDetailsPath(propertyOwnershipId), result["cancelLinkUrl"])
    }

    private fun setupStepConfig(): ConfirmOnlyLandlordStepConfig {
        val stepConfig = ConfirmOnlyLandlordStepConfig(mockPropertyOwnershipService)
        stepConfig.routeSegment = ConfirmOnlyLandlordStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
