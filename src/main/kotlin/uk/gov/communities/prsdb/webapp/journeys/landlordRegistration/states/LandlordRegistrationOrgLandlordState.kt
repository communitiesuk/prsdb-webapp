package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgAddressStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgEmailStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgMainContactStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgNameStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgPhoneNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.UpdateDetailsTodoStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.OrgCharityTask
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.OrgCompaniesHouseTask
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.OrgTypeTask

interface LandlordRegistrationOrgLandlordState : JourneyState {
    val orgNameStep: OrgNameStep
    val orgAddressStep: OrgAddressStep
    val orgEmailStep: OrgEmailStep
    val orgPhoneNumberStep: OrgPhoneNumberStep
    val orgTypeTask: OrgTypeTask
    val companiesHouseTask: OrgCompaniesHouseTask
    val charityTask: OrgCharityTask
    val orgMainContactStep: OrgMainContactStep

    // TODO PDJB-1237 PDJB-1238: remove this placeholder once the org type and companies house update journeys exist.
    val updateDetailsTodoStep: UpdateDetailsTodoStep
}
