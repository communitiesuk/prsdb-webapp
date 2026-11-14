package uk.gov.communities.prsdb.webapp.local.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.exceptions.PersistentSmsSendException
import uk.gov.communities.prsdb.webapp.exceptions.TransientSmsSendException
import uk.gov.communities.prsdb.webapp.models.viewModels.smsModels.SmsTemplateModel
import uk.gov.communities.prsdb.webapp.services.SmsNotificationService

@PrsdbWebService
// The one-login-simulator profile is only active on NFT during performance runs, so no real SMS messages are sent
@Profile("(local & !use-notify) | one-login-simulator")
@Primary
class SmsNotificationStubService<SmsModel : SmsTemplateModel> : SmsNotificationService<SmsModel> {
    @Value("\${local.sms.transientExceptionPhoneNumber:}")
    var transientExceptionPhoneNumber: String? = null

    @Value("\${local.sms.persistentExceptionPhoneNumber:}")
    var persistentExceptionPhoneNumber: String? = null

    override fun sendSms(
        phoneNumber: String,
        sms: SmsModel,
    ) {
        if (phoneNumber == persistentExceptionPhoneNumber) {
            throw PersistentSmsSendException("SMS messages sent to $persistentExceptionPhoneNumber always persistently fail")
        }
        if (phoneNumber == transientExceptionPhoneNumber) {
            throw TransientSmsSendException("SMS messages sent to $transientExceptionPhoneNumber always transiently fail")
        }

        val consoleString =
            """
            *************
            STUB SMS SENT
            *************
            
            SMS sent to:     $phoneNumber
            Template used:   ${sms.template.name}
            Personalisation: ${sms.toHashMap()}
            """.trimIndent()

        println(consoleString)
    }
}
