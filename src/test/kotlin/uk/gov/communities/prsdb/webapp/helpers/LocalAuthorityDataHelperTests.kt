package uk.gov.communities.prsdb.webapp.helpers

import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.LOCAL_AUTHORITIES
import kotlin.test.assertEquals

class LocalAuthorityDataHelperTests {
    @Test
    fun `getLocalAuthorityDisplayName returns the correct Local Authority name for a custodian code`() {
        val custodianCode = LOCAL_AUTHORITIES[10].custodianCode
        val expectedDisplayName = LOCAL_AUTHORITIES[10].displayName

        val localAuthorityDisplayName = LocalAuthorityDataHelper.getLocalAuthorityDisplayName(custodianCode)

        assertEquals(localAuthorityDisplayName, expectedDisplayName)
    }
}
