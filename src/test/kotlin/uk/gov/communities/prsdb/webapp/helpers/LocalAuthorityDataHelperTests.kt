package uk.gov.communities.prsdb.webapp.helpers

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class LocalAuthorityDataHelperTests {
    @Test
    fun `getLocalAuthorityDisplayName returns the correct Local Authority name for a custodian code`() {
        val custodianCode = "1045"
        val expectedDisplayName = "DERBYSHIRE DALES DISTRICT COUNCIL"

        val localAuthorityDisplayName = LocalAuthorityDataHelper.getLocalAuthorityDisplayName(custodianCode)

        assertEquals(localAuthorityDisplayName, expectedDisplayName)
    }
}
