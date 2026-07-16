package uk.gov.communities.prsdb.webapp.models.dataModels

import java.math.BigDecimal

data class CostMetricsDataModel(
    val totalCost: BigDecimal? = null,
    val costPerTransaction: BigDecimal? = null,
    val currencyCode: String? = null,
    val isEstimated: Boolean = false,
)
