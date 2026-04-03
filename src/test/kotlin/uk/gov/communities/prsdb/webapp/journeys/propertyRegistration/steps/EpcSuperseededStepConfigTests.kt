package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData

@ExtendWith(MockitoExtension::class)
class EpcSuperseededStepConfigTests {
    @Mock
    lateinit var mockEpcCertificateUrlProvider: EpcCertificateUrlProvider

    @Mock
    lateinit var mockState: EpcState

    val routeSegment = EpcSuperseededStep.ROUTE_SEGMENT

    private fun setupStepConfig(): EpcSuperseededStepConfig {
        val stepConfig = EpcSuperseededStepConfig(mockEpcCertificateUrlProvider)
        stepConfig.routeSegment = routeSegment
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }

    @Nested
    inner class AfterStepDataIsAdded {
        @Test
        fun `sets acceptedEpc to updatedEpcRetrievedByCertificateNumber`() {
            // Arrange
            val stepConfig = setupStepConfig()
            val updatedEpc = MockEpcData.createEpcDataModel()
            whenever(mockState.updatedEpcRetrievedByCertificateNumber).thenReturn(updatedEpc)

            // Act
            stepConfig.afterStepDataIsAdded(mockState)

            // Assert
            verify(mockState).acceptedEpc = updatedEpc
        }
    }
}
