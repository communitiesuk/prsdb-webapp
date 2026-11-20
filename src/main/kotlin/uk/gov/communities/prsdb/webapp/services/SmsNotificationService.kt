package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.exceptions.SmsSendException
import uk.gov.communities.prsdb.webapp.models.viewModels.smsModels.SmsTemplateModel

interface SmsNotificationService<in SmsModel : SmsTemplateModel> {
    /**
     * Submits an SMS; successful return means Notify accepted it, not that it was delivered.
     *
     * @throws SmsSendException with a reason for rejection, or UNKNOWN if acceptance cannot be confirmed.
     */
    fun sendSms(
        phoneNumber: String,
        sms: SmsModel,
    )
}
