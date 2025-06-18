package uk.gov.communities.prsdb.webapp.services

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.exceptions.PersistentEmailSendException
import uk.gov.communities.prsdb.webapp.exceptions.TransientEmailSentException
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplateModel
import uk.gov.service.notify.NotificationClient
import uk.gov.service.notify.NotificationClientException

@PrsdbWebService
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
            throwEmailSendException(notifyException)
        }
    }

    private fun throwEmailSendException(notifyException: NotificationClientException) {
        val errorTypes = parseNotifyExceptionErrors(notifyException.message ?: "")
        val multipleErrorMessagePrefix =
            if (errorTypes.size > 1) "There were multiple errors sending an email with Notify. " else ""
        when {
            NotifyErrorType.AUTH in errorTypes -> throw PersistentEmailSendException(
                multipleErrorMessagePrefix +
                    "prsdb-web credentials were not accepted by Notify. " +
                    "No emails can be sent until the issue is fixed. See inner exception for details.",
                notifyException,
            )

            NotifyErrorType.BAD_REQUEST in errorTypes -> throw PersistentEmailSendException(
                multipleErrorMessagePrefix +
                    "Send email request was rejected by notify as a bad request. " +
                    "That email cannot be sent until the issue is fixed. See inner exception for details.",
                notifyException,
            )

            NotifyErrorType.TOO_MANY_REQUESTS in errorTypes -> throw PersistentEmailSendException(
                multipleErrorMessagePrefix +
                    "Too many emails have been sent with Notify today. Email sending will not work until tomorrow.",
                notifyException,
            )

            NotifyErrorType.RATE_LIMIT in errorTypes -> throw TransientEmailSentException(
                multipleErrorMessagePrefix +
                    "Too many email have been sent with Notify in the last minute, but retrying may work.",
                notifyException,
            )

            NotifyErrorType.EXCEPTION in errorTypes -> throw TransientEmailSentException(
                multipleErrorMessagePrefix +
                    "Notify has not sent that email, but retrying may work.",
                notifyException,
            )
        }
    }

    private fun parseNotifyExceptionErrors(message: String): Collection<NotifyErrorType> {
        var nonJsonRegex = Regex("^Status code: \\d\\d\\d")
        var jsonString = nonJsonRegex.replace(message, "")
        var parsed = Json.decodeFromString<NotifyErrors>(jsonString)

        return parsed.errors.map { t -> t.error }
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
