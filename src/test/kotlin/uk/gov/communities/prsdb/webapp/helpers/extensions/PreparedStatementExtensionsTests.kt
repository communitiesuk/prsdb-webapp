package uk.gov.communities.prsdb.webapp.helpers.extensions

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.communities.prsdb.webapp.helpers.extensions.PreparedStatementExtensions.Companion.setIntOrNull
import uk.gov.communities.prsdb.webapp.helpers.extensions.PreparedStatementExtensions.Companion.setStringOrNull
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
}
