package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_THIS_LATER_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.householdsAndTenants.UpdateHouseholdsAndTenantsJourneyState

// TODO PDJB-1340: Remove tests when the PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING Feature Flag is removed
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
        assertEquals("forms.buttons.provideThisLater", content["secondarySubmitButtonText"])
        assertEquals(PROVIDE_THIS_LATER_BUTTON_ACTION_NAME, content["secondarySubmitButtonAction"])
        assertEquals(true, content["showSecondarySubmitButton"])
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
        assertFalse(content.containsKey("secondarySubmitButtonText"))
        assertFalse(content.containsKey("secondarySubmitButtonAction"))
        assertFalse(content.containsKey("showSecondarySubmitButton"))
        assertEquals("forms/numberOfHouseholdsFormOld", stepConfig.chooseTemplate(mockUpdateHouseholdsJourneyState))
    }
}
