package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.HouseholdsAndTenantsState

@ExtendWith(MockitoExtension::class)
class HouseholdStepConfigTests {
    @Mock
    private lateinit var mockHouseholdsAndTenantsState: HouseholdsAndTenantsState

    @Test
    fun `Content shows the households content`() {
        // Arrange
        val stepConfig = HouseholdStepConfig()
        whenever(mockHouseholdsAndTenantsState.dependencies).thenReturn(mock())

        // Act
        val content = stepConfig.getStepSpecificContent(mockHouseholdsAndTenantsState)

        // Assert
        assertEquals("forms.numberOfHouseholds.heading", content["fieldSetHeading"])
        assertEquals("forms.numberOfHouseholds.label", content["label"])
        assertEquals("forms/numberOfHouseholdsForm", stepConfig.chooseTemplate(mockHouseholdsAndTenantsState))
    }
}
