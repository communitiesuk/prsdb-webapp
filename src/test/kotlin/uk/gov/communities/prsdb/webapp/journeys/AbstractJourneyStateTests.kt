package uk.gov.communities.prsdb.webapp.journeys

import kotlinx.serialization.serializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException

class AbstractJourneyStateTests {
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
    fun `addStepData adds the step data as a submap to the submitted step data`() {
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
    fun `journeyId retrieves the journeyId from the JourneyStateService`() {
        // Arrange
        val journeyStateService: JourneyStateService = mock()
        whenever(journeyStateService.journeyId).thenReturn("testJourneyId")
        val journeyState =
            object : AbstractJourneyState(journeyStateService) {}

        // Act
        val result = journeyState.journeyId

        // Assert
        assertEquals("testJourneyId", result)
    }
}

class JourneyStateDelegateProviderTests {
    @Test
    fun `setting a var property implemented by mutableDelegate saves the value in the state`() {
        // Arrange
        val journeyStateService: JourneyStateService = mock()
        val delegateProvider = JourneyStateDelegateProvider(journeyStateService)
        var testProperty: String? by delegateProvider.mutableDelegate("testProperty", serializer())

        // Act
        testProperty = "testValue"

        // Assert
        verify(journeyStateService).setValue("testProperty", "\"testValue\"")
    }

    @Test
    fun `getting a var property implemented by mutableDelegate retrieves the value from the state if present`() {
        // Arrange
        val journeyStateService: JourneyStateService = mock()
        whenever(journeyStateService.getValue("testProperty")).thenReturn("\"testValue\"")
        val delegateProvider = JourneyStateDelegateProvider(journeyStateService)
        var testProperty: String? by delegateProvider.mutableDelegate("testProperty", serializer())

        // Act
        val result = testProperty

        // Assert
        assertEquals("testValue", result)
    }

    @Test
    fun `getting a var property implemented by mutableDelegate returns null if value not present in state`() {
        // Arrange
        val journeyStateService: JourneyStateService = mock()
        whenever(journeyStateService.getValue("testProperty")).thenReturn(null)
        val delegateProvider = JourneyStateDelegateProvider(journeyStateService)
        var testProperty: String? by delegateProvider.mutableDelegate("testProperty", serializer())

        // Act
        val result = testProperty

        // Assert
        assertNull(result)
    }

    @Test
    fun `getting a val property implemented by requiredDelegate retrieves the value from the state if present`() {
        // Arrange
        val journeyStateService: JourneyStateService = mock()
        whenever(journeyStateService.getValue("testProperty")).thenReturn("\"testValue\"")
        val delegateProvider = JourneyStateDelegateProvider(journeyStateService)
        val testProperty: String by delegateProvider.requiredDelegate("testProperty", serializer())

        // Act
        val result = testProperty

        // Assert
        assertEquals("testValue", result)
    }

    @Test
    fun `getting a val property implemented by requiredDelegate throws and deletes state if value not present in state`() {
        // Arrange
        val journeyStateService: JourneyStateService = mock()
        whenever(journeyStateService.getValue("testProperty")).thenReturn(null)
        val delegateProvider = JourneyStateDelegateProvider(journeyStateService)
        val testProperty: String by delegateProvider.requiredDelegate("testProperty", serializer())

        // Act & Assert
        assertThrows<IllegalStateException> {
            testProperty
        }

        verify(journeyStateService).deleteState()
    }

    @Test
    fun `setting a mutable property with a key already in use throws an exception`() {
        // Arrange
        val journeyStateService: JourneyStateService = mock()
        val delegateProvider = JourneyStateDelegateProvider(journeyStateService)

        // Act & Assert
        assertThrows<JourneyInitialisationException> {
            var testProperty1: String? by delegateProvider.mutableDelegate("testProperty", serializer())
            var testProperty2: Int? by delegateProvider.mutableDelegate("testProperty", serializer())
        }
    }

    @Test
    fun `setting a required property with a key already in use throws an exception`() {
        // Arrange
        val journeyStateService: JourneyStateService = mock()
        val delegateProvider = JourneyStateDelegateProvider(journeyStateService)

        // Act & Assert
        assertThrows<JourneyInitialisationException> {
            var testProperty1: String? by delegateProvider.mutableDelegate("testProperty", serializer())
            val testProperty2: Int by delegateProvider.requiredDelegate("testProperty", serializer())
        }
    }
}
