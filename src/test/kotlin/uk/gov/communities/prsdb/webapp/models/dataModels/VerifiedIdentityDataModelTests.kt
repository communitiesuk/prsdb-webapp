package uk.gov.communities.prsdb.webapp.models.dataModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.LocalDate

class VerifiedIdentityDataModelTests {
    @ParameterizedTest
    @CsvSource(
        "John Doe, 1990-01-01, true",
        "John Doe, , false",
        ", 1990-01-01, false",
        ", , false",
    )
    fun `isVerified returns true iff name and birthDate are non-null`(
        name: String?,
        birthDateString: String?,
        expectedIsVerified: Boolean,
    ) {
        val birthDate = birthDateString?.let { LocalDate.parse(it) }
        val model = VerifiedIdentityDataModel(name, birthDate)

        assertEquals(expectedIsVerified, model.isVerified)
    }
}
