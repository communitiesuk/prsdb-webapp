package uk.gov.communities.prsdb.webapp.testHelpers.builders

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import uk.gov.communities.prsdb.webapp.constants.enums.WhoProvidesRentalDetails
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LettingAgentEmailStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.WhoProvidesRentalDetailsStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.WhoProvidesRentalDetailsFormModel

interface WhoProvidesDetailsStateBuilder<SelfType : WhoProvidesDetailsStateBuilder<SelfType>> {
    val additionalDataMap: MutableMap<String, String>

    fun withSubmittedValue(
        key: String,
        value: FormModel,
    ): SelfType

    fun self(): SelfType

    fun withLandlordProvidesRentalDetails(): SelfType = withWhoProvidesRentalDetails(WhoProvidesRentalDetails.LANDLORD)

    fun withLettingAgentProvidesRentalDetails(): SelfType = withWhoProvidesRentalDetails(WhoProvidesRentalDetails.LETTING_AGENT)

    fun withDelegatedToLettingAgent(): SelfType {
        withLettingAgentProvidesRentalDetails()
        return withLettingAgentEmail()
    }

    fun withLettingAgentEmail(): SelfType {
        withSubmittedValue(LettingAgentEmailStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withWhoProvidesRentalDetails(whoProvides: WhoProvidesRentalDetails): SelfType {
        val whoProvidesRentalDetailsFormModel = WhoProvidesRentalDetailsFormModel(whoProvides = whoProvides)
        withSubmittedValue(WhoProvidesRentalDetailsStep.ROUTE_SEGMENT, whoProvidesRentalDetailsFormModel)
        additionalDataMap["cachedWhoProvidesRentalDetails"] = Json.encodeToString(serializer(), whoProvides)
        return self()
    }
}
