package uk.gov.communities.prsdb.webapp.models.dataModels

import java.time.Duration

data class MetricsDataModel(
    val numberOfLandlordRegistrations: Long,
    val numberOfVerifiedLandlords: Long,
    val numberOfProperties: Long,
    val numberOfLandlordsWithAProperty: Long,
    val medianTimeToFirstProperty: Duration?,
    val p90TimeToFirstProperty: Duration?,
    val p95TimeToFirstProperty: Duration?,
)
