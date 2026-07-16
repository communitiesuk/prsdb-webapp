package uk.gov.communities.prsdb.webapp.clients

import org.springframework.context.annotation.Profile
import software.amazon.awssdk.services.costexplorer.CostExplorerClient
import software.amazon.awssdk.services.costexplorer.model.DateInterval
import software.amazon.awssdk.services.costexplorer.model.GetCostAndUsageRequest
import software.amazon.awssdk.services.costexplorer.model.Granularity
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.models.dataModels.CostExplorerCostDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.ReportingPeriod
import java.math.BigDecimal
import java.time.ZoneId

@Profile("!local")
@PrsdbWebService
class AwsCostExplorerMetricsClient(
    private val sdkClient: CostExplorerClient,
) : CostExplorerMetricsClient {
    override fun getCost(period: ReportingPeriod): CostExplorerCostDataModel? {
        var totalAmount = BigDecimal.ZERO
        var currencyCode: String? = null
        var isEstimated = false
        var hasMetricValue = false
        var nextPageToken: String? = null

        do {
            val response = sdkClient.getCostAndUsage(buildRequest(period, nextPageToken))

            for (result in response.resultsByTime()) {
                val metric = result.total()[UNBLENDED_COST] ?: return invalidResponse("UnblendedCost was missing")
                val amount = metric.amount()?.toBigDecimalOrNull() ?: return invalidResponse("amount was malformed")
                val unit = metric.unit()?.takeIf { it.isNotBlank() } ?: return invalidResponse("unit was blank")

                if (currencyCode != null && currencyCode != unit) {
                    return invalidResponse("units were inconsistent")
                }

                totalAmount += amount
                currencyCode = unit
                isEstimated = isEstimated || result.estimated()
                hasMetricValue = true
            }

            nextPageToken = response.nextPageToken()
        } while (!nextPageToken.isNullOrBlank())

        if (!hasMetricValue || currencyCode == null) {
            return invalidResponse("no usable metric values were returned")
        }

        return CostExplorerCostDataModel(totalAmount, currencyCode, isEstimated)
    }

    private fun buildRequest(
        period: ReportingPeriod,
        nextPageToken: String?,
    ): GetCostAndUsageRequest {
        val request =
            GetCostAndUsageRequest
                .builder()
                .timePeriod(
                    DateInterval
                        .builder()
                        .start(period.start.atZone(UK_ZONE).toLocalDate().toString())
                        .end(period.end.atZone(UK_ZONE).toLocalDate().plusDays(1).toString())
                        .build(),
                ).granularity(Granularity.DAILY)
                .metrics(UNBLENDED_COST)

        if (!nextPageToken.isNullOrBlank()) {
            request.nextPageToken(nextPageToken)
        }

        return request.build()
    }

    private fun invalidResponse(reason: String): Nothing? {
        println("Failed to parse Cost Explorer metrics: $reason")
        return null
    }

    companion object {
        private const val UNBLENDED_COST = "UnblendedCost"
        private val UK_ZONE = ZoneId.of("Europe/London")
    }
}
