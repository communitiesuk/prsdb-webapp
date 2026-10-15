package uk.gov.communities.prsdb.webapp.performance

data class BasicIterationData(
    val requestNamePrefix: String,
    val profilePhoneNumber: String,
    val profileEmailAddress: String,
    val registrationSubject: String,
    val registrationEmailAddress: String,
    val registrationPhoneNumber: String,
) {
    companion object {
        private const val WARM_UP_LABEL = "warmup"
        private const val PROFILE_PHONE_SUFFIX_START = 123

        fun forPass(
            registrationSubjectPrefix: String,
            measuredIteration: Int?,
        ): BasicIterationData {
            require(registrationSubjectPrefix.isNotBlank()) { "Registration subject prefix must not be blank" }
            require(measuredIteration == null || measuredIteration > 0) {
                "Measured iteration must be a positive one-based index"
            }
            val suffix = measuredIteration?.toString() ?: WARM_UP_LABEL
            val phoneNumber = syntheticPhoneNumber(measuredIteration ?: 0)
            val runToken =
                registrationSubjectPrefix
                    .substringAfterLast(':')
                    .lowercase()
                    .replace(Regex("[^a-z0-9-]"), "-")
                    .ifBlank { "run" }

            return BasicIterationData(
                requestNamePrefix = if (measuredIteration == null) "Warm-up: " else "",
                profilePhoneNumber = phoneNumber,
                profileEmailAddress = "performance-landlord-$runToken-$suffix@example.invalid",
                registrationSubject = "$registrationSubjectPrefix-$suffix",
                registrationEmailAddress = "performance-registration-$runToken-$suffix@example.invalid",
                registrationPhoneNumber = phoneNumber,
            )
        }

        private fun syntheticPhoneNumber(iteration: Int): String {
            val suffix = (PROFILE_PHONE_SUFFIX_START + iteration) % 1000
            return "0207946${suffix.toString().padStart(4, '0')}"
        }
    }
}
