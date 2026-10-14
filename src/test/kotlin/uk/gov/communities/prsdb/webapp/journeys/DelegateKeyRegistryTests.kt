package uk.gov.communities.prsdb.webapp.journeys

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException

class DelegateKeyRegistryTests {
    @Test
    fun `register accepts distinct keys`() {
        val registry = DelegateKeyRegistry()
        assertDoesNotThrow {
            registry.register("a")
            registry.register("b")
            registry.register("route/a")
        }
    }

    @Test
    fun `register throws JourneyInitialisationException on a duplicate key`() {
        val registry = DelegateKeyRegistry()
        registry.register("a")
        assertThrows(JourneyInitialisationException::class.java) { registry.register("a") }
    }

    @Test
    fun `registerAll merges distinct keys from another registry`() {
        val source =
            DelegateKeyRegistry().apply {
                register("a")
                register("b")
            }
        val target = DelegateKeyRegistry().apply { register("c") }
        assertDoesNotThrow { target.registerAll(source) }
        assertThrows(JourneyInitialisationException::class.java) { target.register("a") }
    }

    @Test
    fun `registerAll throws when the two registries share a key`() {
        val source = DelegateKeyRegistry().apply { register("shared") }
        val target = DelegateKeyRegistry().apply { register("shared") }
        assertThrows(JourneyInitialisationException::class.java) { target.registerAll(source) }
    }

    @Test
    fun `cyaAnswerKeys contains only the keys registered as CYA answers`() {
        val registry =
            DelegateKeyRegistry().apply {
                register("answer", isCyaAnswer = true)
                register("notAnAnswer")
                register("route/answer", isCyaAnswer = true)
            }

        assertEquals(setOf("answer", "route/answer"), registry.cyaAnswerKeys)
    }

    @Test
    fun `registerAll keeps whether each key is a CYA answer`() {
        val source =
            DelegateKeyRegistry().apply {
                register("answer", isCyaAnswer = true)
                register("notAnAnswer")
            }
        val target = DelegateKeyRegistry().apply { register("targetAnswer", isCyaAnswer = true) }

        target.registerAll(source)

        assertEquals(setOf("answer", "targetAnswer"), target.cyaAnswerKeys)
    }
}
