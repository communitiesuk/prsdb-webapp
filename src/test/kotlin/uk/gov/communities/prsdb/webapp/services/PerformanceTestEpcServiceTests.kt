package uk.gov.communities.prsdb.webapp.services

import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.repository.AddressRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PerformanceTestEpcServiceTests {
    private val repository: AddressRepository = mock()
    private val service = PerformanceTestEpcService(repository)

    @Test
    fun `UPRN and synthetic certificate lookups return the same valid EPC for the selected address`() {
        whenever(repository.findByIsActiveTrueAndUprn(901)).thenReturn(
            Address(
                AddressDataModel(
                    singleLineAddress = "1 Fixture Road, Fixture Town, FA1 1AA",
                    postcode = "FA1 1AA",
                    uprn = 901,
                ),
            ),
        )
        val uprnResult = service.search("UPRN-000000000901", null)
        val certificateResult = service.search(null, "0000-0000-0000-0000-9011")
        assertEquals(uprnResult, certificateResult)
        val epc = EpcDataModel.fromJsonObject(JSONObject(requireNotNull(uprnResult)))
        assertEquals("1 Fixture Road, Fixture Town, FA1 1AA", epc.singleLineAddress)
        assertEquals("C", epc.energyRating)
        assertTrue(epc.isLatestCertificateForThisProperty())
        assertFalse(epc.isPastExpiryDate())
    }

    @Test
    fun `unknown allocation returns no certificate`() {
        assertNull(service.search("UPRN-000000000901", null))
    }

    @Test
    fun `malformed ambiguous and out of scope certificate variants never query the database`() {
        for ((uprn, rrn) in listOf(
            null to null,
            "UPRN-000000000901" to "0000-0000-0000-0000-9011",
            "UPRN-../901" to null,
            "UPRN-000000000000" to null,
            null to "0000-0000-0000-0000-9012",
            null to "00000000000000009011",
        )) {
            assertFailsWith<IllegalArgumentException> { service.search(uprn, rrn) }
        }
        verifyNoInteractions(repository)
    }
}
