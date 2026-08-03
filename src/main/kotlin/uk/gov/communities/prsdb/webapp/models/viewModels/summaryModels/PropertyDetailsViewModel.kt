package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.controllers.UpdateLicensingController.Companion.getUpdateLicensingBaseRoute
import uk.gov.communities.prsdb.webapp.controllers.UpdateTenancyDetailsController
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.helpers.extensions.MessageSourceExtensions.Companion.getMessageForKey
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LicensingTypeStep

class PropertyDetailsViewModel(
    propertyOwnership: PropertyOwnership,
    isLandlordView: Boolean = true,
    messageSource: MessageSource,
) : PropertyDetailsViewModelBase(propertyOwnership, isLandlordView, messageSource) {
    val isLicensingProvideLater: Boolean = propertyOwnership.licenseProvideLater == true

    val isTenancyProvideLater: Boolean = propertyOwnership.tenancyProvideLater == true

    val showTenancySection: Boolean = isOccupied

    val tenancyHeadingKey: String = "propertyDetails.propertyRecord.tenancy.heading"

    val registrationDetails: List<SummaryListRowViewModel> =
        listOf(registrationNumberRow(), registrationDateRow())

    val propertyDetailsSection: List<SummaryListRowViewModel> =
        listOf(addressRow(), localCouncilRow(), propertyTypeRow(), bedroomsRow())

    val ownershipSection: List<SummaryListRowViewModel> =
        listOf(ownershipTypeRow("propertyDetails.propertyRecord.ownership.ownershipType"))

    val occupiedSection: List<SummaryListRowViewModel> =
        listOf(occupiedRow("propertyDetails.propertyRecord.occupation.isOccupied"))

    val licensingSection: List<SummaryListRowViewModel> =
        when {
            !isLicensingProvideLater -> listOfNotNull(licensingTypeRow(), licensingNumberRow())
            isLandlordView -> listOf(licensingProvideLaterRow())
            else -> emptyList()
        }

    val licensingProvideLaterParagraph: String? =
        if (isLicensingProvideLater && !isLandlordView) {
            if (isOccupied && hasBeenOccupiedSinceRegistration) {
                getProvideLaterDeadlineText("propertyDetails.propertyRecord.licensing.councilOccupied")
            } else {
                messageSource.getMessageForKey("propertyDetails.propertyRecord.licensing.councilNotProvided")
            }
        } else {
            null
        }

    val tenancySection: List<SummaryListRowViewModel> =
        when {
            !showTenancySection -> {
                emptyList()
            }

            isTenancyProvideLater && isLandlordView -> {
                listOf(tenancyProvideLaterRow())
            }

            isTenancyProvideLater && !isLandlordView -> {
                emptyList()
            }

            else -> {
                buildList {
                    add(householdsRow())
                    add(tenantsRow())
                    add(rentFrequencyRow())
                    add(furnishedStatusRow())
                    add(rentIncludesBillsRow())
                    if (propertyOwnership.rentIncludesBills) add(billsIncludedRow(includeChangeLink = false))
                    add(rentAmountRow(includeChangeLink = false))
                }
            }
        }

    val tenancyProvideLaterParagraph: String? =
        when {
            !showTenancySection || isLandlordView -> {
                null
            }

            isTenancyProvideLater && hasBeenOccupiedSinceRegistration -> {
                getProvideLaterDeadlineText("propertyDetails.propertyRecord.tenancy.councilOccupied")
            }

            isTenancyProvideLater -> {
                messageSource.getMessageForKey("propertyDetails.propertyRecord.tenancy.councilNotProvided")
            }

            else -> {
                null
            }
        }

    private fun licensingProvideLaterRow(): SummaryListRowViewModel =
        row(
            "propertyDetails.propertyRecord.licensing.rowName",
            if (isOccupied && hasBeenOccupiedSinceRegistration) {
                getProvideLaterDeadlineText("propertyDetails.propertyRecord.licensing.provideLaterOccupied")
            } else {
                "propertyDetails.propertyRecord.licensing.provideLaterUnoccupied"
            },
            changeLinkMessageKey,
            getUpdateLicensingBaseRoute(propertyOwnership.id) +
                "/${LicensingTypeStep.ROUTE_SEGMENT}",
            withChangeLinks,
        )

    private fun tenancyProvideLaterRow(): SummaryListRowViewModel =
        row(
            "propertyDetails.propertyRecord.tenancy.rowName",
            if (hasBeenOccupiedSinceRegistration) {
                getProvideLaterDeadlineText("propertyDetails.propertyRecord.tenancy.provideLaterOccupied")
            } else {
                "propertyDetails.propertyRecord.tenancy.provideLaterUnoccupied"
            },
            changeLinkMessageKey,
            UpdateTenancyDetailsController.getUpdateTenancyDetailsRoute(propertyOwnership.id) +
                "/${HouseholdStep.ROUTE_SEGMENT}",
            withChangeLinks,
        )
}
