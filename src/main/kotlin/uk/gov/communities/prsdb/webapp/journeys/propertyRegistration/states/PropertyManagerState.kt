package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckPropertyManagerStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyManagerDetailsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyManagerStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemovePropertyManagerStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyManagerDataModel

interface PropertyManagerState : JourneyState {
    val propertyManagerStep: PropertyManagerStep
    val propertyManagerDetailsStep: PropertyManagerDetailsStep
    val checkPropertyManagerStep: CheckPropertyManagerStep
    val removePropertyManagerStep: RemovePropertyManagerStep

    fun getPropertyManagerOrNull(): PropertyManagerDataModel? =
        if (propertyManagerStep.outcome == YesOrNo.YES) {
            propertyManagerDetailsStep.formModelIfReachableOrNull?.let { PropertyManagerDataModel.fromFormData(it) }
        } else {
            null
        }
}
