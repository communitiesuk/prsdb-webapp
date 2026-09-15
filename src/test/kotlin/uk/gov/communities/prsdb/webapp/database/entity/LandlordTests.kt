package uk.gov.communities.prsdb.webapp.database.entity

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createIndividualLandlord
import java.time.MonthDay

class LandlordTests {
    @Test
    fun `setAnniversaryIfAbsent sets day and month when both are null`() {
        val landlord = createIndividualLandlord()

        landlord.setAnniversaryIfAbsent(MonthDay.of(3, 15))

        assertEquals(15, landlord.anniversaryDay)
        assertEquals(3, landlord.anniversaryMonth)
    }

    @Test
    fun `setAnniversaryIfAbsent does not overwrite an existing anniversary`() {
        val landlord = createIndividualLandlord()
        landlord.setAnniversaryIfAbsent(MonthDay.of(3, 15))

        landlord.setAnniversaryIfAbsent(MonthDay.of(6, 30))

        assertEquals(15, landlord.anniversaryDay)
        assertEquals(3, landlord.anniversaryMonth)
    }
}
