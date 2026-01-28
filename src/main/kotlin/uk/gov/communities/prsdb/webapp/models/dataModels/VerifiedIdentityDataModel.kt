package uk.gov.communities.prsdb.webapp.models.dataModels

import kotlinx.serialization.Serializable
import uk.gov.communities.prsdb.webapp.helpers.LocalDateAsStringSerializer
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import java.time.LocalDate

// TODO PRSD-1745: This won't need to be a form model after migrating to the new journey framework
@Serializable
data class VerifiedIdentityDataModel(
    var name: String? = null,
    @Serializable(with = LocalDateAsStringSerializer::class)
    var birthDate: LocalDate? = null,
) : FormModel,
    java.io.Serializable // Required for @Serializable to be compatible with Spring's DefaultSerializer
