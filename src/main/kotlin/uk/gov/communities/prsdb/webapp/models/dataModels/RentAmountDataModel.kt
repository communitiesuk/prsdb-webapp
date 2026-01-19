package uk.gov.communities.prsdb.webapp.models.dataModels

import java.math.BigDecimal

data class RentAmountDataModel(
    val rentAmount: BigDecimal,
) {
    companion object {
        fun fromFormData(rentAmount: String): RentAmountDataModel =
            RentAmountDataModel(
                rentAmount = rentAmount.toBigDecimal(),
            )
    }
}
