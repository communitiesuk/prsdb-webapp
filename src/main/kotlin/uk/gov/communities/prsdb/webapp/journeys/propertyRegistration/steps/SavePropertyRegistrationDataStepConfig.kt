package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import jakarta.persistence.EntityExistsException
import kotlinx.datetime.toJavaLocalDate
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.JointLandlordsPropertyRegistrationStrategy
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LicensingTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NewNumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfBedroomsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OccupancyFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OwnershipTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyTypeFormModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.PropertyRegistrationService

@JourneyFrameworkComponent
class SavePropertyRegistrationDataStepConfig(
    private val propertyRegistrationService: PropertyRegistrationService,
    private val propertyComplianceService: PropertyComplianceService,
    private val epcCertificateUrlProvider: EpcCertificateUrlProvider,
    private val jointLandlordsStrategy: JointLandlordsPropertyRegistrationStrategy,
) : AbstractInternalStepConfig<Complete, PropertyRegistrationJourneyState>() {
    override fun mode(state: PropertyRegistrationJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: PropertyRegistrationJourneyState) {
        try {
            registerProperty(state)
        } catch (_: EntityExistsException) {
            state.isAddressAlreadyRegistered = true
            return
        }
    }

    override fun resolveNextDestination(
        state: PropertyRegistrationJourneyState,
        defaultDestination: Destination,
    ): Destination =
        if (state.isAddressAlreadyRegistered == true) {
            Destination(state.alreadyRegisteredStep)
        } else {
            state.deleteJourney()
            defaultDestination
        }

    private fun registerProperty(state: PropertyRegistrationJourneyState) {
        val isOccupied = state.occupied.formModel.notNullValue(OccupancyFormModel::occupied)
        val billsIncludedDataModel = state.getBillsIncludedOrNull()
        var jointLandlordEmails: List<String>? = null
        jointLandlordsStrategy.ifEnabled {
            jointLandlordEmails = state.invitedJointLandlordEmailsMap?.values?.toList()
        }
        val registrationNumber =
            propertyRegistrationService.registerProperty(
                addressModel = state.getAddress(),
                propertyType = state.propertyTypeStep.formModel.notNullValue(PropertyTypeFormModel::propertyType),
                customPropertyType =
                    if (state.propertyTypeStep.formModel.propertyType == PropertyType.OTHER) {
                        state.propertyTypeStep.formModel.customPropertyType
                    } else {
                        null
                    },
                licenseType = state.licensingTypeStep.formModel.notNullValue(LicensingTypeFormModel::licensingType),
                licenceNumber = state.getLicenceNumberOrNull() ?: "",
                ownershipType = state.ownershipTypeStep.formModel.notNullValue(OwnershipTypeFormModel::ownershipType),
                numberOfHouseholds =
                    if (isOccupied) {
                        state.households.formModel
                            .notNullValue(NumberOfHouseholdsFormModel::numberOfHouseholds)
                            .toInt()
                    } else {
                        0
                    },
                numberOfPeople =
                    if (isOccupied) {
                        state.tenants.formModel
                            .notNullValue(NewNumberOfPeopleFormModel::numberOfPeople)
                            .toInt()
                    } else {
                        0
                    },
                numBedrooms =
                    if (isOccupied) {
                        state.bedrooms.formModel
                            .notNullValue(NumberOfBedroomsFormModel::numberOfBedrooms)
                            .toInt()
                    } else {
                        null
                    },
                billsIncludedList = if (isOccupied) billsIncludedDataModel?.standardBillsIncludedListAsString else null,
                customBillsIncluded = if (isOccupied) billsIncludedDataModel?.customBillsIncluded else null,
                furnishedStatus = if (isOccupied) state.furnishedStatus.formModel.furnishedStatus else null,
                rentFrequency = if (isOccupied) state.rentFrequency.formModel.rentFrequency else null,
                customRentFrequency = if (isOccupied) state.getCustomRentFrequencyIfSelected() else null,
                rentAmount =
                    if (isOccupied) {
                        state.rentAmount.formModel.rentAmount
                            .toBigDecimal()
                    } else {
                        null
                    },
                baseUserId = SecurityContextHolder.getContext().authentication.name,
                jointLandlordEmails = jointLandlordEmails,
                gasSafetyFileUploadIds = state.gasUploadIds,
                electricalSafetyFileUploadIds = state.electricalUploadIds,
            )
        propertyComplianceService.saveRegistrationComplianceData(
            registrationNumberValue = registrationNumber.number,
            hasGasSupply = state.hasGasSupplyStep.outcome == YesOrNo.YES,
            gasSafetyCertIssueDate = state.getGasSafetyCertificateIssueDateIfReachable()?.toJavaLocalDate(),
            gasSafetyFileUploadIds = state.gasUploadIds,
            electricalSafetyFileUploadIds = state.electricalUploadIds,
            electricalSafetyExpiryDate = state.getElectricalCertificateExpiryDateIfReachable()?.toJavaLocalDate(),
            epcCertificateUrl =
                state.acceptedEpcIfReachable?.let {
                    epcCertificateUrlProvider.getEpcCertificateUrl(it.certificateNumber)
                },
            epcExpiryDate = state.acceptedEpcIfReachable?.expiryDateAsJavaLocalDate,
            epcEnergyRating = state.acceptedEpcIfReachable?.energyRating,
            tenancyStartedBeforeEpcExpiry =
                state.epcInDateAtStartOfTenancyCheckStep
                    .formModelIfReachableOrNull
                    ?.tenancyStartedBeforeExpiry,
            epcExemptionReason =
                state.epcExemptionStep
                    .formModelIfReachableOrNull
                    ?.exemptionReason,
            epcMeesExemptionReason =
                state.meesExemptionStep
                    .formModelIfReachableOrNull
                    ?.exemptionReason,
        )
    }
}

@JourneyFrameworkComponent
class SavePropertyRegistrationDataStep(
    stepConfig: SavePropertyRegistrationDataStepConfig,
) : JourneyStep.InternalStep<Complete, PropertyRegistrationJourneyState>(stepConfig)
