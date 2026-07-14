package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.householdsAndTenants.UpdateHouseholdsAndTenantsJourneyState

@ExtendWith(MockitoExtension::class)
class HouseholdStepConfigTests {
    @Mock
    private lateinit var mockFeatureFlagManager: FeatureFlagManager

    @Mock
    private lateinit var mockUpdateHouseholdsJourneyState: UpdateHouseholdsAndTenantsJourneyState

    @Test
    fun `Content shows the restructure and skipping households content when feature flag is enabled`() {
        // Arrange
        val stepConfig = HouseholdStepConfig(mockFeatureFlagManager)
        whenever(mockFeatureFlagManager.checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)).thenReturn(true)

        // Act
        val content = stepConfig.getStepSpecificContent(mockUpdateHouseholdsJourneyState)

        // Assert
        assertEquals("forms.numberOfHouseholdsRestructureAndSkipping.heading", content["fieldSetHeading"])
        assertEquals("forms.numberOfHouseholdsOld.label", content["label"])
        assertEquals("forms/numberOfHouseholdsFormRestructureAndSkipping", stepConfig.chooseTemplate(mockUpdateHouseholdsJourneyState))
    }

    @Test
    fun `Content shows the old households content when feature flag is disabled`() {
        // Arrange
        val stepConfig = HouseholdStepConfig(mockFeatureFlagManager)
        whenever(mockFeatureFlagManager.checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)).thenReturn(false)

        // Act
        val content = stepConfig.getStepSpecificContent(mockUpdateHouseholdsJourneyState)

        // Assert
        assertEquals("forms.numberOfHouseholdsRestructureAndSkipping.heading", content["fieldSetHeading"])
        assertEquals("forms.numberOfHouseholdsOld.label", content["label"])
        assertEquals("forms/numberOfHouseholdsFormOld", stepConfig.chooseTemplate(mockUpdateHouseholdsJourneyState))
    }
}
