package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.Validation
import jakarta.validation.Validator
import jakarta.validation.ValidatorFactory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MetricsDateRangeFormModelTests {
    private lateinit var validatorFactory: ValidatorFactory
    private lateinit var validator: Validator

    @BeforeEach
    fun setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory()
        validator = validatorFactory.validator
    }

    @AfterEach
    fun tearDown() {
        validatorFactory.close()
    }

    private fun model(
        fromDay: String = "",
        fromMonth: String = "",
        fromYear: String = "",
        toDay: String = "",
        toMonth: String = "",
        toYear: String = "",
    ) = MetricsDateRangeFormModel().apply {
        this.fromDay = fromDay
        this.fromMonth = fromMonth
        this.fromYear = fromYear
        this.toDay = toDay
        this.toMonth = toMonth
        this.toYear = toYear
    }

    @Test
    fun `a valid date range produces no violations`() {
        val violations =
            validator.validate(
                model("10", "1", "2025", "20", "1", "2025"),
            )

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `a date range with from equal to to produces no violations`() {
        val violations =
            validator.validate(
                model("15", "6", "2025", "15", "6", "2025"),
            )

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `a blank from date reports the missing date error against fromDay`() {
        val violations =
            validator.validate(
                model(toDay = "20", toMonth = "1", toYear = "2025"),
            )

        val fromDayViolation = violations.single { it.propertyPath.toString() == "fromDay" }
        assertEquals("forms.date.error.missingAll", fromDayViolation.messageTemplate)
    }

    @Test
    fun `an impossible to date reports the invalid format error against toDay`() {
        val violations =
            validator.validate(
                model("10", "1", "2025", "31", "2", "2025"),
            )

        val toDayViolation = violations.single { it.propertyPath.toString() == "toDay" }
        assertEquals("forms.date.error.invalidFormat", toDayViolation.messageTemplate)
    }

    @Test
    fun `a to date before the from date reports the range error against toDay`() {
        val violations =
            validator.validate(
                model("20", "1", "2025", "10", "1", "2025"),
            )

        val toDayViolation = violations.single { it.propertyPath.toString() == "toDay" }
        assertEquals("metrics.dateRange.error.toBeforeFrom", toDayViolation.messageTemplate)
    }

    @Test
    fun `an invalid to date does not also report the range error`() {
        // to date is impossible (31 Feb) and earlier in the year than from; only the date error should show
        val violations =
            validator.validate(
                model("10", "6", "2025", "31", "2", "2025"),
            )

        val toDayViolation = violations.single { it.propertyPath.toString() == "toDay" }
        assertEquals("forms.date.error.invalidFormat", toDayViolation.messageTemplate)
    }

    @Test
    fun `a to date with an out-of-range year does not also report the range error`() {
        // to year 1899 is invalid (must be after 1899) and is before the from date; only the year error should show
        val violations =
            validator.validate(
                model("10", "1", "2000", "10", "1", "1899"),
            )

        val toYearViolation = violations.single { it.propertyPath.toString() == "toYear" }
        assertEquals("forms.date.error.invalidY", toYearViolation.messageTemplate)
        assertTrue(violations.none { it.propertyPath.toString() == "toDay" })
    }

    @Test
    fun `a from date with an out-of-range year does not report the range error against toDay`() {
        // from year 1899 is invalid; the to date is earlier, but no range error should be produced
        val violations =
            validator.validate(
                model("10", "1", "1899", "10", "1", "1900"),
            )

        assertTrue(violations.none { it.propertyPath.toString() == "toDay" })
    }

    @Test
    fun `a reversed range with the earliest valid year still reports the range error`() {
        val violations =
            validator.validate(
                model("10", "1", "1900", "9", "1", "1900"),
            )

        val toDayViolation = violations.single { it.propertyPath.toString() == "toDay" }
        assertEquals("metrics.dateRange.error.toBeforeFrom", toDayViolation.messageTemplate)
    }

    @Test
    fun `toIsOnOrAfterFromDate self-guards when the from date is invalid`() {
        // from date is impossible, to date valid -> cross-field must not fail
        assertTrue(model("31", "2", "2025", "10", "1", "2025").toIsOnOrAfterFromDate())
    }

    @Test
    fun `toIsOnOrAfterFromDate self-guards when the to date is blank`() {
        assertTrue(model("10", "1", "2025").toIsOnOrAfterFromDate())
    }

    @Test
    fun `toIsOnOrAfterFromDate is false when to is strictly before from`() {
        assertFalse(model("20", "1", "2025", "10", "1", "2025").toIsOnOrAfterFromDate())
    }

    @Test
    fun `toIsOnOrAfterFromDate is true when to equals from`() {
        assertTrue(model("10", "1", "2025", "10", "1", "2025").toIsOnOrAfterFromDate())
    }

    @Test
    fun `local date helpers return null for incomplete dates and the parsed date otherwise`() {
        assertNull(model().fromLocalDateOrNull())
        assertEquals(
            java.time.LocalDate.of(2025, 1, 10),
            model("10", "1", "2025", "20", "1", "2025").fromLocalDateOrNull(),
        )
    }
}
