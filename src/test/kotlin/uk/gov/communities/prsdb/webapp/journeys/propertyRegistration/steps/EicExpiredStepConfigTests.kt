package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.EIC_ISSUE_DATE
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyDetailState
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class EicExpiredStepConfigTests {
    @Mock
    lateinit var mockState: ElectricalSafetyDetailState

    @Mock
    lateinit var mockEicExpiryDateStep: EicExpiryDateStep

    @Mock
    lateinit var mockFeatureFlagManager: FeatureFlagManager

    @Test
    fun `getStepSpecificContent uses the EIC expiry date and links back to the EIC expiry date step when EIC_ISSUE_DATE is disabled`() {
        val stepConfig = setupStepConfig()
        val expiryDate = LocalDate(2020, 1, 1)
        whenever(mockFeatureFlagManager.checkFeature(EIC_ISSUE_DATE)).thenReturn(false)
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

    // TODO PDJB-1669: Update this test to use the EIC issue date and issue date step once they exist
    @Test
    fun `getStepSpecificContent uses the EIC expiry date and expiry date step for the issue date content when EIC_ISSUE_DATE is enabled`() {
        val stepConfig = setupStepConfig()
        val date = LocalDate(2020, 1, 1)
        whenever(mockFeatureFlagManager.checkFeature(EIC_ISSUE_DATE)).thenReturn(true)
        whenever(mockState.getEicExpiryDateIfReachable()).thenReturn(date)
        whenever(mockState.eicExpiryDateStep).thenReturn(mockEicExpiryDateStep)
        whenever(mockEicExpiryDateStep.isStepReachable).thenReturn(true)
        whenever(mockEicExpiryDateStep.urlPath).thenReturn(EicExpiryDateStep.ROUTE_SEGMENT)
        whenever(mockState.journeyId).thenReturn("test-journey-id")
        whenever(mockState.isOccupied).thenReturn(false)

        val content = stepConfig.getStepSpecificContent(mockState)

        assertEquals(date.toJavaLocalDate(), content["issueDate"])
        assertEquals(true, (content["changeIssueDateUrl"] as String).contains(EicExpiryDateStep.ROUTE_SEGMENT))
    }

    @Test
    fun `chooseTemplate returns occupied template when isOccupied is true and EIC_ISSUE_DATE is disabled`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.isOccupied).thenReturn(true)
        whenever(mockFeatureFlagManager.checkFeature(EIC_ISSUE_DATE)).thenReturn(false)

        assertEquals("forms/eicExpiredForOccupiedProperty", stepConfig.chooseTemplate(mockState))
    }

    @Test
    fun `chooseTemplate returns unoccupied template when isOccupied is false and EIC_ISSUE_DATE is disabled`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.isOccupied).thenReturn(false)
        whenever(mockFeatureFlagManager.checkFeature(EIC_ISSUE_DATE)).thenReturn(false)

        assertEquals("forms/eicExpiredForUnoccupiedProperty", stepConfig.chooseTemplate(mockState))
    }

    @Test
    fun `chooseTemplate returns occupied need different certificate template when isOccupied is true and EIC_ISSUE_DATE is enabled`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.isOccupied).thenReturn(true)
        whenever(mockFeatureFlagManager.checkFeature(EIC_ISSUE_DATE)).thenReturn(true)

        assertEquals("forms/eicNeedDifferentCertificateForOccupiedProperty", stepConfig.chooseTemplate(mockState))
    }

    @Test
    fun `chooseTemplate returns unoccupied need different certificate template when isOccupied is false and EIC_ISSUE_DATE is enabled`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.isOccupied).thenReturn(false)
        whenever(mockFeatureFlagManager.checkFeature(EIC_ISSUE_DATE)).thenReturn(true)

        assertEquals("forms/eicNeedDifferentCertificateForUnoccupiedProperty", stepConfig.chooseTemplate(mockState))
    }

    private fun setupStepConfig(): EicExpiredStepConfig {
        val stepConfig = EicExpiredStepConfig(mockFeatureFlagManager)
        stepConfig.urlPath = EicExpiredStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
