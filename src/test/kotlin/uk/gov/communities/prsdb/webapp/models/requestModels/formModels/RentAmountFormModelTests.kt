package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class RentAmountFormModelTests {
    @Test
    fun `rentAmount is invalid if it has more than two decimal places`() {
        val rentAmountFormModel = RentAmountFormModel()
        rentAmountFormModel.rentAmount = "400.123"
        assertFalse(rentAmountFormModel.isNotMoreThanTwoDecimalPlaces())
    }

    @ParameterizedTest
    @ValueSource(
        strings = ["400.12", "400.1", "400"],
    )
    fun `rentAmount is invalid if it is two decimal places or fewer`(rentAmount: String) {
        val rentAmountFormModel = RentAmountFormModel()
        rentAmountFormModel.rentAmount = rentAmount
        assertTrue(rentAmountFormModel.isNotMoreThanTwoDecimalPlaces())
    }
}
