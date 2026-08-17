package uk.gov.communities.prsdb.webapp.testHelpers.builders

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeDobStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeEmailStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeNameStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteePhoneStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationType.OrgTypeTrustInterruptionStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.TrusteeAddressTask
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LeadTrusteeDobFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LeadTrusteeEmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LeadTrusteeNameFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LeadTrusteePhoneFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LookupAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectAddressFormModel

interface LeadTrusteeStateBuilder<SelfType : LeadTrusteeStateBuilder<SelfType>> {
    val additionalDataMap: MutableMap<String, String>

    fun withSubmittedValue(
        key: String,
        value: FormModel,
    ): SelfType

    fun self(): SelfType

    fun withTrustInterruption(): SelfType {
        withSubmittedValue(OrgTypeTrustInterruptionStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withLeadTrusteeName(name: String = "Lead Trustee"): SelfType {
        withSubmittedValue(LeadTrusteeNameStep.ROUTE_SEGMENT, LeadTrusteeNameFormModel().apply { this.name = name })
        return self()
    }

    fun withLeadTrusteeDob(
        day: String = "15",
        month: String = "6",
        year: String = "1980",
    ): SelfType {
        withSubmittedValue(
            LeadTrusteeDobStep.ROUTE_SEGMENT,
            LeadTrusteeDobFormModel().apply {
                this.day = day
                this.month = month
                this.year = year
            },
        )
        return self()
    }

    fun withLeadTrusteeEmail(email: String = "trustee@test.com"): SelfType {
        withSubmittedValue(LeadTrusteeEmailStep.ROUTE_SEGMENT, LeadTrusteeEmailFormModel().apply { emailAddress = email })
        return self()
    }

    fun withLeadTrusteePhone(phone: String = "07123456789"): SelfType {
        withSubmittedValue(LeadTrusteePhoneStep.ROUTE_SEGMENT, LeadTrusteePhoneFormModel().apply { phoneNumber = phone })
        return self()
    }

    fun withLeadTrusteeAddress(singleLineAddress: String = "1 Example Street, Exampleton, EG1 2AB"): SelfType {
        val routePrefix = TrusteeAddressTask.ROUTE_SEGMENT
        additionalDataMap["$routePrefix/cachedAddresses"] =
            Json.encodeToString(serializer(), listOf(AddressDataModel(singleLineAddress, localCouncilId = 22, uprn = 44)))
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

    fun withLeadTrusteeDetails(
        name: String = "Lead Trustee",
        email: String = "trustee@test.com",
        phone: String = "07123456789",
    ): SelfType {
        withTrustInterruption()
        withLeadTrusteeName(name)
        withLeadTrusteeDob()
        withLeadTrusteeEmail(email)
        withLeadTrusteePhone(phone)
        withLeadTrusteeAddress()
        return self()
    }
}
