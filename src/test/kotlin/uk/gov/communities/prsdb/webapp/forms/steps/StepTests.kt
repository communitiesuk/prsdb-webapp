package uk.gov.communities.prsdb.webapp.forms.steps

import org.mockito.Mock
import org.mockito.Mockito.mock
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.objectToStringKeyedMap
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import kotlin.test.Test
import kotlin.test.assertEquals

class StepTests {
    @Mock
    val mockPage: Page = mock()

    enum class TestStepId(
        override val urlPathSegment: String,
    ) : StepId {
        StepOne("step1"),
    }

    @Test
    fun `updateJourneyData adds pageData when subPageNumber is null`() {
        // Arrange
        val journeyData: JourneyData =
            mapOf("existingPage" to mapOf("existingProperty" to "existingValue"))
        val pageData: PageData = mapOf("newProperty" to "newValue")
        val testStep = Step<TestStepId>(TestStepId.StepOne, mockPage)

        // Act

        val newJourneyData = testStep.updatedJourneyData(journeyData, pageData, null)
        val existingPageData = objectToStringKeyedMap(newJourneyData["existingPage"])
        val newPageData = objectToStringKeyedMap(newJourneyData[testStep.name])

        // Assert
        assertEquals("existingValue", existingPageData?.get("existingProperty"))
        assertEquals("newValue", newPageData?.get("newProperty"))
    }

    @Test
    fun `updateJourneyData adds subPage data to existing pageData`() {
        // Arrange
        val subPageNumber = 12
        val pageData: PageData = mapOf("newProperty" to "newValue")
        val testStep = Step<TestStepId>(TestStepId.StepOne, mockPage)
        val journeyData: JourneyData =
            mapOf(testStep.name to mapOf(("existingProperty" to "existingValue")))

        // Act
        val newJourneyData = testStep.updatedJourneyData(journeyData, pageData, subPageNumber)
        val existingPageData = objectToStringKeyedMap(newJourneyData[testStep.name])
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
            mapOf("existingPage" to mapOf(("existingProperty" to "existingValue")))
        val pageData: PageData = mapOf("newProperty" to "newValue")
        val testStep = Step<TestStepId>(TestStepId.StepOne, mockPage)

        // Act
        val newJourneyData = testStep.updatedJourneyData(journeyData, pageData, subPageNumber)
        val existingPageData = objectToStringKeyedMap(newJourneyData["existingPage"])
        val newPageData = objectToStringKeyedMap(newJourneyData[TestStepId.StepOne.urlPathSegment])
        val subPageData = objectToStringKeyedMap(newPageData?.get(subPageNumber.toString()))

        // Assert
        assertEquals("existingValue", existingPageData?.get("existingProperty"))
        assertEquals("newValue", subPageData?.get("newProperty"))
    }
}
