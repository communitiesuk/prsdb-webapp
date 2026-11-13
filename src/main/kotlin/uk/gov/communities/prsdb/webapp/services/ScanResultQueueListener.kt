package uk.gov.communities.prsdb.webapp.services

import io.awspring.cloud.sqs.annotation.SqsListener
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.config.ScanResultQueueListenerConfig.Companion.SCAN_RESULT_QUEUE_LISTENER_CONTAINER_FACTORY
import uk.gov.communities.prsdb.webapp.config.ScanResultQueueListenerConfig.Companion.SCAN_RESULT_QUEUE_LISTENER_PROFILES
import uk.gov.communities.prsdb.webapp.exceptions.InvalidScanResultMessageException
import uk.gov.communities.prsdb.webapp.models.dataModels.GuardDutyScanResultEvent
import uk.gov.communities.prsdb.webapp.models.dataModels.ScanResult
import uk.gov.communities.prsdb.webapp.models.dataModels.UploadedFileLocator

@Service
@Profile(SCAN_RESULT_QUEUE_LISTENER_PROFILES)
class ScanResultQueueListener(
    private val virusScanProcessingService: VirusScanProcessingService,
    @Value("\${aws.s3.quarantineBucket}") private val quarantineBucketName: String,
) {
    @SqsListener(queueNames = ["\${scan-result-queue.name}"], factory = SCAN_RESULT_QUEUE_LISTENER_CONTAINER_FACTORY)
    fun receiveScanResult(payload: String) {
        val event = GuardDutyScanResultEvent.fromJson(payload)
        val objectDetails = event.detail.s3ObjectDetails

        if (objectDetails.bucketName != quarantineBucketName) {
            throw InvalidScanResultMessageException("Scan result received for unexpected bucket: ${objectDetails.bucketName}")
        }

        val scanResultStatus = event.detail.scanResultDetails.scanResultStatus
        val scanResult =
            ScanResult.fromStringValueOrNull(scanResultStatus)
                ?: throw InvalidScanResultMessageException("Unknown GuardDuty scan result status: $scanResultStatus")

        val locator = UploadedFileLocator(objectDetails.objectKey, objectDetails.eTag, objectDetails.versionId)

        println("Processing virus scan result $scanResult for $locator")
        virusScanProcessingService.processScan(locator, scanResult)
    }
}
