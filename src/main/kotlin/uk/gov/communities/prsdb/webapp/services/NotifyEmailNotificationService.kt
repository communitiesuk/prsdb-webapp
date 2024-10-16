package uk.gov.communities.prsdb.webapp.services

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.viewmodel.EmailTemplateModel
import uk.gov.service.notify.NotificationClient

@Service
class NotifyEmailNotificationService<EmailModel : EmailTemplateModel>(
    var notificationClient: NotificationClient,
) : EmailNotificationService<EmailModel> {
    override fun sendEmail(
        recipientAddress: String,
        email: EmailModel,
    ) {
        val emailParameters = email.toHashMap()
        notificationClient.sendEmail(email.templateId.idValue, recipientAddress, emailParameters, null)
    }

    fun parseNotifyExceptionErrors(message: String): NotifyErrorType {
        var nonJsonRegex = Regex("^Status code: \\d\\d\\d")
        var jsonString = nonJsonRegex.replace(message, "")
        var parsed = Json.decodeFromString<NotifyErrors>(jsonString)

        return parsed.errors.first().error
    }
}

@Serializable
data class NotifyErrors(
    val errors: List<NotifyErrorClass>,
    val status_code: Int,
)

@Serializable
data class NotifyErrorClass(
    val error: NotifyErrorType,
    val message: String,
)

@Serializable
enum class NotifyErrorType {
    @SerialName("AuthError")
    AUTH,

    @SerialName("BadRequestError")
    BAD_REQUEST,

    @SerialName("RateLimitError")
    RATE_LIMIT,

    @SerialName("TooManyRequestsError")
    TOO_MANY_REQUESTS,

    @SerialName("Exception")
    EXCEPTION,
}
