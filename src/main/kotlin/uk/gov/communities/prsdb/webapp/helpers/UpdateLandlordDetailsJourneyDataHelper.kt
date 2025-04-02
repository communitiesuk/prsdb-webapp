package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.journeys.UpdateLandlordDetailsJourney
import uk.gov.communities.prsdb.webapp.forms.steps.UpdateLandlordDetailsStepId
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.JourneyDataExtensions.Companion.getLookedUpAddress
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.DateOfBirthFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NameFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PhoneNumberFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectAddressFormModel
import java.time.LocalDate

class UpdateLandlordDetailsJourneyDataHelper : JourneyDataHelper() {
    companion object {
        fun getIsIdentityVerified(journeyData: JourneyData): Boolean =
            getStringValueByKey(journeyData, UpdateLandlordDetailsJourney.IS_IDENTITY_VERIFIED_KEY).toBoolean()

        fun getEmailUpdateIfPresent(journeyData: JourneyData) =
            getFieldStringValue(
                journeyData,
                UpdateLandlordDetailsStepId.UpdateEmail.urlPathSegment,
                EmailFormModel::emailAddress.name,
            )

        fun getNameUpdateIfPresent(journeyData: JourneyData) =
            getFieldStringValue(
                journeyData,
                UpdateLandlordDetailsStepId.UpdateName.urlPathSegment,
                NameFormModel::name.name,
            )

        fun getPhoneNumberIfPresent(journeyData: JourneyData) =
            getFieldStringValue(
                journeyData,
                UpdateLandlordDetailsStepId.UpdatePhoneNumber.urlPathSegment,
                PhoneNumberFormModel::phoneNumber.name,
            )

        fun getAddressIfPresent(journeyData: JourneyData): AddressDataModel? {
            val selectedAddress =
                getFieldStringValue(
                    journeyData,
                    UpdateLandlordDetailsStepId.SelectEnglandAndWalesAddress.urlPathSegment,
                    SelectAddressFormModel::address.name,
                )

            return if (selectedAddress == MANUAL_ADDRESS_CHOSEN) {
                getManualAddress(journeyData, UpdateLandlordDetailsStepId.ManualEnglandAndWalesAddress.urlPathSegment)
            } else if (selectedAddress != null) {
                journeyData.getLookedUpAddress(selectedAddress)
            } else {
                null
            }
        }

        fun getDateOfBirthIfPresent(journeyData: JourneyData): LocalDate? {
            val day =
                getFieldIntegerValue(
                    journeyData,
                    UpdateLandlordDetailsStepId.UpdateDateOfBirth.urlPathSegment,
                    DateOfBirthFormModel::day.name,
                ) ?: return null

            val month =
                getFieldIntegerValue(
                    journeyData,
                    UpdateLandlordDetailsStepId.UpdateDateOfBirth.urlPathSegment,
                    DateOfBirthFormModel::month.name,
                ) ?: return null

            val year =
                getFieldIntegerValue(
                    journeyData,
                    UpdateLandlordDetailsStepId.UpdateDateOfBirth.urlPathSegment,
                    DateOfBirthFormModel::year.name,
                ) ?: return null

            return LocalDate.of(year, month, day)
        }
    }
}
