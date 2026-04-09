package uk.gov.communities.prsdb.webapp.helpers.extensions

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.communities.prsdb.webapp.helpers.extensions.PreparedStatementExtensions.Companion.setBigDecimalOrNull
import uk.gov.communities.prsdb.webapp.helpers.extensions.PreparedStatementExtensions.Companion.setBooleanOrNull
import uk.gov.communities.prsdb.webapp.helpers.extensions.PreparedStatementExtensions.Companion.setDateOrNull
import uk.gov.communities.prsdb.webapp.helpers.extensions.PreparedStatementExtensions.Companion.setIntOrNull
import uk.gov.communities.prsdb.webapp.helpers.extensions.PreparedStatementExtensions.Companion.setLongOrNull
import uk.gov.communities.prsdb.webapp.helpers.extensions.PreparedStatementExtensions.Companion.setStringOrNull
import java.math.BigDecimal
import java.sql.Date
import java.sql.PreparedStatement
import java.sql.Types

@ExtendWith(MockitoExtension::class)
class PreparedStatementExtensionsTests {
    @Mock
    private lateinit var mockPreparedStatement: PreparedStatement

    @Test
    fun `setStringOrNull sets string when value is not null`() {
        mockPreparedStatement.setStringOrNull(1, "abc")
        verify(mockPreparedStatement).setString(1, "abc")
    }

    @Test
    fun `setStringOrNull sets null when value is null`() {
        mockPreparedStatement.setStringOrNull(1, null)
        verify(mockPreparedStatement).setNull(1, Types.VARCHAR)
    }

    @Test
    fun `setIntOrNull sets int when value is not null`() {
        mockPreparedStatement.setIntOrNull(1, 100)
        verify(mockPreparedStatement).setInt(1, 100)
    }

    @Test
    fun `setIntOrNull sets null when value is null`() {
        mockPreparedStatement.setIntOrNull(1, null)
        verify(mockPreparedStatement).setNull(1, Types.INTEGER)
    }

    @Test
    fun `setLongOrNull sets long when value is not null`() {
        mockPreparedStatement.setLongOrNull(1, 100L)
        verify(mockPreparedStatement).setLong(1, 100L)
    }

    @Test
    fun `setLongOrNull sets null when value is null`() {
        mockPreparedStatement.setLongOrNull(1, null)
        verify(mockPreparedStatement).setNull(1, Types.BIGINT)
    }

    @Test
    fun `setBigDecimalOrNull sets BigDecimal when value is not null`() {
        val value = BigDecimal("123.45")
        mockPreparedStatement.setBigDecimalOrNull(1, value)
        verify(mockPreparedStatement).setBigDecimal(1, value)
    }

    @Test
    fun `setBigDecimalOrNull sets null when value is null`() {
        mockPreparedStatement.setBigDecimalOrNull(1, null)
        verify(mockPreparedStatement).setNull(1, Types.NUMERIC)
    }

    @Test
    fun `setBooleanOrNull sets boolean when value is not null`() {
        mockPreparedStatement.setBooleanOrNull(1, true)
        verify(mockPreparedStatement).setBoolean(1, true)
    }

    @Test
    fun `setBooleanOrNull sets null when value is null`() {
        mockPreparedStatement.setBooleanOrNull(1, null)
        verify(mockPreparedStatement).setNull(1, Types.BOOLEAN)
    }

    @Test
    fun `setDateOrNull sets date when value is not null`() {
        val value = Date.valueOf("2020-01-01")
        mockPreparedStatement.setDateOrNull(1, value)
        verify(mockPreparedStatement).setDate(1, value)
    }

    @Test
    fun `setDateOrNull sets null when value is null`() {
        mockPreparedStatement.setDateOrNull(1, null)
        verify(mockPreparedStatement).setNull(1, Types.DATE)
    }
}
