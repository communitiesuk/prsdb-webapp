package uk.gov.communities.prsdb.webapp.services

import org.springframework.context.annotation.Profile
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.repository.AddressRepository
import java.time.LocalDate

@Profile("performance-test & nft & !production")
@PrsdbWebService
class PerformanceTestEpcService(
    private val addressRepository: AddressRepository,
) {
    fun search(
        uprn: String?,
        rrn: String?,
    ): Map<String, Any>? {
        require((uprn == null) != (rrn == null)) { "Supply exactly one synthetic EPC query" }
        val digits =
            if (uprn != null) {
                require(uprn.matches(Regex("UPRN-[0-9]{12,18}"))) { "Invalid synthetic UPRN" }
                "0${uprn.removePrefix("UPRN-").padStart(18, '0')}1"
            } else {
                require(requireNotNull(rrn).matches(Regex("[0-9]{4}(-[0-9]{4}){4}"))) { "Invalid synthetic certificate number" }
                rrn.replace("-", "").also {
                    require(it.matches(Regex("[01][0-9]{18}[1-5]"))) { "Unsupported synthetic EPC variant" }
                }
            }
        val mode = digits.last()
        val number = digits.substring(1, 19).toLong()
        require(number > 0) { "Invalid synthetic UPRN" }
        if (mode == '4') return null

        val address = addressRepository.findByIsActiveTrueAndUprn(number) ?: return null
        val parts = address.singleLineAddress.split(",").map { it.trim() }
        require(parts.size >= 3 && !address.postcode.isNullOrBlank()) { "Synthetic EPC needs a complete fixture address" }
        val manual = digits.startsWith("1")
        val certificate = digits.chunked(4).joinToString("-")
        val latestCertificate =
            if (mode == '5') {
                (digits.dropLast(1) + '1').chunked(4).joinToString("-")
            } else {
                certificate
            }
        val energyBand = if (mode == '3') "G" else "C"
        val expiryDate =
            if (mode == '2') {
                LocalDate.now().minusDays(1)
            } else {
                LocalDate.now().plusYears(10)
            }
        return mapOf(
            "data" to
                mapOf(
                    "epcRrn" to certificate,
                    "latestEpcRrnForAddress" to latestCertificate,
                    "address" to
                        mapOf(
                            "addressLine1" to if (manual) "NFT registration $number" else parts.first(),
                            "addressLine2" to if (manual) "" else parts.drop(1).dropLast(2).joinToString(", "),
                            "town" to if (manual) "Performance Town" else parts[parts.lastIndex - 1],
                            "postcode" to requireNotNull(address.postcode),
                        ),
                    "currentEnergyEfficiencyBand" to energyBand,
                    "expiryDate" to expiryDate.toString(),
                ),
        )
    }
}
