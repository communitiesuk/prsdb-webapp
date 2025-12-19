package uk.gov.communities.prsdb.webapp.services

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toKotlinInstant
import kotlinx.serialization.json.Json
import org.springframework.context.annotation.Primary
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbFlip
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.MIGRATE_PROPERTY_REGISTRATION
import uk.gov.communities.prsdb.webapp.database.entity.FormContext
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.SavedJourneyStateRepository
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.PropertyRegistrationJourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.IncompletePropertiesDataModel
import kotlin.time.Duration.Companion.days

interface IncompletePropertyService {
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
}

@PrsdbWebService("newIncompletePropertyService")
class IncompletePropertyServiceImpl(
    private val repository: SavedJourneyStateRepository,
    private val objectMapper: ObjectMapper,
    private val landlordRepository: LandlordRepository,
) : IncompletePropertyService {
    override fun getIncompletePropertiesForLandlord(principalName: String): List<IncompletePropertiesDataModel> =
        landlordRepository.findByBaseUser_Id(principalName)?.let { landlord ->
            landlord.incompleteProperties.map { savedState ->
                IncompletePropertiesDataModel(
                    journeyId = savedState.journeyId,
                    singleLineAddress = savedState.getPropertyRegistrationSingleLineAddress(),
                    completeByDate = DateTimeHelper.getDateInUK(savedState.getMostRecentlyUpdated().toKotlinInstant().plus(28.days)),
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
            ?.getPropertyRegistrationSingleLineAddress()
            ?: throw IllegalArgumentException(
                "Incomplete property not found for id: $incompletePropertyId and principal: $principalName",
            )

    override fun isIncompletePropertyAvailable(
        incompletePropertyId: String,
        principalName: String,
    ): Boolean = repository.existsByJourneyIdAndUser_Id(incompletePropertyId, principalName)

    private fun SavedJourneyState.getPropertyRegistrationSingleLineAddress(): String {
        val stateDataMap = objectMapper.readValue(serializedState, Map::class.java)
        val submittedJourneyData = stateDataMap["journeyData"] as Map<*, *>
        val selectedAddressData = submittedJourneyData["select-address"] as? Map<*, *>
        val selectedAddress = selectedAddressData?.get("address") as? String
        val serializedCachedAddressData = stateDataMap["cachedAddresses"] as String
        val cachedAddressData: List<AddressDataModel> = Json.decodeFromString(serializedCachedAddressData)

        return if (cachedAddressData.any { it.singleLineAddress == selectedAddress }) {
            selectedAddress!!
        } else {
            val manualAddressData = submittedJourneyData["manual-address"] as Map<*, *>
            val localCouncilData = submittedJourneyData["local-council"] as Map<*, *>
            AddressDataModel
                .fromManualAddressData(
                    addressLineOne = manualAddressData["addressLineOne"] as String,
                    addressLineTwo = manualAddressData["addressLineTwo"] as String?,
                    townOrCity = manualAddressData["townOrCity"] as String,
                    county = manualAddressData["county"] as String?,
                    postcode = manualAddressData["postcode"] as String,
                    localCouncilId = localCouncilData["localCouncilId"] as Int?,
                ).singleLineAddress
        }
    }
}

@PrsdbWebService("legacyIncompletePropertyService")
@Primary
class LegacyIncompletePropertyService(
    private val propertyRegistrationService: LegacyIncompletePropertyFormContextService,
) : IncompletePropertyService {
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
}
