package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.CertificateUpload

interface CertificateUploadRepository : JpaRepository<CertificateUpload, Long> {
    @Suppress("ktlint:standard:function-naming")
    fun findByFileUpload_ObjectKeyAndFileUpload_VersionId(
        objectKey: String,
        versionId: String?,
    ): CertificateUpload?

    @Suppress("ktlint:standard:function-naming")
    fun findByFileUpload_Id(fileUploadId: Long): CertificateUpload?
}
