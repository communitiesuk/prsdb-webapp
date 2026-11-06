package uk.gov.communities.prsdb.webapp.performance

import java.time.LocalDate
import java.time.ZoneId

enum class RegistrationVariant(
    val manual: Boolean,
    val occupied: Boolean,
    val licensing: String,
    val gas: String,
    val electrical: String,
    val epc: String,
) {
    VACANT_MANUAL_EXEMPT(true, false, "NO_LICENSING", "none", "missing", "exempt"),
    OCCUPIED_SELECTED_CERTIFICATES(false, true, "SELECTIVE_LICENCE", "upload", "eicr-upload", "valid"),
    OCCUPIED_MANUAL_EXPIRED(true, true, "HMO_MANDATORY_LICENCE", "expired", "eic-expired", "expired"),
    OCCUPIED_SELECTED_MISSING(false, true, "HMO_ADDITIONAL_LICENCE", "missing", "missing", "missing"),
    OCCUPIED_MANUAL_DEFERRED(true, true, "PROVIDE_LATER", "later", "later", "later"),
    OCCUPIED_SELECTED_LOW_RATING(false, true, "NO_LICENSING", "none", "eicr-expired", "low"),
    OCCUPIED_MANUAL_SUPERSEDED(true, true, "NO_LICENSING", "upload", "eic-upload", "superseded"),
    OCCUPIED_SELECTED_NOT_FOUND(false, true, "NO_LICENSING", "expired", "eicr-expired", "not-found"),
    OCCUPIED_SELECTED_UPRN(false, true, "NO_LICENSING", "none", "missing", "uprn"),
    OCCUPIED_MANUAL_GAS_THIRTEEN_MONTHS(true, true, "NO_LICENSING", "boundary", "missing", "exempt"),
}

object ComplianceJourney {
    fun certificateNumber(
        fixture: RegistrationFixture,
        variant: RegistrationVariant,
        supersededLatest: Boolean = false,
    ): String {
        require(fixture.uprn < 1_000_000_000_000_000_000L) { "Fixture UPRN exceeds the local EPC contract" }
        val mode =
            if (supersededLatest) {
                "1"
            } else {
                when (variant.epc) {
                    "expired" -> "2"
                    "low" -> "3"
                    "not-found" -> "4"
                    "superseded" -> "5"
                    else -> "1"
                }
            }
        return ((if (variant.manual) "1" else "0") + fixture.uprn.toString().padStart(18, '0') + mode).chunked(4).joinToString("-")
    }

    fun fields(
        step: String,
        variant: RegistrationVariant,
        fixture: RegistrationFixture?,
    ): Map<String, String>? {
        fun date(value: LocalDate) =
            mapOf("day" to value.dayOfMonth.toString(), "month" to value.monthValue.toString(), "year" to value.year.toString())
        val today = LocalDate.now(ZoneId.of("Europe/London"))
        return when (step) {
            "has-gas-supply" ->
                when (variant.gas) {
                    "later" -> mapOf("action" to "provideThisLater")
                    else -> mapOf("hasGasSupply" to (variant.gas != "none").toString())
                }
            "has-gas-safety" -> mapOf("hasCert" to (variant.gas != "missing").toString())
            "gas-safety-certificate-issue-date" ->
                date(
                    today.minusMonths(
                        when (variant.gas) {
                            "expired" -> 24
                            "boundary" -> 13
                            else -> 1
                        },
                    ),
                )
            "has-electrical-safety" ->
                when (variant.electrical) {
                    "later" -> mapOf("action" to "provideThisLater")
                    "missing" -> mapOf("electricalCertType" to "NO_CERTIFICATE")
                    else -> mapOf("electricalCertType" to if (variant.electrical.startsWith("eicr-")) "HAS_EICR" else "HAS_EIC")
                }
            "eic-expiry-date", "eicr-expiry-date" ->
                date(
                    if (variant.electrical.endsWith("expired")) today.minusDays(5) else today.plusYears(2),
                )
            "has-epc" ->
                when (variant.epc) {
                    "later" -> mapOf("action" to "provideThisLater")
                    else -> mapOf("hasCert" to (variant.epc !in setOf("missing", "exempt")).toString())
                }
            "check-epc-details" -> mapOf("matchedEpcIsCorrect" to (variant.epc == "uprn").toString())
            "find-your-epc" ->
                mapOf(
                    "certificateNumber" to certificateNumber(requireNotNull(fixture) { "EPC lookup requires a fixture" }, variant),
                )
            "confirm-epc-details" -> mapOf("matchedEpcIsCorrect" to "true")
            "is-epc-required" -> mapOf("epcRequired" to (variant.epc != "exempt").toString())
            "epc-exemption" -> mapOf("exemptionReason" to "ANNUAL_USE_LESS_THAN_4_MONTHS")
            "epc-in-date-at-start-of-tenancy-check" -> mapOf("tenancyStartedBeforeExpiry" to "false")
            "has-mees-exemption" -> mapOf("propertyHasExemption" to "true")
            "mees-exemption" -> mapOf("exemptionReason" to "HIGH_COST")
            "gas-safety-certificate-expired", "gas-safety-certificate-missing", "electrical-safety-certificate-missing",
            "eic-expired", "eicr-expired", "epc-expired", "epc-missing", "epc-not-found", "epc-superseded", "low-energy-rating",
            "check-gas-safety-answers", "check-electrical-safety-answers", "check-epc-answers",
            "check-gas-safety-certificate-uploads", "check-eic-uploads", "check-eicr-uploads",
            "provide-gas-safety-certificate-later", "provide-electrical-safety-certificate-later", "provide-epc-later",
            -> emptyMap()
            else -> null
        }
    }
}
