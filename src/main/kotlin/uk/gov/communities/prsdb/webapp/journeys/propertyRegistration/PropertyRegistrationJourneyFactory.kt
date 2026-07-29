package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import kotlinx.datetime.Instant
import org.springframework.beans.factory.ObjectFactory
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING
import uk.gov.communities.prsdb.webapp.constants.TASK_LIST_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController.Companion.PROPERTY_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.AndParents
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.always
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.CertificateUpload
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.CombinedComplianceCheckState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.OccupationState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.TenancyDetailsState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BedroomsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BillsIncludedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckElectricalCertUploadsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckElectricalSafetyAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckEpcAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckGasCertUploadsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckGasSafetyAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ConfirmEpcDetailsRetrievedByCertificateNumberStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ConfirmEpcRetrievedByUprnStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ConfirmMissingComplianceCheckResult
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ConfirmMissingComplianceMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ConfirmMissingComplianceStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertExpiryDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcAgeCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcEnergyRatingCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcInDateAtStartOfTenancyCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcLookupByUprnStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcNotFoundStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcSuperseededStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FindYourEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FinishCyaJourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FurnishedStatusStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.GasCertExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.GasCertIssueDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.GasCertMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasAnyInCollectionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasSupplyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasJointLandlordsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasMeesExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasMissingComplianceStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HmoAdditionalLicenceStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HmoMandatoryLicenceStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.IsEpcRequiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LicensingTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LocalCouncilStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LowEnergyRatingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.MeesExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OccupiedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyOccupiedCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyRegistrationCyaStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyRegistrationTaskListStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideElectricalCertLaterStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideEpcLaterStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideGasCertLaterStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemoveElectricalCertUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemoveGasCertUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentAmountStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentFrequencyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentIncludesBillsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SavePropertyRegistrationDataStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SelectiveLicenceStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.StartEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.TenantsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadElectricalCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadGasCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.ElectricalSafetyDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.ElectricalSafetyTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.EpcDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.EpcTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.GasSafetyDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.GasSafetyTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.HouseHoldsAndTenantsDependencies
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.LicensingTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.OccupationTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.OccupationTaskWithProvideLaterAllowed
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.OwnershipAndLandlordsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.PropertyDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.TenancyDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.CheckJointLandlordsStep
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.InviteJointLandlordsTaskDependencies
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkAnswerStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkAnswerTask
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.duplicableCheckAnswerTask
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.SectionHeaderViewModel
import uk.gov.communities.prsdb.webapp.services.LandlordService
import java.security.Principal

