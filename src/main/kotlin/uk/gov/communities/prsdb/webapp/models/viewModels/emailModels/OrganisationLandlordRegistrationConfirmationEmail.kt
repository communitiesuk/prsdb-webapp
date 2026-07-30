package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class OrganisationLandlordRegistrationConfirmationEmail(
    val registrantName: String,
    val organisationName: String,
    val lrn: String,
    val prsdURL: String,
) : EmailTemplateModel {
    override val template = EmailTemplate.ORGANISATION_LANDLORD_REGISTRATION_CONFIRMATION_EMAIL

    override fun toHashMap() =
        hashMapOf(
            "registrant name" to registrantName,
            "organisation name" to organisationName,
            "LRN" to lrn,
            "PRSD URL" to prsdURL,
        )
}
