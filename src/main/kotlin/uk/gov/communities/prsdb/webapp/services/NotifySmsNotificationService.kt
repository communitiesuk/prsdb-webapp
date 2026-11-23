package uk.gov.communities.prsdb.webapp.services

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.json.JSONException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.exceptions.SmsSendException
import uk.gov.communities.prsdb.webapp.exceptions.SmsSendFailureReason
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
            throw SmsSendException(
                SmsSendFailureReason.TECHNICAL,
                "No Notify template ID is configured for ${sms.template}. No SMS was sent.",
            )
        }
        try {
            notificationClient.sendSms(idValue, phoneNumber, smsParameters, null)
        } catch (notifyException: NotificationClientException) {
            throwSmsSendException(notifyException)
        } catch (exception: JSONException) {
            throw SmsSendException(SmsSendFailureReason.UNKNOWN, UNKNOWN_OUTCOME_MESSAGE, exception)
        } catch (exception: IllegalArgumentException) {
            throw SmsSendException(SmsSendFailureReason.UNKNOWN, UNKNOWN_OUTCOME_MESSAGE, exception)
        }
    }

    // Error types are from https://docs.notifications.service.gov.uk/java.html#send-a-text-message
    // and https://docs.notifications.service.gov.uk/java.html#general-errors
    private fun throwSmsSendException(notifyException: NotificationClientException): Nothing {
        val response =
            parseNotifyExceptionErrors(notifyException.message ?: "")
                ?: throw SmsSendException(SmsSendFailureReason.UNKNOWN, UNKNOWN_OUTCOME_MESSAGE, notifyException)
        val errorTypes = response.errors.map { it.error }
        val reason =
            when {
                response.statusCode !in 400..499 ||
                    errorTypes.isEmpty() ||
                    NotifyErrorType.UNKNOWN in errorTypes ||
                    NotifyErrorType.EXCEPTION in errorTypes -> SmsSendFailureReason.UNKNOWN
                NotifyErrorType.AUTH in errorTypes -> SmsSendFailureReason.TECHNICAL
                NotifyErrorType.TOO_MANY_REQUESTS in errorTypes -> SmsSendFailureReason.DAILY_LIMIT
                NotifyErrorType.BAD_REQUEST in errorTypes || NotifyErrorType.VALIDATION in errorTypes ->
                    SmsSendFailureReason.INVALID_REQUEST
                NotifyErrorType.RATE_LIMIT in errorTypes -> SmsSendFailureReason.MINUTE_LIMIT
                else -> SmsSendFailureReason.UNKNOWN
            }
        val message =
            if (reason == SmsSendFailureReason.UNKNOWN) UNKNOWN_OUTCOME_MESSAGE else "Notify rejected the SMS request: $reason."
        throw SmsSendException(reason, message, notifyException)
    }

    private fun parseNotifyExceptionErrors(message: String): NotifyErrorsDataModel? =
        try {
            NotifyErrorsDataModel.fromExceptionMessage(message, notifyErrorJson)
        } catch (exception: SerializationException) {
            null
        } catch (exception: IllegalArgumentException) {
            null
        }

    companion object {
        private const val UNKNOWN_OUTCOME_MESSAGE =
            "Could not confirm whether Notify accepted the SMS. See inner exception for details."

        // Unrecognised error types decode to UNKNOWN, matching the letter transport.
        private val notifyErrorJson = Json { coerceInputValues = true }
    }
}
