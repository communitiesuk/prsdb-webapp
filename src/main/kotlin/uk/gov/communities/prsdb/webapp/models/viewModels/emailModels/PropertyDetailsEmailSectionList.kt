package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel

data class PropertyDetailsEmailSectionList(
    val propertyList: List<PropertyDetailsEmailSection>,
) {
    override fun toString() = propertyList.joinToString("--- \n") { it.toString() }

    companion object {
        fun fromPropertyOwnerships(propertyOwnerships: List<PropertyOwnership>): PropertyDetailsEmailSectionList =
            PropertyDetailsEmailSectionList(
                propertyOwnerships
                    .filter { it.isActive }
                    .withIndex()
                    .map {
                        PropertyDetailsEmailSection(
                            it.index + 1,
                            RegistrationNumberDataModel.fromRegistrationNumber(it.value.registrationNumber).toString(),
                            it.value.property.address.singleLineAddress,
                        )
                    },
            )
    }
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
