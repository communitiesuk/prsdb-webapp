package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.CharityRegulator
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.LandlordRegistrationOrgLandlordState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeDobStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeEmailStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeNameStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteePhoneStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgAddressStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityNumberEnglandAndWalesStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityNumberNorthernIrelandStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityNumberScotlandStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityRegisteredWithStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompaniesHouseStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompanyNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgDirectorsStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgEmailStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgLandlordCyaStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgMainContactStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgNameStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgPhoneNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTrusteesStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.YourDetailsStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

@JourneyFrameworkComponent
class OrgLandlordRegistrationTask : Task<LandlordRegistrationOrgLandlordState>() {
    override fun makeSubJourney(state: LandlordRegistrationOrgLandlordState) =
        subJourney(state) {
            step(journey.yourDetailsStep) {
                routeSegment(YourDetailsStep.ROUTE_SEGMENT)
                nextStep { journey.orgNameStep }
            }
            step(journey.orgNameStep) {
                routeSegment(OrgNameStep.ROUTE_SEGMENT)
                parents { journey.yourDetailsStep.isComplete() }
                nextStep { journey.orgAddressStep }
            }
            step(journey.orgAddressStep) {
                routeSegment(OrgAddressStep.ROUTE_SEGMENT)
                parents { journey.orgNameStep.isComplete() }
                nextStep { journey.orgEmailStep }
            }
            step(journey.orgEmailStep) {
                routeSegment(OrgEmailStep.ROUTE_SEGMENT)
                parents { journey.orgAddressStep.isComplete() }
                nextStep { journey.orgPhoneNumberStep }
            }
            step(journey.orgPhoneNumberStep) {
                routeSegment(OrgPhoneNumberStep.ROUTE_SEGMENT)
                parents { journey.orgEmailStep.isComplete() }
                nextStep { journey.orgTypeStep }
            }
            step(journey.orgTypeStep) {
                routeSegment(OrgTypeStep.ROUTE_SEGMENT)
                parents { journey.orgPhoneNumberStep.isComplete() }
                nextStep { journey.orgCompaniesHouseStep }
            }
            step(journey.orgCompaniesHouseStep) {
                routeSegment(OrgCompaniesHouseStep.ROUTE_SEGMENT)
                parents { journey.orgTypeStep.isComplete() }
                nextDestination { mode ->
                    when (mode) {
                        YesOrNo.YES -> Destination(journey.orgCompanyNumberStep)
                        YesOrNo.NO -> Destination(journey.orgCharityStep)
                    }
                }
            }
            step(journey.orgCompanyNumberStep) {
                routeSegment(OrgCompanyNumberStep.ROUTE_SEGMENT)
                parents { journey.orgCompaniesHouseStep.hasOutcome(YesOrNo.YES) }
                nextStep { journey.orgCharityStep }
            }
            step(journey.orgCharityStep) {
                routeSegment(OrgCharityStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.orgCompaniesHouseStep.hasOutcome(YesOrNo.NO),
                        journey.orgCompanyNumberStep.isComplete(),
                    )
                }
                nextDestination { mode ->
                    when (mode) {
                        YesOrNo.YES -> Destination(journey.orgCharityRegisteredWithStep)
                        YesOrNo.NO -> Destination(journey.orgDirectorsStep)
                    }
                }
            }
            step(journey.orgCharityRegisteredWithStep) {
                routeSegment(OrgCharityRegisteredWithStep.ROUTE_SEGMENT)
                parents { journey.orgCharityStep.hasOutcome(YesOrNo.YES) }
                nextDestination { mode ->
                    when (mode) {
                        CharityRegulator.ENGLAND_AND_WALES -> Destination(journey.orgCharityNumberEnglandAndWalesStep)
                        CharityRegulator.NORTHERN_IRELAND -> Destination(journey.orgCharityNumberNorthernIrelandStep)
                        CharityRegulator.SCOTLAND -> Destination(journey.orgCharityNumberScotlandStep)
                        CharityRegulator.NONE -> Destination(journey.orgDirectorsStep)
                    }
                }
            }
            step(journey.orgCharityNumberEnglandAndWalesStep) {
                routeSegment(OrgCharityNumberEnglandAndWalesStep.ROUTE_SEGMENT)
                parents { journey.orgCharityRegisteredWithStep.hasOutcome(CharityRegulator.ENGLAND_AND_WALES) }
                nextStep { journey.orgDirectorsStep }
            }
            step(journey.orgCharityNumberNorthernIrelandStep) {
                routeSegment(OrgCharityNumberNorthernIrelandStep.ROUTE_SEGMENT)
                parents { journey.orgCharityRegisteredWithStep.hasOutcome(CharityRegulator.NORTHERN_IRELAND) }
                nextStep { journey.orgDirectorsStep }
            }
            step(journey.orgCharityNumberScotlandStep) {
                routeSegment(OrgCharityNumberScotlandStep.ROUTE_SEGMENT)
                parents { journey.orgCharityRegisteredWithStep.hasOutcome(CharityRegulator.SCOTLAND) }
                nextStep { journey.orgDirectorsStep }
            }
            step(journey.orgDirectorsStep) {
                routeSegment(OrgDirectorsStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.orgCharityStep.hasOutcome(YesOrNo.NO),
                        journey.orgCharityRegisteredWithStep.hasOutcome(CharityRegulator.NONE),
                        journey.orgCharityNumberEnglandAndWalesStep.isComplete(),
                        journey.orgCharityNumberNorthernIrelandStep.isComplete(),
                        journey.orgCharityNumberScotlandStep.isComplete(),
                    )
                }
                nextStep { journey.orgTrusteesStep }
            }
            step(journey.orgTrusteesStep) {
                routeSegment(OrgTrusteesStep.ROUTE_SEGMENT)
                parents { journey.orgDirectorsStep.isComplete() }
                nextStep { journey.leadTrusteeNameStep }
            }
            // TODO: PDJB-1257 Make sure this is the correct place
            step(journey.leadTrusteeNameStep) {
                routeSegment(LeadTrusteeNameStep.ROUTE_SEGMENT)
                parents { journey.orgTrusteesStep.isComplete() }
                nextStep { journey.leadTrusteeEmailStep }
            }
            step(journey.leadTrusteeEmailStep) {
                routeSegment(LeadTrusteeEmailStep.ROUTE_SEGMENT)
                parents { journey.leadTrusteeNameStep.isComplete() }
                nextStep { journey.leadTrusteePhoneStep }
            }
            step(journey.leadTrusteePhoneStep) {
                routeSegment(LeadTrusteePhoneStep.ROUTE_SEGMENT)
                parents { journey.leadTrusteeEmailStep.isComplete() }
                nextStep { journey.leadTrusteeDobStep }
            }
            step(journey.leadTrusteeDobStep) {
                routeSegment(LeadTrusteeDobStep.ROUTE_SEGMENT)
                parents { journey.leadTrusteePhoneStep.isComplete() }
                nextStep { journey.orgLandlordTrusteeAddressTask.firstStep }
            }
            task(journey.orgLandlordTrusteeAddressTask) {
                parents { journey.leadTrusteeDobStep.isComplete() }
                nextStep { journey.orgMainContactStep }
            }
            step(journey.orgMainContactStep) {
                routeSegment(OrgMainContactStep.ROUTE_SEGMENT)
                parents { journey.orgLandlordTrusteeAddressTask.isComplete() }
                nextStep { journey.orgLandlordCyaStep }
            }
            step(journey.orgLandlordCyaStep) {
                routeSegment(OrgLandlordCyaStep.ROUTE_SEGMENT)
                parents { journey.orgMainContactStep.isComplete() }
                nextStep { exitStep }
            }
            exitStep {
                parents { journey.orgLandlordCyaStep.isComplete() }
            }
        }
}
