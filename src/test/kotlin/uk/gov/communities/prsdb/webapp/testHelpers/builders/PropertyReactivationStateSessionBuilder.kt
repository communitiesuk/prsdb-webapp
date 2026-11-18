package uk.gov.communities.prsdb.webapp.testHelpers.builders

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps.ReactivationInfoStep
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps.ReactivationLicensingStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

class PropertyReactivationStateSessionBuilder :
    JourneyStateSessionBuilder<PropertyReactivationStateSessionBuilder>(),
    GasSafetyStateBuilder<PropertyReactivationStateSessionBuilder>,
    ElectricalSafetyStateBuilder<PropertyReactivationStateSessionBuilder>,
    EpcStateBuilder<PropertyReactivationStateSessionBuilder>,
    OccupancyStateBuilder<PropertyReactivationStateSessionBuilder> {
    fun withInfoCompleted(): PropertyReactivationStateSessionBuilder {
        withSubmittedValue(ReactivationInfoStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withIsPropertyAlreadyRegistered(isRegistered: Boolean): PropertyReactivationStateSessionBuilder {
        withAdditionalData("isPropertyAlreadyRegistered", Json.Default.encodeToString(serializer(), isRegistered))
        return self()
    }

    // TODO PDJB-2042: use the licensing state builder once the licensing task (PDJB-2037) is merged
    fun withLicensingCompleted(): PropertyReactivationStateSessionBuilder {
        withSubmittedValue(ReactivationLicensingStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    companion object {
        fun beforeCheckAnswers() =
            PropertyReactivationStateSessionBuilder()
                .withInfoCompleted()
                .withIsPropertyAlreadyRegistered(false)
                .withLicensingCompleted()
                .withGasSafetyTaskCompletedWithNoGasSupply()
                .withElectricalSafetyCertificateMissing()
                .withCompliantEpc()
                .withHouseholds(2)
                .withPeople(4)
                .withRentIncludesBills(false)
                .withFurnished()
                .withRentFrequency()
                .withRentAmount("400")
    }
}
