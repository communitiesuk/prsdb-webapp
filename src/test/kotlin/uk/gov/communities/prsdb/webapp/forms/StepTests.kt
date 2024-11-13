package uk.gov.communities.prsdb.webapp.forms

import org.mockito.Mock
import org.mockito.Mockito.mock
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.journeys.PageData
import uk.gov.communities.prsdb.webapp.forms.journeys.objectToStringKeyedMap
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.steps.StepId
import kotlin.test.Test
import kotlin.test.assertEquals

class StepTests {
    @Mock
    val mockPage: Page = mock()

    enum class TestStepId(
        override val urlPathSegment: String,
    ) : StepId {
        StepOne("step1"),
        StepTwo("step2"),
    }

    @Test
    fun `updateJourneyData adds pageData when subPageNumber is null`() {
        // Arrange
        val journeyData: JourneyData =
            mutableMapOf("existingPage" to mutableMapOf("existingProperty" to "existingValue"))
        val pageData: PageData = mutableMapOf("newProperty" to "newValue")
        val testStep = Step<TestStepId>(TestStepId.StepOne, mockPage)

        // Act
        testStep.updateJourneyData(journeyData, pageData, null)
        val newJourneyData = objectToStringKeyedMap(journeyData)
        val existingPageData = objectToStringKeyedMap(newJourneyData?.get("existingPage"))
        val newPageData = objectToStringKeyedMap(newJourneyData?.get(testStep.name))

        // Assert
        assertEquals("existingValue", existingPageData?.get("existingProperty"))
        assertEquals("newValue", newPageData?.get("newProperty"))
    }

    @Test
    fun `updateJourneyData adds subPage data to existing pageData`() {
        // Arrange
        val subPageNumber = 12
        val pageData: PageData = mutableMapOf("newProperty" to "newValue")
        val testStep = Step<TestStepId>(TestStepId.StepOne, mockPage)
        val journeyData: JourneyData =
            mutableMapOf(testStep.name to mutableMapOf(("existingProperty" to "existingValue")))

        // Act
        testStep.updateJourneyData(journeyData, pageData, subPageNumber)
        val newJourneyData = objectToStringKeyedMap(journeyData)
        val existingPageData = objectToStringKeyedMap(newJourneyData?.get(testStep.name))
        val subPageData = objectToStringKeyedMap(existingPageData?.get(subPageNumber.toString()))

        // Assert
        assertEquals("existingValue", existingPageData?.get("existingProperty"))
        assertEquals("newValue", subPageData?.get("newProperty"))
    }

    @Test
    fun `updateJourneyData adds subPage data to new pageData when it does not already exist`() {
        // Arrange
        val subPageNumber = 12
        val journeyData: JourneyData =
            mutableMapOf("existingPage" to mutableMapOf(("existingProperty" to "existingValue")))
        val pageData: PageData = mutableMapOf("newProperty" to "newValue")
        val testStep = Step<TestStepId>(TestStepId.StepOne, mockPage)

        // Act
        testStep.updateJourneyData(journeyData, pageData, subPageNumber)
        val newJourneyData = objectToStringKeyedMap(journeyData)
        val existingPageData = objectToStringKeyedMap(newJourneyData?.get("existingPage"))
        val newPageData = objectToStringKeyedMap(newJourneyData?.get(TestStepId.StepOne.urlPathSegment))
        val subPageData = objectToStringKeyedMap(newPageData?.get(subPageNumber.toString()))

        // Assert
        assertEquals("existingValue", existingPageData?.get("existingProperty"))
        assertEquals("newValue", subPageData?.get("newProperty"))
    }
}
