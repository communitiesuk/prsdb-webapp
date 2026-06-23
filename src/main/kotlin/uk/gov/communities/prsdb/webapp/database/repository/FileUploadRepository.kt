package uk.gov.communities.prsdb.webapp.database.repository

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload

interface FileUploadRepository : JpaRepository<FileUpload, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findWithLockById(id: Long): FileUpload?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findByObjectKeyAndVersionId(
        objectKey: String,
        versionId: String?,
    ): FileUpload?
}
