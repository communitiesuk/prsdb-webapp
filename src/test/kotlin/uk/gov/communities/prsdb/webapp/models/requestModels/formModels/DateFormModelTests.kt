package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import java.time.LocalDate
import kotlin.test.assertEquals

class DateFormModelTests {
    @Test
    fun `toLocalDate returns the corresponding LocalDate when all fields are valid`() {
        // Arrange
        val formModel =
            DateOfBirthFormModel().apply {
                day = "15"
                month = "08"
                year = "1990"
            }

        // Act
        val result = formModel.toLocalDateOrNull()

        // Assert
        val expectedResult = LocalDate.of(1990, 8, 15)
        assertEquals(expectedResult, result)
    }

    @Test
    fun `toLocalDate returns null when any field is invalid`() {
        // Arrange
        val formModel =
            DateOfBirthFormModel().apply {
                day = "32" // Invalid day
                month = "13"
                year = "1990"
            }

        // Act
        val result = formModel.toLocalDateOrNull()

        // Assert
        assertNull(result)
    }
}
