package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

// TODO PDJB-770: Remove this email template — the "give feedback later" flow no longer exists.
class GiveFeedbackLaterEmail : EmailTemplateModel {
    override val template = EmailTemplate.GIVE_FEEDBACK_LATER

    override fun toHashMap(): HashMap<String, String> = hashMapOf()
}
