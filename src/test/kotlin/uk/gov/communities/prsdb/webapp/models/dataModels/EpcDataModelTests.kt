package uk.gov.communities.prsdb.webapp.models.dataModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
            EpcDataModel.formatCertificateNumber(inputCertificateNumber),
        )
    }

    @Test
    fun `formatCertificateNumber formats the certificate number correctly if passed a valid number including hyphens`() {
        val inputCertificateNumber = "0000-1111-2222-3333-4444"

        assertEquals(
            inputCertificateNumber,
            EpcDataModel.formatCertificateNumber(inputCertificateNumber),
        )
    }

    @Test
    fun `formatCertificateNumber throws an error if passed a certificate number with an invalid length`() {
        val invalidCertificateNumber = "123456"

        val exception = assertThrows<IllegalArgumentException> { EpcDataModel.formatCertificateNumber(invalidCertificateNumber) }

        assertEquals("Input must contain exactly 20 digits", exception.message)
    }

    @Test
    fun `formatCertificateNumber throws an error if passed a certificate number containing invalid characters`() {
        val invalidCertificateNumber = "123456Invalid!"

        val exception = assertThrows<IllegalArgumentException> { EpcDataModel.formatCertificateNumber(invalidCertificateNumber) }

        assertEquals("Input must contain only digits and hyphens", exception.message)
    }
}
