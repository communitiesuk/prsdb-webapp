package uk.gov.communities.prsdb.webapp.constants.enums

enum class LicensingType(
    val displayName: String,
) {
    SELECTIVE_LICENCE("Selective licence"),
    HMO_MANDATORY_LICENCE("HMO licence"),
    HMO_ADDITIONAL_LICENCE("Additional licence"),
    NO_LICENSING("Not Licenced"),
}
