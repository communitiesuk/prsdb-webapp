package uk.gov.communities.prsdb.webapp.performance

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RampScheduleTests {
    @Test
    fun `supported baseline schedules landlord registrations and three update families`() {
        val registrations = RampSchedule(26.0, rampSeconds = 300.0, holdSeconds = 900.0).arrivalTimesMillis()
        val updates = RampSchedule(4.0, rampSeconds = 300.0, holdSeconds = 900.0).arrivalTimesMillis()

        assertEquals(455, registrations.size)
        assertEquals(70, updates.size)
        assertEquals(665, registrations.size + 3 * updates.size)
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
