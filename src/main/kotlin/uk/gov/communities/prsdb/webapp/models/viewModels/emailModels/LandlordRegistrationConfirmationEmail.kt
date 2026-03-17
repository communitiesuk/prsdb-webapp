package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class LandlordRegistrationConfirmationEmail(
    val name: String,
    val lrn: String,
    val prsdURL: String,
) : EmailTemplateModel {
    override val template = EmailTemplate.LANDLORD_REGISTRATION_CONFIRMATION_EMAIL

    override fun toHashMap() =
        hashMapOf(
            "name" to name,
            "PRSD URL" to prsdURL,
            "LRN" to lrn,
        )
}
