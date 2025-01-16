package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.constants.LOCAL_AUTHORITIES
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalAuthorityDataModel

class LocalAuthorityDataHelper {
    companion object {
        fun getLocalAuthorityDisplayName(custodianCode: String?): String {
            val localAuthority: LocalAuthorityDataModel = LOCAL_AUTHORITIES.single { it.custodianCode == custodianCode }
            return localAuthority.displayName
        }
    }
}
