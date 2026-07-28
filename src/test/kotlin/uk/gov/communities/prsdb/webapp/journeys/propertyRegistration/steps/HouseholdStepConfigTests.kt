package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.HouseholdsAndTenantsState

@ExtendWith(MockitoExtension::class)
class HouseholdStepConfigTests {
    @Mock
    private lateinit var mockFeatureFlagManager: FeatureFlagManager

    @Mock
    private lateinit var mockHouseholdsAndTenantsState: HouseholdsAndTenantsState

    @Test
    fun `Content shows the restructure and skipping households content when feature flag is enabled`() {
        // Arrange
        val stepConfig = HouseholdStepConfig(mockFeatureFlagManager)
        whenever(mockFeatureFlagManager.checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)).thenReturn(true)
        whenever(mockHouseholdsAndTenantsState.dependencies).thenReturn(mock())

        // Act
        val content = stepConfig.getStepSpecificContent(mockHouseholdsAndTenantsState)

        // Assert
        assertEquals("forms.numberOfHouseholds.restructureAndSkipping.heading", content["fieldSetHeading"])
        assertEquals("forms/restructureAndSkipping/numberOfHouseholdsForm", stepConfig.chooseTemplate(mockHouseholdsAndTenantsState))
    }

    @Test
    fun `Content shows the legacy households content when feature flag is disabled`() {
        // Arrange
        val stepConfig = HouseholdStepConfig(mockFeatureFlagManager)
        whenever(mockFeatureFlagManager.checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)).thenReturn(false)

        // Act
        val content = stepConfig.getStepSpecificContent(mockHouseholdsAndTenantsState)

        // Assert
        assertEquals("forms.numberOfHouseholds.heading", content["fieldSetHeading"])
        assertEquals("forms.numberOfHouseholds.label", content["label"])
        assertEquals("forms/restructureAndSkipping/numberOfHouseholdsFormLegacy", stepConfig.chooseTemplate(mockHouseholdsAndTenantsState))
    }
}
