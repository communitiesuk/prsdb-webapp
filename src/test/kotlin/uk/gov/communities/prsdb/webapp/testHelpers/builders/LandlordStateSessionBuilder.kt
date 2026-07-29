package uk.gov.communities.prsdb.webapp.testHelpers.builders

import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.mockito.Mockito.mock
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.enums.CharityRegulator
import uk.gov.communities.prsdb.webapp.constants.enums.GoverningBodyMemberType
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordType
import uk.gov.communities.prsdb.webapp.constants.enums.OrgType
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.EmailStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LandlordTypeStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeDobStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeEmailStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeNameStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteePhoneStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityRegisteredWithStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompaniesHouseStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgEmailStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyDetailsStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyMemberDobStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyMemberListStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyMemberNameStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyWhoToProvideStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgMainContactStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgNameStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgPhoneNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.PhoneNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.PrivacyNoticeStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.YourDetailsStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.GovBodyMemberAddressTask
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.OrgAddressTask
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.TrusteeAddressTask
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.GoverningBodyMemberDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CharityRegisteredWithFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GoverningBodyMemberNameFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LandlordPrivacyNoticeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LandlordTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LeadTrusteeDobFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LeadTrusteeNameFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LeadTrusteePhoneFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LookupAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgCharityFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgCompaniesHouseFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgGovBodyDetailsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgGovBodyDetailsMode
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgGovBodyMemberDobFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgGovBodyWhoToProvideFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgMainContactFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgTypeFormModel
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
        val routePrefix = OrgAddressTask.ORGANISATION_ADDRESS_ROUTE_SEGMENT
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

    fun withOrgManualAddressSelected(): LandlordStateSessionBuilder {
        val routePrefix = OrgAddressTask.ORGANISATION_ADDRESS_ROUTE_SEGMENT
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
            SelectAddressFormModel().apply { address = MANUAL_ADDRESS_CHOSEN },
        )
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

    fun withOrgType(orgTypes: List<OrgType> = listOf(OrgType.COMPANY)): LandlordStateSessionBuilder {
        val formModel = OrgTypeFormModel().apply { this.orgTypes = orgTypes.map { it.name }.toMutableList() }
        withSubmittedValue(OrgTypeStep.ROUTE_SEGMENT, formModel)
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
        val routePrefix = TrusteeAddressTask.ROUTE_SEGMENT
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
        withSubmittedValue(
            OrgGovBodyMemberDobStep.ROUTE_SEGMENT,
            OrgGovBodyMemberDobFormModel().apply {
                day = "15"
                month = "6"
                year = "1980"
            },
        )
        return self()
    }

    fun withOrgGovBodyMemberLookupAddress(
        houseNameOrNumber: String = "4",
        postcode: String = "EG1 2AB",
    ): LandlordStateSessionBuilder {
        // The governing body member address is a routed instance of the shared address task, so its data is stored
        // under keys prefixed with the task route.
        val routePrefix = GovBodyMemberAddressTask.ROUTE_SEGMENT
        val address = AddressDataModel("$houseNameOrNumber Street Address, City, $postcode", localCouncilId = 22, uprn = 44)
        withAdditionalData(
            "$routePrefix/cachedAddresses",
            Json.encodeToString(serializer(), listOf(address)),
        )
        withSubmittedValue(
            "$routePrefix/${LookupAddressStep.ROUTE_SEGMENT}",
            LookupAddressFormModel().apply {
                this.houseNameOrNumber = houseNameOrNumber
                this.postcode = postcode
            },
        )
        return self()
    }

    fun withOrgGovBodyMemberAddress(
        houseNameOrNumber: String = "4",
        postcode: String = "EG1 2AB",
    ): LandlordStateSessionBuilder {
        withOrgGovBodyMemberLookupAddress(houseNameOrNumber, postcode)

        val routePrefix = GovBodyMemberAddressTask.ROUTE_SEGMENT
        val address = AddressDataModel("$houseNameOrNumber Street Address, City, $postcode", localCouncilId = 22, uprn = 44)
        withSubmittedValue(
            "$routePrefix/${SelectAddressStep.ROUTE_SEGMENT}",
            SelectAddressFormModel().apply { this.address = address.singleLineAddress },
        )
        return self()
    }

    fun withOrgGovBodyMemberList(): LandlordStateSessionBuilder {
        withSubmittedValue(OrgGovBodyMemberListStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withGoverningBodyMembers(members: Map<Int, GoverningBodyMemberDataModel>): LandlordStateSessionBuilder {
        additionalDataMap["governingBodyMembersMap"] =
            Json.encodeToString(serializer(), members)
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
        private val DEFAULT_GOVERNING_BODY_MEMBERS =
            mapOf(
                1 to
                    GoverningBodyMemberDataModel(
                        name = "Test Member",
                        type = GoverningBodyMemberType.DIRECTOR,
                        dateOfBirth = LocalDate(1970, 1, 1),
                        address = AddressDataModel(singleLineAddress = "1 Test Street, London, SW1A 1AA"),
                    ),
            )

        fun beforeName() = LandlordStateSessionBuilder().withPrivacyNotice().withIdentityNotVerified()

        fun beforeDob() = beforeName().withName()

        fun beforeLandlordType() = beforeDob().withDateOfBirth()

        fun beforeEmail() = beforeLandlordType()

        fun beforePhoneNumber() = beforeEmail().withEmail()

        fun beforeCountryOfResidence() = beforePhoneNumber().withPhoneNumber()

        fun beforeYourDetails() = beforeLandlordType().withLandlordType(LandlordType.ORGANISATION)

        fun beforeOrgName() = beforeYourDetails().withYourDetails()

        fun beforeOrgAddress() = beforeOrgName().withOrgName()

        fun beforeOrgManualAddress() = beforeOrgAddress().withOrgManualAddressSelected()

        fun beforeOrgEmail() = beforeOrgAddress().withOrgAddress()

        fun beforeOrgPhoneNumber() = beforeOrgEmail().withOrgEmail()

        fun beforeOrgType() = beforeOrgPhoneNumber().withOrgPhoneNumber()

        fun beforeLeadTrusteeName() = beforeOrgType().withOrgType(listOf(OrgType.TRUST))

        fun beforeLeadTrusteeDob() = beforeLeadTrusteeName().withLeadTrusteeName()

        fun beforeLeadTrusteeEmail() = beforeLeadTrusteeDob().withLeadTrusteeDob()

        fun beforeLeadTrusteePhone() = beforeLeadTrusteeEmail().withLeadTrusteeEmail()

        fun beforeLeadTrusteeAddress() = beforeLeadTrusteePhone().withLeadTrusteePhone()

        fun beforeOrgCharity() = beforeLeadTrusteeAddress().withLeadTrusteeAddress()

        fun beforeOrgCharityRegisteredWith() = beforeOrgCharity().withOrgCharity(registeredCharity = true)

        fun beforeOrgCharityNumberEnglandAndWales() =
            beforeOrgCharityRegisteredWith().withCharityRegisteredWith(CharityRegulator.ENGLAND_AND_WALES)

        fun beforeOrgCharityNumberNorthernIreland() =
            beforeOrgCharityRegisteredWith().withCharityRegisteredWith(CharityRegulator.NORTHERN_IRELAND)

        fun beforeOrgCharityNumberScotland() = beforeOrgCharityRegisteredWith().withCharityRegisteredWith(CharityRegulator.SCOTLAND)

        fun beforeOrgCompaniesHouse() = beforeOrgCharity().withOrgCharity(registeredCharity = false)

        fun beforeOrgCompanyNumber() = beforeOrgCompaniesHouse().withOrgCompaniesHouse(registeredWithCompaniesHouse = true)

        fun beforeOrgGovBodyDetails() = beforeOrgCompaniesHouse().withOrgCompaniesHouse(registeredWithCompaniesHouse = false)

        fun beforeOrgGovBodyMustProvideInfo() = beforeOrgGovBodyDetails().withOrgGovBodyDetails(OrgGovBodyDetailsMode.NO_DETAILS)

        fun beforeOrgGovBodyWhoToProvide() = beforeOrgGovBodyDetails().withOrgGovBodyDetails(OrgGovBodyDetailsMode.HAS_DETAILS)

        fun beforeOrgGovBodyMemberName() = beforeOrgGovBodyWhoToProvide().withOrgGovBodyWhoToProvide(GoverningBodyMemberType.DIRECTOR)

        fun beforeOrgGovBodyMemberDob() = beforeOrgGovBodyMemberName().withOrgGovBodyMemberName()

        fun beforeOrgGovBodyMemberAddress() = beforeOrgGovBodyMemberDob().withOrgGovBodyMemberDob()

        fun beforeOrgGovBodyMemberSelectAddress() = beforeOrgGovBodyMemberAddress().withOrgGovBodyMemberLookupAddress()

        fun beforeOrgGovBodyMemberList(members: Map<Int, GoverningBodyMemberDataModel> = DEFAULT_GOVERNING_BODY_MEMBERS) =
            beforeOrgGovBodyDetails()
                .withOrgGovBodyDetails(OrgGovBodyDetailsMode.HAS_DETAILS)
                .withGoverningBodyMembers(members)

        fun beforeOrgMainContact() =
            beforeOrgGovBodyMemberList()
                .withOrgGovBodyMemberList()

        fun beforeLookupAddress() = beforeCountryOfResidence().withEnglandOrWalesResidence()

        fun beforeSelectAddress() = beforeLookupAddress().withLookupAddress()

        fun beforeManualAddress() = beforeSelectAddress().withManualAddressSelected()

        fun beforeCheckAnswers() = beforeSelectAddress().withSelectedAddress()
    }
}
