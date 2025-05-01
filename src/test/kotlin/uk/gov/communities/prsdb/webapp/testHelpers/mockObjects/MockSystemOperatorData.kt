package uk.gov.communities.prsdb.webapp.testHelpers.mockObjects

import uk.gov.communities.prsdb.webapp.database.entity.OneLoginUser
import uk.gov.communities.prsdb.webapp.database.entity.SystemOperator

class MockSystemOperatorData {
    companion object {
        const val DEFAULT_SYSTEM_OPERATOR_ID = 123.toLong()

        fun createSystemOperator(
            baseUser: OneLoginUser = MockOneLoginUserData.createOneLoginUser(),
            id: Long = DEFAULT_SYSTEM_OPERATOR_ID,
        ): SystemOperator = SystemOperator(id, baseUser)
    }
}
