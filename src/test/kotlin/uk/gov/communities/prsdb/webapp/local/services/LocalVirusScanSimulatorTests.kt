package uk.gov.communities.prsdb.webapp.local.services

import io.awspring.cloud.sqs.operations.SqsSendOptions
import io.awspring.cloud.sqs.operations.SqsTemplate
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Answers
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.models.dataModels.GuardDutyScanResultEvent
import uk.gov.communities.prsdb.webapp.models.dataModels.ScanResult
import uk.gov.communities.prsdb.webapp.models.dataModels.UploadedFileLocator
import java.util.function.Consumer

class LocalVirusScanSimulatorTests {
    private val sqsTemplate: SqsTemplate = mock()
    private val simulator = LocalVirusScanSimulator(sqsTemplate, SCAN_RESULT_QUEUE, QUARANTINE_BUCKET, DELAY_SECONDS)
    private val locator = UploadedFileLocator("object-key", "e-tag", "version-id")

    private fun sentOptions(): SqsSendOptions<String> {
        val captor = argumentCaptor<Consumer<SqsSendOptions<String>>>()
        verify(sqsTemplate).send(captor.capture())
        val options = mock<SqsSendOptions<String>>(defaultAnswer = Answers.RETURNS_SELF)
        captor.firstValue.accept(options)
        return options
    }

    @Test
    fun `simulateScan sends a delayed no threats found result for the uploaded file`() {
        simulator.simulateScan(locator, "certificate.pdf")

        val options = sentOptions()
        verify(options).queue(SCAN_RESULT_QUEUE)
        verify(options).delaySeconds(DELAY_SECONDS)
        verify(options).payload(GuardDutyScanResultEvent.of(QUARANTINE_BUCKET, locator, ScanResult.NoThreats).toJson())
    }

    @ParameterizedTest
    @ValueSource(strings = ["virus.pdf", "my-VIRUS-test.png", "Virus certificate.jpg"])
    fun `simulateScan sends a threats found result when the file name contains virus`(fileName: String) {
        simulator.simulateScan(locator, fileName)

        verify(sentOptions()).payload(GuardDutyScanResultEvent.of(QUARANTINE_BUCKET, locator, ScanResult.Threats).toJson())
    }

    @Test
    fun `simulateScan does not throw when the scan result cannot be sent`() {
        whenever(sqsTemplate.send(any<Consumer<SqsSendOptions<String>>>())).thenThrow(RuntimeException("SQS unavailable"))

        assertDoesNotThrow { simulator.simulateScan(locator, "certificate.pdf") }
    }

    companion object {
        private const val SCAN_RESULT_QUEUE = "scan-result-queue"
        private const val QUARANTINE_BUCKET = "quarantine-bucket"
        private const val DELAY_SECONDS = 5
    }
}
