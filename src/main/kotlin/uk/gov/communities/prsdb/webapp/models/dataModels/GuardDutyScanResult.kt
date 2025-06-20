package uk.gov.communities.prsdb.webapp.models.dataModels

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

data class GuardDutyScanResult(
    val schemaVersion: String? = null,
    val scanStatus: String,
    val resourceType: String? = null,
    val s3ObjectDetails: S3ObjectDetails? = null,
    val scanResultDetails: ScanResultDetails? = null,
) {
    companion object {
        fun fromJson(json: String): GuardDutyScanResult {
            val mapper = jacksonObjectMapper()
            return mapper.readValue(json)
        }
    }

    data class S3ObjectDetails(
        val bucketName: String,
        val objectKey: String,
        val eTag: String,
        val versionId: String,
        val s3Throttled: Boolean,
    )

    data class ScanResultDetails(
        val scanResultStatus: String,
        val threats: List<Threat>? = null,
    )

    data class Threat(
        val name: String,
    )
}
