package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

class OrgGovBodyDetailsFormModel : FormModel {
    var orgGovBodyDetailsMode: String? = null
}

enum class OrgGovBodyDetailsMode {
    HAS_DETAILS,
    NO_DETAILS,
}
