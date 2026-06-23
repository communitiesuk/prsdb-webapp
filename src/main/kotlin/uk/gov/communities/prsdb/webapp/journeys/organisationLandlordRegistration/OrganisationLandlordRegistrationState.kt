package uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.IndividualLandlordPlaceholderStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.LandlordTypeStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.OrgContactInfoStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.OrgDirectorsStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.OrgLandlordCyaStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.OrgMainContactStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.OrgTrusteesStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.OrgTypeStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.YourDetailsStep

interface OrganisationLandlordRegistrationState : JourneyState {
    val landlordTypeStep: LandlordTypeStep
    val individualLandlordPlaceholderStep: IndividualLandlordPlaceholderStep
    val yourDetailsStep: YourDetailsStep
    val orgContactInfoStep: OrgContactInfoStep
    val orgTypeStep: OrgTypeStep
    val orgDirectorsStep: OrgDirectorsStep
    val orgTrusteesStep: OrgTrusteesStep
    val orgMainContactStep: OrgMainContactStep
    val orgLandlordCyaStep: OrgLandlordCyaStep
}

