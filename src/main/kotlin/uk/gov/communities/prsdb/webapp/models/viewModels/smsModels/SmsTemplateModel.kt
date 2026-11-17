package uk.gov.communities.prsdb.webapp.models.viewModels.smsModels

interface SmsTemplateModel {
    val template: SmsTemplate

    fun toHashMap(): HashMap<String, String>
}

enum class SmsTemplate {
    PASSPHRASE_SMS,
}
