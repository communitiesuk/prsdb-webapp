package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.services.AddressDataService
import java.time.LocalDate

object LandlordRegistrationJourneyDataExtensions {
    fun JourneyData.getName() = getVerifiedName() ?: getManualName()

    private fun JourneyData.getManualName() =
        getFieldStringValue(
            LandlordRegistrationStepId.Name.urlPathSegment,
            "name",
        )

    fun JourneyData.getVerifiedName() =
        getFieldStringValue(
            LandlordRegistrationStepId.VerifyIdentity.urlPathSegment,
            "name",
        )

    fun JourneyData.getDOB() = getVerifiedDOB() ?: getManualDOB()

    fun JourneyData.getVerifiedDOB() =
        getFieldLocalDateValue(
            LandlordRegistrationStepId.VerifyIdentity.urlPathSegment,
            "birthDate",
        )

    private fun JourneyData.getManualDOB(): LocalDate? {
        val day =
            getFieldIntegerValue(
                LandlordRegistrationStepId.DateOfBirth.urlPathSegment,
                "day",
            ) ?: return null

        val month =
            getFieldIntegerValue(
                LandlordRegistrationStepId.DateOfBirth.urlPathSegment,
                "month",
            ) ?: return null

        val year =
            getFieldIntegerValue(
                LandlordRegistrationStepId.DateOfBirth.urlPathSegment,
                "year",
            ) ?: return null

        return LocalDate.of(year, month, day)
    }

    fun JourneyData.getEmail() =
        getFieldStringValue(
            LandlordRegistrationStepId.Email.urlPathSegment,
            "emailAddress",
        )

    fun JourneyData.getPhoneNumber() =
        getFieldStringValue(
            LandlordRegistrationStepId.PhoneNumber.urlPathSegment,
            "phoneNumber",
        )

    fun JourneyData.getLivesInUK() =
        getFieldBooleanValue(
            LandlordRegistrationStepId.CountryOfResidence.urlPathSegment,
            "livesInUK",
        )

    fun JourneyData.getNonUKCountryOfResidence() =
        if (getLivesInUK() == true) {
            null
        } else {
            getFieldStringValue(
                LandlordRegistrationStepId.CountryOfResidence.urlPathSegment,
                "countryOfResidence",
            )
        }

    fun JourneyData.getAddress(addressDataService: AddressDataService): AddressDataModel? {
        val livesInUK = getLivesInUK() ?: return null

        return if (isManualAddressChosen(!livesInUK)) {
            getManualAddress(!livesInUK)
        } else {
            val selectedAddress = getSelectedAddress(!livesInUK) ?: return null
            addressDataService.getAddressData(selectedAddress)
        }
    }

    private fun JourneyData.getSelectedAddress(isContactAddress: Boolean = false): String? {
        val selectAddressPathSegment =
            if (isContactAddress) {
                LandlordRegistrationStepId.SelectContactAddress.urlPathSegment
            } else {
                LandlordRegistrationStepId.SelectAddress.urlPathSegment
            }

        return getFieldStringValue(
            selectAddressPathSegment,
            "address",
        )
    }

    private fun JourneyData.getManualAddress(isContactAddress: Boolean = false): AddressDataModel? {
        val manualAddressPathSegment =
            if (isContactAddress) {
                LandlordRegistrationStepId.ManualContactAddress.urlPathSegment
            } else {
                LandlordRegistrationStepId.ManualAddress.urlPathSegment
            }

        return getManualAddress(manualAddressPathSegment)
    }

    fun JourneyData.getInternationalAddress() =
        getFieldStringValue(
            LandlordRegistrationStepId.InternationalAddress.urlPathSegment,
            "internationalAddress",
        )

    fun JourneyData.isIdentityVerified() =
        getVerifiedName() != null &&
            getVerifiedDOB() != null

    fun JourneyData.isManualAddressChosen(isContactAddress: Boolean = false) = getSelectedAddress(isContactAddress) == MANUAL_ADDRESS_CHOSEN
}
