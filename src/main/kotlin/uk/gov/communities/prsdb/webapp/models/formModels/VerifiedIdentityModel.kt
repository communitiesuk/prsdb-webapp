package uk.gov.communities.prsdb.webapp.models.formModels

import java.time.LocalDate

class VerifiedIdentityModel : FormModel {
    var name: String? = null
    var birthDate: LocalDate? = null
}
