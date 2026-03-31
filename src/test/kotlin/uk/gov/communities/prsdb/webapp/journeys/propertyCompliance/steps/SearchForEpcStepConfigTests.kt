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
import uk.gov.communities.prsdb.webapp.services.EpcLookupService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData

@ExtendWith(MockitoExtension::class)
class SearchForEpcStepConfigTests {
    @Mock
    lateinit var mockEpcLookupService: EpcLookupService

    @Mock
    lateinit var mockEpcState: EpcState

    val routeSegment = SearchForEpcStep.ROUTE_SEGMENT

    @Test
    fun `mode returns NOT_FOUND when searchedEpc is null`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockEpcState.searchedEpc).thenReturn(null)

        // Act
        val result = stepConfig.mode(mockEpcState)

        // Assert
        assertEquals(EpcSearchResult.NOT_FOUND, result)
    }

    @Test
    fun `mode returns FOUND when searchedEpc certificate number matches latest certificate number`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val certificateNumber = "0000-0000-0000-0892-1563"
        val epcData =
            MockEpcData.createEpcDataModel(
                certificateNumber = certificateNumber,
                latestCertificateNumberForThisProperty = certificateNumber,
            )
        whenever(mockEpcState.searchedEpc).thenReturn(epcData)

        // Act
        val result = stepConfig.mode(mockEpcState)

        // Assert
        assertEquals(EpcSearchResult.FOUND, result)
    }

    @Test
    fun `mode returns SUPERSEDED when searchedEpc certificate number does not match latest certificate number`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val epcData =
            MockEpcData.createEpcDataModel(
                certificateNumber = "0000-0000-0000-0892-1563",
                latestCertificateNumberForThisProperty = "0000-0000-0554-8411-0000",
            )
        whenever(mockEpcState.searchedEpc).thenReturn(epcData)

        // Act
        val result = stepConfig.mode(mockEpcState)

        // Assert
        assertEquals(EpcSearchResult.SUPERSEDED, result)
    }

    @Test
    fun `afterStepDataIsAdded looks up EPC by certificate number and sets searchedEpc`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val certificateNumber = "0000-0000-0000-0892-1563"
        val epcData = MockEpcData.createEpcDataModel(certificateNumber = certificateNumber)

        whenever(mockEpcState.getStepData(routeSegment)).thenReturn(mapOf("certificateNumber" to certificateNumber))
        whenever(mockEpcLookupService.getEpcByCertificateNumber(certificateNumber)).thenReturn(epcData)

        // Act
        stepConfig.afterStepDataIsAdded(mockEpcState)

        // Assert
        verify(mockEpcLookupService).getEpcByCertificateNumber(certificateNumber)
        verify(mockEpcState).searchedEpc = epcData
    }

    @Test
    fun `afterStepDataIsAdded sets searchedEpc to null when EPC lookup returns null`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val certificateNumber = "0000-0000-0000-0892-1563"

        whenever(mockEpcState.getStepData(routeSegment)).thenReturn(mapOf("certificateNumber" to certificateNumber))
        whenever(mockEpcLookupService.getEpcByCertificateNumber(certificateNumber)).thenReturn(null)

        // Act
        stepConfig.afterStepDataIsAdded(mockEpcState)

        // Assert
        verify(mockEpcLookupService).getEpcByCertificateNumber(certificateNumber)
        verify(mockEpcState).searchedEpc = null
    }

    @Test
    fun `afterStepDataIsAdded throws when form model is not present`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockEpcState.getStepData(routeSegment)).thenReturn(null)

        // Act, Assert
        assertThrows<PrsdbWebException> { stepConfig.afterStepDataIsAdded(mockEpcState) }
    }

    private fun setupStepConfig(): SearchForEpcStepConfig {
        val stepConfig = SearchForEpcStepConfig(mockEpcLookupService)
        stepConfig.routeSegment = routeSegment
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
