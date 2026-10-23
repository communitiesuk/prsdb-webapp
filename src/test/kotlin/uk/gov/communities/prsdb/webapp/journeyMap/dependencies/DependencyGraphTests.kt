package uk.gov.communities.prsdb.webapp.journeyMap.dependencies

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DependencyGraphTests {
    @Test
    fun `keys read through other steps' outcomes count as read`() {
        val graph =
            DependencyGraph(
                Dependencies(
                    reads = mapOf("internal:Compliance" to listOf("gasTask"), "has-epc" to listOf("step:has-epc")),
                    outcomeReads = mapOf("internal:Compliance" to listOf("has-epc")),
                ),
            )

        assertEquals(setOf("gasTask", "step:has-epc"), graph.keysRead("internal:Compliance"))
    }

    @Test
    fun `writers are the steps that write the keys a step reads, excluding the step itself`() {
        val graph =
            DependencyGraph(
                Dependencies(
                    reads = mapOf("internal:Occupied" to listOf("step:occupancy", "cache")),
                    writers = mapOf("step:occupancy" to listOf("occupancy"), "cache" to listOf("internal:Occupied")),
                ),
            )

        assertEquals(setOf("occupancy"), graph.writersOf("internal:Occupied"))
    }

    @Test
    fun `reading all step data depends on every step that wrote an answer`() {
        val graph =
            DependencyGraph(
                Dependencies(
                    reads = mapOf("check-answers" to listOf("step:*")),
                    writers =
                        mapOf(
                            "step:occupancy" to listOf("occupancy"),
                            "step:has-epc" to listOf("has-epc"),
                            "lookup" to listOf("x"),
                        ),
                ),
            )

        assertTrue(graph.readsAllStepData("check-answers"))
        assertEquals(setOf("has-epc", "occupancy"), graph.writersOf("check-answers"))
    }
}
