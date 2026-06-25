package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.ORGANISATION_LANDLORD_REGISTRATION
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.InternalStep

enum class OrgLandlordFeatureGateMode { ENABLED, DISABLED }

@JourneyFrameworkComponent
class OrgLandlordFeatureGateStepConfig(
    private val featureFlagManager: FeatureFlagManager,
) : AbstractInternalStepConfig<OrgLandlordFeatureGateMode, JourneyState>() {
    override fun mode(state: JourneyState) =
        if (featureFlagManager.checkFeature(ORGANISATION_LANDLORD_REGISTRATION)) {
            OrgLandlordFeatureGateMode.ENABLED
        } else {
            OrgLandlordFeatureGateMode.DISABLED
        }
}

@JourneyFrameworkComponent
class OrgLandlordFeatureGateStep(
    stepConfig: OrgLandlordFeatureGateStepConfig,
) : InternalStep<OrgLandlordFeatureGateMode, JourneyState>(stepConfig)
