package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
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
import kotlin.reflect.full.memberProperties

class PropertyRegistrationJourneyDataHelper : JourneyDataHelper() {
    companion object {
        fun getAddress(
            journeyData: JourneyData,
            lookedUpAddresses: List<AddressDataModel>,
        ): AddressDataModel? =
            if (isManualAddressChosen(journeyData)) {
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
                PropertyTypeFormModel::class.memberProperties.last().name,
            )

        fun getCustomPropertyType(journeyData: JourneyData): String? =
            getFieldStringValue(
                journeyData,
                RegisterPropertyStepId.PropertyType.urlPathSegment,
                PropertyTypeFormModel::class.memberProperties.first().name,
            )

        fun getOwnershipType(journeyData: JourneyData): OwnershipType? =
            getFieldEnumValue<OwnershipType>(
                journeyData,
                RegisterPropertyStepId.OwnershipType.urlPathSegment,
                OwnershipTypeFormModel::class.memberProperties.first().name,
            )

        fun getIsOccupied(journeyData: JourneyData): Boolean? =
            getFieldBooleanValue(
                journeyData,
                RegisterPropertyStepId.Occupancy.urlPathSegment,
                OccupancyFormModel::class.memberProperties.first().name,
            )

        fun getNumberOfHouseholds(journeyData: JourneyData): Int =
            getFieldIntegerValue(
                journeyData,
                RegisterPropertyStepId.NumberOfHouseholds.urlPathSegment,
                NumberOfHouseholdsFormModel::class.memberProperties.first().name,
            ) ?: 0

        fun getNumberOfTenants(journeyData: JourneyData): Int =
            getFieldIntegerValue(
                journeyData,
                RegisterPropertyStepId.NumberOfPeople.urlPathSegment,
                NumberOfPeopleFormModel::class.memberProperties.last().name,
            ) ?: 0

        fun getLicensingType(journeyData: JourneyData): LicensingType? =
            getFieldEnumValue<LicensingType>(
                journeyData,
                RegisterPropertyStepId.LicensingType.urlPathSegment,
                LicensingTypeFormModel::class.memberProperties.first().name,
            )

        fun getLicenseNumber(journeyData: JourneyData): String? =
            when (getLicensingType(journeyData)!!) {
                LicensingType.SELECTIVE_LICENCE ->
                    getFieldStringValue(
                        journeyData,
                        RegisterPropertyStepId.SelectiveLicence.urlPathSegment,
                        SelectiveLicenceFormModel::class.memberProperties.first().name,
                    )
                LicensingType.HMO_MANDATORY_LICENCE ->
                    getFieldStringValue(
                        journeyData,
                        RegisterPropertyStepId.HmoMandatoryLicence.urlPathSegment,
                        HmoMandatoryLicenceFormModel::class.memberProperties.first().name,
                    )
                LicensingType.HMO_ADDITIONAL_LICENCE ->
                    getFieldStringValue(
                        journeyData,
                        RegisterPropertyStepId.HmoAdditionalLicence.urlPathSegment,
                        HmoAdditionalLicenceFormModel::class.memberProperties.first().name,
                    )
                LicensingType.NO_LICENSING -> ""
            }

        private fun getSelectedAddress(journeyData: JourneyData): String? =
            getFieldStringValue(
                journeyData,
                RegisterPropertyStepId.SelectAddress.urlPathSegment,
                SelectAddressFormModel::class.memberProperties.first().name,
            )

        fun isManualAddressChosen(journeyData: JourneyData) = getSelectedAddress(journeyData) == MANUAL_ADDRESS_CHOSEN
    }
}
