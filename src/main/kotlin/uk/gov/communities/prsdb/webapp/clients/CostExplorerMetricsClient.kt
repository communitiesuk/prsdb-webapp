package uk.gov.communities.prsdb.webapp.clients

import uk.gov.communities.prsdb.webapp.models.dataModels.CostExplorerCostDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.ReportingPeriod

interface CostExplorerMetricsClient {
    fun getCost(period: ReportingPeriod): CostExplorerCostDataModel?
}
