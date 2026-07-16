package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.CharityRegulator
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.DuplicableTask
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
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
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgEmailStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyDetailsStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyMemberDobStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyMemberListStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyMemberNameStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyMustProvideInfoStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyWhoToProvideStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgLandlordCyaStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgMainContactStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgNameStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgPhoneNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.YourDetailsStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.GovBodyMemberAddressTask
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.TrusteeAddressTask
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgGovBodyDetailsMode

@JourneyFrameworkComponent
class OrgLandlordRegistrationTask(
    journeyStateService: JourneyStateService,
    override val yourDetailsStep: YourDetailsStep,
    override val orgNameStep: OrgNameStep,
    override val orgAddressStep: OrgAddressStep,
    override val orgEmailStep: OrgEmailStep,
    override val orgPhoneNumberStep: OrgPhoneNumberStep,
    override val orgTypeStep: OrgTypeStep,
    override val orgCompaniesHouseStep: OrgCompaniesHouseStep,
    override val orgCompanyNumberStep: OrgCompanyNumberStep,
    override val orgCharityStep: OrgCharityStep,
    override val orgCharityRegisteredWithStep: OrgCharityRegisteredWithStep,
    override val orgCharityNumberEnglandAndWalesStep: OrgCharityNumberEnglandAndWalesStep,
    override val orgCharityNumberNorthernIrelandStep: OrgCharityNumberNorthernIrelandStep,
    override val orgCharityNumberScotlandStep: OrgCharityNumberScotlandStep,
    override val leadTrusteeNameStep: LeadTrusteeNameStep,
    override val leadTrusteeEmailStep: LeadTrusteeEmailStep,
    override val leadTrusteePhoneStep: LeadTrusteePhoneStep,
    override val leadTrusteeDobStep: LeadTrusteeDobStep,
    override val trusteeAddressTask: TrusteeAddressTask,
    override val orgGovBodyDetailsStep: OrgGovBodyDetailsStep,
    override val orgGovBodyMustProvideInfoStep: OrgGovBodyMustProvideInfoStep,
    override val orgGovBodyWhoToProvideStep: OrgGovBodyWhoToProvideStep,
    override val orgGovBodyMemberNameStep: OrgGovBodyMemberNameStep,
    override val orgGovBodyMemberDobStep: OrgGovBodyMemberDobStep,
    override val govBodyMemberAddressTask: GovBodyMemberAddressTask,
    override val orgGovBodyMemberListStep: OrgGovBodyMemberListStep,
    override val orgMainContactStep: OrgMainContactStep,
    override val orgLandlordCyaStep: OrgLandlordCyaStep,
) : DuplicableTask<LandlordRegistrationOrgLandlordState>(journeyStateService),
    LandlordRegistrationOrgLandlordState {
    override val taskState get() = this

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
                nextStep { journey.leadTrusteeNameStep }
            }
            // TODO: PDJB-1257: branch to here conditionally based on orgTypeStep outcome
            step(journey.leadTrusteeNameStep) {
                routeSegment(LeadTrusteeNameStep.ROUTE_SEGMENT)
                parents { journey.orgTypeStep.isComplete() }
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
                nextStep { journey.trusteeAddressTask.firstStep }
            }
            duplicableTask(journey.trusteeAddressTask, TrusteeAddressTask.LEAD_TRUSTEE_ADDRESS_ROUTE_SEGMENT) {
                parents { journey.leadTrusteeDobStep.isComplete() }
                // TODO PDJB-1257: reroute to the exit point of the trustee section
                nextStep { journey.orgCharityStep }
            }
            step(journey.orgCharityStep) {
                routeSegment(OrgCharityStep.ROUTE_SEGMENT)
                parents { journey.trusteeAddressTask.isComplete() }
                nextDestination { mode ->
                    when (mode) {
                        YesOrNo.YES -> Destination(journey.orgCharityRegisteredWithStep)
                        YesOrNo.NO -> Destination(journey.orgCompaniesHouseStep)
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
                        CharityRegulator.NONE -> Destination(journey.orgCompaniesHouseStep)
                    }
                }
            }
            step(journey.orgCharityNumberEnglandAndWalesStep) {
                routeSegment(OrgCharityNumberEnglandAndWalesStep.ROUTE_SEGMENT)
                parents { journey.orgCharityRegisteredWithStep.hasOutcome(CharityRegulator.ENGLAND_AND_WALES) }
                nextStep { journey.orgCompaniesHouseStep }
            }
            step(journey.orgCharityNumberNorthernIrelandStep) {
                routeSegment(OrgCharityNumberNorthernIrelandStep.ROUTE_SEGMENT)
                parents { journey.orgCharityRegisteredWithStep.hasOutcome(CharityRegulator.NORTHERN_IRELAND) }
                nextStep { journey.orgCompaniesHouseStep }
            }
            step(journey.orgCharityNumberScotlandStep) {
                routeSegment(OrgCharityNumberScotlandStep.ROUTE_SEGMENT)
                parents { journey.orgCharityRegisteredWithStep.hasOutcome(CharityRegulator.SCOTLAND) }
                nextStep { journey.orgCompaniesHouseStep }
            }
            step(journey.orgCompaniesHouseStep) {
                routeSegment(OrgCompaniesHouseStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.orgCharityStep.hasOutcome(YesOrNo.NO),
                        journey.orgCharityRegisteredWithStep.hasOutcome(CharityRegulator.NONE),
                        journey.orgCharityNumberEnglandAndWalesStep.isComplete(),
                        journey.orgCharityNumberNorthernIrelandStep.isComplete(),
                        journey.orgCharityNumberScotlandStep.isComplete(),
                    )
                }
                nextDestination { mode ->
                    when (mode) {
                        YesOrNo.YES -> Destination(journey.orgCompanyNumberStep)
                        YesOrNo.NO -> Destination(journey.orgGovBodyDetailsStep)
                    }
                }
            }
            step(journey.orgCompanyNumberStep) {
                routeSegment(OrgCompanyNumberStep.ROUTE_SEGMENT)
                parents { journey.orgCompaniesHouseStep.hasOutcome(YesOrNo.YES) }
                nextStep { journey.orgMainContactStep }
            }
            step(journey.orgGovBodyDetailsStep) {
                routeSegment(OrgGovBodyDetailsStep.ROUTE_SEGMENT)
                parents { journey.orgCompaniesHouseStep.hasOutcome(YesOrNo.NO) }
                nextDestination { mode ->
                    when (mode) {
                        OrgGovBodyDetailsMode.HAS_DETAILS -> Destination(journey.orgGovBodyWhoToProvideStep)
                        OrgGovBodyDetailsMode.NO_DETAILS -> Destination(journey.orgGovBodyMustProvideInfoStep)
                    }
                }
            }
            step(journey.orgGovBodyMustProvideInfoStep) {
                routeSegment(OrgGovBodyMustProvideInfoStep.ROUTE_SEGMENT)
                parents { journey.orgGovBodyDetailsStep.hasOutcome(OrgGovBodyDetailsMode.NO_DETAILS) }
                noNextDestination()
            }
            step(journey.orgGovBodyWhoToProvideStep) {
                routeSegment(OrgGovBodyWhoToProvideStep.ROUTE_SEGMENT)
                parents { journey.orgGovBodyDetailsStep.hasOutcome(OrgGovBodyDetailsMode.HAS_DETAILS) }
                nextStep { journey.orgGovBodyMemberNameStep }
            }
            step(journey.orgGovBodyMemberNameStep) {
                routeSegment(OrgGovBodyMemberNameStep.ROUTE_SEGMENT)
                parents { journey.orgGovBodyWhoToProvideStep.isComplete() }
                nextStep { journey.orgGovBodyMemberDobStep }
            }
            step(journey.orgGovBodyMemberDobStep) {
                routeSegment(OrgGovBodyMemberDobStep.ROUTE_SEGMENT)
                parents { journey.orgGovBodyMemberNameStep.isComplete() }
                nextStep { journey.govBodyMemberAddressTask.firstStep }
            }
            duplicableTask(journey.govBodyMemberAddressTask, GovBodyMemberAddressTask.ROUTE_SEGMENT) {
                parents { journey.orgGovBodyMemberDobStep.isComplete() }
                nextStep { journey.orgGovBodyMemberListStep }
                configureStep(journey.govBodyMemberAddressTask.selectAddressStep) {
                    withAdditionalContentProperties {
                        mapOf("fieldSetHeading" to "forms.selectAddress.govBodyMemberRegistration.fieldSetHeading")
                    }
                }
            }
            step(journey.orgGovBodyMemberListStep) {
                routeSegment(OrgGovBodyMemberListStep.ROUTE_SEGMENT)
                parents { journey.govBodyMemberAddressTask.isComplete() }
                nextStep { journey.orgMainContactStep }
            }
            step(journey.orgMainContactStep) {
                routeSegment(OrgMainContactStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.orgCompanyNumberStep.isComplete(),
                        journey.orgGovBodyMemberListStep.isComplete(),
                    )
                }
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
