package uk.gov.communities.prsdb.webapp.performance

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RampScheduleTests {
    @Test
    fun `normal calibration profile includes all journey families at approved provisional rates`() {
        val schedules = RampedLoadProfile.schedules(rampSeconds = 300.0, holdSeconds = 900.0)

        assertEquals(RampedJourneyFamily.entries.toSet(), schedules.keys)
        assertEquals(455, schedules.getValue(RampedJourneyFamily.PROPERTY_REGISTRATION).arrivalTimesMillis().size)
        assertTrue(
            schedules.filterKeys { it != RampedJourneyFamily.PROPERTY_REGISTRATION }
                .values.all { it.arrivalTimesMillis().size == 17 },
        )
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
