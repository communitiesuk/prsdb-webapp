package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import kotlinx.datetime.toKotlinLocalDate
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
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class EpcAgeCheckStepConfigTests {
    @Mock
    lateinit var mockState: EpcState

    @Test
    fun `mode throws exception when acceptedEpc is null`() {
        val stepConfig = EpcAgeCheckStepConfig()
        whenever(mockState.acceptedEpc).thenReturn(null)

        assertThrows<NotNullFormModelValueIsNullException> { stepConfig.mode(mockState) }
    }

    @Test
    fun `mode returns EXPIRED when EPC is past expiry date`() {
        val stepConfig = EpcAgeCheckStepConfig()
        val expiredEpc = MockEpcData.createEpcDataModel(expiryDate = LocalDate.now().minusDays(5).toKotlinLocalDate())
        whenever(mockState.acceptedEpc).thenReturn(expiredEpc)

        val result = stepConfig.mode(mockState)

        assertEquals(EpcAgeCheckMode.EPC_OLDER_THAN_10_YEARS, result)
    }

    @Test
    fun `mode returns CURRENT when EPC is not expired`() {
        val stepConfig = EpcAgeCheckStepConfig()
        val currentEpc = MockEpcData.createEpcDataModel()
        whenever(mockState.acceptedEpc).thenReturn(currentEpc)

        val result = stepConfig.mode(mockState)

        assertEquals(EpcAgeCheckMode.EPC_CURRENT, result)
    }

    @Test
    fun `mode returns EXPIRED when EPC is both expired and low rated`() {
        val stepConfig = EpcAgeCheckStepConfig()
        val expiredAndLowRatedEpc =
            MockEpcData.createEpcDataModel(
                energyRating = "F",
                expiryDate = LocalDate.now().minusDays(5).toKotlinLocalDate(),
            )
        whenever(mockState.acceptedEpc).thenReturn(expiredAndLowRatedEpc)

        val result = stepConfig.mode(mockState)

        assertEquals(EpcAgeCheckMode.EPC_OLDER_THAN_10_YEARS, result)
    }
}
