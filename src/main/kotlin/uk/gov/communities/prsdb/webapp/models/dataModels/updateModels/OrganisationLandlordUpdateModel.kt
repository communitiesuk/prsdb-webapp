package uk.gov.communities.prsdb.webapp.models.dataModels.updateModels

import uk.gov.communities.prsdb.webapp.constants.enums.CharityRegulator
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import java.time.LocalDate

data class OrganisationLandlordUpdateModel(
    val name: String? = null,
    val address: AddressDataModel? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val isCompany: Boolean? = null,
    val isCharity: Boolean? = null,
    val isTrust: Boolean? = null,
    val isRegisteredCharity: Boolean? = null,
    val charityRegisteredWith: CharityRegulator? = null,
    val charityNumber: String? = null,
    val leadTrusteeName: String? = null,
    val leadTrusteeDateOfBirth: LocalDate? = null,
    val leadTrusteeEmail: String? = null,
    val leadTrusteePhone: String? = null,
    val leadTrusteeAddress: AddressDataModel? = null,
    val mainContactName: String? = null,
    val mainContactEmail: String? = null,
    val mainContactPhone: String? = null,
)
