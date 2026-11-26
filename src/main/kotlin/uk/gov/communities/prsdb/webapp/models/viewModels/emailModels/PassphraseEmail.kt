package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class PassphraseEmail(
    val passphrase: String,
) : EmailTemplateModel {
    override val template = EmailTemplate.PASSPHRASE_EMAIL

    override fun toHashMap(): HashMap<String, String> = hashMapOf("passphrase" to passphrase)
}
