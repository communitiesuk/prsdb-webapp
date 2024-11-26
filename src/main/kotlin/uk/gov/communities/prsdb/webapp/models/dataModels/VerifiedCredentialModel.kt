// If these unchecked casts cause parsing to fail, an exception will be thrown
@file:Suppress("UNCHECKED_CAST")

package uk.gov.communities.prsdb.webapp.models.dataModels

import uk.gov.communities.prsdb.webapp.exceptions.InvalidVerifiedCredentialsException
import uk.gov.communities.prsdb.webapp.exceptions.VerifiedCredentialParsingException
import java.time.LocalDate

class VerifiedCredentialModel(
    val type: List<String>,
    val credentialSubject: CredentialSubject,
) {
    companion object {
        fun fromUnknownMap(map: Map<*, *>?): VerifiedCredentialModel {
            try {
                return fromJsonMap(map as Map<String, Any>)
            } catch (e: Exception) {
                throw VerifiedCredentialParsingException(e)
            }
        }

        private fun fromJsonMap(jsonMap: Map<String, Any>): VerifiedCredentialModel {
            val type = jsonMap["type"] as List<String>
            val subject = CredentialSubject.fromJsonMap(jsonMap["credentialSubject"] as Map<String, Any>)
            return VerifiedCredentialModel(type, subject)
        }
    }
}

class CredentialSubject(
    val name: List<TemporalName>,
    birthDateList: List<BirthDate>,
) {
    val birthDate: LocalDate =
        birthDateList.singleOrNull()?.value ?: throw InvalidVerifiedCredentialsException("Not exactly one date of birth")

    fun getCurrentName(): String {
        val currentName = name.singleOrNull { it.validTo == null }
        if (currentName == null) {
            throw InvalidVerifiedCredentialsException("Not exactly one current name")
        } else {
            return currentName.nameParts.joinToString(" ")
        }
    }

    companion object {
        fun fromJsonMap(jsonMap: Map<String, Any>): CredentialSubject {
            val name = getListOfMaps(jsonMap, "name").map { TemporalName.fromJsonMap(it) }
            val birthDate = getListOfMaps(jsonMap, "birthDate").map { BirthDate.fromJsonMap(it) }
            return CredentialSubject(name, birthDate)
        }
    }
}

class BirthDate(
    val value: LocalDate,
) {
    companion object {
        fun fromJsonMap(jsonMap: Map<String, Any>): BirthDate {
            val valueString = jsonMap["value"] as String
            return BirthDate(LocalDate.parse(valueString))
        }
    }
}

class TemporalName(
    val nameParts: List<SingleName>,
    val validFrom: LocalDate? = null,
    val validTo: LocalDate? = null,
) {
    companion object {
        fun fromJsonMap(jsonMap: Map<String, Any>): TemporalName {
            val validFromString = jsonMap["validFrom"] as? String
            val validToString = jsonMap["validTo"] as? String
            val nameParts = getListOfMaps(jsonMap, "nameParts").map { SingleName.fromJsonMap(it) }

            return TemporalName(
                nameParts,
                validFromString?.let { LocalDate.parse(it) },
                validToString?.let { LocalDate.parse(it) },
            )
        }
    }
}

class SingleName(
    val value: String,
    val type: String,
) {
    override fun toString() = value

    companion object {
        fun fromJsonMap(jsonMap: Map<String, Any>): SingleName {
            val value = jsonMap["value"] as String
            val type = jsonMap["type"] as String
            return SingleName(value, type)
        }
    }
}

fun getListOfMaps(
    map: Map<String, Any>,
    key: String,
): List<Map<String, Any>> = map[key] as List<Map<String, Any>>
