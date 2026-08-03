package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgAddressStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgEmailStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgMainContactStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgNameStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgPhoneNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.UpdateDetailsTodoStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.LeadTrusteeTask
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.OrgCharityTask
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.OrgCompaniesHouseTask
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.OrgGovBodyTask

interface LandlordRegistrationOrgLandlordState : JourneyState {
    val orgNameStep: OrgNameStep
    val orgAddressStep: OrgAddressStep
    val orgEmailStep: OrgEmailStep
    val orgPhoneNumberStep: OrgPhoneNumberStep
    val orgTypeStep: OrgTypeStep
    val companiesHouseTask: OrgCompaniesHouseTask
    val orgGovBodyTask: OrgGovBodyTask
    val charityTask: OrgCharityTask
    val leadTrusteeTask: LeadTrusteeTask
    val orgMainContactStep: OrgMainContactStep

    // TODO PDJB-1237 PDJB-1238: remove this placeholder once the org type and companies house update journeys exist.
    val updateDetailsTodoStep: UpdateDetailsTodoStep
}
