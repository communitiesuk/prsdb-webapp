package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeDobStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeEmailStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeNameStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteePhoneStep
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.AddressTask

interface LeadTrusteeState : JourneyState {
    val leadTrusteeNameStep: LeadTrusteeNameStep
    val leadTrusteeDobStep: LeadTrusteeDobStep
    val leadTrusteeEmailStep: LeadTrusteeEmailStep
    val leadTrusteePhoneStep: LeadTrusteePhoneStep
    val trusteeAddressTask: AddressTask
}
