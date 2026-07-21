package uk.gov.communities.prsdb.webapp.services

import software.amazon.awssdk.core.exception.SdkException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.clients.CostExplorerMetricsClient
import uk.gov.communities.prsdb.webapp.models.dataModels.CostMetricsDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.ReportingPeriod
import java.math.BigDecimal
import java.math.RoundingMode

@PrsdbWebService
class CostExplorerMetricsService(
    private val client: CostExplorerMetricsClient,
) {
    fun getMetrics(
        period: ReportingPeriod,
        transactionCount: Long,
    ): CostMetricsDataModel {
        val cost =
            try {
                client.getCost(period)
            } catch (e: SdkException) {
                println("Failed to fetch Cost Explorer metrics: ${e.message}")
                return CostMetricsDataModel()
            } ?: return CostMetricsDataModel()

        val costPerTransaction =
            if (transactionCount > 0) {
                cost.amount.divide(BigDecimal.valueOf(transactionCount), 2, RoundingMode.HALF_UP)
            } else {
                null
            }

        return CostMetricsDataModel(
            totalCost = cost.amount,
            costPerTransaction = costPerTransaction,
            currencyCode = cost.currencyCode,
            isEstimated = cost.isEstimated,
        )
    }
}
