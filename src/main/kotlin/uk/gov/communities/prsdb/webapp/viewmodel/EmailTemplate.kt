package uk.gov.communities.prsdb.webapp.viewmodel

interface EmailTemplate {
    val templateId: String

    fun asHashMap(): HashMap<String, String>
}
