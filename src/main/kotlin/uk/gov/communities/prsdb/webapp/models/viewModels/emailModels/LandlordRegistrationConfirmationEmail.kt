package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class LandlordRegistrationConfirmationEmail(
    val lrn: String,
    val prsdURL: String,
) : EmailTemplateModel {
    override val templateId = EmailTemplateId.LANDLORD_REGISTRATION_CONFIRMATION_EMAIL

    override fun toHashMap() =
        hashMapOf(
            "PRSD URL" to prsdURL,
            "LRN" to lrn,
        )
}
