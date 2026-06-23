package uk.gov.communities.prsdb.webapp.integration

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.transfer.s3.S3TransferManager
import uk.gov.communities.prsdb.webapp.TestcontainersConfiguration
import uk.gov.communities.prsdb.webapp.clients.OsDownloadsClient
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.database.repository.FILE_UPLOAD_LOCK_WAIT_TIMEOUT_MILLIS
import uk.gov.communities.prsdb.webapp.database.repository.FileUploadRepository
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@Import(TestcontainersConfiguration::class)
@SpringBootTest
@ActiveProfiles("web-server-deactivated", "local")
// These environment variables are required for the expected beans to be created - values aren't needed
@TestPropertySource(properties = ["EMAILNOTIFICATIONS_APIKEY", "OS_API_KEY"])
class FileUploadRepositoryConcurrencyTests {
    @Autowired
    private lateinit var fileUploadRepository: FileUploadRepository

    @Autowired
    private lateinit var transactionManager: PlatformTransactionManager

    @MockitoBean
    lateinit var s3: S3TransferManager

    @MockitoBean
    lateinit var s3client: S3Client

    @MockitoBean
    lateinit var osDownloadsClient: OsDownloadsClient

    private val holdMillis = 2_000L

    @Test
    fun `findWithLockById blocks while findByObjectKeyAndVersionId holds a pessimistic lock on the same row`() {
        // Arrange - seed a file upload and have the scan-processor finder (TX-A) take and hold its row lock
        val txTemplate = TransactionTemplate(transactionManager)
        val objectKey = "lock-test-${UUID.randomUUID()}"
        val versionId = "v1"
        val id = seedQuarantinedFileUpload(txTemplate, objectKey, versionId)

        val lockHeld = CountDownLatch(1)
        val errors = CopyOnWriteArrayList<Throwable>()
        val blockedNanos = AtomicLong(-1)

        val holder =
            thread(name = "lock-holder") {
                try {
                    txTemplate.executeWithoutResult {
                        requireNotNull(fileUploadRepository.findByObjectKeyAndVersionId(objectKey, versionId)) {
                            "lock holder could not find the seeded file upload"
                        }
                        lockHeld.countDown()
                        Thread.sleep(holdMillis)
                    }
                } catch (t: Throwable) {
                    errors.add(t)
                }
            }
        assertTrue(lockHeld.await(10, TimeUnit.SECONDS), "lock holder transaction never acquired the lock")

        // Act - the submission finder (TX-B) locks the same row by id and must block until TX-A commits
        val contender =
            thread(name = "lock-contender") {
                try {
                    txTemplate.executeWithoutResult {
                        val start = System.nanoTime()
                        requireNotNull(fileUploadRepository.findWithLockById(id)) {
                            "lock contender could not find the seeded file upload"
                        }
                        blockedNanos.set(System.nanoTime() - start)
                    }
                } catch (t: Throwable) {
                    errors.add(t)
                }
            }
        holder.join()
        contender.join()

        // Assert
        assertTrue(errors.isEmpty(), "Unexpected errors during concurrent locking: $errors")
        val blockedMillis = blockedNanos.get() / 1_000_000
        assertTrue(
            blockedMillis >= holdMillis / 2,
            "Expected findWithLockById to block on the row lock for at least ${holdMillis / 2}ms while the other " +
                "transaction held it, but it returned after ${blockedMillis}ms. The pessimistic lock is not " +
                "serialising the scan-processor and submission finders.",
        )
    }

    @Test
    fun `findWithLockById returns promptly when no other transaction holds the row lock`() {
        // Arrange
        val txTemplate = TransactionTemplate(transactionManager)
        val objectKey = "no-contention-${UUID.randomUUID()}"
        val id = seedQuarantinedFileUpload(txTemplate, objectKey, "v1")

        // Act
        val start = System.nanoTime()
        txTemplate.executeWithoutResult {
            requireNotNull(fileUploadRepository.findWithLockById(id)) { "could not find the seeded file upload" }
        }
        val elapsedMillis = (System.nanoTime() - start) / 1_000_000

        // Assert
        assertTrue(
            elapsedMillis < holdMillis / 2,
            "Expected an uncontended locking lookup to be fast (< ${holdMillis / 2}ms) but it took ${elapsedMillis}ms. " +
                "The blocking observed in the contention test must come from row-lock contention, not slow queries.",
        )
    }

