package uk.gov.communities.prsdb.webapp.services

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.json.JSONException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.exceptions.PersistentSmsSendException
import uk.gov.communities.prsdb.webapp.exceptions.TransientSmsSendException
import uk.gov.communities.prsdb.webapp.models.dataModels.NotifyErrorDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.NotifyErrorType
import uk.gov.communities.prsdb.webapp.models.dataModels.NotifyErrorsDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.smsModels.SmsTemplateModel
import uk.gov.service.notify.NotificationClient
import uk.gov.service.notify.NotificationClientException

@PrsdbWebService
class NotifySmsNotificationService<SmsModel : SmsTemplateModel>(
    private val notificationClient: NotificationClient,
    private val notifyIdService: NotifyIdService,
) : SmsNotificationService<SmsModel> {
    override fun sendSms(
        phoneNumber: String,
        sms: SmsModel,
    ) {
        val smsParameters = sms.toHashMap()
        val idValue = notifyIdService.getNotifyIdValue(sms.template)
        if (idValue.isBlank()) {
            throw PersistentSmsSendException("No Notify template ID is configured for ${sms.template}. No SMS was sent.")
        }
        try {
            notificationClient.sendSms(idValue, phoneNumber, smsParameters, null)
        } catch (notifyException: NotificationClientException) {
            throwSmsSendException(notifyException)
        } catch (exception: JSONException) {
            throw PersistentSmsSendException(UNKNOWN_OUTCOME_MESSAGE, exception)
        } catch (exception: IllegalArgumentException) {
            throw PersistentSmsSendException(UNKNOWN_OUTCOME_MESSAGE, exception)
        }
    }

    // Error types are from https://docs.notifications.service.gov.uk/java.html#send-a-text-message
    // and https://docs.notifications.service.gov.uk/java.html#general-errors
    private fun throwSmsSendException(notifyException: NotificationClientException): Nothing {
        val errors =
            parseNotifyExceptionErrors(notifyException.message ?: "")
                ?: throw PersistentSmsSendException(UNKNOWN_OUTCOME_MESSAGE, notifyException)
        val errorTypes = errors.map { it.error }
        val multipleErrorMessagePrefix =
            if (errorTypes.size > 1) "There were multiple errors sending an SMS with Notify. " else ""
        when {
            NotifyErrorType.AUTH in errorTypes -> throw PersistentSmsSendException(
                multipleErrorMessagePrefix +
                    "prsdb-web credentials were not accepted by Notify. " +
                    "No SMS messages can be sent until the issue is fixed. See inner exception for details.",
                notifyException,
            )

            NotifyErrorType.BAD_REQUEST in errorTypes || NotifyErrorType.VALIDATION in errorTypes -> throw PersistentSmsSendException(
                multipleErrorMessagePrefix +
                    "Send SMS request was rejected by Notify as invalid (for example, an invalid recipient number). " +
                    "That SMS cannot be sent until the issue is fixed. See inner exception for details.",
                notifyException,
            )

            NotifyErrorType.TOO_MANY_REQUESTS in errorTypes -> throw PersistentSmsSendException(
                multipleErrorMessagePrefix +
                    "Too many SMS messages have been sent with Notify today. SMS sending will not work until tomorrow.",
                notifyException,
            )

            NotifyErrorType.RATE_LIMIT in errorTypes -> throw TransientSmsSendException(
                multipleErrorMessagePrefix +
                    "Too many SMS messages have been sent with Notify in the last minute, but retrying may work.",
                notifyException,
            )

            NotifyErrorType.EXCEPTION in errorTypes -> throw TransientSmsSendException(
                multipleErrorMessagePrefix +
                    "Notify has not sent that SMS, but retrying may work.",
                notifyException,
            )

            else -> throw PersistentSmsSendException(
                multipleErrorMessagePrefix +
                    "Notify returned an error that could not be classified. See inner exception for details.",
                notifyException,
            )
        }
    }

    private fun parseNotifyExceptionErrors(message: String): List<NotifyErrorDataModel>? =
        try {
            NotifyErrorsDataModel.fromExceptionMessage(message, notifyErrorJson).errors
        } catch (exception: SerializationException) {
            null
        } catch (exception: IllegalArgumentException) {
            null
        }

    companion object {
        private const val UNKNOWN_OUTCOME_MESSAGE =
            "Could not read the response from Notify, so it is unknown whether that SMS was sent. " +
                "Check Notify before retrying to avoid sending a duplicate SMS. See inner exception for details."

        // Unrecognised error types decode to UNKNOWN, matching the letter transport.
        private val notifyErrorJson = Json { coerceInputValues = true }
    }
}
