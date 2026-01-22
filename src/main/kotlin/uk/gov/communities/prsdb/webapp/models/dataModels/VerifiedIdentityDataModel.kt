package uk.gov.communities.prsdb.webapp.models.dataModels

import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import java.time.LocalDate

// TODO PRSD-1745: This won't need to be a form model after migrating to the new journey framework
data class VerifiedIdentityDataModel(
    var name: String? = null,
    var birthDate: LocalDate? = null,
) : FormModel {
    fun toMap() = mapOf("name" to name, "birthDate" to birthDate)

    companion object {
        fun fromMap(data: Map<*, *>): VerifiedIdentityDataModel =
            VerifiedIdentityDataModel(data["name"] as String?, data["birthDate"] as LocalDate?)
    }
}
