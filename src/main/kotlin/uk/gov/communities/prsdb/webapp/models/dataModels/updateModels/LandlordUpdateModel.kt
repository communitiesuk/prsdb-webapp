package uk.gov.communities.prsdb.webapp.models.dataModels.updateModels

import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import java.time.LocalDate

data class LandlordUpdateModel(
    val email: String? = null,
    val name: String? = null,
    val phoneNumber: String? = null,
    val address: AddressDataModel? = null,
    val dateOfBirth: LocalDate? = null,
)
