package uk.gov.communities.prsdb.webapp.enums

import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import kotlin.test.assertEquals

class RegistrationNumberTypeTests {
    @Test
    fun `toInitial returns a unique initial for each RegistrationNumberType`() {
        val regNumTypeInitials = RegistrationNumberType.entries.toTypedArray().map { it.toInitial() }

        assertEquals(regNumTypeInitials.size, regNumTypeInitials.distinct().size)
    }
}
