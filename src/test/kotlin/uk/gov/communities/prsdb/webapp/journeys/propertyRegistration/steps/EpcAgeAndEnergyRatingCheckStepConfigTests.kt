package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import kotlinx.datetime.toKotlinLocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class EpcAgeAndEnergyRatingCheckStepConfigTests {
    @Mock
    lateinit var mockState: EpcState

    @Test
    fun `mode returns null when acceptedEpc is null`() {
        // Arrange
        val stepConfig = EpcAgeAndEnergyRatingCheckStepConfig()
        whenever(mockState.acceptedEpc).thenReturn(null)

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertNull(result)
    }

    @Test
    fun `mode returns EPC_OLDER_THAN_10_YEARS when EPC is past expiry date`() {
        // Arrange
        val stepConfig = EpcAgeAndEnergyRatingCheckStepConfig()
        val expiredEpc = MockEpcData.createEpcDataModel(expiryDate = LocalDate.now().minusDays(5).toKotlinLocalDate())
        whenever(mockState.acceptedEpc).thenReturn(expiredEpc)

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertEquals(EpcAgeAndEnergyRatingCheckMode.EPC_OLDER_THAN_10_YEARS, result)
    }

    @Test
    fun `mode returns EPC_LOW_ENERGY_RATING when energy rating is below E`() {
        // Arrange
        val stepConfig = EpcAgeAndEnergyRatingCheckStepConfig()
        val lowRatingEpc = MockEpcData.createEpcDataModel(energyRating = "F")
        whenever(mockState.acceptedEpc).thenReturn(lowRatingEpc)

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertEquals(EpcAgeAndEnergyRatingCheckMode.EPC_LOW_ENERGY_RATING, result)
    }

    @Test
    fun `mode returns EPC_COMPLIANT when EPC is valid with good energy rating`() {
        // Arrange
        val stepConfig = EpcAgeAndEnergyRatingCheckStepConfig()
        val compliantEpc = MockEpcData.createEpcDataModel()
        whenever(mockState.acceptedEpc).thenReturn(compliantEpc)

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertEquals(EpcAgeAndEnergyRatingCheckMode.EPC_COMPLIANT, result)
    }

    @Test
    fun `mode returns EPC_COMPLIANT when energy rating is E (boundary case)`() {
        // Arrange
        val stepConfig = EpcAgeAndEnergyRatingCheckStepConfig()
        val boundaryEpc = MockEpcData.createEpcDataModel(energyRating = "E")
        whenever(mockState.acceptedEpc).thenReturn(boundaryEpc)

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertEquals(EpcAgeAndEnergyRatingCheckMode.EPC_COMPLIANT, result)
    }

    @Test
    fun `mode returns EPC_OLDER_THAN_10_YEARS over EPC_LOW_ENERGY_RATING when EPC is both expired and low rated`() {
        // Arrange
        val stepConfig = EpcAgeAndEnergyRatingCheckStepConfig()
        val expiredAndLowRatedEpc =
            MockEpcData.createEpcDataModel(
                energyRating = "F",
                expiryDate = LocalDate.now().minusDays(5).toKotlinLocalDate(),
            )
        whenever(mockState.acceptedEpc).thenReturn(expiredAndLowRatedEpc)

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertEquals(EpcAgeAndEnergyRatingCheckMode.EPC_OLDER_THAN_10_YEARS, result)
    }
}
