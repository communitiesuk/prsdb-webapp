package uk.gov.communities.prsdb.webapp.models.requestModels

import uk.gov.communities.prsdb.webapp.constants.enums.FeatureFlagOverrideAction
import uk.gov.communities.prsdb.webapp.constants.enums.FeatureFlagOverrideChoice

class FeatureFlagOverrideRequestModel {
    var flags: MutableMap<String, FeatureFlagOverrideChoice> = mutableMapOf()
    var releases: MutableMap<String, FeatureFlagOverrideChoice> = mutableMapOf()
    var action: FeatureFlagOverrideAction = FeatureFlagOverrideAction.SAVE
}
