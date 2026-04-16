package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityNotFoundException
import org.apache.http.HttpException
import org.hibernate.SessionFactory
import org.hibernate.StatelessSession
import org.hibernate.Transaction
import org.hibernate.jdbc.Work
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Named.named
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.ArgumentMatchers.eq
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockedConstruction
import org.mockito.Mockito.lenient
import org.mockito.Mockito.mockConstruction
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.whenever
import org.springframework.core.env.Environment
import org.springframework.util.ResourceUtils
import uk.gov.communities.prsdb.webapp.clients.OsDownloadsClient
import uk.gov.communities.prsdb.webapp.database.dao.NgdAddressLoaderDao
import uk.gov.communities.prsdb.webapp.database.repository.LocalCouncilRepository
import uk.gov.communities.prsdb.webapp.services.NgdAddressLoader.Companion.BATCH_SIZE
import uk.gov.communities.prsdb.webapp.services.NgdAddressLoader.Companion.DATA_PACKAGE_FILE_NAME
import uk.gov.communities.prsdb.webapp.services.NgdAddressLoader.Companion.DATA_PACKAGE_ID
import uk.gov.communities.prsdb.webapp.services.NgdAddressLoader.Companion.DATA_PACKAGE_VERSION_COMMENT_PREFIX
import uk.gov.communities.prsdb.webapp.services.NgdAddressLoader.Companion.UPRN_BATCH_SIZE
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalCouncilData
import java.io.FileInputStream
import java.sql.Connection
import java.sql.PreparedStatement
import java.util.zip.ZipException
import kotlin.math.ceil
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class NgdAddressLoaderTests {
    @Mock
    private lateinit var mockSessionFactory: SessionFactory

    @Mock
    private lateinit var mockOsDownloadsClient: OsDownloadsClient

    @Mock
    private lateinit var mockLocalCouncilRepository: LocalCouncilRepository

    @Mock
    private lateinit var mockEnvironment: Environment

    @InjectMocks
    private lateinit var ngdAddressLoader: NgdAddressLoader

    @Mock
    private lateinit var mockSession: StatelessSession

    @Mock
    private lateinit var mockTransaction: Transaction

    @Mock
    private lateinit var mockConnection: Connection

    @Mock
    private lateinit var mockPreparedStatement: PreparedStatement

    private lateinit var ngdAddressLoaderDaoMockConstructor: MockedConstruction<NgdAddressLoaderDao>

    private val mockNgdAddressLoaderDao
        get() = ngdAddressLoaderDaoMockConstructor.constructed()[0]

    @BeforeEach
    fun setUp() {
        whenever(mockSessionFactory.openStatelessSession()).thenReturn(mockSession)
        lenient().`when`(mockSession.beginTransaction()).thenReturn(mockTransaction)
        lenient().`when`(mockSession.doWork(any())).doAnswer { invocation ->
            val work = invocation.arguments[0] as Work
            work.execute(mockConnection)
            null
        }

        whenever(mockEnvironment.activeProfiles).thenReturn(arrayOf("local"))
    }

    private fun setUpMockNgdAddressLoaderDao(mockInitializer: (mock: NgdAddressLoaderDao) -> Unit) {
        ngdAddressLoaderDaoMockConstructor =
            mockConstruction(NgdAddressLoaderDao::class.java) { mock, _ ->
                whenever(mock.getLoadAddressPreparedStatement(any())).thenReturn(mockPreparedStatement)
                mockInitializer(mock)
            }
    }

    @AfterEach
    fun tearDown() {
        if (::ngdAddressLoaderDaoMockConstructor.isInitialized) {
            ngdAddressLoaderDaoMockConstructor.close()
        }
    }

    @ParameterizedTest(name = "when {0}")
    @MethodSource("provideNoDataPackageVersionIdComments")
    fun `loadNewDataPackageVersions loads initial data package version when`(commentOnAddressTable: String?) {
        // Arrange
        setUpMockNgdAddressLoaderDao { mock ->
            whenever(mock.findCommentOnAddressTable()).thenReturn(commentOnAddressTable)
        }
        whenever(mockOsDownloadsClient.getDataPackageVersionHistory(DATA_PACKAGE_ID)).thenReturn(versionHistory)
        whenever(mockOsDownloadsClient.getDataPackageVersionFile(DATA_PACKAGE_ID, INITIAL_VERSION_ID, "$DATA_PACKAGE_FILE_NAME.zip"))
            .thenReturn(getNgdFileInputStream("emptyCsv.zip"))
        whenever(mockOsDownloadsClient.getDataPackageVersionDetails(DATA_PACKAGE_ID, INITIAL_VERSION_ID))
            .thenReturn(initialVersionDetailsWithoutNext)

        // Act
        ngdAddressLoader.loadNewDataPackageVersions()

        // Assert
        verify(mockNgdAddressLoaderDao).saveCommentOnAddressTable("$DATA_PACKAGE_VERSION_COMMENT_PREFIX$INITIAL_VERSION_ID")
    }

    @ParameterizedTest(name = "due to {0}")
    @MethodSource("provideNoInitialDataPackageVersionIdResponses")
    fun `loadNewDataPackageVersions throws exception when no initial data package version is found`(
        osDownloadsClientResponse: () -> String,
    ) {
        // Arrange
        setUpMockNgdAddressLoaderDao { mock ->
            whenever(mock.findCommentOnAddressTable()).thenReturn(DATA_PACKAGE_VERSION_COMMENT_PREFIX)
        }
        whenever(mockOsDownloadsClient.getDataPackageVersionHistory(DATA_PACKAGE_ID)).doAnswer { osDownloadsClientResponse() }

        // Act & Assert
        assertThrows<Exception> { ngdAddressLoader.loadNewDataPackageVersions() }
    }

    @Test
    fun `loadNewDataPackageVersions loads next data package versions when there's already one loaded`() {
        // Arrange
        setUpMockNgdAddressLoaderDao { mock ->
            whenever(mock.findCommentOnAddressTable()).thenReturn("$DATA_PACKAGE_VERSION_COMMENT_PREFIX$INITIAL_VERSION_ID")
        }
        whenever(mockOsDownloadsClient.getDataPackageVersionDetails(DATA_PACKAGE_ID, INITIAL_VERSION_ID))
            .thenReturn(initialVersionDetails)
        whenever(mockOsDownloadsClient.getDataPackageVersionFile(DATA_PACKAGE_ID, SECOND_VERSION_ID, "$DATA_PACKAGE_FILE_NAME.zip"))
            .thenReturn(getNgdFileInputStream("emptyCsv.zip"))

        whenever(mockOsDownloadsClient.getDataPackageVersionDetails(DATA_PACKAGE_ID, SECOND_VERSION_ID))
            .thenReturn(secondVersionDetails)
        whenever(mockOsDownloadsClient.getDataPackageVersionFile(DATA_PACKAGE_ID, THIRD_VERSION_ID, "$DATA_PACKAGE_FILE_NAME.zip"))
            .thenReturn(getNgdFileInputStream("emptyCsv.zip"))

        whenever(mockOsDownloadsClient.getDataPackageVersionDetails(DATA_PACKAGE_ID, THIRD_VERSION_ID))
            .thenReturn(thirdVersionDetails)

        // Act
        ngdAddressLoader.loadNewDataPackageVersions()

        // Assert
        val commentCaptor = argumentCaptor<String>()
        verify(mockNgdAddressLoaderDao, times(4)).saveCommentOnAddressTable(commentCaptor.capture())
        assertEquals("$DATA_PACKAGE_VERSION_COMMENT_PREFIX$SECOND_VERSION_ID", commentCaptor.allValues[1])
        assertEquals("$DATA_PACKAGE_VERSION_COMMENT_PREFIX$THIRD_VERSION_ID", commentCaptor.allValues[3])
    }

    @Test
    fun `loadNewDataPackageVersions throws exception when information about next data package version can't be found`() {
        // Arrange
        setUpMockNgdAddressLoaderDao { mock ->
            whenever(mock.findCommentOnAddressTable()).thenReturn("$DATA_PACKAGE_VERSION_COMMENT_PREFIX$INITIAL_VERSION_ID")
        }
        whenever(mockOsDownloadsClient.getDataPackageVersionDetails(DATA_PACKAGE_ID, INITIAL_VERSION_ID)).doAnswer { throw HttpException() }

        // Act & Assert
        assertThrows<HttpException> { ngdAddressLoader.loadNewDataPackageVersions() }
    }

    @Test
    fun `loadNewDataPackageVersions rolls-back current load and doesn't load next versions when error is thrown`() {
        // Arrange
        setUpMockNgdAddressLoaderDao { mock ->
            whenever(mock.findCommentOnAddressTable()).thenReturn(DATA_PACKAGE_VERSION_COMMENT_PREFIX)
        }
        whenever(mockOsDownloadsClient.getDataPackageVersionHistory(DATA_PACKAGE_ID)).thenReturn(versionHistory)
        whenever(mockOsDownloadsClient.getDataPackageVersionFile(DATA_PACKAGE_ID, INITIAL_VERSION_ID, "$DATA_PACKAGE_FILE_NAME.zip"))
            .thenReturn(getNgdFileInputStream("emptyCsv.zip"))

        whenever(mockOsDownloadsClient.getDataPackageVersionDetails(DATA_PACKAGE_ID, INITIAL_VERSION_ID))
            .thenReturn(initialVersionDetails)
        whenever(mockOsDownloadsClient.getDataPackageVersionFile(DATA_PACKAGE_ID, SECOND_VERSION_ID, "$DATA_PACKAGE_FILE_NAME.zip"))
            .thenReturn(getNgdFileInputStream("invalidChangeTypeCsv.zip"))

        // Act & Assert
        assertThrows<Exception> { ngdAddressLoader.loadNewDataPackageVersions() }
        verify(mockTransaction, times(3)).commit() // marker + csv for initial version, marker for second
        verify(mockTransaction).rollback() // csv for second version
        verify(mockSession, times(4)).beginTransaction() // marker + csv for each version
    }

    @ParameterizedTest(name = "due to {0}")
    @MethodSource("provideNoNextDataPackageVersionIdResponses")
    fun `loadNewDataPackageVersions loads no data when there's no next version`(osDownloadsClientResponse: String) {
        // Arrange
        setUpMockNgdAddressLoaderDao { mock ->
            whenever(mock.findCommentOnAddressTable()).thenReturn("$DATA_PACKAGE_VERSION_COMMENT_PREFIX$INITIAL_VERSION_ID")
        }
        whenever(mockOsDownloadsClient.getDataPackageVersionDetails(DATA_PACKAGE_ID, INITIAL_VERSION_ID))
            .thenReturn(osDownloadsClientResponse)

        // Act
        ngdAddressLoader.loadNewDataPackageVersions()

        // Assert
        verify(mockNgdAddressLoaderDao, never()).saveCommentOnAddressTable(any())
    }

    @Test
    fun `loadNewDataPackageVersions updates property ownership addresses in batches during data package loads`() {
        // Arrange
        setUpMockNgdAddressLoaderDao { mock ->
            whenever(mock.findCommentOnAddressTable()).thenReturn("$DATA_PACKAGE_VERSION_COMMENT_PREFIX$SECOND_VERSION_ID")
        }

        whenever(mockOsDownloadsClient.getDataPackageVersionDetails(DATA_PACKAGE_ID, SECOND_VERSION_ID)).thenReturn(secondVersionDetails)
        whenever(mockOsDownloadsClient.getDataPackageVersionFile(DATA_PACKAGE_ID, THIRD_VERSION_ID, "$DATA_PACKAGE_FILE_NAME.zip"))
            .thenReturn(getNgdFileInputStream("veryLargeCsv.zip"))

        val localCouncils = listOf(MockLocalCouncilData.createLocalCouncil(custodianCode = "1"))
        whenever(mockLocalCouncilRepository.findAll()).thenReturn(localCouncils)

        whenever(mockOsDownloadsClient.getDataPackageVersionDetails(DATA_PACKAGE_ID, THIRD_VERSION_ID)).thenReturn(thirdVersionDetails)

        // Act
        ngdAddressLoader.loadNewDataPackageVersions()

        // Assert
        val expectedBatchCount = ceil(VERY_LARGE_CSV_LINE_COUNT / UPRN_BATCH_SIZE).toInt()
        verify(mockNgdAddressLoaderDao, times(expectedBatchCount)).updatePropertyOwnershipAddresses(any())
    }

    @Test
    fun `loadNewDataPackageVersions deletes unused inactive addresses after all data packages have been loaded`() {
        // Arrange
        setUpMockNgdAddressLoaderDao { mock ->
            whenever(mock.findCommentOnAddressTable()).thenReturn("$DATA_PACKAGE_VERSION_COMMENT_PREFIX$SECOND_VERSION_ID")
        }
        whenever(mockOsDownloadsClient.getDataPackageVersionDetails(DATA_PACKAGE_ID, SECOND_VERSION_ID))
            .thenReturn(secondVersionDetails)
        whenever(mockOsDownloadsClient.getDataPackageVersionFile(DATA_PACKAGE_ID, THIRD_VERSION_ID, "$DATA_PACKAGE_FILE_NAME.zip"))
            .thenReturn(getNgdFileInputStream("emptyCsv.zip"))
        whenever(mockOsDownloadsClient.getDataPackageVersionDetails(DATA_PACKAGE_ID, THIRD_VERSION_ID)).thenReturn(thirdVersionDetails)

        // Act
        ngdAddressLoader.loadNewDataPackageVersions()

        // Assert
        verify(mockNgdAddressLoaderDao).deleteUnusedInactiveAddresses()
    }

    @Test
    fun `loadNewDataPackageVersions throws exception when a data package version's ZIP file is missing`() {
        // Arrange
        setUpMockNgdAddressLoaderDao { mock ->
            whenever(mock.findCommentOnAddressTable()).thenReturn("$DATA_PACKAGE_VERSION_COMMENT_PREFIX$INITIAL_VERSION_ID")
        }
        whenever(mockOsDownloadsClient.getDataPackageVersionDetails(DATA_PACKAGE_ID, INITIAL_VERSION_ID))
            .thenReturn(initialVersionDetails)
        whenever(mockOsDownloadsClient.getDataPackageVersionFile(DATA_PACKAGE_ID, SECOND_VERSION_ID, "$DATA_PACKAGE_FILE_NAME.zip"))
            .doAnswer { throw HttpException() }

        // Act & Assert
        assertThrows<HttpException> { ngdAddressLoader.loadNewDataPackageVersions() }
    }

    @Test
    fun `loadNewDataPackageVersions throws exception when a data package version's CSV file is missing`() {
        // Arrange
        setUpMockNgdAddressLoaderDao { mock ->
            whenever(mock.findCommentOnAddressTable()).thenReturn("$DATA_PACKAGE_VERSION_COMMENT_PREFIX$INITIAL_VERSION_ID")
        }
        whenever(mockOsDownloadsClient.getDataPackageVersionDetails(DATA_PACKAGE_ID, INITIAL_VERSION_ID))
            .thenReturn(initialVersionDetails)
        whenever(mockOsDownloadsClient.getDataPackageVersionFile(DATA_PACKAGE_ID, SECOND_VERSION_ID, "$DATA_PACKAGE_FILE_NAME.zip"))
            .thenAnswer { getNgdFileInputStream("missingCsv.zip") }

        // Act & Assert
        assertThrows<ZipException> { ngdAddressLoader.loadNewDataPackageVersions() }
    }

    @Test
    fun `loadNewDataPackageVersions handles (deactivates, upserts or ignores) each record in a data package version accordingly`() {
        // Arrange
        setUpMockNgdAddressLoaderDao { mock ->
            whenever(mock.findCommentOnAddressTable()).thenReturn("$DATA_PACKAGE_VERSION_COMMENT_PREFIX$SECOND_VERSION_ID")
        }

        whenever(mockOsDownloadsClient.getDataPackageVersionDetails(DATA_PACKAGE_ID, SECOND_VERSION_ID)).thenReturn(secondVersionDetails)
        whenever(mockOsDownloadsClient.getDataPackageVersionFile(DATA_PACKAGE_ID, THIRD_VERSION_ID, "$DATA_PACKAGE_FILE_NAME.zip"))
            .thenReturn(getNgdFileInputStream("validCsv.zip"))

        val localCouncils = listOf(MockLocalCouncilData.createLocalCouncil(custodianCode = "1"))
        whenever(mockLocalCouncilRepository.findAll()).thenReturn(localCouncils)

        whenever(mockOsDownloadsClient.getDataPackageVersionDetails(DATA_PACKAGE_ID, THIRD_VERSION_ID)).thenReturn(thirdVersionDetails)

        // Act
        ngdAddressLoader.loadNewDataPackageVersions()

        // Assert
        val uprnCaptor = argumentCaptor<Long>()
        verify(mockPreparedStatement, times(3)).setLong(eq(1), uprnCaptor.capture())

        val localCouncilIndex = 11

        val isActiveCaptor = argumentCaptor<Boolean>()
        verify(mockPreparedStatement, times(3)).setBoolean(eq(12), isActiveCaptor.capture())

        // 'Delete' change type and OS custodian code - deactivate
        assertEquals(10000490106, uprnCaptor.firstValue)
        verify(mockPreparedStatement, times(2)).setNull(localCouncilIndex, java.sql.Types.INTEGER)
        assertFalse(isActiveCaptor.firstValue)

        // 'Upsert' change type and invalid country - deactivate
        assertEquals(10000067954, uprnCaptor.secondValue)
        verify(mockPreparedStatement, times(2)).setNull(localCouncilIndex, java.sql.Types.INTEGER)
        assertFalse(isActiveCaptor.secondValue)

        // 'Upsert' change type - upsert
        assertEquals(10000071648, uprnCaptor.thirdValue)
        verify(mockPreparedStatement).setInt(localCouncilIndex, localCouncils.first().id)
        assertTrue(isActiveCaptor.thirdValue)

        // 'No' change type - ignore (fourth record isn't processed)
        verify(mockPreparedStatement, times(3)).addBatch()

        verify(mockNgdAddressLoaderDao).saveCommentOnAddressTable("$DATA_PACKAGE_VERSION_COMMENT_PREFIX$THIRD_VERSION_ID")
    }

    @Test
    fun `loadNewDataPackageVersions handles data package version records in batches`() {
        // Arrange
        setUpMockNgdAddressLoaderDao { mock ->
            whenever(mock.findCommentOnAddressTable()).thenReturn("$DATA_PACKAGE_VERSION_COMMENT_PREFIX$SECOND_VERSION_ID")
        }

        whenever(mockOsDownloadsClient.getDataPackageVersionDetails(DATA_PACKAGE_ID, SECOND_VERSION_ID)).thenReturn(secondVersionDetails)
        whenever(mockOsDownloadsClient.getDataPackageVersionFile(DATA_PACKAGE_ID, THIRD_VERSION_ID, "$DATA_PACKAGE_FILE_NAME.zip"))
            .thenReturn(getNgdFileInputStream("largeCsv.zip"))

        val localCouncils = listOf(MockLocalCouncilData.createLocalCouncil(custodianCode = "1"))
        whenever(mockLocalCouncilRepository.findAll()).thenReturn(localCouncils)

        whenever(mockOsDownloadsClient.getDataPackageVersionDetails(DATA_PACKAGE_ID, THIRD_VERSION_ID)).thenReturn(thirdVersionDetails)

        // Act
        ngdAddressLoader.loadNewDataPackageVersions()

        // Assert
        val expectedBatchCount = ceil(LARGE_CSV_LINE_COUNT / BATCH_SIZE).toInt()
        verify(mockPreparedStatement, times(expectedBatchCount)).executeBatch()
    }

    @Test
    fun `loadNewDataPackageVersions commits transaction at each UPRN_BATCH_SIZE boundary`() {
        // Arrange
        setUpMockNgdAddressLoaderDao { mock ->
            whenever(mock.findCommentOnAddressTable()).thenReturn("$DATA_PACKAGE_VERSION_COMMENT_PREFIX$SECOND_VERSION_ID")
        }

        whenever(mockOsDownloadsClient.getDataPackageVersionDetails(DATA_PACKAGE_ID, SECOND_VERSION_ID)).thenReturn(secondVersionDetails)
        whenever(mockOsDownloadsClient.getDataPackageVersionFile(DATA_PACKAGE_ID, THIRD_VERSION_ID, "$DATA_PACKAGE_FILE_NAME.zip"))
            .thenReturn(getNgdFileInputStream("veryLargeCsv.zip"))

        val localCouncils = listOf(MockLocalCouncilData.createLocalCouncil(custodianCode = "1"))
        whenever(mockLocalCouncilRepository.findAll()).thenReturn(localCouncils)

        whenever(mockOsDownloadsClient.getDataPackageVersionDetails(DATA_PACKAGE_ID, THIRD_VERSION_ID)).thenReturn(thirdVersionDetails)

        // Act
        ngdAddressLoader.loadNewDataPackageVersions()

        // Assert
        val expectedUprnBatchCommits = (VERY_LARGE_CSV_LINE_COUNT / UPRN_BATCH_SIZE).toInt()
        val markerCommit = 1
        val finalCommit = 1
        val deleteInactiveCommit = 1
        verify(mockTransaction, times(markerCommit + expectedUprnBatchCommits + finalCommit + deleteInactiveCommit)).commit()
    }

    @Test
    fun `loadNewDataPackageVersions saves complete progress after loading CSV records`() {
        // Arrange
        setUpMockNgdAddressLoaderDao { mock ->
            whenever(mock.findCommentOnAddressTable()).thenReturn("$DATA_PACKAGE_VERSION_COMMENT_PREFIX$SECOND_VERSION_ID")
        }

        whenever(mockOsDownloadsClient.getDataPackageVersionDetails(DATA_PACKAGE_ID, SECOND_VERSION_ID)).thenReturn(secondVersionDetails)
        whenever(mockOsDownloadsClient.getDataPackageVersionFile(DATA_PACKAGE_ID, THIRD_VERSION_ID, "$DATA_PACKAGE_FILE_NAME.zip"))
            .thenReturn(getNgdFileInputStream("emptyCsv.zip"))
        whenever(mockOsDownloadsClient.getDataPackageVersionDetails(DATA_PACKAGE_ID, THIRD_VERSION_ID)).thenReturn(thirdVersionDetails)

        // Act
        ngdAddressLoader.loadNewDataPackageVersions()

        // Assert
        val commentCaptor = argumentCaptor<String>()
        verify(mockNgdAddressLoaderDao, atLeast(1)).saveCommentOnAddressTable(commentCaptor.capture())
        assertEquals("$DATA_PACKAGE_VERSION_COMMENT_PREFIX$THIRD_VERSION_ID", commentCaptor.lastValue)
    }

    @Test
    fun `loadNewDataPackageVersions throws exception when an unknown change type is encountered`() {
        // Arrange
        setUpMockNgdAddressLoaderDao { mock ->
            whenever(mock.findCommentOnAddressTable()).thenReturn("$DATA_PACKAGE_VERSION_COMMENT_PREFIX$INITIAL_VERSION_ID")
        }
        whenever(mockOsDownloadsClient.getDataPackageVersionDetails(DATA_PACKAGE_ID, INITIAL_VERSION_ID))
            .thenReturn(initialVersionDetails)
        whenever(mockOsDownloadsClient.getDataPackageVersionFile(DATA_PACKAGE_ID, SECOND_VERSION_ID, "$DATA_PACKAGE_FILE_NAME.zip"))
            .thenReturn(getNgdFileInputStream("invalidChangeTypeCsv.zip"))

        // Act & Assert
        assertThrows<IllegalArgumentException> { ngdAddressLoader.loadNewDataPackageVersions() }
    }

    // TODO PRSD-1643: Enable (and refactor if needed)
    @Disabled
    @Test
    fun `loadNewDataPackageVersions throws exception when an unknown custodian code is encountered`() {
        // Arrange
        setUpMockNgdAddressLoaderDao { mock ->
            whenever(mock.findCommentOnAddressTable()).thenReturn("$DATA_PACKAGE_VERSION_COMMENT_PREFIX$INITIAL_VERSION_ID")
        }

        whenever(mockOsDownloadsClient.getDataPackageVersionDetails(DATA_PACKAGE_ID, INITIAL_VERSION_ID))
            .thenReturn(initialVersionDetails)
        whenever(mockOsDownloadsClient.getDataPackageVersionFile(DATA_PACKAGE_ID, SECOND_VERSION_ID, "$DATA_PACKAGE_FILE_NAME.zip"))
            .thenReturn(getNgdFileInputStream("validCsv.zip"))

        whenever(mockLocalCouncilRepository.findAll()).thenReturn(emptyList())

        // Act & Assert
        assertThrows<EntityNotFoundException> { ngdAddressLoader.loadNewDataPackageVersions() }
    }

    @Test
    fun `loadNewDataPackageVersions propagates IOException after max retries exhausted`() {
        // Arrange
        setUpMockNgdAddressLoaderDao { mock ->
            whenever(mock.findCommentOnAddressTable()).thenReturn("$DATA_PACKAGE_VERSION_COMMENT_PREFIX$SECOND_VERSION_ID")
        }

        whenever(mockOsDownloadsClient.getDataPackageVersionDetails(DATA_PACKAGE_ID, SECOND_VERSION_ID)).thenReturn(secondVersionDetails)
        whenever(mockOsDownloadsClient.getDataPackageVersionFile(DATA_PACKAGE_ID, THIRD_VERSION_ID, "$DATA_PACKAGE_FILE_NAME.zip"))
            .thenAnswer { throw java.io.IOException("Stream closed") }

        // Act & Assert
        assertThrows<java.io.IOException> { ngdAddressLoader.loadNewDataPackageVersions() }

        // Verify it tried MAX_STREAM_RETRIES + 1 times (initial + retries)
        verify(mockOsDownloadsClient, times(NgdAddressLoader.MAX_STREAM_RETRIES + 1))
            .getDataPackageVersionFile(DATA_PACKAGE_ID, THIRD_VERSION_ID, "$DATA_PACKAGE_FILE_NAME.zip")
    }

    @Test
    fun `loadNewDataPackageVersions retries on IllegalStateException wrapping IOException`() {
        // Arrange - simulates CSVParser wrapping IOException in IllegalStateException
        setUpMockNgdAddressLoaderDao { mock ->
            whenever(mock.findCommentOnAddressTable()).thenReturn("$DATA_PACKAGE_VERSION_COMMENT_PREFIX$SECOND_VERSION_ID")
        }

        whenever(mockOsDownloadsClient.getDataPackageVersionDetails(DATA_PACKAGE_ID, SECOND_VERSION_ID)).thenReturn(secondVersionDetails)
        whenever(mockOsDownloadsClient.getDataPackageVersionFile(DATA_PACKAGE_ID, THIRD_VERSION_ID, "$DATA_PACKAGE_FILE_NAME.zip"))
            .thenAnswer {
                throw IllegalStateException(
                    "IOException reading next record: java.io.IOException: closed",
                    java.io.IOException("closed"),
                )
            }

        // Act & Assert
        val thrown = assertThrows<IllegalStateException> { ngdAddressLoader.loadNewDataPackageVersions() }
        assertTrue(thrown.cause is java.io.IOException)

        verify(mockOsDownloadsClient, times(NgdAddressLoader.MAX_STREAM_RETRIES + 1))
            .getDataPackageVersionFile(DATA_PACKAGE_ID, THIRD_VERSION_ID, "$DATA_PACKAGE_FILE_NAME.zip")
    }

    @Test
    fun `loadNewDataPackageVersions does not retry on non-IOException`() {
        // Arrange
        setUpMockNgdAddressLoaderDao { mock ->
            whenever(mock.findCommentOnAddressTable()).thenReturn("$DATA_PACKAGE_VERSION_COMMENT_PREFIX$INITIAL_VERSION_ID")
        }
        whenever(mockOsDownloadsClient.getDataPackageVersionDetails(DATA_PACKAGE_ID, INITIAL_VERSION_ID))
            .thenReturn(initialVersionDetails)
        whenever(mockOsDownloadsClient.getDataPackageVersionFile(DATA_PACKAGE_ID, SECOND_VERSION_ID, "$DATA_PACKAGE_FILE_NAME.zip"))
            .thenReturn(getNgdFileInputStream("invalidChangeTypeCsv.zip"))

        // Act & Assert
        assertThrows<IllegalArgumentException> { ngdAddressLoader.loadNewDataPackageVersions() }

        // Only called once — no retry
        verify(mockOsDownloadsClient, times(1))
            .getDataPackageVersionFile(DATA_PACKAGE_ID, SECOND_VERSION_ID, "$DATA_PACKAGE_FILE_NAME.zip")
    }

    @Test
    fun `loadNewDataPackageVersions resumes in-progress version from stored row offset`() {
        // Arrange
        setUpMockNgdAddressLoaderDao { mock ->
            whenever(mock.findCommentOnAddressTable())
                .thenReturn("$DATA_PACKAGE_VERSION_COMMENT_PREFIX$THIRD_VERSION_ID;rowOffset=5000")
        }

        whenever(mockOsDownloadsClient.getDataPackageVersionFile(DATA_PACKAGE_ID, THIRD_VERSION_ID, "$DATA_PACKAGE_FILE_NAME.zip"))
            .thenReturn(getNgdFileInputStream("emptyCsv.zip"))

        whenever(mockOsDownloadsClient.getDataPackageVersionDetails(DATA_PACKAGE_ID, THIRD_VERSION_ID)).thenReturn(thirdVersionDetails)

        // Act
        ngdAddressLoader.loadNewDataPackageVersions()

        // Assert - it loaded the in-progress version (not the next one)
        verify(mockOsDownloadsClient).getDataPackageVersionFile(DATA_PACKAGE_ID, THIRD_VERSION_ID, "$DATA_PACKAGE_FILE_NAME.zip")
        verify(mockNgdAddressLoaderDao, atLeast(1)).saveCommentOnAddressTable("$DATA_PACKAGE_VERSION_COMMENT_PREFIX$THIRD_VERSION_ID")
    }

    @Test
    fun `loadNewDataPackageVersions saves in-progress marker before loading CSV records`() {
        // Arrange
        setUpMockNgdAddressLoaderDao { mock ->
            whenever(mock.findCommentOnAddressTable()).thenReturn("$DATA_PACKAGE_VERSION_COMMENT_PREFIX$SECOND_VERSION_ID")
        }
        whenever(mockOsDownloadsClient.getDataPackageVersionDetails(DATA_PACKAGE_ID, SECOND_VERSION_ID)).thenReturn(secondVersionDetails)
        whenever(mockOsDownloadsClient.getDataPackageVersionFile(DATA_PACKAGE_ID, THIRD_VERSION_ID, "$DATA_PACKAGE_FILE_NAME.zip"))
            .thenReturn(getNgdFileInputStream("emptyCsv.zip"))
        whenever(mockOsDownloadsClient.getDataPackageVersionDetails(DATA_PACKAGE_ID, THIRD_VERSION_ID)).thenReturn(thirdVersionDetails)

        // Act
        ngdAddressLoader.loadNewDataPackageVersions()

        // Assert - first save should be in-progress, last should be complete
        val commentCaptor = argumentCaptor<String>()
        verify(mockNgdAddressLoaderDao, atLeast(2)).saveCommentOnAddressTable(commentCaptor.capture())
        assertEquals("$DATA_PACKAGE_VERSION_COMMENT_PREFIX$THIRD_VERSION_ID;rowOffset=0", commentCaptor.firstValue)
        assertEquals("$DATA_PACKAGE_VERSION_COMMENT_PREFIX$THIRD_VERSION_ID", commentCaptor.lastValue)
    }

    class DataPackageProgressTests {
        @Test
        fun `parseDataPackageProgress parses version-only comment`() {
            val comment = "dataPackageVersionId=110012"
            val progress = NgdAddressLoader.parseDataPackageProgress(comment)
            assertEquals("110012", progress!!.dataPackageVersionId)
            assertEquals(0, progress.rowOffset)
            assertTrue(progress.isComplete)
        }

        @Test
        fun `parseDataPackageProgress parses comment with row offset`() {
            val comment = "dataPackageVersionId=110012;rowOffset=340000"
            val progress = NgdAddressLoader.parseDataPackageProgress(comment)
            assertEquals("110012", progress!!.dataPackageVersionId)
            assertEquals(340000, progress.rowOffset)
            assertFalse(progress.isComplete)
        }

        @Test
        fun `parseDataPackageProgress returns null for null comment`() {
            assertNull(NgdAddressLoader.parseDataPackageProgress(null))
        }

        @Test
        fun `parseDataPackageProgress returns null for empty prefix`() {
            assertNull(NgdAddressLoader.parseDataPackageProgress("dataPackageVersionId="))
        }

        @Test
        fun `formatProgressComment formats version-only when complete`() {
            val progress = DataPackageProgress("110012", 0, isComplete = true)
            assertEquals("dataPackageVersionId=110012", NgdAddressLoader.formatProgressComment(progress))
        }

        @Test
        fun `formatProgressComment includes row offset when in-progress`() {
            val progress = DataPackageProgress("110012", 340000, isComplete = false)
            assertEquals("dataPackageVersionId=110012;rowOffset=340000", NgdAddressLoader.formatProgressComment(progress))
        }

        @Test
        fun `isRetryableStreamException returns true for IOException`() {
            assertTrue(NgdAddressLoader.isRetryableStreamException(java.io.IOException("closed")))
        }

        @Test
        fun `isRetryableStreamException returns true for IllegalStateException wrapping IOException`() {
            val exception = IllegalStateException("IOException reading next record", java.io.IOException("closed"))
            assertTrue(NgdAddressLoader.isRetryableStreamException(exception))
        }

        @Test
        fun `isRetryableStreamException returns false for IllegalStateException without IOException cause`() {
            assertFalse(NgdAddressLoader.isRetryableStreamException(IllegalStateException("other error")))
        }

        @Test
        fun `isRetryableStreamException returns false for other exceptions`() {
            assertFalse(NgdAddressLoader.isRetryableStreamException(IllegalArgumentException("bad input")))
        }
    }

    companion object {
        private const val INITIAL_VERSION_ID = "110011"
        private const val SECOND_VERSION_ID = "110012"
        private const val THIRD_VERSION_ID = "110013"

        private val versionHistory =
            """
            [
              {
                "id": "$SECOND_VERSION_ID",
                "url": "https://api.os.uk/downloads/v1/dataPackages/0040046952/versions/$SECOND_VERSION_ID",
                "createdOn": "2021-04-01",
                "reason": "UPDATE",
                "supplyType": "FULL",
                "productVersion": "E37 December 2015 Update",
                "format": "CSV",
                "dataPackageUrl": "https://api.os.uk/downloads/v1/dataPackages/0040046952"
              },
              {
                "id": "$INITIAL_VERSION_ID",
                "url": "https://api.os.uk/downloads/v1/dataPackages/0040046952/versions/$INITIAL_VERSION_ID",
                "createdOn": "2021-04-01",
                "reason": "INITIAL",
                "supplyType": "FULL",
                "productVersion": "E37 December 2015 Update",
                "format": "CSV",
                "dataPackageUrl": "https://api.os.uk/downloads/v1/dataPackages/0040046952"
              }
            ]
            """.trimIndent()

        private val versionHistoryWithoutInitial =
            """
            [
              {
                "id": "$SECOND_VERSION_ID",
                "url": "https://api.os.uk/downloads/v1/dataPackages/0040046952/versions/$SECOND_VERSION_ID",
                "createdOn": "2021-04-01",
                "reason": "UPDATE",
                "supplyType": "FULL",
                "productVersion": "E37 December 2015 Update",
                "format": "CSV",
                "dataPackageUrl": "https://api.os.uk/downloads/v1/dataPackages/0040046952"
              }
            ]
            """.trimIndent()

        private val versionHistoryWithoutId =
            """
            [
              {
                "url": "https://api.os.uk/downloads/v1/dataPackages/0040046952/versions/$INITIAL_VERSION_ID",
                "createdOn": "2021-04-01",
                "reason": "INITIAL",
                "supplyType": "FULL",
                "productVersion": "E37 December 2015 Update",
                "format": "CSV",
                "dataPackageUrl": "https://api.os.uk/downloads/v1/dataPackages/0040046952"
              }
            ]
            """.trimIndent()

        private val initialVersionDetails =
            """
            {
              "id": "$INITIAL_VERSION_ID",
              "url": "https://api.os.uk/downloads/v1/dataPackages/0040046952/versions/$INITIAL_VERSION_ID",
              "createdOn": "2021-04-01",
              "reason": "INITIAL",
              "supplyType": "FULL",
              "productVersion": "E37 December 2015 Update",
              "format": "CSV",
              "dataPackageUrl": "https://api.os.uk/downloads/v1/dataPackages/0040046952",
              "nextVersionUrl": "https://api.os.uk/downloads/v1/dataPackages/0040046952/versions/$SECOND_VERSION_ID",
              "downloads": [
                {
                  "url": "https://example.com",
                  "fileName": "data.zip",
                  "size": 1234,
                  "md5": "2f9dd13abd56140afa3b5621e8864f59"
                }
              ]
            }
            """.trimIndent()

        private val initialVersionDetailsWithoutNext =
            """
            {
              "id": "$INITIAL_VERSION_ID",
              "url": "https://api.os.uk/downloads/v1/dataPackages/0040046952/versions/$INITIAL_VERSION_ID",
              "createdOn": "2021-04-01",
              "reason": "INITIAL",
              "supplyType": "FULL",
              "productVersion": "E37 December 2015 Update",
              "format": "CSV",
              "dataPackageUrl": "https://api.os.uk/downloads/v1/dataPackages/0040046952",
              "downloads": [
                {
                  "url": "https://example.com",
                  "fileName": "data.zip",
                  "size": 1234,
                  "md5": "2f9dd13abd56140afa3b5621e8864f59"
                }
              ]
            }
            """.trimIndent()

        private val initialVersionDetailsWithInvalidNext =
            """
            {
              "id": "$INITIAL_VERSION_ID",
              "url": "https://api.os.uk/downloads/v1/dataPackages/0040046952/versions/$INITIAL_VERSION_ID",
              "createdOn": "2021-04-01",
              "reason": "INITIAL",
              "supplyType": "FULL",
              "productVersion": "E37 December 2015 Update",
              "format": "CSV",
              "dataPackageUrl": "https://api.os.uk/downloads/v1/dataPackages/0040046952",
              "nextVersionUrl": "invalid-url",
              "downloads": [
                {
                  "url": "https://example.com",
                  "fileName": "data.zip",
                  "size": 1234,
                  "md5": "2f9dd13abd56140afa3b5621e8864f59"
                }
              ]
            }
            """.trimIndent()

        private val secondVersionDetails =
            """
            {
              "id": "$SECOND_VERSION_ID",
              "url": "https://api.os.uk/downloads/v1/dataPackages/0040046952/versions/$SECOND_VERSION_ID",
              "createdOn": "2021-04-01",
              "reason": "UPDATE",
              "supplyType": "FULL",
              "productVersion": "E37 December 2015 Update",
              "format": "CSV",
              "dataPackageUrl": "https://api.os.uk/downloads/v1/dataPackages/0040046952",
              "previousVersionUrl": "https://api.os.uk/downloads/v1/dataPackages/0040046952/versions/$INITIAL_VERSION_ID",
              "nextVersionUrl": "https://api.os.uk/downloads/v1/dataPackages/0040046952/versions/$THIRD_VERSION_ID",
              "downloads": [
                {
                  "url": "https://example.com",
                  "fileName": "data.zip",
                  "size": 1234,
                  "md5": "2f9dd13abd56140afa3b5621e8864f59"
                }
              ]
            }
            """.trimIndent()

        private val thirdVersionDetails =
            """
            {
              "id": "$THIRD_VERSION_ID",
              "url": "https://api.os.uk/downloads/v1/dataPackages/0040046952/versions/$THIRD_VERSION_ID",
              "createdOn": "2021-04-01",
              "reason": "UPDATE",
              "supplyType": "FULL",
              "productVersion": "E37 December 2015 Update",
              "format": "CSV",
              "dataPackageUrl": "https://api.os.uk/downloads/v1/dataPackages/0040046952",
              "previousVersionUrl": "https://api.os.uk/downloads/v1/dataPackages/0040046952/versions/$SECOND_VERSION_ID",
              "downloads": [
                {
                  "url": "https://example.com",
                  "fileName": "data.zip",
                  "size": 1234,
                  "md5": "2f9dd13abd56140afa3b5621e8864f59"
                }
              ]
            }
            """.trimIndent()

        private const val LARGE_CSV_LINE_COUNT = 10001f
        private const val VERY_LARGE_CSV_LINE_COUNT = 25001f

        fun getNgdFileInputStream(fileName: String) = FileInputStream(ResourceUtils.getFile("classpath:data/ngd/$fileName"))

        @JvmStatic
        fun provideNoDataPackageVersionIdComments() =
            listOf(
                named("there is no comment on the Address table", null),
                named("the comment on the Address table doesn't contain an ID", DATA_PACKAGE_VERSION_COMMENT_PREFIX),
            )

        @JvmStatic
        fun provideNoInitialDataPackageVersionIdResponses() =
            listOf(
                named("an OSDownloadsClient error") { throw HttpException() },
                named("the data not containing the initial version") { versionHistoryWithoutInitial },
                named("the initial version not having an id") { versionHistoryWithoutId },
            )

        @JvmStatic
        fun provideNoNextDataPackageVersionIdResponses() =
            listOf(
                named("the data not containing a next version URL", initialVersionDetailsWithoutNext),
                named("the next version URL being malformed", initialVersionDetailsWithInvalidNext),
            )
    }
}
