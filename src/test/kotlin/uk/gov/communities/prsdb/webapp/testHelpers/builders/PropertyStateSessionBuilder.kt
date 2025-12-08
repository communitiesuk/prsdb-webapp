package uk.gov.communities.prsdb.webapp.testHelpers.builders

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.mockito.Mockito.mock
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OwnershipTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyTypeFormModel
import uk.gov.communities.prsdb.webapp.services.LocalCouncilService

class PropertyStateSessionBuilder(
    override val mockLocalCouncilService: LocalCouncilService = mock(),
) : JourneyStateSessionBuilder<PropertyStateSessionBuilder>(),
    AddressStateBuilder<PropertyStateSessionBuilder>,
    LicensingStateBuilder<PropertyStateSessionBuilder>,
    OccupancyStateBuilder<PropertyStateSessionBuilder> {
    fun withIsAddressAlreadyRegistered(isRegistered: Boolean): PropertyStateSessionBuilder {
        additionalDataMap["isAddressAlreadyRegistered"] = Json.Default.encodeToString(serializer(), isRegistered)
        return this
    }

    fun withCheckedAnswers(): PropertyStateSessionBuilder {
        val checkAnswersFormModel = CheckAnswersFormModel()
        withSubmittedValue("check-answers", checkAnswersFormModel)
        return this
    }

    fun withPropertyType(
        type: PropertyType = PropertyType.DETACHED_HOUSE,
        customType: String = "type",
    ): PropertyStateSessionBuilder {
        val propertyTypeFormModel =
            PropertyTypeFormModel().apply {
                propertyType = type
                if (type == PropertyType.OTHER) {
                    customPropertyType = customType
                }
            }
        withSubmittedValue("property-type", propertyTypeFormModel)
        return this
    }

    fun withOwnershipType(ownershipType: OwnershipType = OwnershipType.FREEHOLD): PropertyStateSessionBuilder {
        val ownershipTypeFormModel =
            OwnershipTypeFormModel().apply {
                this.ownershipType = ownershipType
            }
        withSubmittedValue("ownership-type", ownershipTypeFormModel)
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