@PrsdbWebService
class PropertyRegistrationJourneyFactory(
    private val stateFactory: ObjectFactory<PropertyRegistrationJourneyState>,
    private val featureFlagManager: FeatureFlagManager,
) {
    final fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        val checkingAnswersFor = state.checkingAnswersFor
        return if (checkingAnswersFor == null) {
            mainJourneyMap(state)
        } else {
            checkYourAnswersJourneyMap(state, checkingAnswersFor)
        }
    }

    private fun checkYourAnswersJourneyMap(
        state: PropertyRegistrationJourneyState,
        checkingAnswersFor: String,
    ): Map<String, StepLifecycleOrchestrator> =
        journey(state) {
            unreachableStepDestination { journey.returnToCyaPageDestination }
            configure {
                withAdditionalContentProperty { "title" to "registerProperty.title" }
            }
            configureFirst { backDestination { journey.returnToCyaPageDestination } }

            when (checkingAnswersFor) {
                LookupAddressStep.ROUTE_SEGMENT -> {
                    duplicableCheckAnswerTask(journey.propertyDetailsTask.addressTask)
                }

                LocalCouncilStep.ROUTE_SEGMENT -> {
                    fromTask(journey.propertyDetailsTask.addressTask) {
                        checkAnswerStep(task.localCouncilStep, LocalCouncilStep.ROUTE_SEGMENT)
                    }
                }

                PropertyTypeStep.ROUTE_SEGMENT -> {
                    fromTask(journey.propertyDetailsTask) {
                        checkAnswerStep(task.propertyTypeStep, PropertyTypeStep.ROUTE_SEGMENT)
                    }
                }

                OwnershipTypeStep.ROUTE_SEGMENT -> {
                    fromTask(journey.ownershipAndLandlordsTask) {
                        checkAnswerStep(task.ownershipTypeStep, OwnershipTypeStep.ROUTE_SEGMENT)
                    }
                }

                LicensingTypeStep.ROUTE_SEGMENT,
                SelectiveLicenceStep.ROUTE_SEGMENT,
                HmoMandatoryLicenceStep.ROUTE_SEGMENT,
                HmoAdditionalLicenceStep.ROUTE_SEGMENT,
                -> {
                    duplicableCheckAnswerTask(journey.licensingTask)
                }

                OccupiedStep.ROUTE_SEGMENT -> {
                    if (featureFlagManager.checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)) {
                        checkAnswerStep(journey.occupied, OccupiedStep.ROUTE_SEGMENT)
                    } else {
                        checkAnswerTask(journey.occupationTask)
                    }
                }

                HouseholdStep.ROUTE_SEGMENT, TenantsStep.ROUTE_SEGMENT -> {
                    duplicableCheckAnswerTask(journey.householdsAndTenantsTask, { HouseHoldsAndTenantsDependencies(true) })
                }

                BedroomsStep.ROUTE_SEGMENT -> {
                    checkAnswerStep(journey.bedrooms, BedroomsStep.ROUTE_SEGMENT)
                }

                RentIncludesBillsStep.ROUTE_SEGMENT -> {
                    duplicableCheckAnswerTask(journey.rentIncludesBillsTask)
                }

                BillsIncludedStep.ROUTE_SEGMENT -> {
                    fromTask(journey.rentIncludesBillsTask) {
                        checkAnswerStep(task.billsIncluded, BillsIncludedStep.ROUTE_SEGMENT)
                    }
                }

                FurnishedStatusStep.ROUTE_SEGMENT -> {
                    checkAnswerStep(journey.furnishedStatus, FurnishedStatusStep.ROUTE_SEGMENT)
                }

                RentFrequencyStep.ROUTE_SEGMENT, RentAmountStep.ROUTE_SEGMENT -> {
                    duplicableCheckAnswerTask(journey.rentFrequencyAndAmountTask)
                }

                HasJointLandlordsStep.ROUTE_SEGMENT,
                CheckJointLandlordsStep.ROUTE_SEGMENT,
                -> {
                    duplicableCheckAnswerTask(journey.ownershipAndLandlordsTask.jointLandlordsTask, { journey })
                }

                HasGasSupplyStep.ROUTE_SEGMENT,
                HasGasCertStep.ROUTE_SEGMENT,
                GasCertIssueDateStep.ROUTE_SEGMENT,
                CheckGasCertUploadsStep.ROUTE_SEGMENT,
                -> {
                    checkAnswerTask(journey.gasSafetyDetailsTask)
                }

                HasElectricalCertStep.ROUTE_SEGMENT,
                ElectricalCertExpiryDateStep.ROUTE_SEGMENT,
                CheckElectricalCertUploadsStep.ROUTE_SEGMENT,
                -> {
                    checkAnswerTask(journey.electricalSafetyDetailsTask)
                }

                StartEpcStep.ROUTE_SEGMENT,
                HasEpcStep.ROUTE_SEGMENT,
                EpcInDateAtStartOfTenancyCheckStep.ROUTE_SEGMENT,
                HasMeesExemptionStep.ROUTE_SEGMENT,
                MeesExemptionStep.ROUTE_SEGMENT,
                IsEpcRequiredStep.ROUTE_SEGMENT,
                EpcExemptionStep.ROUTE_SEGMENT,
                -> {
                    checkAnswerTask(journey.epcDetailsTask)
                }

                else -> {
                    throw IllegalStateException("Unknown checkable element $checkingAnswersFor")
                }
            }
            step(journey.finishCyaStep) {
                initialStep()
                nextDestination { Destination.Nowhere() }
                saveProgress()
            }
        }

    private fun mainJourneyMap(state: PropertyRegistrationJourneyState): Map<String, StepLifecycleOrchestrator> =
        if (featureFlagManager.checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)) {
            restructuredMainJourneyMap(state)
        } else {
            legacyMainJourneyMap(state)
        }

    private fun legacyMainJourneyMap(state: PropertyRegistrationJourneyState): Map<String, StepLifecycleOrchestrator> =
        journey(state) {
            unreachableStepStep { journey.taskListStep }
            configure {
                withAdditionalContentProperty { "title" to "registerProperty.title" }
            }
            configureFirst { backDestination { journey.returnToCyaPageDestination } }
            configureStep(journey.confirmMissingComplianceStep) {
                withAdditionalContentProperty {
                    "sectionHeaderInfo" to
                        SectionHeaderViewModel(
                            sectionNameKey = "registerProperty.submitRegistration",
                            sectionNumber = 0,
                            totalSections = 0,
                            useNumbering = false,
                        )
                }
            }
            step(journey.taskListStep) {
                routeSegment(TASK_LIST_PATH_SEGMENT)
                initialStep()
                noNextDestination()
            }
            section {
                if (featureFlagManager.checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)) {
                    withHeadingMessageKey("registerProperty.taskList.register.restructureAndSkipping.heading", false)
                } else {
                    withHeadingMessageKey("registerProperty.taskList.register.heading")
                }
                fromTask(journey.propertyDetailsTask) {
                    duplicableTask(task.addressTask) {
                        parents { journey.taskListStep.always() }
                        nextStep { task.addToLandlordIncompletePropertiesStep }
                    }
                    step(task.addToLandlordIncompletePropertiesStep) {
                        parents { task.addressTask.isComplete() }
                        nextStep { task.propertyTypeStep }
                        saveProgress()
                    }
                    step(task.propertyTypeStep) {
                        routeSegment(PropertyTypeStep.ROUTE_SEGMENT)
                        parents { task.addressTask.isComplete() }
                        nextStep { journey.ownershipAndLandlordsTask.ownershipTypeStep }
                        saveProgress()
                    }
                }
                fromTask(journey.ownershipAndLandlordsTask) {
                    step(task.ownershipTypeStep) {
                        routeSegment(OwnershipTypeStep.ROUTE_SEGMENT)
                        parents { journey.propertyDetailsTask.propertyTypeStep.isComplete() }
                        nextStep { journey.licensingTask.firstStep }
                        saveProgress()
                    }
                }
                duplicableTask(journey.licensingTask) {
                    parents { journey.ownershipAndLandlordsTask.ownershipTypeStep.isComplete() }
                    nextStep { journey.occupationTask.firstStep }
                    saveProgress()
                }

                task(journey.occupationTask) {
                    parents { journey.licensingTask.isComplete() }
                    nextStep { journey.ownershipAndLandlordsTask.jointLandlordsTask.firstStep }
                    saveProgress()
                }
                duplicableTask(journey.ownershipAndLandlordsTask.jointLandlordsTask) {
                    withDependencies { journey }
                    parents { journey.occupationTask.isComplete() }
                    nextStep { journey.gasSafetyTask.firstStep }
                    saveProgress()
                }
                task(journey.gasSafetyTask) {
                    parents { journey.ownershipAndLandlordsTask.jointLandlordsTask.isComplete() }
                    nextStep { journey.taskListStep }
                    saveProgress()
                }
                task(journey.electricalSafetyTask) {
                    parents { journey.gasSafetyTask.isComplete() }
                    backStep { journey.taskListStep }
                    nextStep { journey.taskListStep }
                    saveProgress()
                }
                task(journey.epcTask) {
                    parents { journey.electricalSafetyTask.isComplete() }
                    backStep { journey.taskListStep }
                    nextStep { journey.taskListStep }
                    saveProgress()
                }
            }
            section {
                withHeadingMessageKey("registerProperty.taskList.checkAndSubmit.heading")
                step(journey.cyaStep) {
                    routeSegment(PropertyRegistrationCyaStep.ROUTE_SEGMENT)
                    backStep { journey.taskListStep }
                    parents {
                        journey.epcTask.isComplete()
                    }
                    nextStep { journey.hasMissingComplianceStep }
                }
                step(journey.hasMissingComplianceStep) {
                    parents { journey.cyaStep.isComplete() }
                    nextStep { mode ->
                        when (mode) {
                            ConfirmMissingComplianceCheckResult.OCCUPIED_AND_HAS_INVALID_CERTIFICATES -> {
                                journey.confirmMissingComplianceStep
                            }

                            ConfirmMissingComplianceCheckResult.UNOCCUPIED_OR_VALID_CERTIFICATES -> {
                                journey.savePropertyRegistrationDataStep
                            }
                        }
                    }
                }
                step(journey.confirmMissingComplianceStep) {
                    routeSegment(ConfirmMissingComplianceStep.ROUTE_SEGMENT)
                    parents {
                        journey.hasMissingComplianceStep.hasOutcome(
                            ConfirmMissingComplianceCheckResult.OCCUPIED_AND_HAS_INVALID_CERTIFICATES,
                        )
                    }
                    nextDestination { mode ->
                        when (mode) {
                            ConfirmMissingComplianceMode.GO_BACK -> {
                                Destination(journey.cyaStep)
                            }

                            ConfirmMissingComplianceMode.CONFIRMED -> {
                                Destination(journey.savePropertyRegistrationDataStep)
                            }
                        }
                    }
                }
                step(journey.savePropertyRegistrationDataStep) {
                    parents {
                        OrParents(
                            journey.hasMissingComplianceStep.hasOutcome(
                                ConfirmMissingComplianceCheckResult.UNOCCUPIED_OR_VALID_CERTIFICATES,
                            ),
                            journey.confirmMissingComplianceStep.hasOutcome(ConfirmMissingComplianceMode.CONFIRMED),
                        )
                    }
                    nextUrl { "$PROPERTY_REGISTRATION_ROUTE/$CONFIRMATION_PATH_SEGMENT" }
                }
            }
        }

    private fun restructuredMainJourneyMap(state: PropertyRegistrationJourneyState): Map<String, StepLifecycleOrchestrator> =
        journey(state) {
            unreachableStepStep { journey.taskListStep }
            configure {
                withAdditionalContentProperty { "title" to "registerProperty.title" }
            }
            configureFirst { backDestination { journey.returnToCyaPageDestination } }
            configureStep(journey.confirmMissingComplianceStep) {
                withAdditionalContentProperty {
                    "sectionHeaderInfo" to
                        SectionHeaderViewModel(
                            sectionNameKey = "registerProperty.submitRegistration",
                            sectionNumber = 0,
                            totalSections = 0,
                            useNumbering = false,
                        )
                }
            }
            // We don't have a section header for these pages, as their titles are the same as the respective page header
            listOf(
                journey.checkGasSafetyAnswersStep,
                journey.checkElectricalSafetyAnswersStep,
                journey.checkEpcAnswersStep,
            ).forEach { checkAnswersStep ->
                configureStep(checkAnswersStep) {
                    withAdditionalContentProperty { "sectionHeaderInfo" to null }
                }
            }
            step(journey.taskListStep) {
                routeSegment(TASK_LIST_PATH_SEGMENT)
                initialStep()
                noNextDestination()
            }
            section {
                withHeadingMessageKey("registerProperty.taskList.aboutYourProperty.propertyDetails", shouldUseNumbering = false)
                duplicableTask(journey.propertyDetailsTask) {
                    parents { journey.taskListStep.always() }
                    nextStep { journey.ownershipAndLandlordsTask.firstStep }
                    saveProgress()
                }
            }
            section {
                withHeadingMessageKey("registerProperty.taskList.aboutYourProperty.ownershipAndLandlords", shouldUseNumbering = false)
                duplicableTask(journey.ownershipAndLandlordsTask) {
                    withDependencies { journey }
                    parents { journey.propertyDetailsTask.isComplete() }
                    nextStep { journey.occupied }
                    saveProgress()
                }
            }
            section {
                withHeadingMessageKey("registerProperty.taskList.aboutYourProperty.occupied", shouldUseNumbering = false)
                step(journey.occupied) {
                    routeSegment(OccupiedStep.ROUTE_SEGMENT)
                    parents { journey.ownershipAndLandlordsTask.isComplete() }
                    nextStep { journey.licensingTask.firstStep }
                    saveProgress()
                }
            }
            section {
                withHeadingMessageKey("registerProperty.taskList.rentedOut.licensing", shouldUseNumbering = false)
                duplicableTask(journey.licensingTask) {
                    parents {
                        OrParents(
                            journey.occupied.hasOutcome(YesOrNo.YES),
                            journey.occupied.hasOutcome(YesOrNo.NO),
                        )
                    }
                    nextStep { journey.gasSafetyTask.firstStep }
                    saveProgress()
                }
            }
            section {
                withHeadingMessageKey("registerProperty.taskList.gasSafety", shouldUseNumbering = false)
                task(journey.gasSafetyTask) {
                    parents { journey.licensingTask.isComplete() }
                    nextStep { journey.taskListStep }
                    saveProgress()
                }
            }
            section {
                withHeadingMessageKey("registerProperty.taskList.electricalSafety", shouldUseNumbering = false)
                task(journey.electricalSafetyTask) {
                    parents { journey.gasSafetyTask.isComplete() }
                    backStep { journey.taskListStep }
                    nextStep { journey.taskListStep }
                    saveProgress()
                }
            }
            section {
                withHeadingMessageKey("registerProperty.taskList.epc", shouldUseNumbering = false)
                task(journey.epcTask) {
                    parents { journey.electricalSafetyTask.isComplete() }
                    backStep { journey.taskListStep }
                    nextStep { journey.taskListStep }
                    saveProgress()
                }
            }
            section {
                withHeadingMessageKey("registerProperty.taskList.rentedOut.tenancyDetails", shouldUseNumbering = false)
                duplicableTask(journey.tenancyDetailsTask) {
                    parents {
                        AndParents(
                            journey.epcTask.isComplete(),
                            journey.occupied.hasOutcome(YesOrNo.YES),
                        )
                    }
                    backStep { journey.taskListStep }
                    nextDestination { _ -> getTenancyDetailsTaskDestination(state) }
                    saveProgress()
                }
            }
            section {
                withHeadingMessageKey("registerProperty.taskList.submitYourRegistration.heading", shouldUseNumbering = false)
                step(journey.cyaStep) {
                    routeSegment(PropertyRegistrationCyaStep.ROUTE_SEGMENT)
                    backStep { journey.taskListStep }
                    parents {
                        AndParents(
                            journey.epcTask.isComplete(),
                            OrParents(
                                journey.tenancyDetailsTask.isComplete(),
                                journey.occupied.hasOutcome(YesOrNo.NO),
                            ),
                        )
                    }
                    nextStep { journey.hasMissingComplianceStep }
                }
                step(journey.hasMissingComplianceStep) {
                    parents { journey.cyaStep.isComplete() }
                    nextStep { mode ->
                        when (mode) {
                            ConfirmMissingComplianceCheckResult.OCCUPIED_AND_HAS_INVALID_CERTIFICATES -> {
                                journey.confirmMissingComplianceStep
                            }

                            ConfirmMissingComplianceCheckResult.UNOCCUPIED_OR_VALID_CERTIFICATES -> {
                                journey.savePropertyRegistrationDataStep
                            }
                        }
                    }
                }
                step(journey.confirmMissingComplianceStep) {
                    routeSegment(ConfirmMissingComplianceStep.ROUTE_SEGMENT)
                    parents {
                        journey.hasMissingComplianceStep.hasOutcome(
                            ConfirmMissingComplianceCheckResult.OCCUPIED_AND_HAS_INVALID_CERTIFICATES,
                        )
                    }
                    nextDestination { mode ->
                        when (mode) {
                            ConfirmMissingComplianceMode.GO_BACK -> {
                                Destination(journey.cyaStep)
                            }

                            ConfirmMissingComplianceMode.CONFIRMED -> {
                                Destination(journey.savePropertyRegistrationDataStep)
                            }
                        }
                    }
                }
                step(journey.savePropertyRegistrationDataStep) {
                    parents {
                        OrParents(
                            journey.hasMissingComplianceStep.hasOutcome(
                                ConfirmMissingComplianceCheckResult.UNOCCUPIED_OR_VALID_CERTIFICATES,
                            ),
                            journey.confirmMissingComplianceStep.hasOutcome(ConfirmMissingComplianceMode.CONFIRMED),
                        )
                    }
                    nextUrl { "$PROPERTY_REGISTRATION_ROUTE/$CONFIRMATION_PATH_SEGMENT" }
                }
            }
        }

    private fun getTenancyDetailsTaskDestination(state: PropertyRegistrationJourneyState): Destination =
        when {
            state.provideTenancyDetailsLater -> Destination(state.cyaStep)
            else -> Destination(state.taskListStep)
        }

    fun initializeJourneyState(user: Principal): String = stateFactory.getObject().initializeOrRestoreState(user)
}

