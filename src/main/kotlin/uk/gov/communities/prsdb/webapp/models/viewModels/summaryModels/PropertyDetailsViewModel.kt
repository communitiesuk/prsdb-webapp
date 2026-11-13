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
    // TODO PDJB-2016: remove this parameter and the flag-off branches below with the flag. It
    //  defaults to the flag-on behaviour so that removing it leaves the right default.
    associatedPartiesEnabled: Boolean = true,
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
    // TODO PDJB-2016: remove this property, showOwnershipSectionOnPropertyDetailsTab and the
    //  th:if blocks they drive in propertyDetailsView.html and fragments/landlordDetails.html.
    //  Once PDJB-1999 is done both views always use the flag-on version.
    val showPeopleResponsibleOwnership: Boolean = isLandlordView && associatedPartiesEnabled

    val showOwnershipSectionOnPropertyDetailsTab: Boolean = !showPeopleResponsibleOwnership

    val peopleResponsibleTabHeadingKey: String =
        if (associatedPartiesEnabled) {
            "propertyDetails.peopleResponsible.heading"
        } else {
            "propertyDetails.landlordDetails.heading"
        }

    val peopleResponsibleOwnershipHeadingKey: String = "propertyDetails.peopleResponsible.ownership.heading"

    val peopleResponsibleOwnershipSection: List<SummaryListRowViewModel> =
        if (showPeopleResponsibleOwnership) ownershipAnswerRows() else emptyList()

    val showFreeholderSection: Boolean =
        showPeopleResponsibleOwnership && propertyOwnership.ownershipType.requiresFreeholderRecords()

    val freeholderSectionHeadingKey: String =
        if (propertyOwnership.ownershipType.isJointFreeholder()) {
            "registerProperty.freeholders.checkAnswers.heading.joint"
        } else {
            "registerProperty.freeholders.checkAnswers.heading.nonJoint"
        }

    val freeholderSectionLinkTextKey: String =
        if (propertyOwnership.ownershipType.isJointFreeholder()) {
            "registerProperty.freeholders.checkAnswers.changeLink.joint"
        } else {
            "registerProperty.freeholders.checkAnswers.changeLink.nonJoint"
        }

    // TODO PDJB-1993: Replace this placeholder URL with the freeholder update journey route.
    val freeholderSectionUpdateUrl: String = PLACEHOLDER_ACTION_LINK

    val registrationDetails: List<SummaryListRowViewModel> =
        listOf(registrationNumberRow(), registrationDateRow())

    val propertyDetailsSection: List<SummaryListRowViewModel> =
        listOf(addressRow(), localCouncilRow(), propertyTypeRow(), bedroomsRow())

    val ownershipSectionBeforePdjb955: List<SummaryListRowViewModel> =
        if (showOwnershipSectionOnPropertyDetailsTab) listOf(ownershipTypeRowBeforePdjb955()) else emptyList()

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
