package uk.gov.communities.prsdb.webapp.testHelpers.builders

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.mockito.Mockito.mock
import uk.gov.communities.prsdb.webapp.constants.enums.CharityRegulator
import uk.gov.communities.prsdb.webapp.constants.enums.GoverningBodyMemberType
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordType
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.EmailStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LandlordTypeStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeDobStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeEmailStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeNameStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteePhoneStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgAddressStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityRegisteredWithStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompaniesHouseStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgDirectorsStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgEmailStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyDetailsStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyMemberAddressStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyMemberDobStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyMemberListStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyMemberNameStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyWhoToProvideStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgMainContactStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgNameStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgPhoneNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTrusteesStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.PhoneNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.PrivacyNoticeStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.YourDetailsStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.TrusteeAddressTask
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CharityRegisteredWithFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GoverningBodyMemberNameFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LandlordPrivacyNoticeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LandlordTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LeadTrusteeDobFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LeadTrusteeNameFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LeadTrusteePhoneFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LookupAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ManualAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgCharityFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgCompaniesHouseFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgGovBodyDetailsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgGovBodyDetailsMode
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgGovBodyWhoToProvideFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgMainContactFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PhoneNumberFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectAddressFormModel
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

    fun withCharityRegisteredWith(regulator: CharityRegulator): LandlordStateSessionBuilder {
        val formModel = CharityRegisteredWithFormModel().apply { this.charityRegisteredWith = regulator }
        withSubmittedValue(OrgCharityRegisteredWithStep.ROUTE_SEGMENT, formModel)
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

    fun withLeadTrusteeName(name: String = "Lead Trustee"): LandlordStateSessionBuilder {
        val leadTrusteeNameFormModel = LeadTrusteeNameFormModel().apply { this.name = name }
        withSubmittedValue(LeadTrusteeNameStep.ROUTE_SEGMENT, leadTrusteeNameFormModel)
        return self()
    }

    fun withLeadTrusteeEmail(email: String = "trustee@test.com"): LandlordStateSessionBuilder {
        val emailFormModel = EmailFormModel().apply { emailAddress = email }
        withSubmittedValue(LeadTrusteeEmailStep.ROUTE_SEGMENT, emailFormModel)
        return self()
    }

    fun withLeadTrusteePhone(): LandlordStateSessionBuilder {
        withSubmittedValue(LeadTrusteePhoneStep.ROUTE_SEGMENT, LeadTrusteePhoneFormModel().apply { phoneNumber = "07123456789" })
        return self()
    }

    fun withLeadTrusteeDob(): LandlordStateSessionBuilder {
        withSubmittedValue(
            LeadTrusteeDobStep.ROUTE_SEGMENT,
            LeadTrusteeDobFormModel().apply {
                day = "15"
                month = "6"
                year = "1980"
            },
        )
        return self()
    }

    fun withLeadTrusteeAddress(): LandlordStateSessionBuilder {
        // The lead trustee address is a routed instance of the shared address task, so its data is stored under
        // keys prefixed with the task route. Provide a full "found and selected an address" path so the task is
        // complete and the journey can proceed past it.
        val routePrefix = TrusteeAddressTask.LEAD_TRUSTEE_ADDRESS_ROUTE_SEGMENT
        val singleLineAddress = "1 Example Street, Exampleton, EG1 2AB"
        withAdditionalData(
            "$routePrefix/cachedAddresses",
            Json.encodeToString(serializer(), listOf(AddressDataModel(singleLineAddress, localCouncilId = 22, uprn = 44))),
        )
        withSubmittedValue(
            "$routePrefix/${LookupAddressStep.ROUTE_SEGMENT}",
            LookupAddressFormModel().apply {
                postcode = "EG1 2AB"
                houseNameOrNumber = "1"
            },
        )
        withSubmittedValue(
            "$routePrefix/${SelectAddressStep.ROUTE_SEGMENT}",
            SelectAddressFormModel().apply { address = singleLineAddress },
        )
        return self()
    }

    fun withOrgGovBodyDetails(mode: OrgGovBodyDetailsMode): LandlordStateSessionBuilder {
        val formModel = OrgGovBodyDetailsFormModel().apply { orgGovBodyDetailsMode = mode.name }
        withSubmittedValue(OrgGovBodyDetailsStep.ROUTE_SEGMENT, formModel)
        return self()
    }

    fun withOrgGovBodyWhoToProvide(option: GoverningBodyMemberType): LandlordStateSessionBuilder {
        val formModel = OrgGovBodyWhoToProvideFormModel()
        formModel.whoToProvide = option
        withSubmittedValue(OrgGovBodyWhoToProvideStep.ROUTE_SEGMENT, formModel)
        return self()
    }

    fun withOrgGovBodyMemberName(name: String = "Governing Body Member"): LandlordStateSessionBuilder {
        val formModel = GoverningBodyMemberNameFormModel().apply { this.name = name }
        withSubmittedValue(OrgGovBodyMemberNameStep.ROUTE_SEGMENT, formModel)
        return self()
    }

    fun withOrgGovBodyMemberDob(): LandlordStateSessionBuilder {
        withSubmittedValue(OrgGovBodyMemberDobStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withOrgGovBodyMemberAddress(): LandlordStateSessionBuilder {
        withSubmittedValue(OrgGovBodyMemberAddressStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withOrgGovBodyMemberList(): LandlordStateSessionBuilder {
        withSubmittedValue(OrgGovBodyMemberListStep.ROUTE_SEGMENT, NoInputFormModel())
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

        fun beforeOrgCharityNumberEnglandAndWales() =
            beforeOrgCharityRegisteredWith().withCharityRegisteredWith(CharityRegulator.ENGLAND_AND_WALES)

        fun beforeOrgCharityNumberNorthernIreland() =
            beforeOrgCharityRegisteredWith().withCharityRegisteredWith(CharityRegulator.NORTHERN_IRELAND)

        fun beforeOrgCharityNumberScotland() = beforeOrgCharityRegisteredWith().withCharityRegisteredWith(CharityRegulator.SCOTLAND)

        fun beforeOrgDirectors() = beforeOrgCharity().withOrgCharity(registeredCharity = false)

        fun beforeOrgTrustees() = beforeOrgDirectors().withOrgDirectors()

        fun beforeLeadTrusteeName() = beforeOrgTrustees().withOrgTrustees()

        fun beforeLeadTrusteeEmail() = beforeLeadTrusteeName().withLeadTrusteeName()

        fun beforeLeadTrusteePhone() = beforeLeadTrusteeEmail().withLeadTrusteeEmail()

        fun beforeLeadTrusteeDob() = beforeLeadTrusteePhone().withLeadTrusteePhone()

        fun beforeLeadTrusteeAddress() = beforeLeadTrusteeDob().withLeadTrusteeDob()

        fun beforeOrgGovBodyDetails() = beforeLeadTrusteeAddress().withLeadTrusteeAddress()

        fun beforeOrgGovBodyMustProvideInfo() = beforeOrgGovBodyDetails().withOrgGovBodyDetails(OrgGovBodyDetailsMode.NO_DETAILS)

        fun beforeOrgGovBodyWhoToProvide() = beforeOrgGovBodyDetails().withOrgGovBodyDetails(OrgGovBodyDetailsMode.HAS_DETAILS)

        fun beforeOrgGovBodyMemberName() = beforeOrgGovBodyWhoToProvide().withOrgGovBodyWhoToProvide(GoverningBodyMemberType.DIRECTOR)

        fun beforeOrgGovBodyMemberDob() = beforeOrgGovBodyMemberName().withOrgGovBodyMemberName()

        fun beforeOrgGovBodyMemberAddress() = beforeOrgGovBodyMemberDob().withOrgGovBodyMemberDob()

        fun beforeOrgGovBodyMemberList() = beforeOrgGovBodyMemberAddress().withOrgGovBodyMemberAddress()

        fun beforeOrgMainContact() = beforeOrgGovBodyMemberList().withOrgGovBodyMemberList()

        fun beforeLookupAddress() = beforeCountryOfResidence().withEnglandOrWalesResidence()

        fun beforeSelectAddress() = beforeLookupAddress().withLookupAddress()

        fun beforeManualAddress() = beforeSelectAddress().withManualAddressSelected()

        fun beforeCheckAnswers() = beforeSelectAddress().withSelectedAddress()
    }
}
