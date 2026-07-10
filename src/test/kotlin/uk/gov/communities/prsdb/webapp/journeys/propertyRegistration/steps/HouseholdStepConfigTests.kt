package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.ALLOW_SKIPPING_PROPERTY_REGISTRATION_FIELDS
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.householdsAndTenants.UpdateHouseholdsAndTenantsJourneyState

@ExtendWith(MockitoExtension::class)
class HouseholdStepConfigTests {
    @Mock
    private lateinit var mockFeatureFlagManager: FeatureFlagManager

    @Mock
    private lateinit var mockUpdateHouseholdsJourneyState: UpdateHouseholdsAndTenantsJourneyState

    @Test
    fun `step content shows the new households content when feature flag is enabled`() {
        val stepConfig = HouseholdStepConfig(mockFeatureFlagManager)
        whenever(mockFeatureFlagManager.checkFeature(ALLOW_SKIPPING_PROPERTY_REGISTRATION_FIELDS)).thenReturn(true)

        val content = stepConfig.getStepSpecificContent(mockUpdateHouseholdsJourneyState)

        assertEquals(true, content["skipTenancyFlow"])
        assertEquals("forms.numberOfHouseholds.heading", content["fieldSetHeading"])
        assertEquals("forms.numberOfHouseholds.label", content["label"])
        assertEquals("forms/numberOfHouseholdsForm.skipTenancyFlow", stepConfig.chooseTemplate(mockUpdateHouseholdsJourneyState))
    }

    @Test
    fun `step content shows the original households content when feature flag is disabled`() {
        val stepConfig = HouseholdStepConfig(mockFeatureFlagManager)
        whenever(mockFeatureFlagManager.checkFeature(ALLOW_SKIPPING_PROPERTY_REGISTRATION_FIELDS)).thenReturn(false)

        val content = stepConfig.getStepSpecificContent(mockUpdateHouseholdsJourneyState)

        assertEquals(false, content["skipTenancyFlow"])
        assertEquals("forms.numberOfHouseholds.heading", content["fieldSetHeading"])
        assertEquals("forms.numberOfHouseholds.label", content["label"])
        assertEquals("forms/numberOfHouseholdsForm", stepConfig.chooseTemplate(mockUpdateHouseholdsJourneyState))
    }
}
