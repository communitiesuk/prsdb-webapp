package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import uk.gov.communities.prsdb.webapp.database.entity.OrganisationLandlord

// TODO: PDJB-1276: Replace this skeleton view model with summary list rows
class OrgLandlordViewModel(
    orgLandlord: OrganisationLandlord,
) {
    val name: String = orgLandlord.name

    val singleLineAddress: String = orgLandlord.address.singleLineAddress

    val email: String = orgLandlord.wholeOrgEmail

    val phoneNumber: String = orgLandlord.phoneNumber

    val isCompany: Boolean = orgLandlord.isCompany

    val isCharity: Boolean = orgLandlord.isCharity

    val isTrust: Boolean = orgLandlord.isTrust

    val companyNumber: String? = orgLandlord.companyNumber

    val charityNumber: String? = orgLandlord.charityNumber

    val mainContactName: String = orgLandlord.mainContactName

    val mainContactEmail: String = orgLandlord.mainContactEmail

    val mainContactPhone: String = orgLandlord.mainContactPhone

    val leadTrusteeName: String? = orgLandlord.leadTrusteeName

    val leadTrusteeEmail: String? = orgLandlord.leadTrusteeEmail

    val leadTrusteePhone: String? = orgLandlord.leadTrusteePhone
}