@JourneyFrameworkComponent
class PropertyRegistrationJourney(
    // Task list step
    override val taskListStep: PropertyRegistrationTaskListStep,
    // Licensing task
    override val licensingTask: LicensingTask,
    // Occupation steps
    override val occupied: OccupiedStep,
    // ===== Journey-structure tasks (the two alternative flows diverge here) =====
    // Legacy journey only (flag-off) — delete this (and OccupationTask, legacyMainJourneyMap,
    // legacySectionViewModels, bedrooms override) when the old journey is removed.
    override val occupationTask: OccupationTaskWithProvideLaterAllowed,
    // Restructured journey only (flag-on) — grouping tasks for the new task-list structure.
    override val propertyDetailsTask: PropertyDetailsTask,
    override val ownershipAndLandlordsTask: OwnershipAndLandlordsTask,
    override val tenancyDetailsTask: TenancyDetailsTask,
    // Gas safety task
    override val gasSafetyTask: GasSafetyTask,
    override val gasSafetyDetailsTask: GasSafetyDetailsTask,
    override val hasUploadedCert: HasAnyInCollectionStep,
    override val hasGasSupplyStep: HasGasSupplyStep,
    override val hasGasCertStep: HasGasCertStep,
    override val gasCertIssueDateStep: GasCertIssueDateStep,
    override val uploadGasCertStep: UploadGasCertStep,
    override val checkGasCertUploadsStep: CheckGasCertUploadsStep,
    override val removeGasCertUploadStep: RemoveGasCertUploadStep,
    override val gasCertExpiredStep: GasCertExpiredStep,
    override val gasCertMissingStep: GasCertMissingStep,
    override val provideGasCertLaterStep: ProvideGasCertLaterStep,
    override val checkGasSafetyAnswersStep: CheckGasSafetyAnswersStep,
    // Electrical safety task
    override val electricalSafetyTask: ElectricalSafetyTask,
    override val electricalSafetyDetailsTask: ElectricalSafetyDetailsTask,
    override val hasElectricalCertStep: HasElectricalCertStep,
    override val electricalCertExpiryDateStep: ElectricalCertExpiryDateStep,
    override val uploadElectricalCertStep: UploadElectricalCertStep,
    override val hasUploadedElectricalCert: HasAnyInCollectionStep,
    override val checkElectricalCertUploadsStep: CheckElectricalCertUploadsStep,
    override val removeElectricalCertUploadStep: RemoveElectricalCertUploadStep,
    override val electricalCertExpiredStep: ElectricalCertExpiredStep,
    override val electricalCertMissingStep: ElectricalCertMissingStep,
    override val provideElectricalCertLaterStep: ProvideElectricalCertLaterStep,
    override val checkElectricalSafetyAnswersStep: CheckElectricalSafetyAnswersStep,
    // EPC task
    override val epcTask: EpcTask,
    override val epcDetailsTask: EpcDetailsTask,
    override val startEpcStep: StartEpcStep,
    override val epcLookupByUprnStep: EpcLookupByUprnStep,
    override val hasEpcStep: HasEpcStep,
    override val checkUprnMatchedEpcStep: ConfirmEpcRetrievedByUprnStep,
    override val epcAgeCheckStep: EpcAgeCheckStep,
    override val epcEnergyRatingCheckStep: EpcEnergyRatingCheckStep,
    override val isPropertyOccupiedCheckStep: PropertyOccupiedCheckStep,
    override val confirmEpcDetailsRetrievedByCertificateNumberStep: ConfirmEpcDetailsRetrievedByCertificateNumberStep,
    override val findYourEpcStep: FindYourEpcStep,
    override val checkSupersededEpcStep: EpcSuperseededStep,
    override val epcNotFoundStep: EpcNotFoundStep,
    override val epcInDateAtStartOfTenancyCheckStep: EpcInDateAtStartOfTenancyCheckStep,
    override val hasMeesExemptionStep: HasMeesExemptionStep,
    override val meesExemptionStep: MeesExemptionStep,
    override val lowEnergyRatingStep: LowEnergyRatingStep,
    override val epcExpiredStep: EpcExpiredStep,
    override val isEpcRequiredStep: IsEpcRequiredStep,
    override val epcExemptionStep: EpcExemptionStep,
    override val epcMissingStep: EpcMissingStep,
    override val provideEpcLaterStep: ProvideEpcLaterStep,
    override val checkEpcAnswersStep: CheckEpcAnswersStep,
    // Check your answers step
    override val cyaStep: PropertyRegistrationCyaStep,
    override val finishCyaStep: FinishCyaJourneyStep,
    // Confirm missing compliance steps
    override val hasMissingComplianceStep: HasMissingComplianceStep,
    override val confirmMissingComplianceStep: ConfirmMissingComplianceStep,
    // Save data step
    override val savePropertyRegistrationDataStep: SavePropertyRegistrationDataStep,
    journeyStateService: JourneyStateService,
    private val landlordService: LandlordService,
    override val stateFactory: ObjectFactory<PropertyRegistrationJourneyState>,
) : AbstractJourneyState(journeyStateService),
    PropertyRegistrationJourneyState {
    override var cachedOccupied: Boolean? by delegateProvider.nullableDelegate("cachedOccupied")
    override var cyaJourneys: Map<String, String> = mapOf()
    override var originalJourneyUpdated: Instant? by delegateProvider.nullableDelegate("originalJourneyUpdated")

    override var checkingAnswersFor: String? by delegateProvider.nullableDelegate("checkingAnswersFor")

    override var epcRetrievedByUprn: EpcDataModel? by delegateProvider.nullableDelegate("epcRetrievedByUprn")
    override var epcRetrievedByUprnUpdatedSinceUserReview: Boolean?
        by delegateProvider.nullableDelegate("epcRetrievedByUprnUpdatedSinceUserReview")
    override var epcRetrievedByCertificateNumber: EpcDataModel? by delegateProvider.nullableDelegate("epcRetrievedByCertificateNumber")
    override var epcRetrievedByCertificateNumberUpdatedSinceUserReview: Boolean?
        by delegateProvider.nullableDelegate("epcRetrievedByCertificateNumberUpdatedSinceUserReview")
    override var updatedEpcRetrievedByCertificateNumber: EpcDataModel? by delegateProvider
        .nullableDelegate("updatedEpcRetrievedByCertificateNumber")
    override var acceptedEpc: EpcDataModel? by delegateProvider.nullableDelegate("acceptedEpc")

    override var cyaUrlPath: String? by delegateProvider.nullableDelegate("cyaRouteSegment")

    // Reads cachedOccupied first; falls back to the step's submitted form data when the upstream step
    // is wired up (parent journey context). The cache is essential when a CYA child journey reads this
    // value: in that context only steps in the child's journey graph are wired up, so the fallback would
    // throw. The cache is populated on step submission (see OccupiedStepConfig.afterStepDataIsAdded).
    override val isOccupied: Boolean
        get() {
            cachedOccupied?.let { return it }
            return occupied.formModelOrNull?.occupied
                ?: throw PrsdbWebException("Cannot use isOccupied until after the occupation step")
        }

    // Legacy steps and tasks that should be removed when the legacy structure is retired
    override val bedrooms = propertyDetailsTask.bedrooms
    override val householdsAndTenantsTask = tenancyDetailsTask.householdsAndTenantsTask
    override val rentIncludesBillsTask = tenancyDetailsTask.rentIncludesBillsTask
    override val rentFrequencyAndAmountTask = tenancyDetailsTask.rentFrequencyAndAmountTask
    override val furnishedStatus = tenancyDetailsTask.furnishedStatus

    override var gasUploadMap: Map<Int, CertificateUpload> by delegateProvider.requiredDelegate("gasUploadMap", mapOf())
    override var highestAssignedGasMemberId: Int? by delegateProvider.nullableDelegate("highestGasUploadMemberId")

    override var electricalUploadMap: Map<Int, CertificateUpload> by delegateProvider.requiredDelegate("electricalUploadMap", mapOf())
    override var highestAssignedElectricalMemberId: Int? by delegateProvider.nullableDelegate("highestAssignedElectricalMemberId")

    override var registrationNumberValue: Long? by delegateProvider.nullableDelegate("registrationNumberValue")

    // Cache reasoning matches isOccupied above. The cached value is the raw selected address string so we can
    // distinguish "not yet submitted" (null) from "manual address chosen" (cached non-null but resolves to no UPRN).
    // Cache is populated on step submission (see SelectAddressStepConfig.afterStepDataIsAdded).
    override val uprn: Long?
        get() {
            val addressTask = propertyDetailsTask.addressTask
            addressTask.cachedSelectedAddress?.let { return addressTask.getMatchingAddress(it)?.uprn }
            val submittedAddress = addressTask.selectAddressStep.formModelOrNull?.address ?: return null
            return addressTask.getMatchingAddress(submittedAddress)?.uprn
        }

    override var backUrlKey: Int? by delegateProvider.nullableDelegate("backUrlKey")

    override val allowProvideCertificateLaterRoute: Boolean = true

    override fun generateJourneyId(seed: Any?): String {
        val user = seed as? Principal

        return super<AbstractJourneyState>.generateJourneyId(user?.let { generateSeedForUser(it) } ?: seed)
    }

    override val loggedInLandlordEmail: String?
        // TODO: PDJB-1274: Update emails to account for org landlord
        get() =
            landlordService
                .retrieveLandlordByBaseUserId(SecurityContextHolder.getContext().authentication.name)
                ?.email

    companion object {
        fun generateSeedForUser(user: Principal): String = "Prop reg journey for user ${user.name} at time ${System.currentTimeMillis()}"
    }
}

interface PropertyRegistrationJourneyState :
    OccupationState,
    TenancyDetailsState,
    InviteJointLandlordsTaskDependencies,
    CombinedComplianceCheckState,
    CheckYourAnswersJourneyState {
    val taskListStep: PropertyRegistrationTaskListStep
    val licensingTask: LicensingTask

    // Journey-structure tasks (the two alternative flows)
    // Legacy journey only (flag-off) — remove with the old journey
    val occupationTask: OccupationTask

    // Restructured journey only (flag-on)
    val propertyDetailsTask: PropertyDetailsTask
    val ownershipAndLandlordsTask: OwnershipAndLandlordsTask
    val tenancyDetailsTask: TenancyDetailsTask
    override val finishCyaStep: FinishCyaJourneyStep
    val gasSafetyTask: GasSafetyTask
    val electricalSafetyTask: ElectricalSafetyTask
    val epcTask: EpcTask
    override val cyaStep: PropertyRegistrationCyaStep
    val hasMissingComplianceStep: HasMissingComplianceStep
    val confirmMissingComplianceStep: ConfirmMissingComplianceStep
    val savePropertyRegistrationDataStep: SavePropertyRegistrationDataStep
    var registrationNumberValue: Long?
    var backUrlKey: Int?
}
