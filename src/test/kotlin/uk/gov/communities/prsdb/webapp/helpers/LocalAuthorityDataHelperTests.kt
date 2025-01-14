package uk.gov.communities.prsdb.webapp.helpers

import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalAuthorityDataModel
import kotlin.test.assertEquals

class LocalAuthorityDataHelperTests {
    @Test
    fun `getLocalAuthorityDisplayName returns the correct Local Authority name for a custodian code`() {
        val localAuthority =
            LocalAuthorityDataModel(
                custodianCode = "1045",
                displayName = "DERBYSHIRE DALES DISTRICT COUNCIL",
            )
        val localAuthorityDisplayName = LocalAuthorityDataHelper.getLocalAuthorityDisplayName(localAuthority.custodianCode)

        assertEquals(localAuthorityDisplayName, localAuthority.displayName)
    }
}
