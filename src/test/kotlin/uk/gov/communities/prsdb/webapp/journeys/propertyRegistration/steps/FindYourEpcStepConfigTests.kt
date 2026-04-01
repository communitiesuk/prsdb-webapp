package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.services.EpcLookupService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData

@ExtendWith(MockitoExtension::class)
class FindYourEpcStepConfigTests {
    @Mock
    lateinit var mockEpcLookupService: EpcLookupService

    @Mock
    lateinit var mockState: EpcState

    val routeSegment = FindYourEpcStep.ROUTE_SEGMENT

    private fun setupStepConfig(): FindYourEpcStepConfig {
        val stepConfig = FindYourEpcStepConfig(mockEpcLookupService)
        stepConfig.routeSegment = routeSegment
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }

    @Nested
    inner class Mode {
        @Test
        fun `returns NOT_FOUND when epcRetrievedByCertificateNumber is null`() {
            // Arrange
            val stepConfig = setupStepConfig()
            whenever(mockState.epcRetrievedByCertificateNumber).thenReturn(null)

            // Act
            val result = stepConfig.mode(mockState)

            // Assert
            assertEquals(FindYourEpcMode.NOT_FOUND, result)
        }

        @Test
        fun `returns LATEST_EPC_FOUND when EPC is the latest certificate for this property`() {
            // Arrange
            val stepConfig = setupStepConfig()
            val certificateNumber = MockEpcData.DEFAULT_EPC_CERTIFICATE_NUMBER
            val epcData =
                MockEpcData.createEpcDataModel(
                    certificateNumber = certificateNumber,
                    latestCertificateNumberForThisProperty = certificateNumber,
                )
            whenever(mockState.epcRetrievedByCertificateNumber).thenReturn(epcData)

            // Act
            val result = stepConfig.mode(mockState)

            // Assert
            assertEquals(FindYourEpcMode.LATEST_EPC_FOUND, result)
        }

        @Test
        fun `returns SUPERSEDED_EPC_FOUND when EPC is not the latest certificate for this property`() {
            // Arrange
            val stepConfig = setupStepConfig()
            val epcData =
                MockEpcData.createEpcDataModel(
                    certificateNumber = MockEpcData.DEFAULT_EPC_CERTIFICATE_NUMBER,
                    latestCertificateNumberForThisProperty = MockEpcData.SECONDARY_EPC_CERTIFICATE_NUMBER,
                )
            whenever(mockState.epcRetrievedByCertificateNumber).thenReturn(epcData)

            // Act
            val result = stepConfig.mode(mockState)

            // Assert
            assertEquals(FindYourEpcMode.SUPERSEDED_EPC_FOUND, result)
        }
    }

