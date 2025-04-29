package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.JourneyDataExtensions.Companion.getLookedUpAddresses
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HmoAdditionalLicenceFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HmoMandatoryLicenceFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LicensingTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OccupancyFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OwnershipTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectiveLicenceFormModel

class PropertyRegistrationJourneyDataHelper : JourneyDataHelper() {
    companion object {
        fun getAddress(
            journeyData: JourneyData,
            lookedUpAddresses: List<AddressDataModel>,
        ): AddressDataModel? =
            if (isManualAddressChosen(journeyData, lookedUpAddresses)) {
                getManualAddress(
                    journeyData,
                    RegisterPropertyStepId.ManualAddress.urlPathSegment,
                    RegisterPropertyStepId.LocalAuthority.urlPathSegment,
                )
            } else {
                val selectedAddress = getSelectedAddress(journeyData)
                lookedUpAddresses.singleOrNull { it.singleLineAddress == selectedAddress }
            }

        fun getPropertyType(journeyData: JourneyData): PropertyType? =
            getFieldEnumValue<PropertyType>(
                journeyData,
                RegisterPropertyStepId.PropertyType.urlPathSegment,
                PropertyTypeFormModel::propertyType.name,
            )

        fun getCustomPropertyType(journeyData: JourneyData): String? =
            getFieldStringValue(
                journeyData,
                RegisterPropertyStepId.PropertyType.urlPathSegment,
                PropertyTypeFormModel::customPropertyType.name,
            )

        fun getOwnershipType(journeyData: JourneyData): OwnershipType? =
            getFieldEnumValue<OwnershipType>(
                journeyData,
                RegisterPropertyStepId.OwnershipType.urlPathSegment,
                OwnershipTypeFormModel::ownershipType.name,
            )

        fun getIsOccupied(journeyData: JourneyData): Boolean? =
            getFieldBooleanValue(
                journeyData,
                RegisterPropertyStepId.Occupancy.urlPathSegment,
                OccupancyFormModel::occupied.name,
            )

        fun getNumberOfHouseholds(journeyData: JourneyData): Int =
            getFieldIntegerValue(
                journeyData,
                RegisterPropertyStepId.NumberOfHouseholds.urlPathSegment,
                NumberOfHouseholdsFormModel::numberOfHouseholds.name,
            ) ?: 0

        fun getNumberOfTenants(journeyData: JourneyData): Int =
            getFieldIntegerValue(
                journeyData,
                RegisterPropertyStepId.NumberOfPeople.urlPathSegment,
                NumberOfPeopleFormModel::numberOfPeople.name,
            ) ?: 0

        fun getLicensingType(journeyData: JourneyData): LicensingType? =
            getFieldEnumValue<LicensingType>(
                journeyData,
                RegisterPropertyStepId.LicensingType.urlPathSegment,
                LicensingTypeFormModel::licensingType.name,
            )

        fun getLicenseNumber(journeyData: JourneyData): String? {
            val (stepId, fieldName) =
                when (getLicensingType(journeyData)!!) {
                    LicensingType.SELECTIVE_LICENCE ->
                        Pair(RegisterPropertyStepId.SelectiveLicence, SelectiveLicenceFormModel::licenceNumber.name)
                    LicensingType.HMO_MANDATORY_LICENCE ->
                        Pair(RegisterPropertyStepId.HmoMandatoryLicence, HmoMandatoryLicenceFormModel::licenceNumber.name)
                    LicensingType.HMO_ADDITIONAL_LICENCE ->
                        Pair(RegisterPropertyStepId.HmoAdditionalLicence, HmoAdditionalLicenceFormModel::licenceNumber.name)
                    LicensingType.NO_LICENSING -> return ""
                }

            return getFieldStringValue(journeyData, stepId.urlPathSegment, fieldName)
        }

        private fun getSelectedAddress(journeyData: JourneyData): String? =
            getFieldStringValue(
                journeyData,
                RegisterPropertyStepId.SelectAddress.urlPathSegment,
                SelectAddressFormModel::address.name,
            )

        fun isManualAddressChosen(
            journeyData: JourneyData,
            lookedUpAddresses: List<AddressDataModel> = journeyData.getLookedUpAddresses(),
        ): Boolean = lookedUpAddresses.isEmpty() || getSelectedAddress(journeyData) == MANUAL_ADDRESS_CHOSEN
    }
}
