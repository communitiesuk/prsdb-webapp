package uk.gov.communities.prsdb.webapp.services

import kotlinx.serialization.SerializationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.InvalidScanResultMessageException
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.models.dataModels.GuardDutyScanResultEvent
import uk.gov.communities.prsdb.webapp.models.dataModels.GuardDutyScanResultEventTests
import uk.gov.communities.prsdb.webapp.models.dataModels.ScanResult
import uk.gov.communities.prsdb.webapp.models.dataModels.UploadedFileLocator

class ScanResultQueueListenerTests {
    private val virusScanProcessingService: VirusScanProcessingService = mock()
    private val listener = ScanResultQueueListener(virusScanProcessingService, QUARANTINE_BUCKET)
    private val locator = UploadedFileLocator("object-key", "e-tag", "version-id")

    @ParameterizedTest
    @EnumSource(ScanResult::class)
    fun `receiveScanResult processes the scan result for the scanned file`(scanResult: ScanResult) {
        listener.receiveScanResult(GuardDutyScanResultEvent.of(QUARANTINE_BUCKET, locator, scanResult).toJson())

        verify(virusScanProcessingService).processScan(locator, scanResult)
    }

    @Test
    fun `receiveScanResult processes a full GuardDuty scan result event`() {
        listener.receiveScanResult(GuardDutyScanResultEventTests.SAMPLE_EVENT.replace("prsdb-quarantine-integration", QUARANTINE_BUCKET))

        verify(virusScanProcessingService).processScan(
            UploadedFileLocator("certificateUpload.journey.gas-safety-certificate-upload.1", "sample-e-tag", "version-1"),
            ScanResult.NoThreats,
        )
    }

    @Test
    fun `receiveScanResult rejects a scan result for a different bucket`() {
        val payload = GuardDutyScanResultEvent.of("other-bucket", locator, ScanResult.NoThreats).toJson()

        assertThrows<InvalidScanResultMessageException> { listener.receiveScanResult(payload) }
        verifyNoInteractions(virusScanProcessingService)
    }

    @Test
    fun `receiveScanResult rejects an unknown scan result status`() {
        val payload =
            GuardDutyScanResultEvent(
                GuardDutyScanResultEvent.Detail(
                    GuardDutyScanResultEvent.S3ObjectDetails(QUARANTINE_BUCKET, "object-key", "e-tag", "version-id"),
                    GuardDutyScanResultEvent.ScanResultDetails("NOT_A_STATUS"),
                ),
            ).toJson()

        assertThrows<InvalidScanResultMessageException> { listener.receiveScanResult(payload) }
        verifyNoInteractions(virusScanProcessingService)
    }

    @Test
    fun `receiveScanResult throws a SerializationException for an undecodable message`() {
        assertThrows<SerializationException> { listener.receiveScanResult("not json") }
    }

    @Test
    fun `receiveScanResult rethrows processing failures so the scan result is retried`() {
        val failure = PrsdbWebException("Failed to dequarantine file: object-key")
        whenever(virusScanProcessingService.processScan(any(), any())).thenThrow(failure)
        val payload = GuardDutyScanResultEvent.of(QUARANTINE_BUCKET, locator, ScanResult.NoThreats).toJson()

        val thrown = assertThrows<PrsdbWebException> { listener.receiveScanResult(payload) }

        assertEquals(failure, thrown)
    }

    companion object {
        private const val QUARANTINE_BUCKET = "quarantine-bucket"
    }
}
