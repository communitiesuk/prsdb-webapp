package uk.gov.communities.prsdb.webapp.testHelpers

import uk.gov.communities.prsdb.webapp.constants.JsonDeserializationKeys

enum class NotifyEnvironment(
    private val apiKeyEnvironmentVariable: String,
    val templateIdJsonKey: String,
) {
    INTEGRATION("EMAILNOTIFICATIONS_APIKEY", JsonDeserializationKeys.TEST_NOTIFY_ID_KEY),
    PRODUCTION("EMAILNOTIFICATIONS_PRODUCTION_APIKEY", JsonDeserializationKeys.PRODUCTION_NOTIFY_ID_KEY),
    ;

    val apiKey: String?
        get() = System.getenv(apiKeyEnvironmentVariable).takeUnless { it.isNullOrBlank() }

    val isConfigured: Boolean
        get() = apiKey != null
}
