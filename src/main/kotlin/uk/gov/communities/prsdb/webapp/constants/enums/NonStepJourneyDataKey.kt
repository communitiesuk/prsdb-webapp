package uk.gov.communities.prsdb.webapp.constants.enums

enum class NonStepJourneyDataKey(
    val key: String,
) {
    LookedUpAddresses("looked-up-addresses"),
    LookedUpEpc("looked-up-epc"),
    AutoMatchedEpc("auto-matched-epc"),
}
