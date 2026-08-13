package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class OrganisationalLandlordDeregistrationConfirmationEmail(
    private val registrantName: String,
    private val organisationName: String,
) : EmailTemplateModel {
    private val registrantNameKey = "registrant name"
    private val organisationNameKey = "organisation name"

    override val template = EmailTemplate.ORGANISATIONAL_LANDLORD_DEREGISTRATION_CONFIRMATION_EMAIL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            registrantNameKey to registrantName,
            organisationNameKey to organisationName,
        )
}
