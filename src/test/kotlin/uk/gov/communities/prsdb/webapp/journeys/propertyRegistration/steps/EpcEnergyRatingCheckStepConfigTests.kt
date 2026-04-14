package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData

@ExtendWith(MockitoExtension::class)
class EpcEnergyRatingCheckStepConfigTests {
    @Mock
    lateinit var mockState: EpcState

    @Test
    fun `mode throws exception when acceptedEpc is null`() {
        val stepConfig = EpcEnergyRatingCheckStepConfig()
        whenever(mockState.acceptedEpc).thenReturn(null)

        assertThrows<NotNullFormModelValueIsNullException> { stepConfig.mode(mockState) }
    }

    @Test
    fun `mode returns BELOW_THRESHOLD when energy rating is below E`() {
        val stepConfig = EpcEnergyRatingCheckStepConfig()
        val lowRatingEpc = MockEpcData.createEpcDataModel(energyRating = "F")
        whenever(mockState.acceptedEpc).thenReturn(lowRatingEpc)

        val result = stepConfig.mode(mockState)

        assertEquals(EpcEnergyRatingCheckMode.BELOW_THRESHOLD, result)
    }

    @Test
    fun `mode returns MEETS_REQUIREMENTS when energy rating is E`() {
        val stepConfig = EpcEnergyRatingCheckStepConfig()
        val boundaryEpc = MockEpcData.createEpcDataModel(energyRating = "E")
        whenever(mockState.acceptedEpc).thenReturn(boundaryEpc)

        val result = stepConfig.mode(mockState)

        assertEquals(EpcEnergyRatingCheckMode.MEETS_REQUIREMENTS, result)
    }

    @Test
    fun `mode returns MEETS_REQUIREMENTS when energy rating is above E`() {
        val stepConfig = EpcEnergyRatingCheckStepConfig()
        val goodRatingEpc = MockEpcData.createEpcDataModel()
        whenever(mockState.acceptedEpc).thenReturn(goodRatingEpc)

        val result = stepConfig.mode(mockState)

        assertEquals(EpcEnergyRatingCheckMode.MEETS_REQUIREMENTS, result)
    }
}
