package uk.gov.communities.prsdb.webapp.database.entity

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createIndividualLandlord
import java.time.LocalDate

class LandlordTests {
    @Test
    fun `setRenewalDateIfAbsent sets the renewal date when it is null`() {
        val landlord = createIndividualLandlord()
        val date = LocalDate.of(2024, 1, 15)

        landlord.setRenewalDateIfAbsent(date)

        assertEquals(date, landlord.renewalDate)
    }

    @Test
    fun `setRenewalDateIfAbsent does not overwrite an existing renewal date`() {
        val landlord = createIndividualLandlord()
        val originalDate = LocalDate.of(2024, 1, 15)
        landlord.setRenewalDateIfAbsent(originalDate)

        landlord.setRenewalDateIfAbsent(LocalDate.of(2025, 6, 30))

        assertEquals(originalDate, landlord.renewalDate)
    }
}
