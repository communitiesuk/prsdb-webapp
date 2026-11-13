package uk.gov.communities.prsdb.webapp.models.dataModels

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class GuardDutyScanResultEvent(
    val detail: Detail,
) {
    @Serializable
    data class Detail(
        val s3ObjectDetails: S3ObjectDetails,
        val scanResultDetails: ScanResultDetails,
    )

    @Serializable
    data class S3ObjectDetails(
        val bucketName: String,
        val objectKey: String,
        val eTag: String,
        val versionId: String? = null,
    )

    @Serializable
    data class ScanResultDetails(
        val scanResultStatus: String,
    )

    fun toJson(): String = json.encodeToString(this)

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromJson(payload: String): GuardDutyScanResultEvent = json.decodeFromString(payload)

        fun of(
            bucketName: String,
            locator: UploadedFileLocator,
            scanResult: ScanResult,
        ) = GuardDutyScanResultEvent(
            Detail(
                S3ObjectDetails(bucketName, locator.objectKey, locator.eTag, locator.versionId),
                ScanResultDetails(scanResult.value),
            ),
        )
    }
}
