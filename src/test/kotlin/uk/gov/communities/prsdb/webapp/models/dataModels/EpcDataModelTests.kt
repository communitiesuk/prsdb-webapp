package uk.gov.communities.prsdb.webapp.models.dataModels

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toLocalDateTime
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData.Companion.DEFAULT_EPC_CERTIFICATE_NUMBER
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData.Companion.SECONDARY_EPC_CERTIFICATE_NUMBER
import java.time.Clock

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
    fun `isPastExpiryDate returns true if the expiry date is in the past`() {
        val epcDataModel =
            MockEpcData.createEpcDataModel(
                expiryDate = LocalDate(2020, 1, 1),
            )

        assertTrue(epcDataModel.isPastExpiryDate())
    }

    @Test
    fun `isPastExpiryDate returns false if the expiry date is today`() {
        val dateNow =
            Clock
                .systemDefaultZone()
                .instant()
                .toKotlinInstant()
                .toLocalDateTime(TimeZone.of("Europe/London"))
                .date

        val epcDataModel =
            MockEpcData.createEpcDataModel(
                expiryDate = dateNow,
            )

        assertFalse(epcDataModel.isPastExpiryDate())
    }

    @Test
    fun `isPastExpiryDate returns false if the expiry date is in the future`() {
        val dateNow =
            Clock
                .systemDefaultZone()
                .instant()
                .toKotlinInstant()
                .toLocalDateTime(TimeZone.of("Europe/London"))
                .date

        val epcDataModel =
            MockEpcData.createEpcDataModel(
                expiryDate = dateNow.plus(DatePeriod(years = 2)),
            )

        assertFalse(epcDataModel.isPastExpiryDate())
    }

    @ParameterizedTest(name = "{1} when the energy rating is {0}")
    @MethodSource("provideEnergyRatings")
    fun `isEnergyRatingEOrBetter returns `(
        energyRating: String,
        expectedResult: Boolean,
    ) {
        val epcDataModel = MockEpcData.createEpcDataModel(energyRating = energyRating)

        assertEquals(expectedResult, epcDataModel.isEnergyRatingEOrBetter())
    }

    @Test
    fun `parseCertificateNumberOrNull formats the certificate number correctly if passed a valid number with no hyphens`() {
        val inputCertificateNumber = "00001111222233334444"
        val expectedFormattedCertificateNumber = "0000-1111-2222-3333-4444"

        assertEquals(
            expectedFormattedCertificateNumber,
            EpcDataModel.parseCertificateNumberOrNull(inputCertificateNumber),
        )
    }

    @Test
    fun `parseCertificateNumberOrNull formats the certificate number correctly if passed a valid number including hyphens`() {
        val inputCertificateNumber = "0000-1111-2222-3333-4444"

        assertEquals(
            inputCertificateNumber,
            EpcDataModel.parseCertificateNumberOrNull(inputCertificateNumber),
        )
    }

    @Test
    fun `parseCertificateNumberOrNull returns null if passed a certificate number with an invalid length`() {
        val invalidCertificateNumber = "123456"

        assertNull(EpcDataModel.parseCertificateNumberOrNull(invalidCertificateNumber))
    }

    @Test
    fun `parseCertificateNumberOrNull returns null if passed a certificate number containing invalid characters`() {
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

    companion object {
        @JvmStatic
        fun provideEnergyRatings() =
            arrayOf(
                Arguments.of("A", true),
                Arguments.of("B", true),
                Arguments.of("C", true),
                Arguments.of("D", true),
                Arguments.of("E", true),
                Arguments.of("F", false),
                Arguments.of("G", false),
                Arguments.of("a", true),
                Arguments.of("b", true),
                Arguments.of("c", true),
                Arguments.of("d", true),
                Arguments.of("e", true),
                Arguments.of("f", false),
                Arguments.of("g", false),
            )
    }
}
