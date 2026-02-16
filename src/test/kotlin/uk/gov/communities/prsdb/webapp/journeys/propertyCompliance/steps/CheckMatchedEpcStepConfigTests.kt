package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import kotlinx.datetime.toKotlinLocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.validation.BindingResult
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.EpcState
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckMatchedEpcFormModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class CheckMatchedEpcStepConfigTests {
    @Mock
    lateinit var mockEpcCertificateUrlProvider: EpcCertificateUrlProvider

    @Mock
    lateinit var mockEpcState: EpcState

    @Mock
    lateinit var mockCheckMatchedEpcStep: CheckMatchedEpcStep

    @Mock
    lateinit var mockSavedJourneyState: SavedJourneyState

    val routeSegment = CheckMatchedEpcStep.ROUTE_SEGMENT

    @Test
    fun `mode returns null when form model is not present`() {
        // Arrange
        val stepConfig = setupStepConfig()

        // Act
        val result = stepConfig.mode(mockEpcState)

        // Assert
        assertNull(result)
    }

    @Test
    fun `mode returns null when matchedEpcIsCorrect is null`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockEpcState.getStepData(routeSegment)).thenReturn(emptyMap())

        // Act
        val result = stepConfig.mode(mockEpcState)

        // Assert
        assertNull(result)
    }

    @Test
    fun `mode returns EPC_INCORRECT when matchedEpcIsCorrect is false`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockEpcState.getStepData(routeSegment)).thenReturn(mapOf("matchedEpcIsCorrect" to false))

        // Act
        val result = stepConfig.mode(mockEpcState)

        // Assert
        assertEquals(CheckMatchedEpcMode.EPC_INCORRECT, result)
    }

    @Test
    fun `mode returns EPC_EXPIRED when EPC is past expiry date`() {
        // Arrange
        val expiredEpc = MockEpcData.createEpcDataModel(expiryDate = LocalDate.now().minusDays(5).toKotlinLocalDate())
        val stepConfig = setupStepConfig(expiredEpc)
        whenever(mockEpcState.getStepData(routeSegment)).thenReturn(mapOf("matchedEpcIsCorrect" to true))

        // Act
        val result = stepConfig.mode(mockEpcState)

        // Assert
        assertEquals(CheckMatchedEpcMode.EPC_EXPIRED, result)
    }

    @Test
    fun `mode returns EPC_LOW_ENERGY_RATING when energy rating is below E`() {
        // Arrange
        val lowRatingEpc = MockEpcData.createEpcDataModel(energyRating = "F")
        val stepConfig = setupStepConfig(lowRatingEpc)
        whenever(mockEpcState.getStepData(routeSegment)).thenReturn(mapOf("matchedEpcIsCorrect" to true))

        // Act
        val result = stepConfig.mode(mockEpcState)

        // Assert
        assertEquals(CheckMatchedEpcMode.EPC_LOW_ENERGY_RATING, result)
    }

    @Test
    fun `mode returns EPC_COMPLIANT when EPC is valid with good energy rating`() {
        // Arrange
        val compliantEpc = MockEpcData.createEpcDataModel()
        val stepConfig = setupStepConfig(compliantEpc)
        whenever(mockEpcState.getStepData(routeSegment)).thenReturn(mapOf("matchedEpcIsCorrect" to true))

        // Act
        val result = stepConfig.mode(mockEpcState)

        // Assert
        assertEquals(CheckMatchedEpcMode.EPC_COMPLIANT, result)
    }

    @Test
    fun `mode returns EPC_COMPLIANT when energy rating is E (boundary case)`() {
        // Arrange
        val compliantEpc = MockEpcData.createEpcDataModel(energyRating = "E")
        val stepConfig = setupStepConfig(compliantEpc)
        whenever(mockEpcState.getStepData(routeSegment)).thenReturn(mapOf("matchedEpcIsCorrect" to true))

        // Act
        val result = stepConfig.mode(mockEpcState)

        // Assert
        assertEquals(CheckMatchedEpcMode.EPC_COMPLIANT, result)
    }

    @Test
    fun `afterStepDataIsAdded sets acceptedEpc on state`() {
        // Arrange
        val stepConfig = CheckMatchedEpcStepConfig(mockEpcCertificateUrlProvider)
        val epcData = MockEpcData.createEpcDataModel()
        stepConfig.usingEpc { epcData }

        // Act
        stepConfig.afterStepDataIsAdded(mockEpcState)

        // Assert
        verify(mockEpcState).acceptedEpc = epcData
    }

    @Test
    fun `afterStepDataIsAdded is called by the step on submitFormData`() {
        // Arrange
        val mockStepConfig = mock<CheckMatchedEpcStepConfig>()
        whenever(mockStepConfig.routeSegment).thenReturn(routeSegment)
        whenever(mockStepConfig.formModelClass).thenReturn(CheckMatchedEpcFormModel::class)
        val mockBindingResult = mock<BindingResult>()
        whenever(mockBindingResult.target).thenReturn(CheckMatchedEpcFormModel())

        val checkMatchedEpcStep = CheckMatchedEpcStep(mockStepConfig)
        checkMatchedEpcStep.initialize(
            routeSegment,
            mockEpcState,
            mock(),
            mock(),
            mock(),
            mock(),
            true,
        )

        // Act
        checkMatchedEpcStep.submitFormData(mockBindingResult)

        // Assert
        verify(mockStepConfig).afterStepDataIsAdded(mockEpcState)
    }

    private fun setupStepConfig(usingEpc: EpcDataModel = mock()): CheckMatchedEpcStepConfig {
        val stepConfig = CheckMatchedEpcStepConfig(mockEpcCertificateUrlProvider)
        stepConfig.usingEpc { usingEpc }
        stepConfig.routeSegment = routeSegment
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
