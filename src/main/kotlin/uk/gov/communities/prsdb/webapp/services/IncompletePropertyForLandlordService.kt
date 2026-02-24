package uk.gov.communities.prsdb.webapp.services

import kotlinx.datetime.LocalDate
import org.springframework.context.annotation.Primary
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbFlip
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.MIGRATE_PROPERTY_REGISTRATION
import uk.gov.communities.prsdb.webapp.database.entity.FormContext
import uk.gov.communities.prsdb.webapp.database.entity.LandlordIncompleteProperties
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import uk.gov.communities.prsdb.webapp.database.repository.LandlordIncompletePropertiesRepository
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.SavedJourneyStateRepository
import uk.gov.communities.prsdb.webapp.helpers.CompleteByDateHelper
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.PropertyRegistrationJourneyDataHelper
import uk.gov.communities.prsdb.webapp.helpers.extensions.savedJourneyStateExtensions.SavedJourneyStateExtensions.Companion.getPropertyRegistrationSingleLineAddress
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.IncompletePropertiesDataModel

interface IncompletePropertyForLandlordService {
    @PrsdbFlip(name = MIGRATE_PROPERTY_REGISTRATION, alterBean = "newIncompletePropertyService")
    fun getIncompletePropertiesForLandlord(principalName: String): List<IncompletePropertiesDataModel>

    @PrsdbFlip(name = MIGRATE_PROPERTY_REGISTRATION, alterBean = "newIncompletePropertyService")
    fun deleteIncompleteProperty(
        journeyId: String,
        principalName: String,
    )

    @PrsdbFlip(name = MIGRATE_PROPERTY_REGISTRATION, alterBean = "newIncompletePropertyService")
    fun getAddressData(
        incompletePropertyId: String,
        principalName: String,
    ): String

    @PrsdbFlip(name = MIGRATE_PROPERTY_REGISTRATION, alterBean = "newIncompletePropertyService")
    fun isIncompletePropertyAvailable(
        incompletePropertyId: String,
        principalName: String,
    ): Boolean

    @PrsdbFlip(name = MIGRATE_PROPERTY_REGISTRATION, alterBean = "newIncompletePropertyService")
    fun addIncompletePropertyToLandlord(state: SavedJourneyState)
}

@PrsdbWebService("newIncompletePropertyService")
class IncompletePropertyForLandlordServiceImpl(
    private val repository: SavedJourneyStateRepository,
    private val landlordRepository: LandlordRepository,
    private val landlordIncompletePropertiesRepository: LandlordIncompletePropertiesRepository,
) : IncompletePropertyForLandlordService {
    override fun getIncompletePropertiesForLandlord(principalName: String): List<IncompletePropertiesDataModel> =
        landlordRepository.findByBaseUser_Id(principalName)?.let { landlord ->
            landlord.incompleteProperties
                .sortedBy { it.createdDate }
                .map { savedState ->
                    IncompletePropertiesDataModel(
                        journeyId = savedState.journeyId,
                        singleLineAddress = savedState.getPropertyRegistrationSingleLineAddress(),
                        completeByDate = CompleteByDateHelper.getIncompletePropertyCompleteByDateFromSavedJourneyState(savedState),
                    )
                }
        } ?: throw IllegalArgumentException("Landlord not found for principal: $principalName")

    override fun deleteIncompleteProperty(
        journeyId: String,
        principalName: String,
    ) = repository.deleteByJourneyIdAndUser_Id(journeyId, principalName)

    override fun getAddressData(
        incompletePropertyId: String,
        principalName: String,
    ): String =
        repository
            .findByJourneyIdAndUser_Id(incompletePropertyId, principalName)
            ?.let { savedJourneyState ->
                savedJourneyState.getPropertyRegistrationSingleLineAddress()
            }
            ?: throw IllegalArgumentException(
                "Incomplete property not found for id: $incompletePropertyId and principal: $principalName",
            )

    override fun isIncompletePropertyAvailable(
        incompletePropertyId: String,
        principalName: String,
    ): Boolean = repository.existsByJourneyIdAndUser_Id(incompletePropertyId, principalName)

    override fun addIncompletePropertyToLandlord(state: SavedJourneyState) {
        landlordRepository.findByBaseUser_Id(state.user.id)?.let { landlord ->
            val newEntry =
                LandlordIncompleteProperties(
                    landlord = landlord,
                    savedJourneyState = state,
                )
            landlordIncompletePropertiesRepository.save(newEntry)
        }
    }
}

@PrsdbWebService("legacyIncompletePropertyService")
@Primary
class LegacyIncompletePropertyForLandlordService(
    private val propertyRegistrationService: LegacyIncompletePropertyFormContextService,
) : IncompletePropertyForLandlordService {
    override fun isIncompletePropertyAvailable(
        incompletePropertyId: String,
        principalName: String,
    ): Boolean = propertyRegistrationService.getFormContext(incompletePropertyId.toLong()) != null

    override fun getIncompletePropertiesForLandlord(principalName: String): List<IncompletePropertiesDataModel> {
        val formContexts = propertyRegistrationService.getAllInDateIncompletePropertiesForLandlord(principalName)

        val incompleteProperties = mutableListOf<IncompletePropertiesDataModel>()

        formContexts.forEach { formContext ->
            val completeByDate = LegacyIncompletePropertyFormContextService.getIncompletePropertyCompleteByDate(formContext.createdDate)

            if (!DateTimeHelper().isDateInPast(completeByDate)) {
                incompleteProperties.add(getIncompletePropertiesDataModels(formContext, completeByDate))
            }
        }
        return incompleteProperties
    }

    override fun deleteIncompleteProperty(
        journeyId: String,
        principalName: String,
    ) {
        val formContext =
            propertyRegistrationService.getIncompletePropertyFormContextForLandlordOrThrowNotFound(
                journeyId.toLong(),
                principalName,
            )
        propertyRegistrationService.deleteFormContext(formContext)
    }

    private fun getIncompletePropertiesDataModels(
        formContext: FormContext,
        completeByDate: LocalDate,
    ): IncompletePropertiesDataModel {
        val address = formContext.toAddressData()

        return IncompletePropertiesDataModel(
            journeyId = formContext.id.toString(),
            completeByDate = completeByDate,
            singleLineAddress = address.singleLineAddress,
        )
    }

    override fun getAddressData(
        incompletePropertyId: String,
        principalName: String,
    ): String {
        val incompletePropertyFormContext =
            propertyRegistrationService.getIncompletePropertyFormContextForLandlordIfNotExpired(
                incompletePropertyId.toLong(),
                principalName,
            )
        return incompletePropertyFormContext.toAddressData().singleLineAddress
    }

    private fun FormContext.toAddressData(): AddressDataModel = PropertyRegistrationJourneyDataHelper.getAddress(this.toJourneyData())!!

    override fun addIncompletePropertyToLandlord(state: SavedJourneyState) {
        // Not used for legacy service
    }
}
