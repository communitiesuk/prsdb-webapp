package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityNotFoundException
import org.apache.http.HttpException
import org.hibernate.SessionFactory
import org.hibernate.StatelessSession
import org.hibernate.Transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Named.named
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockedConstruction
import org.mockito.Mockito.lenient
import org.mockito.Mockito.mockConstruction
import org.mockito.Mockito.verify
import org.mockito.internal.matchers.apachecommons.ReflectionEquals
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.whenever
import org.springframework.util.ResourceUtils
import uk.gov.communities.prsdb.webapp.clients.OsDownloadsClient
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityRepository
import uk.gov.communities.prsdb.webapp.database.repository.NgdAddressLoaderRepository
import uk.gov.communities.prsdb.webapp.services.NgdAddressLoader.Companion.DATA_PACKAGE_FILE_NAME
import uk.gov.communities.prsdb.webapp.services.NgdAddressLoader.Companion.DATA_PACKAGE_ID
import uk.gov.communities.prsdb.webapp.services.NgdAddressLoader.Companion.DATA_PACKAGE_VERSION_COMMENT_PREFIX
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.io.FileInputStream
import java.util.zip.ZipException
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class NgdAddressLoaderTests {
    @Mock
    private lateinit var mockSessionFactory: SessionFactory

    @Mock
    private lateinit var mockOsDownloadsClient: OsDownloadsClient

    @Mock
    private lateinit var mockLocalAuthorityRepository: LocalAuthorityRepository

    @InjectMocks
    private lateinit var ngdAddressLoader: NgdAddressLoader

    @Mock
    private lateinit var mockSession: StatelessSession

    @Mock
    private lateinit var mockTransaction: Transaction

    private lateinit var ngdAddressLoaderRepositoryMockConstructor: MockedConstruction<NgdAddressLoaderRepository>

    private val mockNgdAddressLoaderRepository
        get() = ngdAddressLoaderRepositoryMockConstructor.constructed()[0]

    @BeforeEach
    fun setUp() {
        whenever(mockSessionFactory.openStatelessSession()).thenReturn(mockSession)
        lenient().`when`(mockSession.beginTransaction()).thenReturn(mockTransaction)
    }

    private fun setUpMockNgdAddressLoaderRepository(mockInitializer: (mock: NgdAddressLoaderRepository) -> Unit) {
        ngdAddressLoaderRepositoryMockConstructor =
            mockConstruction(NgdAddressLoaderRepository::class.java) { mock, _ -> mockInitializer(mock) }
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
    fun `loadNewDataPackageVersions handles (deletes, upserts or ignores) each record in a data package version accordingly`() {
        // Arrange
        val deleteInUseUprn = 10000490106
        val deleteNotInUseInvalidCountryUprn = 10000067954
        val insertWelshUprn = 10000001714
        val updateAddress = MockLandlordData.createAddress(uprn = 10000071648, id = 1)

        setUpMockNgdAddressLoaderRepository { mock ->
            whenever(mock.findCommentOnAddressTable()).thenReturn("$DATA_PACKAGE_VERSION_COMMENT_PREFIX$SECOND_VERSION_ID")

            whenever(mock.findAddressReferencingTableAndColumnNames()).thenReturn(listOf("property" to "address_id"))
            whenever(mock.countReferencesToAddressInTableColumn(deleteInUseUprn, "property", "address_id")).thenReturn(1)
            whenever(mock.countReferencesToAddressInTableColumn(deleteNotInUseInvalidCountryUprn, "property", "address_id")).thenReturn(0)

            whenever(mock.findAddressId(updateAddress.uprn!!)).thenReturn(updateAddress.id)
            whenever(mock.findAddressId(insertWelshUprn)).thenReturn(null)
        }

        whenever(mockOsDownloadsClient.getDataPackageVersionDetails(DATA_PACKAGE_ID, SECOND_VERSION_ID)).thenReturn(secondVersionDetails)
        whenever(mockOsDownloadsClient.getDataPackageVersionFile(DATA_PACKAGE_ID, THIRD_VERSION_ID, "$DATA_PACKAGE_FILE_NAME.zip"))
            .thenReturn(getNgdFileInputStream("validCsv.zip"))

        whenever(mockLocalAuthorityRepository.findByCustodianCode("3")).thenReturn(updateAddress.localAuthority)

        whenever(mockOsDownloadsClient.getDataPackageVersionDetails(DATA_PACKAGE_ID, THIRD_VERSION_ID)).thenReturn(thirdVersionDetails)

        // Act
        ngdAddressLoader.loadNewDataPackageVersions()

        // Assert
        verify(mockNgdAddressLoaderRepository).deactivateAddress(deleteInUseUprn)
        verify(mockNgdAddressLoaderRepository).deleteAddress(deleteNotInUseInvalidCountryUprn)

        val expectedInsertAddress =
            Address(
                id = null,
                uprn = insertWelshUprn,
                singleLineAddress = "12, PARR ROAD, STANMORE, HA7 1NL",
                organisation = null,
                subBuilding = null,
                buildingName = null,
                buildingNumber = "12",
                streetName = "PARR ROAD",
                locality = null,
                townName = "STANMORE",
                postcode = "HA7 1NL",
                localAuthority = null,
            )
        val insertAddressCaptor = argumentCaptor<Address>()
        verify(mockSession).insert(insertAddressCaptor.capture())
        assertTrue(ReflectionEquals(expectedInsertAddress).matches(insertAddressCaptor.firstValue))

        val expectedUpdateAddress =
            Address(
                id = updateAddress.id,
                uprn = updateAddress.uprn!!,
                singleLineAddress = "FLAT B, CYCLISTS REST, LANGTON ROAD, LANGTON GREEN, TUNBRIDGE WELLS, TN3 0HL",
                organisation = "COMPLIANCE BUILDING CONTROL LTD",
                subBuilding = "FLAT B",
                buildingName = "CYCLISTS REST",
                buildingNumber = null,
                streetName = "LANGTON ROAD",
                locality = "LANGTON GREEN",
                townName = "TUNBRIDGE WELLS",
                postcode = "TN3 0HL",
                localAuthority = updateAddress.localAuthority,
            )
        val updateAddressCaptor = argumentCaptor<Address>()
        verify(mockSession).update(updateAddressCaptor.capture())
        assertTrue(ReflectionEquals(expectedUpdateAddress).matches(updateAddressCaptor.firstValue))

        verify(mockNgdAddressLoaderRepository).saveCommentOnAddressTable("$DATA_PACKAGE_VERSION_COMMENT_PREFIX$THIRD_VERSION_ID")
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

        whenever(mockLocalAuthorityRepository.findByCustodianCode(any())).thenReturn(null)

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
