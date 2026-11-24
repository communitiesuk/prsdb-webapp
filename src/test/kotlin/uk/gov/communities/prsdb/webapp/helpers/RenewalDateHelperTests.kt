package uk.gov.communities.prsdb.webapp.helpers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.MonthDay

class RenewalDateHelperTests {
    @Test
    fun `getRenewalDate returns the anniversary in the reference year when it is after the reference date`() {
        assertEquals(
            LocalDate.of(2026, 12, 1),
            RenewalDateHelper.getRenewalDate(MonthDay.of(12, 1), LocalDate.of(2026, 1, 10)),
        )
    }

    @Test
    fun `getRenewalDate returns the anniversary in the following year when it is before the reference date`() {
        assertEquals(
            LocalDate.of(2027, 1, 15),
            RenewalDateHelper.getRenewalDate(MonthDay.of(1, 15), LocalDate.of(2026, 9, 24)),
        )
    }

    @Test
    fun `getRenewalDate returns the anniversary in the following year when it falls on the reference date`() {
        assertEquals(
            LocalDate.of(2027, 2, 1),
            RenewalDateHelper.getRenewalDate(MonthDay.of(2, 1), LocalDate.of(2026, 2, 1)),
        )
    }

    @Test
    fun `getRenewalDate maps 29 Feb to 1 Mar when the reference year is not a leap year`() {
        assertEquals(
            LocalDate.of(2027, 3, 1),
            RenewalDateHelper.getRenewalDate(MonthDay.of(2, 29), LocalDate.of(2027, 1, 10)),
        )
    }

    @Test
    fun `getRenewalDate keeps 29 Feb when the reference year is a leap year`() {
        assertEquals(
            LocalDate.of(2028, 2, 29),
            RenewalDateHelper.getRenewalDate(MonthDay.of(2, 29), LocalDate.of(2028, 1, 10)),
        )
    }

    @Test
    fun `getRenewalDate maps 29 Feb to 1 Mar when the anniversary has passed and the following year is not a leap year`() {
        assertEquals(
            LocalDate.of(2029, 3, 1),
            RenewalDateHelper.getRenewalDate(MonthDay.of(2, 29), LocalDate.of(2028, 3, 15)),
        )
    }

    @Test
    fun `isOnAnniversary returns true when the date falls on the anniversary`() {
        assertTrue(RenewalDateHelper.isOnAnniversary(LocalDate.of(2027, 6, 15), MonthDay.of(6, 15)))
    }

    @Test
    fun `isOnAnniversary returns false when the date does not fall on the anniversary`() {
        assertFalse(RenewalDateHelper.isOnAnniversary(LocalDate.of(2027, 6, 15), MonthDay.of(6, 16)))
    }

    @Test
    fun `isOnAnniversary returns true for 1 Mar on a non-leap year when the anniversary is 29 Feb`() {
        assertTrue(RenewalDateHelper.isOnAnniversary(LocalDate.of(2027, 3, 1), MonthDay.of(2, 29)))
    }

    @Test
    fun `isOnAnniversary returns false for 1 Mar on a leap year when the anniversary is 29 Feb`() {
        assertFalse(RenewalDateHelper.isOnAnniversary(LocalDate.of(2028, 3, 1), MonthDay.of(2, 29)))
    }

    @Test
    fun `getRenewalDateOnReactivation keeps the current renewal date when it is on the anniversary, even if it has passed`() {
        assertEquals(
            LocalDate.of(2020, 6, 15),
            RenewalDateHelper.getRenewalDateOnReactivation(LocalDate.of(2020, 6, 15), MonthDay.of(6, 15), LocalDate.of(2027, 1, 10)),
        )
    }

    @Test
    fun `getRenewalDateOnReactivation moves to the next anniversary after the current date when the current renewal date is not on it`() {
        assertEquals(
            LocalDate.of(2027, 6, 15),
            RenewalDateHelper.getRenewalDateOnReactivation(LocalDate.of(2026, 1, 1), MonthDay.of(6, 15), LocalDate.of(2027, 1, 10)),
        )
    }
}
