package uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.PropertyReactivationJourneyState

@ExtendWith(MockitoExtension::class)
class ReactivationAlreadyRegisteredStepConfigTests {
    @Mock
    lateinit var mockState: PropertyReactivationJourneyState

    @Test
    fun `chooseTemplate returns the reactivation already registered page`() {
        val result = ReactivationAlreadyRegisteredStepConfig().chooseTemplate(mockState)

        assertEquals("reactivationAlreadyRegisteredPropertyPage", result)
    }

    @Test
    fun `getStepSpecificContent provides the property registration url`() {
        val result = ReactivationAlreadyRegisteredStepConfig().getStepSpecificContent(mockState)

        assertEquals(RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE, result["registerPropertyUrl"])
    }
}
