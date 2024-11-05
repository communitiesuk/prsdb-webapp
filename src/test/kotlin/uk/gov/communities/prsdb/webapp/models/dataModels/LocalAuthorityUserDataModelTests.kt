package uk.gov.communities.prsdb.webapp.models.dataModels

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class LocalAuthorityUserDataModelTests {
    @Test
    fun `LocalAuthorityUserDataModel's constructor produces an email of the expected format`() {
        val userName = "name"
        val localAuthorityName = "LAName"
        val expectedEmail = "$userName@$localAuthorityName.co.uk"

        val localAuthorityUserDataModel = LocalAuthorityUserDataModel(1, userName, localAuthorityName, false)

        assertEquals(expectedEmail, localAuthorityUserDataModel.email)
    }
}
