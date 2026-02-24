package uk.gov.communities.prsdb.webapp.models.dataModels

import kotlinx.serialization.Serializable
import uk.gov.communities.prsdb.webapp.helpers.LocalDateSerializer
import java.time.LocalDate
import java.io.Serializable as JavaSerializable

@Serializable
data class VerifiedIdentityDataModel(
    var name: String,
    @Serializable(with = LocalDateSerializer::class)
    var birthDate: LocalDate,
) : JavaSerializable // Required for @Serializable to be compatible with Spring's DefaultSerializer
