package uk.gov.communities.prsdb.webapp.services

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.exceptions.NotifyAllowlistException
import uk.gov.communities.prsdb.webapp.exceptions.PersistentEmailSendException
import uk.gov.communities.prsdb.webapp.exceptions.TransientEmailSentException
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplateModel
import uk.gov.service.notify.NotificationClient
import uk.gov.service.notify.NotificationClientException

@Service
class NotifyEmailNotificationService<EmailModel : EmailTemplateModel>(
    private val notificationClient: NotificationClient,
    private val notifyIdService: NotifyIdService,
    @Value("\${notify.use-production-notify}") private val useProductionNotify: Boolean,
) : EmailNotificationService<EmailModel> {
    override fun sendEmail(
        recipientAddress: String,
        email: EmailModel,
    ) {
        val emailParameters = email.toHashMap()
        try {
            val idValue = notifyIdService.getNotifyIdValue(email.template)
            notificationClient.sendEmail(idValue, recipientAddress, emailParameters, null)
        } catch (notifyException: NotificationClientException) {
            throwEmailSendException(notifyException)
        }
    }

    private fun throwEmailSendException(notifyException: NotificationClientException) {
        val errors = parseNotifyExceptionErrors(notifyException.message ?: "")
        val errorTypes = errors.map { it.error }
        val multipleErrorMessagePrefix =
            if (errorTypes.size > 1) "There were multiple errors sending an email with Notify. " else ""
        when {
            isAllowlistError(errors) -> throw NotifyAllowlistException(
                multipleErrorMessagePrefix +
                    "An email was sent to a recipient that is not on the Notify allowlist. " +
                    "This is expected in non-production environments. See inner exception for details.",
                notifyException,
            )

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

    private fun isAllowlistError(errors: List<NotifyErrorClass>): Boolean =
        !useProductionNotify &&
            errors.any {
                it.error == NotifyErrorType.BAD_REQUEST &&
                    it.message.contains(NOTIFY_ALLOWLIST_MESSAGE_FRAGMENT, ignoreCase = true)
            }

    private fun parseNotifyExceptionErrors(message: String): List<NotifyErrorClass> {
        val nonJsonRegex = Regex("^Status code: \\d\\d\\d")
        val jsonString = nonJsonRegex.replace(message, "")
        return Json.decodeFromString<NotifyErrors>(jsonString).errors
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

    companion object {
        private const val NOTIFY_ALLOWLIST_MESSAGE_FRAGMENT = "send to this recipient"
    }
}
