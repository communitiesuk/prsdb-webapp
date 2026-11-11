package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.controllers.LandlordUpdateCorrespondenceAddressController
import uk.gov.communities.prsdb.webapp.controllers.LandlordUpdateCorrespondenceEmailController
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.helpers.extensions.MessageSourceExtensions.Companion.getMessageForKey

class PropertyDetailsViewModel(
    propertyOwnership: PropertyOwnership,
    isLandlordView: Boolean = true,
    messageSource: MessageSource,
    // TODO PDJB-2016: remove this parameter and the flag-off branches below with the flag
    associatedPartiesEnabled: Boolean = false,
) : PropertyDetailsViewModelBase(
        propertyOwnership,
        if (isLandlordView) PropertyDetailsViewType.LANDLORD else PropertyDetailsViewType.LOCAL_COUNCIL,
        messageSource,
    ) {
    val showTenancySection: Boolean = isOccupied

    val tenancyHeadingKey: String = "propertyDetails.propertyRecord.tenancy.heading"

    // The landlord's ownership information moves from the property record tab to the people
    // responsible tab when the flag is on.
    // TODO PDJB-1999: give the local council view its own ownership section on the people
    //  responsible tab. Until then it keeps the single legacy row on the property record tab.
    val showPeopleResponsibleOwnership: Boolean = isLandlordView && associatedPartiesEnabled

    val showOwnershipSection: Boolean = !showPeopleResponsibleOwnership

    val peopleResponsibleTabHeadingKey: String =
        if (associatedPartiesEnabled) {
            "propertyDetails.peopleResponsible.heading"
        } else {
            "propertyDetails.landlordDetails.heading"
        }

    val peopleResponsibleOwnershipHeadingKey: String = "propertyDetails.peopleResponsible.ownership.heading"

    val peopleResponsibleOwnershipSection: List<SummaryListRowViewModel> =
        if (showPeopleResponsibleOwnership) ownershipAnswerRows() else emptyList()

    val registrationDetails: List<SummaryListRowViewModel> =
        listOf(registrationNumberRow(), registrationDateRow())

    val propertyDetailsSection: List<SummaryListRowViewModel> =
        listOf(addressRow(), localCouncilRow(), propertyTypeRow(), bedroomsRow())

    val ownershipSection: List<SummaryListRowViewModel> =
        if (showOwnershipSection) listOf(ownershipTypeRow()) else emptyList()

    val correspondenceSection: List<SummaryListRowViewModel> =
        listOf(
            rowWithViewTypeSpecificChangeLink(
                key = "propertyDetails.propertyRecord.correspondence.emailAddress",
                value = propertyOwnership.correspondenceEmail,
                landlordActionLink =
                    LandlordUpdateCorrespondenceEmailController
                        .getUpdateCorrespondenceEmailFirstStepRoute(propertyOwnership.id),
            ),
            rowWithViewTypeSpecificChangeLink(
                key = "propertyDetails.propertyRecord.correspondence.address",
                value = propertyOwnership.correspondenceAddress.toMultiLineAddress().split("\n"),
                landlordActionLink =
                    LandlordUpdateCorrespondenceAddressController
                        .getUpdateCorrespondenceAddressFirstStepRoute(propertyOwnership.id),
            ),
        )

    val occupiedSection: List<SummaryListRowViewModel> =
        listOf(occupiedRow())

    val licensingSection: List<SummaryListRowViewModel> = buildLicensingSection()

    val licensingProvideLaterParagraph: String? =
        if (isLicensingProvideLater && !isLandlordView) {
            if (hasBeenOccupiedSinceRegistration) {
                getProvideLaterDeadlineText("propertyDetails.propertyRecord.licensing.councilOccupied")
            } else {
                messageSource.getMessageForKey("propertyDetails.propertyRecord.licensing.councilNotProvided")
            }
        } else {
            null
        }

    val tenancySection: List<SummaryListRowViewModel> = buildTenancySection()

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
}
