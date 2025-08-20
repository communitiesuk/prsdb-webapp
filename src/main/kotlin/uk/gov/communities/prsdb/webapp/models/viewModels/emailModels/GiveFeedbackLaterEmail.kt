package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

class GiveFeedbackLaterEmail : EmailTemplateModel {
    override val template = EmailTemplate.GIVE_FEEDBACK_LATER

    override fun toHashMap(): HashMap<String, String> = hashMapOf()
}
