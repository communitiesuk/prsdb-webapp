package uk.gov.communities.prsdb.webapp.helpers.extensions

import java.math.BigDecimal
import java.sql.Date
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

        fun PreparedStatement.setLongOrNull(
            parameterIndex: Int,
            value: Long?,
        ) {
            if (value == null) {
                this.setNull(parameterIndex, Types.BIGINT)
            } else {
                this.setLong(parameterIndex, value)
            }
        }

        fun PreparedStatement.setBigDecimalOrNull(
            parameterIndex: Int,
            value: BigDecimal?,
        ) {
            if (value == null) {
                this.setNull(parameterIndex, Types.NUMERIC)
            } else {
                this.setBigDecimal(parameterIndex, value)
            }
        }

        fun PreparedStatement.setBooleanOrNull(
            parameterIndex: Int,
            value: Boolean?,
        ) {
            if (value == null) {
                this.setNull(parameterIndex, Types.BOOLEAN)
            } else {
                this.setBoolean(parameterIndex, value)
            }
        }

        fun PreparedStatement.setDateOrNull(
            parameterIndex: Int,
            value: Date?,
        ) {
            if (value == null) {
                this.setNull(parameterIndex, Types.DATE)
            } else {
                this.setDate(parameterIndex, value)
            }
        }
    }
}
