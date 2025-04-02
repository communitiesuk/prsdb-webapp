package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class PropertyDetailsEmailSectionList(
    val propertyList: List<PropertyDetailsEmailSection>,
) {
    override fun toString() = propertyList.joinToString("--- \n") { it.toString() }
}

data class PropertyDetailsEmailSection(
    val propertyNumber: Int,
    val prn: String,
    val singleLineAddress: String,
) {
    override fun toString() =
        "### Property ${this.propertyNumber} \n\n" +
            "Property registration number: \n\n" +
            "^${this.prn} \n\n" +
            "Address: ${this.singleLineAddress} \n\n"
}
