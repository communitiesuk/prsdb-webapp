package uk.gov.communities.prsdb.webapp.services

import kotlinx.datetime.LocalDate
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.FormContext
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.PropertyRegistrationJourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.IncompletePropertiesDataModel

interface IncompletePropertyService {
    fun getIncompletePropertiesForLandlord(principalName: String): List<IncompletePropertiesDataModel>

    fun deleteIncompleteProperty(
        journeyId: String,
        principalName: String,
    )

    fun getAddressData(
        incompletePropertyId: String,
        principalName: String,
    ): String

    fun isIncompletePropertyAvailable(
        incompletePropertyId: String,
        principalName: String,
    ): Boolean
}

@PrsdbWebService
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
            contextId = formContext.id,
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
