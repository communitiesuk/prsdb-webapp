package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.EPC_GUIDE_URL
import uk.gov.communities.prsdb.webapp.constants.REGISTER_PRS_EXEMPTION_URL
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData

@ExtendWith(MockitoExtension::class)
class ConfirmEpcDetailsRetrievedByUprnStepConfigTests {
    @Mock
    lateinit var mockEpcCertificateUrlProvider: EpcCertificateUrlProvider

    @Mock
    lateinit var mockState: EpcState

    private val routeSegment = ConfirmEpcDetailsRetrievedByUprnStep.ROUTE_SEGMENT

    private fun setupStepConfig(): ConfirmEpcDetailsRetrievedByUprnStepConfig {
        val stepConfig = ConfirmEpcDetailsRetrievedByUprnStepConfig(mockEpcCertificateUrlProvider)
        stepConfig.routeSegment = routeSegment
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }

    @Nested
    inner class GetStepSpecificContent {
        @Test
        fun `returns epc details and certificate url when epc is available`() {
            // Arrange
            val epcData = MockEpcData.createEpcDataModel()
            val expectedCertificateUrl = "https://example.com/epc/${epcData.certificateNumber}"
            whenever(mockState.epcRetrievedByUprn).thenReturn(epcData)
            whenever(mockEpcCertificateUrlProvider.getEpcCertificateUrl(epcData.certificateNumber))
                .thenReturn(expectedCertificateUrl)
            val stepConfig = setupStepConfig()

            // Act
            val content = stepConfig.getStepSpecificContent(mockState)

            // Assert
            assertEquals(epcData, content["epcDetails"])
            assertEquals(expectedCertificateUrl, content["epcCertificateUrl"])
            assertEquals(REGISTER_PRS_EXEMPTION_URL, content["registerPrsExemptionUrl"])
            assertEquals(EPC_GUIDE_URL, content["epcGuideUrl"])
        }

        @Test
        fun `throws exception when epc is null`() {
            // Arrange
            whenever(mockState.epcRetrievedByUprn).thenReturn(null)
            val stepConfig = setupStepConfig()

            // Act & Assert
            assertThrows<NotNullFormModelValueIsNullException> {
                stepConfig.getStepSpecificContent(mockState)
            }
        }
    }

    @Nested
    inner class ChooseTemplate {
        @Test
        fun `returns the correct template`() {
            // Arrange
            val stepConfig = setupStepConfig()

            // Act
            val template = stepConfig.chooseTemplate(mockState)

            // Assert
            assertEquals("forms/confirmEpcDetailsByUprnForm", template)
        }
    }
}
