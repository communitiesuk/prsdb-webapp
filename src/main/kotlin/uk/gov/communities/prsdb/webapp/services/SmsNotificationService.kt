package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.exceptions.PersistentSmsSendException
import uk.gov.communities.prsdb.webapp.exceptions.TransientSmsSendException
import uk.gov.communities.prsdb.webapp.models.viewModels.smsModels.SmsTemplateModel

interface SmsNotificationService<in SmsModel : SmsTemplateModel> {
    /**
     * Submits an SMS; successful return means Notify accepted it, not that it was delivered.
     *
     * @throws TransientSmsSendException if the SMS was not sent and retrying later may succeed.
     * @throws PersistentSmsSendException if retrying will not help, or it is unknown whether the SMS was sent.
     */
    fun sendSms(
        phoneNumber: String,
        sms: SmsModel,
    )
}