    @Test
    fun `a locking finder aborts with a timeout rather than waiting indefinitely when the row lock is held too long`() {
        // Arrange - hold the row lock for longer than the configured lock-wait timeout, simulating a hung dequarantine
        val lockTimeoutMillis = FILE_UPLOAD_LOCK_WAIT_TIMEOUT_MILLIS.toLong()
        val holdLongerThanTimeoutMillis = lockTimeoutMillis + 2_000L

        val txTemplate = TransactionTemplate(transactionManager)
        val objectKey = "lock-timeout-${UUID.randomUUID()}"
        val id = seedQuarantinedFileUpload(txTemplate, objectKey, "v1")

        val lockHeld = CountDownLatch(1)
        val errors = CopyOnWriteArrayList<Throwable>()
        val contenderError = AtomicReference<Throwable?>(null)
        val contenderNanos = AtomicLong(-1)

        val holder =
            thread(name = "long-lock-holder") {
                try {
                    txTemplate.executeWithoutResult {
                        requireNotNull(fileUploadRepository.findWithLockById(id)) {
                            "lock holder could not find the seeded file upload"
                        }
                        lockHeld.countDown()
                        Thread.sleep(holdLongerThanTimeoutMillis)
                    }
                } catch (t: Throwable) {
                    errors.add(t)
                }
            }
        assertTrue(lockHeld.await(10, TimeUnit.SECONDS), "lock holder transaction never acquired the lock")

        // Act - the contending finder must give up around the timeout, not wait for the full hold
        val contender =
            thread(name = "timing-out-contender") {
                val start = System.nanoTime()
                try {
                    txTemplate.executeWithoutResult {
                        fileUploadRepository.findByObjectKeyAndVersionId(objectKey, "v1")
                    }
                } catch (t: Throwable) {
                    contenderError.set(t)
                } finally {
                    contenderNanos.set(System.nanoTime() - start)
                }
            }
        holder.join()
        contender.join()

        // Assert
        assertTrue(errors.isEmpty(), "Unexpected errors on the lock holder: $errors")
        val thrown =
            assertNotNull(
                contenderError.get(),
                "Expected the contending finder to abort with a lock-wait timeout, but it returned normally after " +
                    "${contenderNanos.get() / 1_000_000}ms. The query.timeout hint is not bounding the FOR UPDATE lock wait.",
            )
        assertTrue(
            isLockWaitTimeout(thrown),
            "Expected a query/lock timeout exception but got ${thrown::class.qualifiedName}: ${thrown.message}",
        )
        val contenderMillis = contenderNanos.get() / 1_000_000
        assertTrue(
            contenderMillis < holdLongerThanTimeoutMillis,
            "Expected the contender to abort around the ${lockTimeoutMillis}ms timeout, well before the " +
                "${holdLongerThanTimeoutMillis}ms hold, but it took ${contenderMillis}ms.",
        )
    }

    private fun seedQuarantinedFileUpload(
        txTemplate: TransactionTemplate,
        objectKey: String,
        versionId: String,
    ): Long =
        txTemplate.execute {
            fileUploadRepository.save(FileUpload(FileUploadStatus.QUARANTINED, objectKey, "txt", "etag", versionId)).id
        }!!

    private fun isLockWaitTimeout(throwable: Throwable): Boolean {
        var cause: Throwable? = throwable
        while (cause != null) {
            val name = cause::class.qualifiedName.orEmpty()
            val message = cause.message.orEmpty()
            if (name.contains("QueryTimeout", ignoreCase = true) ||
                name.contains("LockTimeout", ignoreCase = true) ||
                name.contains("PessimisticLock", ignoreCase = true) ||
                message.contains("canceling statement due to user request", ignoreCase = true)
            ) {
                return true
            }
            cause = cause.cause
        }
        return false
    }
}
