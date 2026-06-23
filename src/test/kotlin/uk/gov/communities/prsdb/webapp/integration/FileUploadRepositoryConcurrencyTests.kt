package uk.gov.communities.prsdb.webapp.integration

import org.junit.jupiter.api.Assertions.assertTrue
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
import uk.gov.communities.prsdb.webapp.database.repository.FileUploadRepository
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.thread

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
        val txTemplate = TransactionTemplate(transactionManager)
        val objectKey = "lock-test-${UUID.randomUUID()}"
        val versionId = "v1"
        val id =
            txTemplate.execute {
                fileUploadRepository.save(FileUpload(FileUploadStatus.QUARANTINED, objectKey, "txt", "etag", versionId)).id
            }!!

        val lockHeld = CountDownLatch(1)
        val errors = CopyOnWriteArrayList<Throwable>()
        val blockedNanos = AtomicLong(-1)

        // TX-A: the scan-processor path locks the row by object key + version id and holds it.
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

        assertTrue(lockHeld.await(10, TimeUnit.SECONDS)) { "lock holder transaction never acquired the lock" }

        // TX-B: the submission path locks the same row by id and must block until TX-A commits.
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

        assertTrue(errors.isEmpty()) { "Unexpected errors during concurrent locking: $errors" }

        val blockedMillis = blockedNanos.get() / 1_000_000
        assertTrue(blockedMillis >= holdMillis / 2) {
            "Expected findWithLockById to block on the row lock for at least ${holdMillis / 2}ms while the other " +
                "transaction held it, but it returned after ${blockedMillis}ms. The pessimistic lock is not " +
                "serialising the scan-processor and submission finders."
        }
    }

    @Test
    fun `findWithLockById returns promptly when no other transaction holds the row lock`() {
        val txTemplate = TransactionTemplate(transactionManager)
        val objectKey = "no-contention-${UUID.randomUUID()}"
        val id =
            txTemplate.execute {
                fileUploadRepository.save(FileUpload(FileUploadStatus.QUARANTINED, objectKey, "txt", "etag", "v1")).id
            }!!

        val start = System.nanoTime()
        txTemplate.executeWithoutResult {
            requireNotNull(fileUploadRepository.findWithLockById(id)) { "could not find the seeded file upload" }
        }
        val elapsedMillis = (System.nanoTime() - start) / 1_000_000

        assertTrue(elapsedMillis < holdMillis / 2) {
            "Expected an uncontended locking lookup to be fast (< ${holdMillis / 2}ms) but it took ${elapsedMillis}ms. " +
                "The blocking observed in the contention test must come from row-lock contention, not slow queries."
        }
    }
}
