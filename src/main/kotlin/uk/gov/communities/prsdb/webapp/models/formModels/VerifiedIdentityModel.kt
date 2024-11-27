package uk.gov.communities.prsdb.webapp.models.formModels

import java.time.LocalDate

class VerifiedIdentityModel : FormModel {
    var name: String? = null
    var birthDate: LocalDate? = null

    companion object {
        const val NAME_KEY = "name"
        const val BIRTH_DATE_KEY = "birthDate"
    }
}
