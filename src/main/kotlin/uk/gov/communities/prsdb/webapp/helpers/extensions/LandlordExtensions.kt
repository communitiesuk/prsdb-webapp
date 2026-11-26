package uk.gov.communities.prsdb.webapp.helpers.extensions

import uk.gov.communities.prsdb.webapp.database.entity.IndividualLandlord
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord

class LandlordExtensions {
    companion object {
        /**
         * For displaying or matching a landlord's email only, not for sending emails to: for organisations this is
         * the organisation's email, which must not be used for sending emails.
         */
        fun Landlord.getDisplayEmail(): String =
            when (this) {
                is IndividualLandlord -> email
                is OrganisationalLandlord -> wholeOrgEmail
                else -> throw IllegalArgumentException("Unknown landlord type")
            }
    }
}
