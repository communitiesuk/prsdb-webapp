package uk.gov.communities.prsdb.webapp.services

import kotlinx.datetime.LocalDate
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.clients.EpcRegisterClient
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel

@ExtendWith(MockitoExtension::class)
class EpcLookupServiceTests {
    @Mock
    private lateinit var mockEpcRegisterClient: EpcRegisterClient

    @InjectMocks
    private lateinit var epcLookupService: EpcLookupService

    @Test
    fun `getEpcByCertificateNumber throws BAD_REQUEST error if the certificate number is the wrong format`() {
        val invalidCertificateNumber = "12345678"

        val exception =
            assertThrows(ResponseStatusException::class.java) {
                epcLookupService.getEpcByCertificateNumber(invalidCertificateNumber)
            }

        assertEquals("400 BAD_REQUEST \"Certificate number should be of the form XXXX-XXXX-XXXX-XXXX-XXXX\"", exception.message)
    }

    @Test
    fun `getEpcByCertificateNumber returns EpcDataModel when valid response`() {
        // Arrange
        val certificateNumber = "1234-5678-9012-3456-7890"
        val mockResponse =
            """
            {
                "data": {
                    "epcRrn": "$certificateNumber",
                    "currentEnergyEfficiencyBand": "C",
                    "expiryDate": "2027-01-05T00:00:00.000Z",
                    "latestEpcRrnForAddress": "$certificateNumber",
                    "address": {
                        "addressLine1": "123 Test Street",
                        "town": "Test Town",
                        "postcode": "TT1 1TT",
                        "addressLine2": "Flat 1"
                    }
                }
            }
            """.trimIndent()
        val expectedDataModel =
            EpcDataModel(
                certificateNumber = certificateNumber,
                singleLineAddress = "123 Test Street, Flat 1, Test Town, TT1 1TT",
                energyRating = "C",
                expiryDate = LocalDate(2027, 1, 5),
                latestCertificateNumberForThisProperty = certificateNumber,
            )

        whenever(mockEpcRegisterClient.getByRrn(certificateNumber)).thenReturn(mockResponse)

        // Act
        val result = epcLookupService.getEpcByCertificateNumber(certificateNumber)

        assertEquals(expectedDataModel, result)
    }

    @Test
    fun `getEpcByCertificateNumber returns null when NOT_FOUND error is returned by the client`() {
        val certificateNumber = "1234-5678-9012-3456-7890"
        val mockResponse =
            """
            {
                "errors": [
                    {
                        "code": "NOT_FOUND",
                        "title": "Certificate not found"
                    }
                ]
            }
            """.trimIndent()

        whenever(mockEpcRegisterClient.getByRrn(certificateNumber)).thenReturn(mockResponse)

        val result = epcLookupService.getEpcByCertificateNumber(certificateNumber)

        assertNull(result)
    }

    @Test
    fun `getEpcByCertificateNumber throws BAD_REQUEST error if this is returned by the client`() {
        val certificateNumber = "1234-5678-9012-3456-7890"
        val mockResponse =
            """
            {
                "errors": [
                    {
                        "code": "INVALID_REQUEST",
                        "title": "Invalid certificate number"
                    }
                ]
            }
            """.trimIndent()

        whenever(mockEpcRegisterClient.getByRrn(certificateNumber)).thenReturn(mockResponse)

        val exception =
            assertThrows(ResponseStatusException::class.java) {
                epcLookupService.getEpcByCertificateNumber(certificateNumber)
            }

        assertEquals("400 BAD_REQUEST \"Invalid certificate number\"", exception.message)
    }

    @Test
    fun `getErrorCode returns the error code from a jsonObject with exactly one error`() {
        val jsonObject =
            JSONObject(
                """
                {
                    "errors": [
                        {
                            "code": "INVALID_REQUEST",
                            "title": "Invalid certificate number"
                        }
                    ]
                }
                """.trimIndent(),
            )
        val errorCode = EpcLookupService.getErrorCode(jsonObject)
        assertEquals("INVALID_REQUEST", errorCode)
    }

    @Test
    fun `getErrorCode returns null if the jsonObject has a non-singular number of errors`() {
        val jsonObject =
            JSONObject(
                """
                {
                    "errors": []
                }
                """.trimIndent(),
            )
        val errorCode = EpcLookupService.getErrorCode(jsonObject)
        assertNull(errorCode)
    }

    @Test
    fun `getErrorMessage returns the error message from a jsonObject`() {
        val jsObject =
            JSONObject(
                """
                {
                    "errors": [
                        {
                            "code": "INVALID_REQUEST",
                            "title": "Invalid certificate number"
                        }
                    ]
                }
                """.trimIndent(),
            )
        val errorMessage = EpcLookupService.getErrorMessage(jsObject)
        assertEquals("Invalid certificate number", errorMessage)
    }

    @Test
    fun `getErrorMessage returns null if the jsonObject has a non-singular number of errors`() {
        val jsonObject =
            JSONObject(
                """
                {
                    "errors": []
                }
                """.trimIndent(),
            )
        val errorMessage = EpcLookupService.getErrorMessage(jsonObject)
        assertNull(errorMessage)
    }
}
