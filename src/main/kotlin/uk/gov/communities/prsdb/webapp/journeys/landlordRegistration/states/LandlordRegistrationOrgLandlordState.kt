package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgAddressStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompaniesHouseStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgDirectorsStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgEmailStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgLandlordCyaStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgMainContactStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgNameStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgPhoneNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTrusteesStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.YourDetailsStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.OrgLandlordRegistrationTask

interface LandlordRegistrationOrgLandlordState : JourneyState {
    val orgLandlordRegistrationTask: OrgLandlordRegistrationTask
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
