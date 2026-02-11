package uk.gov.communities.prsdb.webapp.helpers.extensions

import java.sql.PreparedStatement
import java.sql.Types

class PreparedStatementExtensions {
    companion object {
        fun PreparedStatement.setStringOrNull(
            parameterIndex: Int,
            value: String?,
        ) {
            if (value == null) {
                this.setNull(parameterIndex, Types.VARCHAR)
            } else {
                this.setString(parameterIndex, value)
            }
        }

        fun PreparedStatement.setIntOrNull(
            parameterIndex: Int,
            value: Int?,
        ) {
            if (value == null) {
                this.setNull(parameterIndex, Types.INTEGER)
            } else {
                this.setInt(parameterIndex, value)
            }
        }
    }
}
