package uk.gov.communities.prsdb.webapp.models.viewModels.formModels

import uk.gov.communities.prsdb.webapp.constants.RENTING_OUT_AN_HMO_URL

data class HMOAdditionalDetailModel(
    val paragraphTwo: String,
    val paragraphThree: String,
    val bulletPoints: List<String>? = null,
) {
    val linkUrl = RENTING_OUT_AN_HMO_URL
}
