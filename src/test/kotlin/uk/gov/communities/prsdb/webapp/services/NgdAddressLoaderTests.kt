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
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.whenever
import org.springframework.core.env.Environment
import org.springframework.util.ResourceUtils
import uk.gov.communities.prsdb.webapp.clients.OsDownloadsClient
import uk.gov.communities.prsdb.webapp.database.repository.LocalCouncilRepository
import uk.gov.communities.prsdb.webapp.database.repository.NgdAddressLoaderRepository
import uk.gov.communities.prsdb.webapp.services.NgdAddressLoader.Companion.BATCH_SIZE
import uk.gov.communities.prsdb.webapp.services.NgdAddressLoader.Companion.DATA_PACKAGE_FILE_NAME
import uk.gov.communities.prsdb.webapp.services.NgdAddressLoader.Companion.DATA_PACKAGE_ID
import uk.gov.communities.prsdb.webapp.services.NgdAddressLoader.Companion.DATA_PACKAGE_VERSION_COMMENT_PREFIX
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalCouncilData
import java.io.FileInputStream
import java.sql.Connection
import java.sql.PreparedStatement
import java.util.zip.ZipException
import kotlin.math.ceil
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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

    private lateinit var ngdAddressLoaderRepositoryMockConstructor: MockedConstruction<NgdAddressLoaderRepository>

    private val mockNgdAddressLoaderRepository
        get() = ngdAddressLoaderRepositoryMockConstructor.constructed()[0]

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

    private fun setUpMockNgdAddressLoaderRepository(mockInitializer: (mock: NgdAddressLoaderRepository) -> Unit) {
        ngdAddressLoaderRepositoryMockConstructor =
            mockConstruction(NgdAddressLoaderRepository::class.java) { mock, _ ->
                whenever(mock.getLoadAddressPreparedStatement(any())).thenReturn(mockPreparedStatement)
                mockInitializer(mock)
            }
    }

    @AfterEach
    fun tearDown() {
        ngdAddressLoaderRepositoryMockConstructor.close()
    }

    @ParameterizedTest(name = "when {0}")
    @MethodSource("provideNoDataPackageVersionIdComments")
    fun `loadNewDataPackageVersions loads initial data package version when`(commentOnAddressTable: String?) {
        // Arrange
        setUpMockNgdAddressLoaderRepository { mock ->
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
        verify(mockNgdAddressLoaderRepository).saveCommentOnAddressTable("$DATA_PACKAGE_VERSION_COMMENT_PREFIX$INITIAL_VERSION_ID")
    }

    @ParameterizedTest(name = "due to {0}")
    @MethodSource("provideNoInitialDataPackageVersionIdResponses")
    fun `loadNewDataPackageVersions throws exception when no initial data package version is found`(
        osDownloadsClientResponse: () -> String,
    ) {
        // Arrange
        setUpMockNgdAddressLoaderRepository { mock ->
            whenever(mock.findCommentOnAddressTable()).thenReturn(DATA_PACKAGE_VERSION_COMMENT_PREFIX)
        }
        whenever(mockOsDownloadsClient.getDataPackageVersionHistory(DATA_PACKAGE_ID)).doAnswer { osDownloadsClientResponse() }

        // Act & Assert
        assertThrows<Exception> { ngdAddressLoader.loadNewDataPackageVersions() }
    }

    @Test
    fun `loadNewDataPackageVersions loads next data package versions when there's already one loaded`() {
        // Arrange
        setUpMockNgdAddressLoaderRepository { mock ->
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
        verify(mockNgdAddressLoaderRepository, times(2)).saveCommentOnAddressTable(commentCaptor.capture())
        assertEquals("$DATA_PACKAGE_VERSION_COMMENT_PREFIX$SECOND_VERSION_ID", commentCaptor.firstValue)
        assertEquals("$DATA_PACKAGE_VERSION_COMMENT_PREFIX$THIRD_VERSION_ID", commentCaptor.secondValue)
    }

    @Test
    fun `loadNewDataPackageVersions throws exception when information about next data package version can't be found`() {
        // Arrange
        setUpMockNgdAddressLoaderRepository { mock ->
            whenever(mock.findCommentOnAddressTable()).thenReturn("$DATA_PACKAGE_VERSION_COMMENT_PREFIX$INITIAL_VERSION_ID")
        }
        whenever(mockOsDownloadsClient.getDataPackageVersionDetails(DATA_PACKAGE_ID, INITIAL_VERSION_ID)).doAnswer { throw HttpException() }

        // Act & Assert
        assertThrows<HttpException> { ngdAddressLoader.loadNewDataPackageVersions() }
    }

    @Test
    fun `loadNewDataPackageVersions rolls-back current load and doesn't load next versions when error is thrown`() {
        // Arrange
        setUpMockNgdAddressLoaderRepository { mock ->
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
        verify(mockTransaction).commit() // initial version is commited
        verify(mockTransaction).rollback() // second version is rolled-back
        verify(mockSession, times(2)).beginTransaction() // third version isn't loaded
    }

    @ParameterizedTest(name = "due to {0}")
    @MethodSource("provideNoNextDataPackageVersionIdResponses")
    fun `loadNewDataPackageVersions loads no data when there's no next version`(osDownloadsClientResponse: String) {
        // Arrange
        setUpMockNgdAddressLoaderRepository { mock ->
            whenever(mock.findCommentOnAddressTable()).thenReturn("$DATA_PACKAGE_VERSION_COMMENT_PREFIX$INITIAL_VERSION_ID")
        }
        whenever(mockOsDownloadsClient.getDataPackageVersionDetails(DATA_PACKAGE_ID, INITIAL_VERSION_ID))
            .thenReturn(osDownloadsClientResponse)

        // Act
        ngdAddressLoader.loadNewDataPackageVersions()

        // Assert
        verify(mockNgdAddressLoaderRepository, never()).saveCommentOnAddressTable(any())
    }

    @Test
    fun `loadNewDataPackageVersions deletes unused inactive addresses after all data packages have been loaded`() {
        // Arrange
        setUpMockNgdAddressLoaderRepository { mock ->
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
        verify(mockNgdAddressLoaderRepository).deleteUnusedInactiveAddresses()
    }

    @Test
    fun `loadNewDataPackageVersions throws exception when a data package version's ZIP file is missing`() {
        // Arrange
        setUpMockNgdAddressLoaderRepository { mock ->
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
        setUpMockNgdAddressLoaderRepository { mock ->
            whenever(mock.findCommentOnAddressTable()).thenReturn("$DATA_PACKAGE_VERSION_COMMENT_PREFIX$INITIAL_VERSION_ID")
        }
        whenever(mockOsDownloadsClient.getDataPackageVersionDetails(DATA_PACKAGE_ID, INITIAL_VERSION_ID))
            .thenReturn(initialVersionDetails)
        whenever(mockOsDownloadsClient.getDataPackageVersionFile(DATA_PACKAGE_ID, SECOND_VERSION_ID, "$DATA_PACKAGE_FILE_NAME.zip"))
            .thenReturn(getNgdFileInputStream("missingCsv.zip"))

        // Act & Assert
        assertThrows<ZipException> { ngdAddressLoader.loadNewDataPackageVersions() }
    }

    @Test
    fun `loadNewDataPackageVersions handles (deactivates, upserts or ignores) each record in a data package version accordingly`() {
        // Arrange
        setUpMockNgdAddressLoaderRepository { mock ->
            whenever(mock.findCommentOnAddressTable()).thenReturn("$DATA_PACKAGE_VERSION_COMMENT_PREFIX$SECOND_VERSION_ID")
        }

        whenever(mockOsDownloadsClient.getDataPackageVersionDetails(DATA_PACKAGE_ID, SECOND_VERSION_ID)).thenReturn(secondVersionDetails)
        whenever(mockOsDownloadsClient.getDataPackageVersionFile(DATA_PACKAGE_ID, THIRD_VERSION_ID, "$DATA_PACKAGE_FILE_NAME.zip"))
            .thenReturn(getNgdFileInputStream("validCsv.zip"))

        val localAuthorities = listOf(MockLocalCouncilData.createLocalAuthority(custodianCode = "1"))
        whenever(mockLocalCouncilRepository.findAll()).thenReturn(localAuthorities)

        whenever(mockOsDownloadsClient.getDataPackageVersionDetails(DATA_PACKAGE_ID, THIRD_VERSION_ID)).thenReturn(thirdVersionDetails)

        // Act
        ngdAddressLoader.loadNewDataPackageVersions()

        // Assert
        val uprnCaptor = argumentCaptor<Long>()
        verify(mockPreparedStatement, times(3)).setLong(eq(1), uprnCaptor.capture())

        val localAuthorityIndex = 11

        val isActiveCaptor = argumentCaptor<Boolean>()
        verify(mockPreparedStatement, times(3)).setBoolean(eq(12), isActiveCaptor.capture())

        // 'Delete' change type and OS custodian code - deactivate
        assertEquals(10000490106, uprnCaptor.firstValue)
        verify(mockPreparedStatement, times(2)).setNull(localAuthorityIndex, java.sql.Types.INTEGER)
        assertFalse(isActiveCaptor.firstValue)

        // 'Upsert' change type and invalid country - deactivate
        assertEquals(10000067954, uprnCaptor.secondValue)
        verify(mockPreparedStatement, times(2)).setNull(localAuthorityIndex, java.sql.Types.INTEGER)
        assertFalse(isActiveCaptor.secondValue)

        // 'Upsert' change type - upsert
        assertEquals(10000071648, uprnCaptor.thirdValue)
        verify(mockPreparedStatement).setInt(localAuthorityIndex, localAuthorities.first().id)
        assertTrue(isActiveCaptor.thirdValue)

        // 'No' change type - ignore (fourth record isn't processed)
        verify(mockPreparedStatement, times(3)).addBatch()

        verify(mockNgdAddressLoaderRepository).saveCommentOnAddressTable("$DATA_PACKAGE_VERSION_COMMENT_PREFIX$THIRD_VERSION_ID")
    }

    @Test
    fun `loadNewDataPackageVersions handles data package version records in batches`() {
        // Arrange
        setUpMockNgdAddressLoaderRepository { mock ->
            whenever(mock.findCommentOnAddressTable()).thenReturn("$DATA_PACKAGE_VERSION_COMMENT_PREFIX$SECOND_VERSION_ID")
        }

        whenever(mockOsDownloadsClient.getDataPackageVersionDetails(DATA_PACKAGE_ID, SECOND_VERSION_ID)).thenReturn(secondVersionDetails)
        whenever(mockOsDownloadsClient.getDataPackageVersionFile(DATA_PACKAGE_ID, THIRD_VERSION_ID, "$DATA_PACKAGE_FILE_NAME.zip"))
            .thenReturn(getNgdFileInputStream("largeCsv.zip"))

        val localAuthorities = listOf(MockLocalCouncilData.createLocalAuthority(custodianCode = "1"))
        whenever(mockLocalCouncilRepository.findAll()).thenReturn(localAuthorities)

        whenever(mockOsDownloadsClient.getDataPackageVersionDetails(DATA_PACKAGE_ID, THIRD_VERSION_ID)).thenReturn(thirdVersionDetails)

        // Act
        ngdAddressLoader.loadNewDataPackageVersions()

        // Assert
        val largeCsvLineCount = 10001f
        val expectedBatchCount = ceil(largeCsvLineCount / BATCH_SIZE).toInt()
        verify(mockPreparedStatement, times(expectedBatchCount)).executeBatch()
    }

    @Test
    fun `loadNewDataPackageVersions throws exception when an unknown change type is encountered`() {
        // Arrange
        setUpMockNgdAddressLoaderRepository { mock ->
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
        setUpMockNgdAddressLoaderRepository { mock ->
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
