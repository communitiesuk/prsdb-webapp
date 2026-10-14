package uk.gov.communities.prsdb.webapp.journeys

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
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

    @Test
    fun `cyaAnswerDelegateKeys returns the CYA answer keys for the whole journey`() {
        // Arrange
        val journeyStateService: JourneyStateService = mock()
        val journeyState =
            object : AbstractJourneyState(journeyStateService) {
                var answer: String? by delegateProvider.nullableDelegate("answer", isCyaAnswer = true)
                var notAnAnswer: String? by delegateProvider.nullableDelegate("notAnAnswer")
            }
        val task =
            object : AbstractJourneyState(journeyStateService) {
                val taskAnswer: Map<Int, String> by delegateProvider.requiredDelegate("taskAnswer", mapOf(), isCyaAnswer = true)

                init {
                    delegateProvider.bindRoutePrefix("task-route")
                }
            }
        val registry = DelegateKeyRegistry()

        // Act
        journeyState.bindKeyRegistry(registry)
        task.bindKeyRegistry(registry)

        // Assert
        assertEquals(setOf("answer", "task-route/taskAnswer"), journeyState.cyaAnswerDelegateKeys)
    }

    @Test
    fun `getEncodedDelegateValue retrieves the raw value from the JourneyStateService`() {
        // Arrange
        val journeyStateService: JourneyStateService = mock()
        whenever(journeyStateService.getValue("task-route/taskAnswer")).thenReturn("{\"1\":\"value\"}")
        val journeyState = object : AbstractJourneyState(journeyStateService) {}

        // Act
        val result = journeyState.getEncodedDelegateValue("task-route/taskAnswer")

        // Assert
        assertEquals("{\"1\":\"value\"}", result)
    }
}

class JourneyStateDelegateProviderTests {
    @Test
    fun `setting a var property implemented by nullableDelegate saves the value in the state`() {
        // Arrange
        val journeyStateService: JourneyStateService = mock()
        val delegateProvider = JourneyStateDelegateProvider(journeyStateService)
        var testProperty: String? by delegateProvider.nullableDelegate("testProperty")

        // Act
        testProperty = "testValue"

        // Assert
        verify(journeyStateService).setValue("testProperty", "\"testValue\"")
    }

    @Test
    fun `getting a var property implemented by nullableDelegate retrieves the value from the state if present`() {
        // Arrange
        val journeyStateService: JourneyStateService = mock()
        whenever(journeyStateService.getValue("testProperty")).thenReturn("\"testValue\"")
        val delegateProvider = JourneyStateDelegateProvider(journeyStateService)
        var testProperty: String? by delegateProvider.nullableDelegate("testProperty")

        // Act
        val result = testProperty

        // Assert
        assertEquals("testValue", result)
    }

