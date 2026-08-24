package uk.gov.communities.prsdb.webapp.testHelpers.builders

import uk.gov.communities.prsdb.webapp.constants.enums.WhoProvidesRentalDetails
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.WhoProvidesRentalDetailsStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.WhoProvidesRentalDetailsFormModel

interface WhoProvidesDetailsStateBuilder<SelfType : WhoProvidesDetailsStateBuilder<SelfType>> {
    fun withSubmittedValue(
        key: String,
        value: FormModel,
    ): SelfType

    fun self(): SelfType

    fun withLandlordProvidesRentalDetails(): SelfType = withWhoProvidesRentalDetails(WhoProvidesRentalDetails.LANDLORD)

    fun withLettingAgentProvidesRentalDetails(): SelfType = withWhoProvidesRentalDetails(WhoProvidesRentalDetails.LETTING_AGENT)

    fun withWhoProvidesRentalDetails(whoProvides: WhoProvidesRentalDetails): SelfType {
        val whoProvidesRentalDetailsFormModel = WhoProvidesRentalDetailsFormModel(whoProvides = whoProvides)
        withSubmittedValue(WhoProvidesRentalDetailsStep.ROUTE_SEGMENT, whoProvidesRentalDetailsFormModel)
        return self()
    }
}
