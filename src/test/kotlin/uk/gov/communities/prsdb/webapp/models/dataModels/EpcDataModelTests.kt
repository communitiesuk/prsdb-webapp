package uk.gov.communities.prsdb.webapp.models.dataModels

import kotlinx.datetime.LocalDate
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData.Companion.DEFAULT_EPC_CERTIFICATE_NUMBER
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData.Companion.SECONDARY_EPC_CERTIFICATE_NUMBER

class EpcDataModelTests {
    @Test
    fun `isLatestCertificateForThisProperty returns true if the certificateNumber matches the latestCertificateNumberForThisProperty`() {
        val epcDataModel =
            MockEpcData.createEpcDataModel(
                certificateNumber = DEFAULT_EPC_CERTIFICATE_NUMBER,
                latestCertificateNumberForThisProperty = DEFAULT_EPC_CERTIFICATE_NUMBER,
            )

        assertTrue(epcDataModel.isLatestCertificateForThisProperty())
    }

    @Test
    fun `isLatestCertificateForThisProperty returns true latestCertificateNumberForThisProperty is null`() {
        val epcDataModel =
            MockEpcData.createEpcDataModel(
                certificateNumber = DEFAULT_EPC_CERTIFICATE_NUMBER,
                latestCertificateNumberForThisProperty = null,
            )

        assertTrue(epcDataModel.isLatestCertificateForThisProperty())
    }

    @Test
    fun `isLatestCertificateForThisProperty returns false if the certificateNumber is not the latestCertificateNumberForThisProperty`() {
        val epcDataModel =
            MockEpcData.createEpcDataModel(
                certificateNumber = DEFAULT_EPC_CERTIFICATE_NUMBER,
                latestCertificateNumberForThisProperty = SECONDARY_EPC_CERTIFICATE_NUMBER,
            )

        assertFalse(epcDataModel.isLatestCertificateForThisProperty())
    }

    @Test
    fun `formatCertificateNumber formats the certificate number correctly if passed a valid number with no hyphens`() {
        val inputCertificateNumber = "00001111222233334444"
        val expectedFormattedCertificateNumber = "0000-1111-2222-3333-4444"

        assertEquals(
            expectedFormattedCertificateNumber,
            EpcDataModel.parseCertificateNumberOrNull(inputCertificateNumber),
        )
    }

    @Test
    fun `formatCertificateNumber formats the certificate number correctly if passed a valid number including hyphens`() {
        val inputCertificateNumber = "0000-1111-2222-3333-4444"

        assertEquals(
            inputCertificateNumber,
            EpcDataModel.parseCertificateNumberOrNull(inputCertificateNumber),
        )
    }

    @Test
    fun `formatCertificateNumber returns null if passed a certificate number with an invalid length`() {
        val invalidCertificateNumber = "123456"

        assertNull(EpcDataModel.parseCertificateNumberOrNull(invalidCertificateNumber))
    }

    @Test
    fun `formatCertificateNumber returns null if passed a certificate number containing invalid characters`() {
        val invalidCertificateNumber = "123456Invalid!"

        assertNull(EpcDataModel.parseCertificateNumberOrNull(invalidCertificateNumber))
    }

    @Test
    fun `fromJsonObject parses the jsonResponse and returns an EpcDataModel`() {
        val certificateNumber = "1234-5678-9012-3456-7890"
        val jsonString =
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

        val epcDataModel = EpcDataModel.fromJsonObject(JSONObject(jsonString))

        assertEquals(expectedDataModel, epcDataModel)
    }
}
