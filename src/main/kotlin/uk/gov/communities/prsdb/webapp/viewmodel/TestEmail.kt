package uk.gov.communities.prsdb.webapp.viewmodel

data class TestEmail(
    var firstName: String,
) {
    private val firstNameName = "first name"

    val templateId = "71551da6-f616-45c7-a4e0-23d6a8434561"

    fun asHashMap(): HashMap<String, String> = hashMapOf(firstNameName to firstName)
}
