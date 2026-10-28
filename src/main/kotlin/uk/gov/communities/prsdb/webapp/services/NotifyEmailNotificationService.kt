package uk.gov.communities.prsdb.webapp.services

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.exceptions.NotifyAllowlistException
import uk.gov.communities.prsdb.webapp.exceptions.PersistentEmailSendException
import uk.gov.communities.prsdb.webapp.exceptions.TransientEmailSentException
import uk.gov.communities.prsdb.webapp.models.dataModels.NotifyErrorDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.NotifyErrorType
import uk.gov.communities.prsdb.webapp.models.dataModels.NotifyErrorsDataModel
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
        val errors = NotifyErrorsDataModel.fromExceptionMessage(notifyException.message ?: "", Json).errors
        val errorTypes = errors.map { it.error }
        val multipleErrorMessagePrefix =
            if (errorTypes.size > 1) "There were multiple errors sending an email with Notify. " else ""
        when {
            // These error types previously failed to parse, so they still throw a SerializationException rather than an email exception
            NotifyErrorType.VALIDATION in errorTypes || NotifyErrorType.UNKNOWN in errorTypes -> throw SerializationException(
                "Notify returned an error type that email sending does not handle. See inner exception for details.",
                notifyException,
            )

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

    // A non-allowlisted recipient yields a "Can't send to this recipient ..." BadRequestError. The apostrophe in
    // "Can't" is a typographic one (U+2019), so it is normalised to a straight apostrophe before matching.
    private fun isAllowlistError(errors: List<NotifyErrorDataModel>): Boolean =
        !useProductionNotify &&
            errors.any {
                it.error == NotifyErrorType.BAD_REQUEST &&
                    it.message.replace("\u2019", "'")
                        .contains(NOTIFY_ALLOWLIST_MESSAGE_FRAGMENT, ignoreCase = true)
            }

    companion object {
        private const val NOTIFY_ALLOWLIST_MESSAGE_FRAGMENT = "can't send to this recipient"
    }
}
