package uk.gov.communities.prsdb.webapp.performance

import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BaselineWorkflowTests {
    @Test
    fun `workflow ramp supplies all mandatory basic configuration`() {
        val command =
            Files.readString(Path.of(".github/workflows/run-performance-tests.yml"))
                .substringAfter("--simulation ${BaselineLoadSimulation::class.java.name}")
                .substringBefore("\n      - name:")
        val arguments =
            command.lineSequence().map { it.trim().removeSuffix("\\").trim() }
                .filter { it.startsWith("-Dgatling.") }
                .associate {
                    val (key, value) = it.removePrefix("-D").split("=", limit = 2)
                    key to value.removeSurrounding("\"")
                }
        val config = BasicRunConfig.from(arguments)
        assertEquals(PerformanceTarget.NFT, config.target)
        assertTrue(config.landlordSubject.isNotBlank())
        val rampSeconds = arguments.getValue("gatling.ramp.rampMinutes").toDouble() * 60
        val holdSeconds = arguments.getValue("gatling.ramp.holdMinutes").toDouble() * 60
        val registrations =
            RampSchedule(arguments.getValue("gatling.ramp.registrationsPerMinute").toDouble(), rampSeconds, holdSeconds)
                .arrivalTimesMillis()
        val updates =
            RampSchedule(arguments.getValue("gatling.ramp.updatesPerMinute").toDouble(), rampSeconds, holdSeconds)
                .arrivalTimesMillis()
        val offset = arguments.getValue("gatling.ramp.fixtureOffset").toInt()
        assertTrue(offset >= 2 * (config.iterations + 1), "Ramp must skip property-update and certificate correctness allocations")
        assertTrue(offset + updates.size <= 101, "Ramp must fit each exported fixture pool")
        assertEquals(665, registrations.size + 3 * updates.size)
        assertTrue("gatling.registration.paymentBaseUrl" !in arguments, "Baseline must not require the unfinished payment ingress")
    }
}
