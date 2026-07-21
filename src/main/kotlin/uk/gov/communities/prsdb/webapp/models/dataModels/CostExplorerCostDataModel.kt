package uk.gov.communities.prsdb.webapp.models.dataModels

import java.math.BigDecimal

data class CostExplorerCostDataModel(
    val amount: BigDecimal,
    val currencyCode: String,
    val isEstimated: Boolean,
)
