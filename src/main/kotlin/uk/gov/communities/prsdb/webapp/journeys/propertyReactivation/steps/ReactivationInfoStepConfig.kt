package uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.PropertyReactivationJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.AddressAvailabilityService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class ReactivationInfoStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val addressAvailabilityService: AddressAvailabilityService,
) : AbstractRequestableStepConfig<ReactivationInfoMode, NoInputFormModel, PropertyReactivationJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: PropertyReactivationJourneyState) =
        mapOf(
            "addressLines" to
                propertyOwnershipService
                    .getInactivePropertyOwnershipForCurrentLandlord(state.propertyOwnershipId)
                    .address
                    .toMultiLineAddress()
                    .split("\n"),
        )

    override fun chooseTemplate(state: PropertyReactivationJourneyState) = "forms/reactivatePropertyInfoForm"

    override fun mode(state: PropertyReactivationJourneyState): ReactivationInfoMode? =
        getFormModelFromStateOrNull(state)?.let {
            if (state.isPropertyAlreadyRegistered == true) {
                ReactivationInfoMode.PROPERTY_ALREADY_REGISTERED
            } else {
                ReactivationInfoMode.PROPERTY_AVAILABLE
            }
        }

    override fun afterStepDataIsAdded(state: PropertyReactivationJourneyState) {
        val uprn =
            propertyOwnershipService
                .getInactivePropertyOwnershipForCurrentLandlord(state.propertyOwnershipId)
                .address
                .uprn
        state.isPropertyAlreadyRegistered = uprn?.let { addressAvailabilityService.isAddressOwned(it) } ?: false
    }
}

@JourneyFrameworkComponent
final class ReactivationInfoStep(
    stepConfig: ReactivationInfoStepConfig,
) : RequestableStep<ReactivationInfoMode, NoInputFormModel, PropertyReactivationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "info"
    }
}

enum class ReactivationInfoMode {
    PROPERTY_AVAILABLE,
    PROPERTY_ALREADY_REGISTERED,
}