    @Test
    fun `getting a var property implemented by nullableDelegate returns null if value not present in state`() {
        // Arrange
        val journeyStateService: JourneyStateService = mock()
        whenever(journeyStateService.getValue("testProperty")).thenReturn(null)
        val delegateProvider = JourneyStateDelegateProvider(journeyStateService)
        var testProperty: String? by delegateProvider.nullableDelegate("testProperty")

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
        val testProperty: String by delegateProvider.requiredDelegate("testProperty")

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
        val testProperty: String by delegateProvider.requiredDelegate("testProperty")

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
            var testProperty1: String? by delegateProvider.nullableDelegate("testProperty")
            var testProperty2: Int? by delegateProvider.nullableDelegate("testProperty")
        }
    }

    @Test
    fun `setting a required property with a key already in use throws an exception`() {
        // Arrange
        val journeyStateService: JourneyStateService = mock()
        val delegateProvider = JourneyStateDelegateProvider(journeyStateService)

        // Act & Assert
        assertThrows<JourneyInitialisationException> {
            var testProperty1: String? by delegateProvider.nullableDelegate("testProperty")
            val testProperty2: Int by delegateProvider.requiredDelegate("testProperty")
        }
    }

    @Test
    fun `bindKeyRegistry flushes bare keys when no route is bound`() {
        val journeyStateService: JourneyStateService = mock()
        val registry = DelegateKeyRegistry()
        val provider = JourneyStateDelegateProvider(journeyStateService)
        provider.registerKey("cachedAddresses")

        provider.bindKeyRegistry(registry)

        val other = JourneyStateDelegateProvider(journeyStateService).apply { registerKey("cachedAddresses") }
        assertThrows<JourneyInitialisationException> { other.bindKeyRegistry(registry) }
    }

    @Test
    fun `bindKeyRegistry flushes route-scoped keys when a route is bound`() {
        val journeyStateService: JourneyStateService = mock()
        val registry = DelegateKeyRegistry()
        val provider = JourneyStateDelegateProvider(journeyStateService)
        provider.registerKey("cachedAddresses")
        provider.bindRoutePrefix("lead-trustee-address")

        provider.bindKeyRegistry(registry)

        assertDoesNotThrow {
            JourneyStateDelegateProvider(journeyStateService)
                .apply { registerKey("cachedAddresses") }
                .bindKeyRegistry(registry)
        }
        assertThrows<JourneyInitialisationException> {
            registry.register("lead-trustee-address/cachedAddresses")
        }
    }

    @Test
    fun `a key registered after bindKeyRegistry forwards to the registry live`() {
        val journeyStateService: JourneyStateService = mock()
        val registry = DelegateKeyRegistry()
        val provider = JourneyStateDelegateProvider(journeyStateService)
        provider.bindKeyRegistry(registry)

        provider.registerKey("addedLater")

        assertThrows<JourneyInitialisationException> { registry.register("addedLater") }
    }

    @Test
    fun `bindKeyRegistry registers route-scoped CYA answer keys as CYA answers`() {
        val journeyStateService: JourneyStateService = mock()
        val registry = DelegateKeyRegistry()
        val provider = JourneyStateDelegateProvider(journeyStateService)
        var answer: String? by provider.nullableDelegate("answer", isCyaAnswer = true)
        var notAnAnswer: String? by provider.nullableDelegate("notAnAnswer")
        provider.bindRoutePrefix("route")

        provider.bindKeyRegistry(registry)

        assertEquals(setOf("route/answer"), registry.cyaAnswerKeys)
        assertEquals(setOf("route/answer"), provider.cyaAnswerKeys)
    }

    @Test
    fun `a CYA answer key registered after bindKeyRegistry is registered as a CYA answer`() {
        val journeyStateService: JourneyStateService = mock()
        val registry = DelegateKeyRegistry()
        val provider = JourneyStateDelegateProvider(journeyStateService)
        provider.bindKeyRegistry(registry)

        provider.registerKey("addedLater", isCyaAnswer = true)

        assertEquals(setOf("addedLater"), registry.cyaAnswerKeys)
    }

    @Test
    fun `cyaAnswerKeys returns the provider's own scoped CYA answer keys when no registry is bound`() {
        val journeyStateService: JourneyStateService = mock()
        val provider = JourneyStateDelegateProvider(journeyStateService)
        var answer: String? by provider.nullableDelegate("answer", isCyaAnswer = true)
        var notAnAnswer: String? by provider.nullableDelegate("notAnAnswer")
        provider.bindRoutePrefix("route")

        assertEquals(setOf("route/answer"), provider.cyaAnswerKeys)
    }

    @Test
    fun `two same-route providers with an equal key collide in one registry`() {
        val journeyStateService: JourneyStateService = mock()
        val registry = DelegateKeyRegistry()
        JourneyStateDelegateProvider(journeyStateService)
            .apply {
                registerKey("cachedAddresses")
                bindRoutePrefix("addr")
            }.bindKeyRegistry(registry)

        assertThrows<JourneyInitialisationException> {
            JourneyStateDelegateProvider(journeyStateService)
                .apply {
                    registerKey("cachedAddresses")
                    bindRoutePrefix("addr")
                }.bindKeyRegistry(registry)
        }
    }
}
