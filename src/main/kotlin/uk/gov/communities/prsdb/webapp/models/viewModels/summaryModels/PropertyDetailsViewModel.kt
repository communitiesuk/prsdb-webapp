package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.controllers.LandlordUpdateCorrespondenceAddressController
import uk.gov.communities.prsdb.webapp.controllers.LandlordUpdateCorrespondenceEmailController
import uk.gov.communities.prsdb.webapp.controllers.UpdateFreeholdersController
import uk.gov.communities.prsdb.webapp.controllers.UpdatePropertyManagerController
import uk.gov.communities.prsdb.webapp.controllers.UpdateSuperiorLandlordsController
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.helpers.extensions.MessageSourceExtensions.Companion.getMessageForKey
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.FreeholderSummaryCardHelper
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.SuperiorLandlordSummaryCardHelper

class PropertyDetailsViewModel(
    propertyOwnership: PropertyOwnership,
    viewType: PropertyDetailsViewType = PropertyDetailsViewType.LANDLORD,
    messageSource: MessageSource,
    // TODO PDJB-2016: remove this parameter and the flag-off branches below with the flag. It
    //  defaults to the flag-on behaviour so that removing it leaves the right default.
    associatedPartiesEnabled: Boolean = true,
) : PropertyDetailsViewModelBase(
        propertyOwnership,
        viewType,
        messageSource,
    ) {
    val showTenancySection: Boolean = isOccupied

    val tenancyHeadingKey: String = "propertyDetails.propertyRecord.tenancy.heading"

    // The ownership information moves from the property record tab to the people responsible tab
    // when the flag is on.
    // TODO PDJB-2016: remove this property, showOwnershipSectionOnPropertyDetailsTab and the
    //  th:if blocks they drive in propertyDetailsView.html and fragments/peopleResponsible.html.
    val showPeopleResponsibleOwnership: Boolean = associatedPartiesEnabled

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

    private val freeholderHeadingType = if (propertyOwnership.ownershipType.isJointFreeholder()) "joint" else "nonJoint"

    val freeholderSectionHeadingKey: String = "registerProperty.freeholders.checkAnswers.heading.$freeholderHeadingType"

    val freeholderSectionLinkTextKey: String = "registerProperty.freeholders.checkAnswers.changeLink.$freeholderHeadingType"

    val showFreeholderSectionUpdateLink: Boolean = viewType == PropertyDetailsViewType.LANDLORD

    val unknownFreeholderBodyTextKey: String =
        if (viewType == PropertyDetailsViewType.LOCAL_COUNCIL) {
            "propertyDetails.peopleResponsible.freeholder.unknownDetailsUnavailable"
        } else {
            FreeholderSummaryCardHelper.UNKNOWN_DETAILS_BODY_KEY
        }

    val freeholderSectionUpdateUrl: String =
        UpdateFreeholdersController.getUpdateFreeholdersFirstStepRoute(propertyOwnership.id)

    val showSuperiorLandlordSection: Boolean =
        showPeopleResponsibleOwnership && propertyOwnership.ownershipType.requiresSuperiorLandlordInfo()

    val hasSuperiorLandlords: Boolean = propertyOwnership.superiorLandlords.isNotEmpty()

    val showSuperiorLandlordSectionUpdateLink: Boolean = viewType == PropertyDetailsViewType.LANDLORD

    val unknownSuperiorLandlordBodyTextKey: String =
        if (viewType == PropertyDetailsViewType.LOCAL_COUNCIL) {
            "propertyDetails.peopleResponsible.superiorLandlord.unknownDetailsUnavailable"
        } else {
            SuperiorLandlordSummaryCardHelper.UNKNOWN_DETAILS_BODY_KEY
        }

    val superiorLandlordSectionUpdateUrl: String =
        UpdateSuperiorLandlordsController.getUpdateSuperiorLandlordsFirstStepRoute(propertyOwnership.id)

    val superiorLandlordNotUsedRows: List<SummaryListRowViewModel> =
        listOf(
            rowWithViewTypeSpecificChangeLink(
                key = "registerProperty.superiorLandlords.checkAnswers.anyoneInOwnershipChain",
                // Resolves as 'No'
                value = MessageKeyConverter.convert(false),
                landlordActionLink = superiorLandlordSectionUpdateUrl,
            ),
        )

    // TODO PDJB-2016: Remove showPropertyManagerSection, its guard in PropertyDetailsController and the th:if it
    //  drives in fragments/peopleResponsible.html when the ASSOCIATED_PARTIES flag is removed.
    val showPropertyManagerSection: Boolean = showPeopleResponsibleOwnership

    val hasPropertyManager: Boolean = propertyOwnership.propertyManager != null

    val propertyManagerUpdateUrl: String = UpdatePropertyManagerController.getUpdatePropertyManagerFirstStepRoute(propertyOwnership.id)

    val showPropertyManagerUpdateLink: Boolean = viewType == PropertyDetailsViewType.LANDLORD

    val propertyManagerNotUsedRows: List<SummaryListRowViewModel> =
        if (showPropertyManagerSection && !hasPropertyManager) {
            listOf(
                rowWithViewTypeSpecificChangeLink(
                    key = "registerProperty.propertyManager.usingPropertyManager.fieldSetHeading",
                    value = "registerProperty.propertyManager.usingPropertyManager.radios.no",
                    landlordActionLink = propertyManagerUpdateUrl,
                ),
            )
        } else {
            emptyList()
        }

    val registrationDetails: List<SummaryListRowViewModel> =
        listOf(registrationNumberRow(), registrationDateRow())

    val propertyDetailsSection: List<SummaryListRowViewModel> =
        listOf(addressRow(), localCouncilRow(), propertyTypeRow(), bedroomsRow())

    // TODO PDJB-2016: remove this legacy ownership section when the ASSOCIATED_PARTIES flag is removed.
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
        if (isLicensingProvideLater && viewType == PropertyDetailsViewType.LOCAL_COUNCIL) {
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
            !showTenancySection || viewType != PropertyDetailsViewType.LOCAL_COUNCIL -> {
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
