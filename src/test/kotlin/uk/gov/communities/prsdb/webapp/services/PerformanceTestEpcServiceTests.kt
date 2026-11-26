package uk.gov.communities.prsdb.webapp.services

import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.context.annotation.AnnotationConfigApplicationContext
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
    fun `service is active for local and NFT performance runs but not production`() {
        for ((profiles, enabled) in listOf(
            arrayOf("local", "performance-test") to true,
            arrayOf("nft", "performance-test") to true,
            arrayOf("local") to false,
            arrayOf("local", "performance-test", "production") to false,
            arrayOf("nft", "performance-test", "production") to false,
        )) {
            AnnotationConfigApplicationContext().use { context ->
                context.environment.setActiveProfiles(*profiles)
                context.beanFactory.registerSingleton("addressRepository", repository)
                context.register(PerformanceTestEpcService::class.java)
                context.refresh()
                assertEquals(enabled, context.getBeansOfType(PerformanceTestEpcService::class.java).isNotEmpty())
            }
        }
    }

    @Test
    fun `UPRN and synthetic certificate lookups return the same valid EPC for the selected address`() {
        givenAddress()

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
    fun `expired low-rating and superseded certificates return their respective EPC outcomes`() {
        givenAddress()

        val expired = epcFor("0000-0000-0000-0000-9012")
        val lowRating = epcFor("0000-0000-0000-0000-9013")
        val superseded = epcFor("0000-0000-0000-0000-9015")

        assertTrue(expired.isPastExpiryDate())
        assertEquals("C", expired.energyRating)
        assertFalse(lowRating.isPastExpiryDate())
        assertFalse(lowRating.isEnergyRatingEOrBetter())
        assertFalse(superseded.isLatestCertificateForThisProperty())
        assertEquals("0000-0000-0000-0000-9011", superseded.latestCertificateNumberForThisProperty)
    }

    @Test
    fun `not-found certificate variant returns no EPC without looking up an address`() {
        val result = service.search(null, "0000-0000-0000-0000-9014")

        assertNull(result)
        verifyNoInteractions(repository)
    }

    @Test
    fun `malformed ambiguous and out of scope certificate variants never query the database`() {
        for ((uprn, rrn) in listOf(
            null to null,
            "UPRN-000000000901" to "0000-0000-0000-0000-9011",
            "UPRN-../901" to null,
            "UPRN-000000000000" to null,
            null to "0000-0000-0000-0000-9010",
            null to "0000-0000-0000-0000-9016",
            null to "0000-0000-0000-0000-0004",
            null to "2000-0000-0000-0000-9011",
            null to "00000000000000009011",
        )) {
            assertFailsWith<IllegalArgumentException> { service.search(uprn, rrn) }
        }
        verifyNoInteractions(repository)
    }

    private fun epcFor(certificateNumber: String): EpcDataModel =
        EpcDataModel.fromJsonObject(JSONObject(requireNotNull(service.search(null, certificateNumber))))

    private fun givenAddress() {
        whenever(repository.findByIsActiveTrueAndUprn(901)).thenReturn(
            Address(
                AddressDataModel(
                    singleLineAddress = "1 Fixture Road, Fixture Town, FA1 1AA",
                    postcode = "FA1 1AA",
                    uprn = 901,
                ),
            ),
        )
    }
}
