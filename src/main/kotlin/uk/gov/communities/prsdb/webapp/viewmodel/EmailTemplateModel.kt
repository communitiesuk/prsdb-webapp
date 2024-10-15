package uk.gov.communities.prsdb.webapp.viewmodel

interface EmailTemplateModel {
    val templateId: String

    fun toHashMap(): HashMap<String, String>
}
