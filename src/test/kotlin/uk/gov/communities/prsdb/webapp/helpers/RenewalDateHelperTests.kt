package uk.gov.communities.prsdb.webapp.helpers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.MonthDay

class RenewalDateHelperTests {
    @Test
    fun `getRenewalDate returns the anniversary in the year after the reference year`() {
        assertEquals(
            LocalDate.of(2027, 2, 1),
            RenewalDateHelper.getRenewalDate(MonthDay.of(2, 1), 2026),
        )
    }

    @Test
    fun `getRenewalDate maps 29 Feb to 1 Mar when the target year is not a leap year`() {
        assertEquals(
            LocalDate.of(2027, 3, 1),
            RenewalDateHelper.getRenewalDate(MonthDay.of(2, 29), 2026),
        )
    }

    @Test
    fun `getRenewalDate keeps 29 Feb when the target year is a leap year`() {
        assertEquals(
            LocalDate.of(2028, 2, 29),
            RenewalDateHelper.getRenewalDate(MonthDay.of(2, 29), 2027),
        )
    }
}
