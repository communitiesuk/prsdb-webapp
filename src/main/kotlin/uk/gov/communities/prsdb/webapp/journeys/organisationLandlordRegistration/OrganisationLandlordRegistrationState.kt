package uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.IndividualLandlordPlaceholderStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.LandlordTypeStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.OrgAddressStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.OrgCharityStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.OrgCompaniesHouseStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.OrgDirectorsStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.OrgEmailStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.OrgLandlordCyaStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.OrgMainContactStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.OrgNameStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.OrgPhoneNumberStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.OrgTrusteesStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.OrgTypeStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.YourDetailsStep

interface OrganisationLandlordRegistrationState : JourneyState {
    val landlordTypeStep: LandlordTypeStep
    val individualLandlordPlaceholderStep: IndividualLandlordPlaceholderStep
    val yourDetailsStep: YourDetailsStep
    val orgNameStep: OrgNameStep
    val orgAddressStep: OrgAddressStep
    val orgEmailStep: OrgEmailStep
    val orgPhoneNumberStep: OrgPhoneNumberStep
    val orgTypeStep: OrgTypeStep
    val orgCompaniesHouseStep: OrgCompaniesHouseStep
    val orgCharityStep: OrgCharityStep
    val orgDirectorsStep: OrgDirectorsStep
    val orgTrusteesStep: OrgTrusteesStep
    val orgMainContactStep: OrgMainContactStep
    val orgLandlordCyaStep: OrgLandlordCyaStep
}
