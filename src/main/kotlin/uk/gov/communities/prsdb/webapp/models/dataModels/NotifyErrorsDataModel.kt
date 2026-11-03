package uk.gov.communities.prsdb.webapp.models.dataModels

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class NotifyErrorsDataModel(
    val errors: List<NotifyErrorDataModel>,
    @SerialName("status_code")
    val statusCode: Int,
) {
    companion object {
        private val nonJsonRegex = Regex("^Status code: \\d\\d\\d")

        fun fromExceptionMessage(
            message: String,
            json: Json,
        ): NotifyErrorsDataModel = json.decodeFromString(nonJsonRegex.replace(message, ""))
    }
}

@Serializable
data class NotifyErrorDataModel(
    val error: NotifyErrorType = NotifyErrorType.UNKNOWN,
    val message: String,
)

@Serializable
enum class NotifyErrorType {
    @SerialName("AuthError")
    AUTH,

    @SerialName("BadRequestError")
    BAD_REQUEST,

    @SerialName("ValidationError")
    VALIDATION,

    @SerialName("RateLimitError")
    RATE_LIMIT,

    @SerialName("TooManyRequestsError")
    TOO_MANY_REQUESTS,

    @SerialName("Exception")
    EXCEPTION,

    // Only produced when decoding with coerceInputValues, or when the error type is missing
    UNKNOWN,
}
