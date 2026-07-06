package uk.gov.communities.prsdb.webapp.models.dataModels

data class CloudWatchMetricsDataModel(
    val peakMemoryUtilisation: Double?,
    val averageMemoryUtilisation: Double?,
    val peakCpuUtilisation: Double?,
    val elastiCacheCpuUtilisation: Double?,
    val cloudFrontClientErrorRate: Double?,
    val cloudFrontServerErrorRate: Double?,
)
