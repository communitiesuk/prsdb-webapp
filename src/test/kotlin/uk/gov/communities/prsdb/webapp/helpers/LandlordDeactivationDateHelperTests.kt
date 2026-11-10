package uk.gov.communities.prsdb.webapp.helpers

import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertEquals

class LandlordDeactivationDateHelperTests {
    @Test
    fun `getDeactivationDateFromCreatedDate returns 3 months after the UK date of the createdDate`() {
        // Arrange
        // 23:30 UTC on 31 May is 00:30 BST on 1 June in the UK
        val createdDate = Instant.parse("2026-05-31T23:30:00Z")

        // Act
        val deactivationDate = LandlordDeactivationDateHelper.getDeactivationDateFromCreatedDate(createdDate)

        // Assert
        assertEquals(LocalDate(2026, 9, 1), deactivationDate)
    }
}
