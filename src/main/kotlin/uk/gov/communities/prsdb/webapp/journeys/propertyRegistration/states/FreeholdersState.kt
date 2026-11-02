package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholderContactTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholderEmailStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholderNameStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholderProvideDetailsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholderRemoveConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdersInfoStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdersListStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.FreeholderAddressTask

interface FreeholdersState : JourneyState {
    val freeholdersInfoStep: FreeholdersInfoStep
    val freeholderProvideDetailsStep: FreeholderProvideDetailsStep
    val freeholderNameStep: FreeholderNameStep
    val freeholderContactTypeStep: FreeholderContactTypeStep
    val freeholderEmailStep: FreeholderEmailStep
    val freeholderAddressTask: FreeholderAddressTask
    val freeholdersListStep: FreeholdersListStep
    val removeFreeholderConfirmationStep: FreeholderRemoveConfirmationStep
}
