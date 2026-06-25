package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.VirusScanCallback

interface VirusScanCallbackRepository : JpaRepository<VirusScanCallback, Long> {
    @Suppress("ktlint:standard:function-naming")
    fun findAllByFileUpload_ObjectKeyAndFileUpload_VersionId(
        objectKey: String,
        versionId: String?,
    ): List<VirusScanCallback>

    @Suppress("ktlint:standard:function-naming")
    fun findAllByFileUpload_Id(fileUploadId: Long): List<VirusScanCallback>
}
