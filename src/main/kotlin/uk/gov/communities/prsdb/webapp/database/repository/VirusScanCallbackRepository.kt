package uk.gov.communities.prsdb.webapp.database.repository

import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import uk.gov.communities.prsdb.webapp.database.entity.VirusScanCallback

interface VirusScanCallbackRepository : JpaRepository<VirusScanCallback, Long> {
    @Suppress("ktlint:standard:function-naming")
    fun findAllByFileUpload_ObjectKeyAndFileUpload_VersionId(
        objectKey: String,
        versionId: String?,
    ): List<VirusScanCallback>

    @Suppress("ktlint:standard:function-naming")
    fun findAllByFileUpload_Id(fileUploadId: Long): List<VirusScanCallback>

    // Set-based update so that re-pointing a callback whose row a concurrent scan has already deleted is a harmless
    // no-op rather than a stale-state failure (a loaded-entity save would fail Hibernate's row-count check on 0 rows).
    @Modifying
    @Transactional
    @Query("UPDATE VirusScanCallback c SET c.encodedCallbackData = :encodedCallbackData WHERE c.id = :id")
    fun updateEncodedCallbackDataById(
        id: Long,
        encodedCallbackData: String,
    )
}
