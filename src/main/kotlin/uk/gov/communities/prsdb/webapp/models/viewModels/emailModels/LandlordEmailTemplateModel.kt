package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole

val adminsOnly = listOf(OrganisationalLandlordUserRole.ADMIN)
val anyOrgLandlordUser = OrganisationalLandlordUserRole.entries.toList()

interface LandlordEmailTemplateModel : EmailTemplateModel {
    val orgRolesToSendTo: List<OrganisationalLandlordUserRole>
        get() = adminsOnly
}
