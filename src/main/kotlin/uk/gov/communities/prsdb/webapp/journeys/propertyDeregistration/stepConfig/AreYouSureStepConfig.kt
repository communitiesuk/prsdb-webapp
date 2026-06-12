package uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.PropertyDeregistrationJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyDeregistrationAreYouSureFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import kotlin.reflect.KClass

@JourneyFrameworkComponent
class AreYouSureStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val featureFlagManager: FeatureFlagManager,
) : AbstractRequestableStepConfig<AreYouSureMode, PropertyDeregistrationAreYouSureFormModel, PropertyDeregistrationJourneyState>() {
    @Suppress("UNCHECKED_CAST")
    override val formModelClass: KClass<PropertyDeregistrationAreYouSureFormModel>
        get() =
            if (featureFlagManager.checkFeature(JOINT_LANDLORDS)) {
                NoInputFormModel::class as KClass<PropertyDeregistrationAreYouSureFormModel>
            } else {
                PropertyDeregistrationAreYouSureFormModel::class
            }

    override fun getStepSpecificContent(state: PropertyDeregistrationJourneyState) =
        if (featureFlagManager.checkFeature(JOINT_LANDLORDS)) {
            mapOf(
                "address" to getPropertySingleLineAddress(state.propertyOwnershipId),
            )
        } else {
            mapOf(
                "fieldSetHeading" to "forms.areYouSure.propertyDeregistration.fieldSetHeading",
                "radioOptions" to RadiosViewModel.yesOrNoRadios(),
                "optionalFieldSetHeadingParam" to getPropertySingleLineAddress(state.propertyOwnershipId),
            )
        }

    override fun chooseTemplate(state: PropertyDeregistrationJourneyState) =
        if (featureFlagManager.checkFeature(JOINT_LANDLORDS)) {
            "forms/deregisterPropertyInfoForm"
        } else {
            "forms/areYouSureForm"
        }

    override fun mode(state: PropertyDeregistrationJourneyState): AreYouSureMode? =
        if (featureFlagManager.checkFeature(JOINT_LANDLORDS)) {
            getFormModelFromStateOrNull(state)?.let { AreYouSureMode.WANTS_TO_PROCEED }
        } else {
            getFormModelFromStateOrNull(state)?.wantsToProceed?.let {
                if (it) AreYouSureMode.WANTS_TO_PROCEED else AreYouSureMode.DOES_NOT_WANT_TO_PROCEED
            }
        }

    private fun getPropertySingleLineAddress(propertyOwnershipId: Long): String =
        propertyOwnershipService.getPropertyOwnership(propertyOwnershipId).address.singleLineAddress
}

@JourneyFrameworkComponent
final class AreYouSureStep(
    stepConfig: AreYouSureStepConfig,
) : RequestableStep<AreYouSureMode, PropertyDeregistrationAreYouSureFormModel, PropertyDeregistrationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "are-you-sure"
    }
}

enum class AreYouSureMode {
    WANTS_TO_PROCEED,
    DOES_NOT_WANT_TO_PROCEED,
}
