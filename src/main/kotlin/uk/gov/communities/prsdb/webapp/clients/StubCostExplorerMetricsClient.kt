package uk.gov.communities.prsdb.webapp.clients

import org.springframework.context.annotation.Profile
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.models.dataModels.CostExplorerCostDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.ReportingPeriod
import java.math.BigDecimal

@Profile("local")
@PrsdbWebService
class StubCostExplorerMetricsClient : CostExplorerMetricsClient {
    override fun getCost(period: ReportingPeriod): CostExplorerCostDataModel = CostExplorerCostDataModel(BigDecimal("123.45"), "USD", true)
}
