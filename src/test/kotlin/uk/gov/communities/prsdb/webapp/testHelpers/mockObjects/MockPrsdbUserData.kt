package uk.gov.communities.prsdb.webapp.testHelpers.mockObjects

import uk.gov.communities.prsdb.webapp.database.entity.PrsdbUser

class MockPrsdbUserData {
    companion object {
        fun createPrsdbUser(id: String = "test-base-user-id"): PrsdbUser = PrsdbUser(id)
    }
}
