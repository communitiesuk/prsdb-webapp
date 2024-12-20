package uk.gov.communities.prsdb.webapp.models.dataModels

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordType
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.models.viewModels.FormSummaryViewModel

class FormSummaryViewModelTest {
    companion object {
        @JvmStatic
        val convertableValues =
            listOf(true, false) +
                // TODO PRSD-674 - Organisational Landlords have not yet been implemented
                LandlordType.entries.filter { it != LandlordType.COMPANY } +
                PropertyType.entries +
                OwnershipType.entries +
                LicensingType.entries
    }

    @Test
    fun `isListSummaryItem returns true if the summary value is a list`() {
        // Arrange
        val model = FormSummaryViewModel("", listOf<Any>(), null)

        // Act
        val result = model.isListSummaryItem()

        // Assert
        assertTrue(result)
    }

    @Test
    fun `isListSummaryItem returns false if the summary value is not a list`() {
        // Arrange
        val model = FormSummaryViewModel("", "value", null)

        // Act
        val result = model.isListSummaryItem()

        // Assert
        assertFalse(result)
    }

    @ParameterizedTest
    @MethodSource("getConvertableValues")
    fun `getConvertedFieldValue converts known values to message keys`(value: Any) {
        // Arrange
        val model = FormSummaryViewModel("", value, null)
        val messageText = javaClass.getResource("/messages.properties")?.readText() ?: ""

        // Act
        val result = model.getConvertedFieldValue()

        // Assert
        messageText.contains(result.toString())
    }
}
