package uk.gov.communities.prsdb.webapp.models.viewModels.smsModels

data class PassphraseSms(
    val passphrase: String,
) : SmsTemplateModel {
    override val template = SmsTemplate.PASSPHRASE_SMS

    override fun toHashMap(): HashMap<String, String> = hashMapOf("passphrase" to passphrase)
}
