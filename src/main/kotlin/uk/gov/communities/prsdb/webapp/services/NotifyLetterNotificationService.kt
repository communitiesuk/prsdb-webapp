package uk.gov.communities.prsdb.webapp.services

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.exceptions.PersistentLetterSendException
import uk.gov.communities.prsdb.webapp.exceptions.TransientLetterSendException
import uk.gov.communities.prsdb.webapp.models.dataModels.LetterAddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.NotifyErrorDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.NotifyErrorType
import uk.gov.communities.prsdb.webapp.models.dataModels.NotifyErrorsDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.letterModels.LetterTemplateModel
import uk.gov.service.notify.NotificationClient
import uk.gov.service.notify.NotificationClientException

@Service
class NotifyLetterNotificationService<LetterModel : LetterTemplateModel>(
    private val notificationClient: NotificationClient,
    private val notifyIdService: NotifyIdService,
) : LetterNotificationService<LetterModel> {
    override fun sendLetter(
        recipientAddress: LetterAddressDataModel,
        letter: LetterModel,
    ) {
        val templateParameters = letter.toHashMap()
        require(templateParameters.keys.none { isReservedAddressKey(it) }) {
            "Letter personalisation must not contain reserved address keys"
        }
        val letterParameters = templateParameters + recipientAddress.toPersonalisation()
        try {
            val idValue = notifyIdService.getNotifyIdValue(letter.template)
            notificationClient.sendLetter(idValue, letterParameters, null)
        } catch (notifyException: NotificationClientException) {
            throwLetterSendException(notifyException)
        }
    }

    private fun throwLetterSendException(notifyException: NotificationClientException): Nothing {
        val errors =
            parseNotifyExceptionErrors(notifyException.message ?: "")
                ?: throw PersistentLetterSendException(
                    "Could not read the response from Notify, so it is unknown whether that letter was sent. " +
                        "Check Notify before retrying to avoid sending a duplicate letter. See inner exception for details.",
                    notifyException,
                )
        val errorTypes = errors.map { it.error }
        val multipleErrorMessagePrefix =
            if (errorTypes.size > 1) "There were multiple errors sending a letter with Notify. " else ""
        when {
            NotifyErrorType.AUTH in errorTypes -> throw PersistentLetterSendException(
                multipleErrorMessagePrefix +
                    "prsdb-web credentials were not accepted by Notify. " +
                    "No letters can be sent until the issue is fixed. See inner exception for details.",
                notifyException,
            )

            NotifyErrorType.BAD_REQUEST in errorTypes || NotifyErrorType.VALIDATION in errorTypes -> throw PersistentLetterSendException(
                multipleErrorMessagePrefix +
                    "Send letter request was rejected by Notify as invalid (for example, an invalid recipient address). " +
                    "That letter cannot be sent until the issue is fixed. See inner exception for details.",
                notifyException,
            )

            NotifyErrorType.TOO_MANY_REQUESTS in errorTypes -> throw PersistentLetterSendException(
                multipleErrorMessagePrefix +
                    "Too many letters have been sent with Notify today. Letter sending will not work until tomorrow.",
                notifyException,
            )

            NotifyErrorType.RATE_LIMIT in errorTypes -> throw TransientLetterSendException(
                multipleErrorMessagePrefix +
                    "Too many letters have been sent with Notify in the last minute, but retrying may work.",
                notifyException,
            )

            NotifyErrorType.EXCEPTION in errorTypes -> throw TransientLetterSendException(
                multipleErrorMessagePrefix +
                    "Notify has not sent that letter, but retrying may work.",
                notifyException,
            )

            else -> throw PersistentLetterSendException(
                multipleErrorMessagePrefix +
                    "Notify returned an error that could not be classified. See inner exception for details.",
                notifyException,
            )
        }
    }

    // Unlike emails, an unreadable response is reported rather than ignored so a failed letter is never missed
    private fun parseNotifyExceptionErrors(message: String): List<NotifyErrorDataModel>? =
        try {
            NotifyErrorsDataModel.fromExceptionMessage(message, notifyErrorJson).errors
        } catch (exception: SerializationException) {
            null
        } catch (exception: IllegalArgumentException) {
            null
        }

    companion object {
        // Unrecognised error types decode to UNKNOWN so a readable response is not mistaken for an unreadable one
        private val notifyErrorJson = Json { coerceInputValues = true }

        private val NOTIFY_IGNORED_KEY_CHARACTERS = Regex("[ _-]")
        private val RESERVED_ADDRESS_KEY = Regex("addressline\\d+|postcode")

        // Notify matches personalisation keys case-insensitively, ignoring spaces, underscores and hyphens
        fun isReservedAddressKey(key: String): Boolean =
            RESERVED_ADDRESS_KEY.matches(key.replace(NOTIFY_IGNORED_KEY_CHARACTERS, "").lowercase())
    }
}
