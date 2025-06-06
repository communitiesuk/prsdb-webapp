package uk.gov.communities.prsdb.webapp.constants.enums

enum class JourneyDataKey(
    val key: String,
) {
    LookedUpAddresses("looked-up-addresses"),
    LookedUpEpc("looked-up-epc"),
    AutoMatchedEpc("auto-matched-epc"),
}
