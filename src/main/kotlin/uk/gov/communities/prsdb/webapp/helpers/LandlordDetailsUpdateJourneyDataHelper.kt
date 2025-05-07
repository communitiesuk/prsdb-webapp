package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.journeys.LandlordDetailsUpdateJourney
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordDetailsUpdateStepId
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.JourneyDataExtensions.Companion.getLookedUpAddress
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.JourneyDataExtensions.Companion.getLookedUpAddresses
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.DateOfBirthFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NameFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PhoneNumberFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectAddressFormModel
import java.time.LocalDate

class LandlordDetailsUpdateJourneyDataHelper : JourneyDataHelper() {
    companion object {
        fun getIsIdentityVerified(journeyData: JourneyData): Boolean =
            getStringValueByKey(journeyData, LandlordDetailsUpdateJourney.IS_IDENTITY_VERIFIED_KEY).toBoolean()

        fun getEmailUpdateIfPresent(journeyData: JourneyData) =
            getFieldStringValue(
                journeyData,
                LandlordDetailsUpdateStepId.UpdateEmail.urlPathSegment,
                EmailFormModel::emailAddress.name,
            )

        fun getNameUpdateIfPresent(journeyData: JourneyData) =
            getFieldStringValue(
                journeyData,
                LandlordDetailsUpdateStepId.UpdateName.urlPathSegment,
                NameFormModel::name.name,
            )

        fun getPhoneNumberIfPresent(journeyData: JourneyData) =
            getFieldStringValue(
                journeyData,
                LandlordDetailsUpdateStepId.UpdatePhoneNumber.urlPathSegment,
                PhoneNumberFormModel::phoneNumber.name,
            )

        fun getAddressIfPresent(journeyData: JourneyData): AddressDataModel? {
            val selectedAddress =
                getFieldStringValue(
                    journeyData,
                    LandlordDetailsUpdateStepId.SelectEnglandAndWalesAddress.urlPathSegment,
                    SelectAddressFormModel::address.name,
                )

            return if (journeyData.getLookedUpAddresses().isEmpty() || selectedAddress == MANUAL_ADDRESS_CHOSEN) {
                getManualAddress(journeyData, LandlordDetailsUpdateStepId.ManualEnglandAndWalesAddress.urlPathSegment)
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
                    LandlordDetailsUpdateStepId.UpdateDateOfBirth.urlPathSegment,
                    DateOfBirthFormModel::day.name,
                ) ?: return null

            val month =
                getFieldIntegerValue(
                    journeyData,
                    LandlordDetailsUpdateStepId.UpdateDateOfBirth.urlPathSegment,
                    DateOfBirthFormModel::month.name,
                ) ?: return null

            val year =
                getFieldIntegerValue(
                    journeyData,
                    LandlordDetailsUpdateStepId.UpdateDateOfBirth.urlPathSegment,
                    DateOfBirthFormModel::year.name,
                ) ?: return null

            return LocalDate.of(year, month, day)
        }
    }
}
