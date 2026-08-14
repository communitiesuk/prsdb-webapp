package uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.stepConfig

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.LandlordDeregistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

class AreYouSureStepConfigTests {
    private val stepConfig =
        AreYouSureStepConfig().also {
            it.urlPath = AreYouSureStep.ROUTE_SEGMENT
            it.validator = AlwaysTrueValidator()
        }

    @Test
    fun `mode returns null when form model is not present`() {
        val mockState =
            mock<LandlordDeregistrationJourneyState> {
                on {
                    getStepData(AreYouSureStep.ROUTE_SEGMENT)
                } doReturn null
            }

        assertNull(stepConfig.mode(mockState))
    }

    @Test
    fun `mode returns COMPLETE when form is submitted`() {
        val mockState =
            mock<LandlordDeregistrationJourneyState> {
                on {
                    getStepData(AreYouSureStep.ROUTE_SEGMENT)
                } doReturn emptyMap()
            }

        assertEquals(Complete.COMPLETE, stepConfig.mode(mockState))
    }

    @Test
    fun `getStepSpecificContent includes userHasRegisteredProperties from state`() {
        val mockState =
            mock<LandlordDeregistrationJourneyState> {
                on {
                    userHasRegisteredProperties
                } doReturn true
            }

        val content = stepConfig.getStepSpecificContent(mockState)

        assertEquals(true, content["userHasRegisteredProperties"])
    }
}
