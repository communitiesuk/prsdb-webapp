package uk.gov.communities.prsdb.webapp.local.services

import io.awspring.cloud.sqs.operations.SqsSendOptions
import io.awspring.cloud.sqs.operations.SqsTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.models.dataModels.GuardDutyScanResultEvent
import uk.gov.communities.prsdb.webapp.models.dataModels.ScanResult
import uk.gov.communities.prsdb.webapp.models.dataModels.UploadedFileLocator
import java.util.function.Consumer

@PrsdbWebService
@Profile("local")
class LocalVirusScanSimulator(
    private val sqsTemplate: SqsTemplate,
    @Value("\${scan-result-queue.name}") private val scanResultQueueName: String,
    @Value("\${aws.s3.quarantineBucket}") private val quarantineBucketName: String,
    @Value("\${local.virus-scan.simulated-scan-delay-seconds}") private val simulatedScanDelaySeconds: Int,
) {
    fun simulateScan(
        locator: UploadedFileLocator,
        fileName: String?,
    ) {
        val scanResult =
            if (fileName?.contains(THREAT_FILE_NAME_MARKER, ignoreCase = true) == true) ScanResult.Threats else ScanResult.NoThreats
        val payload = GuardDutyScanResultEvent.of(quarantineBucketName, locator, scanResult).toJson()

        try {
            sqsTemplate.send(
                Consumer<SqsSendOptions<String>> { options ->
                    options.queue(scanResultQueueName).payload(payload).delaySeconds(simulatedScanDelaySeconds)
                },
            )
        } catch (exception: RuntimeException) {
            println(
                "The simulated virus scan result for ${locator.objectKey} could not be sent, so the file will stay " +
                    "quarantined: ${exception.message}",
            )
        }
    }

    companion object {
        const val THREAT_FILE_NAME_MARKER = "virus"
    }
}
