package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyDetailState
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class EicExpiredStepConfigTests {
    @Mock
    lateinit var mockState: ElectricalSafetyDetailState

    @Mock
    lateinit var mockEicExpiryDateStep: EicExpiryDateStep

    @Test
    fun `getStepSpecificContent uses the EIC expiry date and links back to the EIC expiry date step`() {
        val stepConfig = setupStepConfig()
        val expiryDate = LocalDate(2020, 1, 1)
        whenever(mockState.getEicExpiryDateIfReachable()).thenReturn(expiryDate)
        whenever(mockState.eicExpiryDateStep).thenReturn(mockEicExpiryDateStep)
        whenever(mockEicExpiryDateStep.isStepReachable).thenReturn(true)
        whenever(mockEicExpiryDateStep.urlPath).thenReturn(EicExpiryDateStep.ROUTE_SEGMENT)
        whenever(mockState.journeyId).thenReturn("test-journey-id")
        whenever(mockState.isOccupied).thenReturn(true)

        val content = stepConfig.getStepSpecificContent(mockState)

        assertEquals(expiryDate.toJavaLocalDate(), content["expiryDate"])
        assertEquals(true, (content["changeExpiryDateUrl"] as String).contains(EicExpiryDateStep.ROUTE_SEGMENT))
    }

    @Test
    fun `chooseTemplate returns occupied template when isOccupied is true`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.isOccupied).thenReturn(true)

        assertEquals("forms/eicExpiredForOccupiedProperty", stepConfig.chooseTemplate(mockState))
    }

    @Test
    fun `chooseTemplate returns unoccupied template when isOccupied is false`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.isOccupied).thenReturn(false)

        assertEquals("forms/eicExpiredForUnoccupiedProperty", stepConfig.chooseTemplate(mockState))
    }

    private fun setupStepConfig(): EicExpiredStepConfig {
        val stepConfig = EicExpiredStepConfig()
        stepConfig.urlPath = EicExpiredStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
