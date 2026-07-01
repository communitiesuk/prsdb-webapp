package uk.gov.communities.prsdb.webapp.testHelpers.builders

import org.mockito.Mockito.mock
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.EmailStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LandlordTypeStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgAddressStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompaniesHouseStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgDirectorsStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgEmailStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgMainContactStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgNameStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgPhoneNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTrusteesStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.PhoneNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.PrivacyNoticeStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.YourDetailsStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LandlordPrivacyNoticeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LandlordType
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LandlordTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ManualAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgCharityFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgCompaniesHouseFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgMainContactFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PhoneNumberFormModel
import uk.gov.communities.prsdb.webapp.services.LocalCouncilService

class LandlordStateSessionBuilder(
    override val mockLocalCouncilService: LocalCouncilService = mock(),
) : JourneyStateSessionBuilder<LandlordStateSessionBuilder>(),
    IdentityStateBuilder<LandlordStateSessionBuilder>,
    AddressStateBuilder<LandlordStateSessionBuilder> {
    fun withPrivacyNotice(): LandlordStateSessionBuilder {
        val privacyNoticeFormModel =
            LandlordPrivacyNoticeFormModel().apply {
                agreesToPrivacyNotice = true
            }
        withSubmittedValue(PrivacyNoticeStep.ROUTE_SEGMENT, privacyNoticeFormModel)
        return self()
    }

    fun withEmail(email: String = "email@test.com"): LandlordStateSessionBuilder {
        val emailFormModel = EmailFormModel().apply { emailAddress = email }
        withSubmittedValue(EmailStep.ROUTE_SEGMENT, emailFormModel)
        return self()
    }

    fun withPhoneNumber(phoneNumber: String = "01234567890"): LandlordStateSessionBuilder {
        val phoneFormModel = PhoneNumberFormModel().apply { this.phoneNumber = phoneNumber }
        withSubmittedValue(PhoneNumberStep.ROUTE_SEGMENT, phoneFormModel)
        return self()
    }

    fun withLandlordType(landlordType: LandlordType): LandlordStateSessionBuilder {
        val landlordTypeFormModel = LandlordTypeFormModel().apply { this.landlordType = landlordType }
        withSubmittedValue(LandlordTypeStep.ROUTE_SEGMENT, landlordTypeFormModel)
        return self()
    }

    fun withYourDetails(): LandlordStateSessionBuilder {
        withSubmittedValue(YourDetailsStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withOrgName(): LandlordStateSessionBuilder {
        withSubmittedValue(OrgNameStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withOrgAddress(): LandlordStateSessionBuilder {
        val manualAddressFormModel =
            ManualAddressFormModel().apply {
                addressLineOne = "1 Example Street"
                townOrCity = "Exampleton"
                postcode = "EG1 2AB"
            }
        withSubmittedValue(OrgAddressStep.ROUTE_SEGMENT, manualAddressFormModel)
        return self()
    }

    fun withOrgEmail(email: String = "org@test.com"): LandlordStateSessionBuilder {
        val emailFormModel = EmailFormModel().apply { emailAddress = email }
        withSubmittedValue(OrgEmailStep.ROUTE_SEGMENT, emailFormModel)
        return self()
    }

    fun withOrgPhoneNumber(): LandlordStateSessionBuilder {
        withSubmittedValue(OrgPhoneNumberStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withOrgType(): LandlordStateSessionBuilder {
        withSubmittedValue(OrgTypeStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withOrgCompaniesHouse(registeredWithCompaniesHouse: Boolean): LandlordStateSessionBuilder {
        val formModel = OrgCompaniesHouseFormModel().apply { companiesHouse = registeredWithCompaniesHouse }
        withSubmittedValue(OrgCompaniesHouseStep.ROUTE_SEGMENT, formModel)
        return self()
    }

    fun withOrgCharity(registeredCharity: Boolean): LandlordStateSessionBuilder {
        val formModel = OrgCharityFormModel().apply { this.charity = registeredCharity }
        withSubmittedValue(OrgCharityStep.ROUTE_SEGMENT, formModel)
        return self()
    }

    fun withOrgDirectors(): LandlordStateSessionBuilder {
        withSubmittedValue(OrgDirectorsStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withOrgTrustees(): LandlordStateSessionBuilder {
        withSubmittedValue(OrgTrusteesStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withOrgMainContact(): LandlordStateSessionBuilder {
        val formModel =
            OrgMainContactFormModel().apply {
                name = "Jane Doe"
                emailAddress = "jane@example.com"
                phoneNumber = "07123456789"
            }
        withSubmittedValue(OrgMainContactStep.ROUTE_SEGMENT, formModel)
        return self()
    }

    companion object {
        fun beforeName() = LandlordStateSessionBuilder().withPrivacyNotice().withIdentityNotVerified()

        fun beforeDob() = beforeName().withName()

        fun beforeLandlordType() = beforeDob().withDateOfBirth()

        fun beforeEmail() = beforeLandlordType()

        fun beforePhoneNumber() = beforeEmail().withEmail()

        fun beforeCountryOfResidence() = beforePhoneNumber().withPhoneNumber()

        fun beforeYourDetails() = beforeLandlordType().withLandlordType(LandlordType.ORGANISATION)

        fun beforeOrgName() = beforeYourDetails().withYourDetails()

        fun beforeOrgAddress() = beforeOrgName().withOrgName()

        fun beforeOrgEmail() = beforeOrgAddress().withOrgAddress()

        fun beforeOrgPhoneNumber() = beforeOrgEmail().withOrgEmail()

        fun beforeOrgType() = beforeOrgPhoneNumber().withOrgPhoneNumber()

        fun beforeOrgCompaniesHouse() = beforeOrgType().withOrgType()

        fun beforeOrgCompanyNumber() = beforeOrgCompaniesHouse().withOrgCompaniesHouse(registeredWithCompaniesHouse = true)

        fun beforeOrgCharity() = beforeOrgCompaniesHouse().withOrgCompaniesHouse(registeredWithCompaniesHouse = false)

        fun beforeOrgCharityRegisteredWith() = beforeOrgCharity().withOrgCharity(registeredCharity = true)

        fun beforeOrgDirectors() = beforeOrgCharity().withOrgCharity(registeredCharity = false)

        fun beforeOrgTrustees() = beforeOrgDirectors().withOrgDirectors()

        fun beforeOrgMainContact() = beforeOrgTrustees().withOrgTrustees()

        fun beforeLookupAddress() = beforeCountryOfResidence().withEnglandOrWalesResidence()

        fun beforeSelectAddress() = beforeLookupAddress().withLookupAddress()

        fun beforeManualAddress() = beforeSelectAddress().withManualAddressSelected()

        fun beforeCheckAnswers() = beforeSelectAddress().withSelectedAddress()
    }
}
