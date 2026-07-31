package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class OrganisationLandlordRegistrationConfirmationEmail(
    val registrantName: String,
    val organisationName: String,
    val lrn: String,
    val prsdURL: String,
) : EmailTemplateModel {
    private val registrantNameKey = "registrant name"
    private val organisationNameKey = "organisation name"
    private val lrnKey = "LRN"
    private val prsdURLKey = "PRSD URL"

    override val template = EmailTemplate.ORGANISATION_LANDLORD_REGISTRATION_CONFIRMATION_EMAIL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            registrantNameKey to registrantName,
            organisationNameKey to organisationName,
            lrnKey to lrn,
            prsdURLKey to prsdURL,
        )
}
