package uk.gov.communities.prsdb.webapp.models.requestModels.searchModels

import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType

class PropertySearchRequestModel : SearchRequestModel() {
    var restrictToLA: Boolean? = null

    var restrictToLicenses: List<LicensingType>? = null
}
