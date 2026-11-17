package uk.gov.communities.prsdb.webapp.helpers.extensions

import uk.gov.communities.prsdb.webapp.database.entity.IndividualLandlord
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord

class LandlordExtensions {
    companion object {
        fun Landlord.getContactEmail(): String =
            when (this) {
                is IndividualLandlord -> email
                is OrganisationalLandlord -> wholeOrgEmail
                else -> throw IllegalArgumentException("Unknown landlord type")
            }
    }
}
