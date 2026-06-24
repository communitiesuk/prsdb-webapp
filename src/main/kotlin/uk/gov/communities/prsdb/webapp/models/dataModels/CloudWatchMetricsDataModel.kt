package uk.gov.communities.prsdb.webapp.models.dataModels

data class CloudWatchMetricsDataModel(
    val peakMemoryUtilisation: Double?,
    val averageMemoryUtilisation: Double?,
    val albClientErrorCount: Long?,
    val albServerErrorCount: Long?,
)
