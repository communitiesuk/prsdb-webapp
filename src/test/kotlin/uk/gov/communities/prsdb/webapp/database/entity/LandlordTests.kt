package uk.gov.communities.prsdb.webapp.database.entity

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.test.util.ReflectionTestUtils
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createIndividualLandlord
import java.time.MonthDay

class LandlordTests {
    @Test
    fun `anniversary is null when both day and month are null`() {
        val landlord = createIndividualLandlord()

        assertNull(landlord.anniversary)
    }

    @Test
    fun `anniversary returns the day and month as a MonthDay when both are set`() {
        val landlord = createIndividualLandlord()
        landlord.setAnniversaryIfAbsent(MonthDay.of(3, 15))

        assertEquals(MonthDay.of(3, 15), landlord.anniversary)
    }

    @Test
    fun `anniversary throws when only the day is set`() {
        val landlord = createIndividualLandlord()
        ReflectionTestUtils.setField(landlord, "anniversaryDay", 15)

        assertThrows<IllegalStateException> { landlord.anniversary }
    }

    @Test
    fun `anniversary throws when only the month is set`() {
        val landlord = createIndividualLandlord()
        ReflectionTestUtils.setField(landlord, "anniversaryMonth", 3)

        assertThrows<IllegalStateException> { landlord.anniversary }
    }

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
