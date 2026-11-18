package uk.gov.communities.prsdb.webapp.performance

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class RampScheduleTests {
    @Test
    fun `baseline schedules all supported families at their approved rates`() {
        val counts = BaselineLoadProfile.schedules(emptyMap()).mapValues { it.value.arrivalTimesMillis().size }
        assertEquals(
            mapOf(
                "LANDLORD" to 455,
                "SAVED" to 70,
                "PROPERTY" to 70,
                "ORGANISATION_PROFILE" to 70,
                "COMPLIANCE" to 17,
                "ORGANISATION_TEAM" to 17,
                "JOINT" to 17,
                "AGENT" to 17,
                "OPERATOR" to 17,
                "OFFBOARDING" to 17,
                "PAID_REGISTRATION" to 455,
                "COUNCIL_READ" to 17,
            ),
            counts.mapKeys { it.key.name },
        )
        assertEquals(1_239, counts.values.sum())
    }

    @Test
    fun `new family rates do not replace the original workload`() {
        val original = BaselineLoadProfile.schedules(emptyMap())
        val changed =
            BaselineLoadProfile.schedules(
                mapOf(
                    "gatling.ramp.paidRegistrationsPerMinute" to "2",
                    "gatling.ramp.councilReadsPerMinute" to "2",
                ),
            )
        val counts = changed.mapKeys { it.key.name }.mapValues { it.value.arrivalTimesMillis().size }
        assertEquals(35, counts["PAID_REGISTRATION"])
        assertEquals(35, counts["COUNCIL_READ"])
        original.filterKeys { it.name !in setOf("PAID_REGISTRATION", "COUNCIL_READ") }.forEach { (family, schedule) ->
            assertEquals(schedule, changed[family], "$family must retain its existing arrival schedule")
        }
    }

    @Test
    fun `new families reject zero negative nonfinite and malformed rates`() {
        for (key in listOf("gatling.ramp.paidRegistrationsPerMinute", "gatling.ramp.councilReadsPerMinute")) {
            for (value in listOf("0", "-1", "NaN", "Infinity", "invalid")) {
                assertFailsWith<IllegalArgumentException>("$key=$value must fail before starting workload") {
                    BaselineLoadProfile.schedules(mapOf(key to value))
                }
            }
        }
    }

    @Test
    fun `arrivals ramp up then hold at the target rate`() {
        val times = RampSchedule(perMinute = 26.0, rampSeconds = 300.0, holdSeconds = 900.0).arrivalTimesMillis()

        // 26/min for 15 minutes plus half of 26/min for 5 minutes, i.e. 390 + 65
        assertEquals(455, times.size)
        assertTrue(times.zipWithNext().all { (first, second) -> second >= first })
        assertTrue(times.last() <= 1_200_000)
    }

    @Test
    fun `arrivals are sparser during the ramp than during the hold`() {
        val times = RampSchedule(perMinute = 26.0, rampSeconds = 300.0, holdSeconds = 900.0).arrivalTimesMillis()

        val firstMinute = times.count { it < 60_000 }
        val lastMinute = times.count { it >= 1_140_000 }
        assertTrue(firstMinute < 3)
        assertTrue(lastMinute in 25..27)
    }
}
