package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.EpcState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckMatchedEpcFormModel
import uk.gov.communities.prsdb.webapp.services.EpcLookupService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData

@ExtendWith(MockitoExtension::class)
class EpcSupersededStepConfigTests {
    @Mock
    lateinit var mockEpcLookupService: EpcLookupService

    @Mock
    lateinit var mockEpcState: EpcState

    @Mock
    lateinit var mockCheckMatchedEpcStep: CheckMatchedEpcStep

    val routeSegment = EpcSupersededStep.ROUTE_SEGMENT
    val supersededCertificateNumber = "0000-0000-0000-0000-0001"
    val newCertificateNumber = "0000-0000-0000-0000-0002"
    val supersededEpc =
        MockEpcData.createEpcDataModel(
            certificateNumber = supersededCertificateNumber,
            latestCertificateNumberForThisProperty = newCertificateNumber,
        )
    val latestEpc =
        MockEpcData.createEpcDataModel(
            certificateNumber = newCertificateNumber,
            latestCertificateNumberForThisProperty = newCertificateNumber,
        )

    @Test
    fun `afterStepDataIsAdded throws exception when searchedEpc is null`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockEpcState.searchedEpc).thenReturn(null)

        // Act & Assert
        val exception =
            assertThrows<PrsdbWebException> {
                stepConfig.afterStepDataIsAdded(mockEpcState)
            }
        assertEquals(
            "latestCertificateNumberForThisProperty should not be null when searchedEpc is superseded",
            exception.message,
        )
    }

    @Test
    fun `afterStepDataIsAdded throws exception when latestCertificateNumberForThisProperty is null`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val searchedEpc =
            MockEpcData.createEpcDataModel(
                certificateNumber = supersededCertificateNumber,
                latestCertificateNumberForThisProperty = null,
            )
        whenever(mockEpcState.searchedEpc).thenReturn(searchedEpc)

        // Act & Assert
        val exception =
            assertThrows<PrsdbWebException> {
                stepConfig.afterStepDataIsAdded(mockEpcState)
            }
        assertEquals(
            "latestCertificateNumberForThisProperty should not be null when searchedEpc is superseded",
            exception.message,
        )
    }

    @Test
    fun `afterStepDataIsAdded looks up latest EPC and updates searchedEpc`() {
        // Arrange
        val stepConfig = setupStepConfig()

        val checkMatchedEpcFormModel = CheckMatchedEpcFormModel().apply { matchedEpcIsCorrect = true }

        whenever(mockEpcState.searchedEpc).thenReturn(supersededEpc)
        whenever(mockEpcLookupService.getEpcByCertificateNumber(newCertificateNumber)).thenReturn(latestEpc)
        whenever(mockEpcState.checkMatchedEpcStep).thenReturn(mockCheckMatchedEpcStep)
        whenever(mockCheckMatchedEpcStep.formModelOrNull).thenReturn(checkMatchedEpcFormModel)

        // Act
        stepConfig.afterStepDataIsAdded(mockEpcState)

        // Assert
        verify(mockEpcLookupService).getEpcByCertificateNumber(newCertificateNumber)
        verify(mockEpcState).searchedEpc = latestEpc
    }

    @Test
    fun `afterStepDataIsAdded resets checkMatchedEpcStep matchedEpcIsCorrect when latest EPC differs from searched EPC`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val checkMatchedEpcFormModel = CheckMatchedEpcFormModel().apply { matchedEpcIsCorrect = true }

        whenever(mockEpcState.searchedEpc).thenReturn(supersededEpc)
        whenever(mockEpcLookupService.getEpcByCertificateNumber(newCertificateNumber)).thenReturn(latestEpc)
        whenever(mockEpcState.checkMatchedEpcStep).thenReturn(mockCheckMatchedEpcStep)
        whenever(mockCheckMatchedEpcStep.formModelOrNull).thenReturn(checkMatchedEpcFormModel)

        // Act
        stepConfig.afterStepDataIsAdded(mockEpcState)

        // Assert
        verify(mockEpcLookupService).getEpcByCertificateNumber(newCertificateNumber)
        assertEquals(null, checkMatchedEpcFormModel.matchedEpcIsCorrect)
        verify(mockEpcState).searchedEpc = latestEpc
    }

    @Test
    fun `afterStepDataIsAdded does not reset checkMatchedEpcStep matchedEpcIsCorrect when latest EPC is same as searched EPC`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val latestEpc = supersededEpc // Same EPC
        val checkMatchedEpcFormModel = CheckMatchedEpcFormModel().apply { matchedEpcIsCorrect = true }

        whenever(mockEpcState.searchedEpc).thenReturn(supersededEpc)
        whenever(mockEpcLookupService.getEpcByCertificateNumber(newCertificateNumber)).thenReturn(latestEpc)

        // Act
        stepConfig.afterStepDataIsAdded(mockEpcState)

        // Assert
        verify(mockEpcLookupService).getEpcByCertificateNumber(newCertificateNumber)
        assertEquals(true, checkMatchedEpcFormModel.matchedEpcIsCorrect)
        verify(mockEpcState).searchedEpc = latestEpc
    }

    private fun setupStepConfig(): EpcSupersededStepConfig {
        val stepConfig = EpcSupersededStepConfig(mockEpcLookupService)
        stepConfig.routeSegment = routeSegment
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
