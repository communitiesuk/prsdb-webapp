package uk.gov.communities.prsdb.webapp.constants.enums

enum class RegistrationNumberType {
    PROPERTY,
    LANDLORD,
    AGENT,
    ;

    fun toInitial(): Char = this.toString()[0]

    companion object {
        fun initialToType(initial: Char): RegistrationNumberType =
            when (initial) {
                'P' -> PROPERTY
                'L' -> LANDLORD
                'A' -> AGENT
                else -> throw IllegalArgumentException("Invalid Initial")
            }
    }
}
