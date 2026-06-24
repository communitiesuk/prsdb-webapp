package uk.gov.communities.prsdb.webapp.database.repository

import jakarta.persistence.LockModeType
import jakarta.persistence.QueryHint
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.QueryHints
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload

// Bounds how long a locking finder waits for the file_upload row lock before failing. The scan-processor holds this
// lock across non-transactional S3 work, so without a bound a hung S3 call could pin the row (and its database
// connection) indefinitely. On PostgreSQL a query timeout cancels the whole SELECT ... FOR UPDATE - including the
// lock wait - raising a QueryTimeoutException that rolls the transaction back.
const val FILE_UPLOAD_LOCK_WAIT_TIMEOUT_MILLIS = "10000"

interface FileUploadRepository : JpaRepository<FileUpload, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(QueryHint(name = "jakarta.persistence.query.timeout", value = FILE_UPLOAD_LOCK_WAIT_TIMEOUT_MILLIS))
    fun findWithLockById(id: Long): FileUpload?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(QueryHint(name = "jakarta.persistence.query.timeout", value = FILE_UPLOAD_LOCK_WAIT_TIMEOUT_MILLIS))
    fun findWithLockByObjectKeyAndVersionId(
        objectKey: String,
        versionId: String?,
    ): FileUpload?
}
