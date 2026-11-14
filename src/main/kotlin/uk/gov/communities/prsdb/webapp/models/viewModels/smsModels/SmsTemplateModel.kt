package uk.gov.communities.prsdb.webapp.models.viewModels.smsModels

interface SmsTemplateModel {
    val template: SmsTemplate

    fun toHashMap(): HashMap<String, String>
}

// Template entries and their Notify IDs will be added with the first SMS notification.
enum class SmsTemplate
