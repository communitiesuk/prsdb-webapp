package uk.gov.communities.prsdb.webapp.constants.enums

enum class JourneyType : IJourneyType {
    LANDLORD_REGISTRATION {
        override val urlPathSegment: String
            get() = "register-as-a-landlord"
    },
}

interface IJourneyType {
    val urlPathSegment: String
}
