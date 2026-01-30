package uk.gov.communities.prsdb.webapp.models.dataModels

import kotlinx.serialization.Serializable
import uk.gov.communities.prsdb.webapp.helpers.LocalDateSerializer
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import java.time.LocalDate
import java.io.Serializable as JavaSerializable

// TODO PRSD-1745: This won't need to be a form model after migrating to the new journey framework
@Serializable
data class VerifiedIdentityDataModel(
    var name: String,
    @Serializable(with = LocalDateSerializer::class)
    var birthDate: LocalDate,
) : FormModel,
    JavaSerializable // Required for @Serializable to be compatible with Spring's DefaultSerializer
