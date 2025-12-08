package uk.gov.communities.prsdb.webapp.testHelpers.builders

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel

class PropertyStateSessionBuilder : JourneyStateSessionBuilder<PropertyStateSessionBuilder>() {
    fun withIsAddressAlreadyRegistered(isRegistered: Boolean): PropertyStateSessionBuilder {
        additionalDataMap["isAddressAlreadyRegistered"] = Json.Default.encodeToString(serializer(), isRegistered)
        return this
    }

    companion object {
        fun beforePropertyRegistrationSelectAddress(customLookedUpAddresses: List<AddressDataModel>? = null) =
            if (customLookedUpAddresses != null) {
                PropertyStateSessionBuilder()
                    .withLookupAddress()
                    .withCachedAddresses(customLookedUpAddresses)
            } else {
                PropertyStateSessionBuilder().withLookupAddress()
            }

        fun beforePropertyRegistrationManualAddress() = beforePropertyRegistrationSelectAddress().withManualAddressSelected()

        fun beforePropertyRegistrationSelectLocalCouncil() = beforePropertyRegistrationManualAddress().withManualAddress()

        fun beforePropertyRegistrationPropertyType() = PropertyStateSessionBuilder().withLookupAddress().withSelectedAddress()

        fun beforePropertyRegistrationOwnershipType() = beforePropertyRegistrationPropertyType().withPropertyType()

        fun beforePropertyRegistrationLicensingType() = beforePropertyRegistrationOwnershipType().withOwnershipType()

        fun beforePropertyRegistrationSelectiveLicence() =
            beforePropertyRegistrationLicensingType().withLicensingType(LicensingType.SELECTIVE_LICENCE)

        fun beforePropertyRegistrationHmoMandatoryLicence() =
            beforePropertyRegistrationLicensingType().withLicensingType(LicensingType.HMO_MANDATORY_LICENCE)

        fun beforePropertyRegistrationHmoAdditionalLicence() =
            beforePropertyRegistrationLicensingType().withLicensingType(LicensingType.HMO_ADDITIONAL_LICENCE)

        fun beforePropertyRegistrationOccupancy() = beforePropertyRegistrationLicensingType().withLicensingType(LicensingType.NO_LICENSING)

        fun beforePropertyRegistrationHouseholds() = beforePropertyRegistrationOccupancy().withOccupancyStatus(true)

        fun beforePropertyRegistrationPeople() = beforePropertyRegistrationHouseholds().withHouseholds()

        fun beforePropertyRegistrationCheckAnswers() = beforePropertyRegistrationOccupancy().withOccupancyStatus(false)

        fun beforePropertyRegistrationDeclaration() = beforePropertyRegistrationCheckAnswers().withCheckedAnswers()
    }
}
