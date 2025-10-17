package uk.gov.communities.prsdb.webapp.journeys

import kotlinx.serialization.serializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AbstractJourneyStateTest {
    @Test
    fun `getStepData returns the corresponding submap of the submitted step data`() {
        // Arrange
        val journeyStateService: JourneyStateService = mock()
        val answerMap =
            mapOf(
                "field1" to "value1",
                "field2" to 21,
            )
        whenever(journeyStateService.getSubmittedStepData()).thenReturn(
            mapOf(
                "stepKey" to answerMap,
                "otherStepKey" to
                    mapOf(
                        "fieldA" to "valueA",
                    ),
            ),
        )

        val journeyState = object : AbstractJourneyState(journeyStateService) {}

        // Act
        val result = journeyState.getStepData("stepKey")

        // Assert
        assertEquals(result?.keys, answerMap.keys)
        answerMap.keys.forEach { key ->
            assertEquals(answerMap[key], result?.get(key))
        }
    }

    @Test
    fun addStepData() {
        // Arrange
        val journeyStateService: JourneyStateService = mock()
        val journeyState = object : AbstractJourneyState(journeyStateService) {}
        val stepData =
            mapOf(
                "field1" to "value1",
                "field2" to 21,
            )

        // Act
        journeyState.addStepData("stepKey", stepData)

        // Assert
        verify(journeyStateService).addSingleStepData("stepKey", stepData)
    }

    @Test
    fun `setting a var property implemented by delegate saves the value in the state`() {
        // Arrange
        val journeyStateService: JourneyStateService = mock()
        val journeyState =
            object : AbstractJourneyState(journeyStateService) {
                var testProperty: String? by delegate("testProperty", serializer())
            }

        // Act
        journeyState.testProperty = "testValue"

        // Assert
        verify(journeyStateService).setValue("testProperty", "\"testValue\"")
    }

    @Test
    fun `getting a var property implemented by delegate retrieves the value from the state if present`() {
        // Arrange
        val journeyStateService: JourneyStateService = mock()
        whenever(journeyStateService.getValue("testProperty")).thenReturn("\"testValue\"")
        val journeyState =
            object : AbstractJourneyState(journeyStateService) {
                var testProperty: String? by delegate("testProperty", serializer())
            }

        // Act
        val result = journeyState.testProperty

        // Assert
        assertEquals("testValue", result)
    }

    @Test
    fun `getting a var property implemented by delegate returns null if value not present in state`() {
        // Arrange
        val journeyStateService: JourneyStateService = mock()
        whenever(journeyStateService.getValue("testProperty")).thenReturn(null)
        val journeyState =
            object : AbstractJourneyState(journeyStateService) {
                var testProperty: String? by delegate("testProperty", serializer())
            }

        // Act
        val result = journeyState.testProperty

        // Assert
        assertNull(result)
    }

    @Test
    fun `getting a val property implemented by compulsoryDelegate retrieves the value from the state if present`() {
        // Arrange
        val journeyStateService: JourneyStateService = mock()
        whenever(journeyStateService.getValue("testProperty")).thenReturn("\"testValue\"")
        val journeyState =
            object : AbstractJourneyState(journeyStateService) {
                val testProperty: String by compulsoryDelegate("testProperty", serializer())
            }

        // Act
        val result = journeyState.testProperty

        // Assert
        assertEquals("testValue", result)
    }

    @Test
    fun `getting a val property implemented by compulsoryDelegate throws and deletes state if value not present in state`() {
        // Arrange
        val journeyStateService: JourneyStateService = mock()
        whenever(journeyStateService.getValue("testProperty")).thenReturn(null)
        val journeyState =
            object : AbstractJourneyState(journeyStateService) {
                val testProperty: String by compulsoryDelegate("testProperty", serializer())
            }
        // Act & Assert
        assertThrows<IllegalStateException> {
            journeyState.testProperty
        }

        verify(journeyStateService).deleteState()
    }
}
