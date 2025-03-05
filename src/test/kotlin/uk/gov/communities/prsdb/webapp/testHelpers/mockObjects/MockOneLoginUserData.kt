package uk.gov.communities.prsdb.webapp.testHelpers.mockObjects

import uk.gov.communities.prsdb.webapp.database.entity.OneLoginUser

class MockOneLoginUserData {
    companion object {
        fun createOneLoginUser(id: String = "test-base-user-id"): OneLoginUser = OneLoginUser(id)
    }
}
