package uk.gov.communities.prsdb.webapp.services

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.exceptions.CannotSendEmailsException
import uk.gov.communities.prsdb.webapp.exceptions.EmailWasNotSentException
import uk.gov.communities.prsdb.webapp.viewmodel.EmailTemplateModel
import uk.gov.service.notify.NotificationClient
import uk.gov.service.notify.NotificationClientException

@Service
class NotifyEmailNotificationService<EmailModel : EmailTemplateModel>(
    var notificationClient: NotificationClient,
) : EmailNotificationService<EmailModel> {
    override fun sendEmail(
        recipientAddress: String,
        email: EmailModel,
    ) {
        val emailParameters = email.toHashMap()
        try {
            notificationClient.sendEmail(email.templateId.idValue, recipientAddress, emailParameters, null)
        } catch (notifyException: NotificationClientException) {
            val errorType = parseNotifyExceptionError(notifyException.message ?: "")
            when (errorType) {
                NotifyErrorType.AUTH, NotifyErrorType.BAD_REQUEST -> throw CannotSendEmailsException(
                    "prsdb-web does not have the correct credentials to send emails with Notify." +
                        " No emails can be sent until the authentication issue is fixed.",
                    notifyException,
                )
                NotifyErrorType.TOO_MANY_REQUESTS -> throw CannotSendEmailsException(
                    "Too many emails have been sent with Notify today. Email sending will not work until tomorrow.",
                    notifyException,
                )
                NotifyErrorType.EXCEPTION, NotifyErrorType.RATE_LIMIT -> throw EmailWasNotSentException(
                    notifyException,
                )
            }
        }
    }

    private fun parseNotifyExceptionError(message: String): NotifyErrorType {
        var nonJsonRegex = Regex("^Status code: \\d\\d\\d")
        var jsonString = nonJsonRegex.replace(message, "")
        var parsed = Json.decodeFromString<NotifyErrors>(jsonString)

        return parsed.errors.first().error
    }

    @Serializable
    private data class NotifyErrors(
        val errors: List<NotifyErrorClass>,
        val status_code: Int,
    )

    @Serializable
    private data class NotifyErrorClass(
        val error: NotifyErrorType,
        val message: String,
    )

    @Serializable
    private enum class NotifyErrorType {
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
}
