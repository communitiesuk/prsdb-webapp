package uk.gov.communities.prsdb.webapp.models.dataModels

enum class ScanResult(
    val value: String,
) {
    NoThreats("NO_THREATS_FOUND"),
    Threats(" THREATS_FOUND"),
    Unsupported("UNSUPPORTED"),
    AccessDenied("ACCESS_DENIED"),
    Failed("FAILED"),
    ;

    companion object {
        fun fromStringValueOrNull(value: String) = ScanResult.entries.singleOrNull { it.value == value }
    }
}