    @Nested
    inner class AfterStepDataIsAdded {
        @Test
        fun `looks up EPC by certificate number and sets epcRetrievedByCertificateNumber`() {
            // Arrange
            val stepConfig = setupStepConfig()
            val certificateNumber = MockEpcData.DEFAULT_EPC_CERTIFICATE_NUMBER
            val epcData = MockEpcData.createEpcDataModel(certificateNumber = certificateNumber)

            whenever(mockState.epcRetrievedByCertificateNumber).thenReturn(null)
            whenever(mockState.getStepData(routeSegment)).thenReturn(mapOf("certificateNumber" to certificateNumber))
            whenever(mockEpcLookupService.getEpcByCertificateNumber(certificateNumber)).thenReturn(epcData)

            // Act
            stepConfig.afterStepDataIsAdded(mockState)

            // Assert
            verify(mockEpcLookupService).getEpcByCertificateNumber(certificateNumber)
            verify(mockState).epcRetrievedByCertificateNumber = epcData
        }

        @Test
        fun `sets epcRetrievedByCertificateNumber to null when lookup returns null`() {
            // Arrange
            val stepConfig = setupStepConfig()
            val certificateNumber = MockEpcData.DEFAULT_EPC_CERTIFICATE_NUMBER

            whenever(mockState.epcRetrievedByCertificateNumber).thenReturn(null)
            whenever(mockState.getStepData(routeSegment)).thenReturn(mapOf("certificateNumber" to certificateNumber))
            whenever(mockEpcLookupService.getEpcByCertificateNumber(certificateNumber)).thenReturn(null)

            // Act
            stepConfig.afterStepDataIsAdded(mockState)

            // Assert
            verify(mockEpcLookupService).getEpcByCertificateNumber(certificateNumber)
            verify(mockState).epcRetrievedByCertificateNumber = null
        }

        @Test
        fun `sets epcRetrievedByCertificateNumberUpdatedSinceUserReview when previous and new EPCs are different`() {
            // Arrange
            val stepConfig = setupStepConfig()
            val certificateNumber = MockEpcData.DEFAULT_EPC_CERTIFICATE_NUMBER

            val previousEpc =
                MockEpcData.createEpcDataModel(
                    certificateNumber = MockEpcData.SECONDARY_EPC_CERTIFICATE_NUMBER,
                    energyRating = "D",
                )
            val newEpc = MockEpcData.createEpcDataModel(certificateNumber = certificateNumber, energyRating = "C")

            whenever(mockState.epcRetrievedByCertificateNumber).thenReturn(previousEpc, newEpc)
            whenever(mockState.getStepData(routeSegment)).thenReturn(mapOf("certificateNumber" to certificateNumber))
            whenever(mockEpcLookupService.getEpcByCertificateNumber(certificateNumber)).thenReturn(newEpc)

            // Act
            stepConfig.afterStepDataIsAdded(mockState)

            // Assert
            verify(mockState).epcRetrievedByCertificateNumberUpdatedSinceUserReview = true
        }

        @Test
        fun `does not set epcRetrievedByCertificateNumberUpdatedSinceUserReview when there is no previous EPC`() {
            // Arrange
            val stepConfig = setupStepConfig()
            val certificateNumber = MockEpcData.DEFAULT_EPC_CERTIFICATE_NUMBER
            val newEpc = MockEpcData.createEpcDataModel(certificateNumber = certificateNumber)

            whenever(mockState.epcRetrievedByCertificateNumber).thenReturn(null)
            whenever(mockState.getStepData(routeSegment)).thenReturn(mapOf("certificateNumber" to certificateNumber))
            whenever(mockEpcLookupService.getEpcByCertificateNumber(certificateNumber)).thenReturn(newEpc)

            // Act
            stepConfig.afterStepDataIsAdded(mockState)

            // Assert
            verify(mockState, never()).epcRetrievedByCertificateNumberUpdatedSinceUserReview = true
        }

        @Test
        fun `sets updatedEpcRetrievedByCertificateNumber when retrieved EPC is superseded`() {
            // Arrange
            val stepConfig = setupStepConfig()
            val certificateNumber = MockEpcData.DEFAULT_EPC_CERTIFICATE_NUMBER
            val latestCertificateNumber = MockEpcData.SECONDARY_EPC_CERTIFICATE_NUMBER
            val supersededEpc =
                MockEpcData.createEpcDataModel(
                    certificateNumber = certificateNumber,
                    latestCertificateNumberForThisProperty = latestCertificateNumber,
                )
            val latestEpc = MockEpcData.createEpcDataModel(certificateNumber = latestCertificateNumber)

            whenever(mockState.epcRetrievedByCertificateNumber).thenReturn(null).thenReturn(supersededEpc)
            whenever(mockState.getStepData(routeSegment)).thenReturn(mapOf("certificateNumber" to certificateNumber))
            whenever(mockEpcLookupService.getEpcByCertificateNumber(certificateNumber)).thenReturn(supersededEpc)
            whenever(mockEpcLookupService.getEpcByCertificateNumber(latestCertificateNumber)).thenReturn(latestEpc)

            // Act
            stepConfig.afterStepDataIsAdded(mockState)

            // Assert
            verify(mockEpcLookupService).getEpcByCertificateNumber(latestCertificateNumber)
            verify(mockState).updatedEpcRetrievedByCertificateNumber = latestEpc
        }

        @Test
        fun `does not set updatedEpcRetrievedByCertificateNumber when retrieved EPC is the latest for this property`() {
            // Arrange
            val stepConfig = setupStepConfig()
            val certificateNumber = MockEpcData.DEFAULT_EPC_CERTIFICATE_NUMBER
            val latestEpc =
                MockEpcData.createEpcDataModel(
                    certificateNumber = certificateNumber,
                    latestCertificateNumberForThisProperty = certificateNumber,
                )

            whenever(mockState.epcRetrievedByCertificateNumber).thenReturn(null).thenReturn(latestEpc)
            whenever(mockState.getStepData(routeSegment)).thenReturn(mapOf("certificateNumber" to certificateNumber))
            whenever(mockEpcLookupService.getEpcByCertificateNumber(certificateNumber)).thenReturn(latestEpc)

            // Act
            stepConfig.afterStepDataIsAdded(mockState)

            // Assert
            verify(mockState, never()).updatedEpcRetrievedByCertificateNumber = anyOrNull()
        }

        @Test
        fun `does not set updatedEpcRetrievedByCertificateNumber when lookup returns null`() {
            // Arrange
            val stepConfig = setupStepConfig()
            val certificateNumber = MockEpcData.DEFAULT_EPC_CERTIFICATE_NUMBER

            whenever(mockState.epcRetrievedByCertificateNumber).thenReturn(null)
            whenever(mockState.getStepData(routeSegment)).thenReturn(mapOf("certificateNumber" to certificateNumber))
            whenever(mockEpcLookupService.getEpcByCertificateNumber(certificateNumber)).thenReturn(null)

            // Act
            stepConfig.afterStepDataIsAdded(mockState)

            // Assert
            verify(mockState, never()).updatedEpcRetrievedByCertificateNumber = anyOrNull()
        }

        @Test
        fun `does not set updatedEpcRetrievedByCertificateNumber when superseded EPC has null latestCertificateNumberForThisProperty`() {
            // Arrange
            val stepConfig = setupStepConfig()
            val certificateNumber = MockEpcData.DEFAULT_EPC_CERTIFICATE_NUMBER
            val supersededEpc =
                MockEpcData.createEpcDataModel(
                    certificateNumber = certificateNumber,
                    latestCertificateNumberForThisProperty = null,
                )

            whenever(mockState.epcRetrievedByCertificateNumber).thenReturn(null).thenReturn(supersededEpc)
            whenever(mockState.getStepData(routeSegment)).thenReturn(mapOf("certificateNumber" to certificateNumber))
            whenever(mockEpcLookupService.getEpcByCertificateNumber(certificateNumber)).thenReturn(supersededEpc)

            // Act
            stepConfig.afterStepDataIsAdded(mockState)

            // Assert
            verify(mockState, never()).updatedEpcRetrievedByCertificateNumber = anyOrNull()
        }

        @Test
        fun `does not set epcRetrievedByCertificateNumberUpdatedSinceUserReview when new EPC is same as previous`() {
            // Arrange
            val stepConfig = setupStepConfig()
            val certificateNumber = MockEpcData.DEFAULT_EPC_CERTIFICATE_NUMBER
            val epc = MockEpcData.createEpcDataModel(certificateNumber = certificateNumber)

            whenever(mockState.epcRetrievedByCertificateNumber).thenReturn(epc)
            whenever(mockState.getStepData(routeSegment)).thenReturn(mapOf("certificateNumber" to certificateNumber))
            whenever(mockEpcLookupService.getEpcByCertificateNumber(certificateNumber)).thenReturn(epc)

            // Act
            stepConfig.afterStepDataIsAdded(mockState)

            // Assert
            verify(mockState, never()).epcRetrievedByCertificateNumberUpdatedSinceUserReview = true
        }
    }
}
