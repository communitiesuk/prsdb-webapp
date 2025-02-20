package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType

class SummaryListRowViewModelTests {
    companion object {
        @JvmStatic
        val convertableValues =
            listOf(true, false) +
                PropertyType.entries +
                OwnershipType.entries +
                LicensingType.entries
    }

    @ParameterizedTest
    @MethodSource("getConvertableValues")
    fun `getConvertedFieldValue converts known values to message keys`(value: Any) {
        // Arrange
        val model = SummaryListRowViewModel("", value, null)
        val messageText = javaClass.getResource("/messages.properties")?.readText() ?: ""

        // Act
        val result = model.getConvertedFieldValue()

        // Assert
        messageText.contains(result.toString())
    }
}
