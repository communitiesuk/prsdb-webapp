package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toKotlinInstant
import uk.gov.communities.prsdb.webapp.constants.REGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.RESUME_PAGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.database.entity.FormContext
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.PropertyRegistrationJourneyDataHelper
import uk.gov.communities.prsdb.webapp.helpers.extensions.addAction
import uk.gov.communities.prsdb.webapp.helpers.extensions.addCard
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.JourneyDataExtensions.Companion.getLookedUpAddresses
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityService
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory
import java.time.Instant

class IncompletePropertiesViewModel(
    private val formContexts: List<FormContext>,
    journeyDataServiceFactory: JourneyDataServiceFactory,
    private val localAuthorityService: LocalAuthorityService,
) {
    val journeyDataService = journeyDataServiceFactory.create(REGISTER_PROPERTY_JOURNEY_URL)

    val incompleteProperties: List<SummaryCardViewModel> = getIncompleteProperties()

    private fun getIncompleteProperties(): List<SummaryCardViewModel> {
        val incompleteProperties = mutableListOf<SummaryCardViewModel>()

        formContexts.forEach { formContext ->
            val completeByDate = getCompleteByDate(formContext.createdDate)
            if (DateTimeHelper.isDateInPast(completeByDate)) {
                return@forEach
            }

            val address = getAddressData(formContext)

            incompleteProperties
                .apply {
                    addCard(
                        title = "landlord.incompleteProperties.summaryCardTitlePrefix",
                        summaryList = getSummaryList(address, completeByDate),
                        actions = getActions(formContext.id),
                    )
                }.toList()
        }

        return incompleteProperties
    }

    // TODO PRSD-1127 make AddressDataModel param not nullable
    private fun getSummaryList(
        address: AddressDataModel?,
        completeByDate: LocalDate,
    ): List<SummaryListRowViewModel> =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                addRow(
                    "landlord.incompleteProperties.summaryRow.propertyAddress",
                    // TODO PRSD-1127 remove if statement and "Not yet completed" option as address should always exists now
                    if (address?.singleLineAddress != null) {
                        address.singleLineAddress
                    } else {
                        "Not yet completed"
                    },
                )
                addRow(
                    "landlord.incompleteProperties.summaryRow.localAuthority",
                    // TODO PRSD-1127 remove if statement and "Not yet completed" option as address should always exists now
                    if (address?.localAuthorityId != null) {
                        localAuthorityService.retrieveLocalAuthorityById(address.localAuthorityId).name
                    } else {
                        "Not yet completed"
                    },
                )
                addRow(
                    "landlord.incompleteProperties.summaryRow.completeBy",
                    completeByDate,
                )
            }.toList()

    private fun getActions(contextId: Long): List<SummaryCardActionViewModel> =
        mutableListOf<SummaryCardActionViewModel>()
            .apply {
                addAction(
                    "landlord.incompleteProperties.action.continue",
                    "/$REGISTER_PROPERTY_JOURNEY_URL/$RESUME_PAGE_PATH_SEGMENT?contextId=$contextId",
                )
                addAction(
                    "landlord.incompleteProperties.action.delete",
                    // TODO PRSD-700 change url below to point to the delete incomplete properties endpoint
                    "/$REGISTER_PROPERTY_JOURNEY_URL/delete-incomplete-property?contextId=$contextId",
                )
            }.toList()

    private fun getCompleteByDate(createdDate: Instant): LocalDate {
        val createdDateInUk = DateTimeHelper.getDateInUK(createdDate.toKotlinInstant())
        return DateTimeHelper.get28DaysFromDate(createdDateInUk)
    }

    private fun getAddressData(formContext: FormContext): AddressDataModel? {
        val formContextJourneyData = journeyDataService.getFormContextAsJourneyData(formContext)!!
        val lookedUpAddresses = formContextJourneyData.getLookedUpAddresses()
        // TODO PRSD-1127 new ticket number set this to return a not nullable AddressDataModel
        return PropertyRegistrationJourneyDataHelper.getAddress(formContextJourneyData, lookedUpAddresses)
    }
}
