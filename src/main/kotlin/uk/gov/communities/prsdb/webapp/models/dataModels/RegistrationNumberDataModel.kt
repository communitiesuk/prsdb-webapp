package uk.gov.communities.prsdb.webapp.models.dataModels

import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType

data class RegistrationNumberDataModel(
    val type: RegistrationNumberType,
    val number: Long,
)
