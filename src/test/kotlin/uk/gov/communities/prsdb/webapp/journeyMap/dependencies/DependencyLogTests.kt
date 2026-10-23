package uk.gov.communities.prsdb.webapp.journeyMap.dependencies

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DependencyLogTests {
    private val log = DependencyLog()

    @Test
    fun `a read is attributed to every step whose mode is being worked out`() {
        log.read(listOf("outer", "inner"), "step:occupancy")

        val dependencies = log.snapshot()
        assertEquals(listOf("step:occupancy"), dependencies.reads["outer"])
        assertEquals(listOf("step:occupancy"), dependencies.reads["inner"])
    }

    @Test
    fun `outcome reads and writes are recorded against the reading and writing steps`() {
        log.outcomeRead("check", "has-epc")
        log.outcomeRead(null, "ignored")
        log.outcomeRead("same", "same")
        log.written("occupancy", "step:occupancy")
        log.written(null, "ignored")

        val dependencies = log.snapshot()
        assertEquals(mapOf("check" to listOf("has-epc")), dependencies.outcomeReads)
        assertEquals(mapOf("step:occupancy" to listOf("occupancy")), dependencies.writers)
    }

    @Test
    fun `each outcome a step is passed with is recorded once for each journey`() {
        log.passed("j1", "occupied", "YES")
        log.passed("j1", "occupied", "YES")
        log.passed("j1", "occupied", "NO")
        log.passed("j2", "occupied", "YES")
        log.passed("j1", "internal:Check", null)
        log.passed(null, "start", "YES")

        assertEquals(
            mapOf(
                "j1" to mapOf("occupied" to listOf("NO", "YES")),
                "j2" to mapOf("occupied" to listOf("YES")),
                "" to mapOf("start" to listOf("YES")),
            ),
            log.snapshot().outcomesPassed,
        )
    }

    @Test
    fun `outcomes passed can leave out journeys, such as those that change answers from check your answers`() {
        val dependencies =
            Dependencies(
                outcomesPassed =
                    mapOf(
                        "main" to mapOf("occupied" to listOf("YES")),
                        "change" to mapOf("occupied" to listOf("NO"), "other" to listOf("A")),
                        "restarted" to mapOf("occupied" to listOf("YES", "NO")),
                    ),
            )

        assertEquals(mapOf("occupied" to listOf("NO", "YES")), dependencies.outcomesPassedOutside(setOf("change")))
    }
}
