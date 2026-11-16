package uk.gov.communities.prsdb.webapp.models.dataModels

import kotlinx.serialization.SerializationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GuardDutyScanResultEventTests {
    @Test
    fun `fromJson reads the scanned object and result from a GuardDuty scan result event`() {
        val event = GuardDutyScanResultEvent.fromJson(SAMPLE_EVENT)

        assertEquals("prsdb-quarantine-integration", event.detail.s3ObjectDetails.bucketName)
        assertEquals("certificateUpload.journey.gas-safety-certificate-upload.1", event.detail.s3ObjectDetails.objectKey)
        assertEquals("sample-e-tag", event.detail.s3ObjectDetails.eTag)
        assertEquals("version-1", event.detail.s3ObjectDetails.versionId)
        assertEquals("NO_THREATS_FOUND", event.detail.scanResultDetails.scanResultStatus)
    }

    @Test
    fun `fromJson throws a SerializationException for a message that is not a scan result event`() {
        assertThrows<SerializationException> { GuardDutyScanResultEvent.fromJson("""{"detail":{}}""") }
    }

    @Test
    fun `an event created for an uploaded file can be read back`() {
        val locator = UploadedFileLocator("object-key", "e-tag", "version-id")

        val event = GuardDutyScanResultEvent.fromJson(GuardDutyScanResultEvent.of("bucket", locator, ScanResult.Threats).toJson())

        assertEquals("bucket", event.detail.s3ObjectDetails.bucketName)
        assertEquals(locator, event.detail.s3ObjectDetails.let { UploadedFileLocator(it.objectKey, it.eTag, it.versionId) })
        assertEquals(ScanResult.Threats.value, event.detail.scanResultDetails.scanResultStatus)
    }

    companion object {
        // Shape of the EventBridge event for "GuardDuty Malware Protection Object Scan Result"
        const val SAMPLE_EVENT = """
            {
              "version": "0",
              "id": "72c7d362-737a-6dce-fc78-9e27a0171419",
              "detail-type": "GuardDuty Malware Protection Object Scan Result",
              "source": "aws.guardduty",
              "account": "111122223333",
              "time": "2026-11-07T10:00:00Z",
              "region": "eu-west-2",
              "resources": ["arn:aws:guardduty:eu-west-2:111122223333:malware-protection-plan/example"],
              "detail": {
                "schemaVersion": "1.0",
                "scanStatus": "COMPLETED",
                "resourceType": "S3_OBJECT",
                "s3ObjectDetails": {
                  "bucketName": "prsdb-quarantine-integration",
                  "objectKey": "certificateUpload.journey.gas-safety-certificate-upload.1",
                  "eTag": "sample-e-tag",
                  "versionId": "version-1",
                  "s3Throttled": false
                },
                "scanResultDetails": {
                  "scanResultStatus": "NO_THREATS_FOUND",
                  "threats": null
                }
              }
            }
        """
    }
}
