package uk.gov.communities.prsdb.webapp.helpers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.MonthDay

class RenewalDateHelperTests {
    @Test
    fun `getRenewalDate returns this year's anniversary when it is still to come`() {
        assertEquals(
            LocalDate.of(2026, 12, 1),
            RenewalDateHelper.getRenewalDate(MonthDay.of(12, 1), LocalDate.of(2026, 1, 10)),
        )
    }

    @Test
    fun `getRenewalDate returns next year's anniversary when this year's has passed`() {
        assertEquals(
            LocalDate.of(2027, 1, 15),
            RenewalDateHelper.getRenewalDate(MonthDay.of(1, 15), LocalDate.of(2026, 9, 24)),
        )
    }

    @Test
    fun `getRenewalDate rolls to next year when the anniversary falls on the reference date`() {
        assertEquals(
            LocalDate.of(2027, 2, 1),
            RenewalDateHelper.getRenewalDate(MonthDay.of(2, 1), LocalDate.of(2026, 2, 1)),
        )
    }

    @Test
    fun `getRenewalDate maps 29 Feb to 1 Mar when this year's anniversary is in a non-leap year`() {
        assertEquals(
            LocalDate.of(2027, 3, 1),
            RenewalDateHelper.getRenewalDate(MonthDay.of(2, 29), LocalDate.of(2027, 1, 10)),
        )
    }

    @Test
    fun `getRenewalDate keeps 29 Feb when this year's anniversary is in a leap year`() {
        assertEquals(
            LocalDate.of(2028, 2, 29),
            RenewalDateHelper.getRenewalDate(MonthDay.of(2, 29), LocalDate.of(2028, 1, 10)),
        )
    }

    @Test
    fun `getRenewalDate maps 29 Feb to 1 Mar of the next year when this year's anniversary has passed`() {
        assertEquals(
            LocalDate.of(2029, 3, 1),
            RenewalDateHelper.getRenewalDate(MonthDay.of(2, 29), LocalDate.of(2028, 3, 15)),
        )
    }
}
