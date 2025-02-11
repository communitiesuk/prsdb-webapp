package uk.gov.communities.prsdb.webapp.helpers.converters

import uk.gov.communities.prsdb.webapp.constants.enums.LandlordType
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyRegistrationSection
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.forms.tasks.SectionId

class MessageKeyConverter {
    companion object {
        fun convert(boolean: Boolean): String =
            when (boolean) {
                true -> "commonText.yes"
                false -> "commonText.no"
            }

        fun convert(enum: Enum<*>): String =
            when (enum) {
                is PropertyType -> convertPropertyType(enum)
                is OwnershipType -> convertOwnershipType(enum)
                is LicensingType -> convertLicensingType(enum)
                is LandlordType -> convertLandlordType(enum)
                is PropertyRegistrationSection -> convertPropertyRegistrationSection(enum)
                else -> throw NotImplementedError(
                    "Was not able to convert Enum as ${this::class.simpleName} does not have a conversion for ${enum::class.simpleName}",
                )
            }

        fun convert(sectionId: SectionId): String =
            when (sectionId) {
                is PropertyRegistrationSection -> convertPropertyRegistrationSection(sectionId)
                else -> throw NotImplementedError(
                    "Was not able to convert SectionId as ${this::class.simpleName} " +
                        "does not have a conversion for ${sectionId::class.simpleName}",
                )
            }

        private fun convertLandlordType(landlordType: LandlordType): String =
            when (landlordType) {
                LandlordType.SOLE -> "forms.checkPropertyAnswers.propertyDetails.landlordType.sole"
                LandlordType.JOINT -> "forms.checkPropertyAnswers.propertyDetails.landlordType.joint"
                LandlordType.COMPANY -> TODO("PRSD-674 - Organisational Landlords")
            }

        private fun convertLicensingType(licensingType: LicensingType): String =
            when (licensingType) {
                LicensingType.SELECTIVE_LICENCE -> "forms.licensingType.radios.option.selectiveLicence.label"
                LicensingType.HMO_MANDATORY_LICENCE -> "forms.licensingType.radios.option.hmoMandatory.label"
                LicensingType.HMO_ADDITIONAL_LICENCE -> "forms.licensingType.radios.option.hmoAdditional.label"
                LicensingType.NO_LICENSING -> "forms.checkPropertyAnswers.propertyDetails.noLicensing"
            }

        private fun convertOwnershipType(ownershipType: OwnershipType): String =
            when (ownershipType) {
                OwnershipType.FREEHOLD -> "forms.ownershipType.radios.option.freehold.label"
                OwnershipType.LEASEHOLD -> "forms.ownershipType.radios.option.leasehold.label"
            }

        private fun convertPropertyType(propertyType: PropertyType): String =
            when (propertyType) {
                PropertyType.OTHER -> "forms.propertyType.radios.option.other.label"
                PropertyType.DETACHED_HOUSE -> "forms.propertyType.radios.option.detachedHouse.label"
                PropertyType.SEMI_DETACHED_HOUSE -> "forms.propertyType.radios.option.semiDetachedHouse.label"
                PropertyType.TERRACED_HOUSE -> "forms.propertyType.radios.option.terracedHouse.label"
                PropertyType.FLAT -> "forms.propertyType.radios.option.flat.label"
            }

        private fun convertPropertyRegistrationSection(propertyRegistrationSection: PropertyRegistrationSection): String =
            when (propertyRegistrationSection) {
                PropertyRegistrationSection.PROPERTY_DETAILS -> "registerProperty.taskList.register.heading"
                PropertyRegistrationSection.CHECK_AND_SUBMIT -> "registerProperty.taskList.checkAndSubmit.heading"
            }
    }
}
