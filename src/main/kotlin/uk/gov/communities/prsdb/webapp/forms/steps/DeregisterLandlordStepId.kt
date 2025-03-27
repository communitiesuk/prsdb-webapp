package uk.gov.communities.prsdb.webapp.forms.steps

import uk.gov.communities.prsdb.webapp.controllers.DeregisterLandlordController

enum class DeregisterLandlordStepId(
    override val urlPathSegment: String,
) : StepId {
    CheckForUserProperties(DeregisterLandlordController.CHECK_FOR_REGISTERED_PROPERTIES_PATH_SEGMENT),
    AreYouSure("are-you-sure"),
    Reason("reason"),
}
