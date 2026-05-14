package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.UploadService

internal const val DOWNLOAD_URL = "example.com/download"
internal const val VIRUS_SCAN_PENDING_WITH_NAME_KEY = "propertyCompliance.uploadedFile.virusScanPendingWithName"

abstract class ComplianceViewModelFactoryTests {
    abstract fun createRows(
        uploadService: UploadService,
        propertyCompliance: PropertyCompliance,
    ): List<SummaryListRowViewModel>

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideRows")
    fun `fromEntity returns the correct summary rows`(
        propertyCompliance: PropertyCompliance,
        expectedRows: List<SummaryListRowViewModel>,
    ) {
        val uploadService = mock<UploadService>()
        whenever(uploadService.getDownloadUrlOrNull(any(), anyOrNull())).thenReturn(DOWNLOAD_URL)

        val rows = createRows(uploadService, propertyCompliance)

        assertIterableEquals(rows, expectedRows)
    }
}
