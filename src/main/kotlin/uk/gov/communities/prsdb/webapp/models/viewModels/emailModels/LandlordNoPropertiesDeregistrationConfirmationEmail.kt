package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

class LandlordNoPropertiesDeregistrationConfirmationEmail : EmailTemplateModel {
    override val template = EmailTemplate.LANDLORD_NO_PROPERTIES_DEREGISTRATION_CONFIRMATION

    override fun toHashMap(): HashMap<String, String> = hashMapOf<String, String>()
}
