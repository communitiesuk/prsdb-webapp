package uk.gov.communities.prsdb.webapp.testHelpers.builders

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps.ReactivationInfoStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

class PropertyReactivationStateSessionBuilder : JourneyStateSessionBuilder<PropertyReactivationStateSessionBuilder>() {
    fun withInfoCompleted(): PropertyReactivationStateSessionBuilder {
        withSubmittedValue(ReactivationInfoStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withIsPropertyAlreadyRegistered(isRegistered: Boolean): PropertyReactivationStateSessionBuilder {
        withAdditionalData("isPropertyAlreadyRegistered", Json.Default.encodeToString(serializer(), isRegistered))
        return self()
    }
}
