package uk.gov.communities.prsdb.webapp.integration.journeyMap.descriptors

import uk.gov.communities.prsdb.webapp.constants.MULTI_USER_ORGANISATIONS
import uk.gov.communities.prsdb.webapp.constants.TASK_LIST_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.TOKEN
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrRejectJointLandlordInvitationController
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrganisationalLandlordUserInvitationController
import uk.gov.communities.prsdb.webapp.controllers.CancelJointLandlordInvitationController
import uk.gov.communities.prsdb.webapp.controllers.CancelLettingAgentDelegationController
import uk.gov.communities.prsdb.webapp.controllers.ChangeOrgTeamMemberAccessLevelController
import uk.gov.communities.prsdb.webapp.controllers.DelegateToLettingAgentController
import uk.gov.communities.prsdb.webapp.controllers.DeregisterLandlordController
import uk.gov.communities.prsdb.webapp.controllers.DeregisterOrganisationalLandlordController
import uk.gov.communities.prsdb.webapp.controllers.DeregisterPropertyController
import uk.gov.communities.prsdb.webapp.controllers.InviteJointLandlordController
import uk.gov.communities.prsdb.webapp.controllers.InviteOrganisationalLandlordUserController
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.controllers.LandlordUpdateCorrespondenceAddressController
import uk.gov.communities.prsdb.webapp.controllers.LandlordUpdateCorrespondenceEmailController
import uk.gov.communities.prsdb.webapp.controllers.LandlordUpdateElectricalSafetyController
import uk.gov.communities.prsdb.webapp.controllers.LandlordUpdateEpcController
import uk.gov.communities.prsdb.webapp.controllers.LandlordUpdateFurnishedStatusController
import uk.gov.communities.prsdb.webapp.controllers.LandlordUpdateGasSafetyController
import uk.gov.communities.prsdb.webapp.controllers.LandlordUpdateHouseholdsAndTenantsController
import uk.gov.communities.prsdb.webapp.controllers.LandlordUpdateLicensingController
import uk.gov.communities.prsdb.webapp.controllers.LandlordUpdateRentFrequencyAndAmountController
import uk.gov.communities.prsdb.webapp.controllers.LandlordUpdateRentIncludesBillsController
import uk.gov.communities.prsdb.webapp.controllers.LandlordUpdateTenancyDetailsController
import uk.gov.communities.prsdb.webapp.controllers.LeavePropertyController
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentInvitationController
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.controllers.RegisterLocalCouncilUserController
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.controllers.SwitchToIndividualController
import uk.gov.communities.prsdb.webapp.controllers.TeamMembersController
import uk.gov.communities.prsdb.webapp.controllers.UpdateBedroomsController
import uk.gov.communities.prsdb.webapp.controllers.UpdateCompaniesHouseController
import uk.gov.communities.prsdb.webapp.controllers.UpdateGoverningBodyController
import uk.gov.communities.prsdb.webapp.controllers.UpdateLandlordAddressController
import uk.gov.communities.prsdb.webapp.controllers.UpdateLandlordDateOfBirthController
import uk.gov.communities.prsdb.webapp.controllers.UpdateLandlordEmailController
import uk.gov.communities.prsdb.webapp.controllers.UpdateLandlordNameController
import uk.gov.communities.prsdb.webapp.controllers.UpdateLandlordPhoneNumberController
import uk.gov.communities.prsdb.webapp.controllers.UpdateLeadTrusteeController
import uk.gov.communities.prsdb.webapp.controllers.UpdateOccupancyController
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationLandlordCharityController
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationLandlordEmailController
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationLandlordNameController
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationLandlordPhoneNumberController
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationMainContactController
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationTypeController
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationalLandlordAddressController
import uk.gov.communities.prsdb.webapp.controllers.UpdateOwnershipTypeController
import uk.gov.communities.prsdb.webapp.integration.journeyMap.JourneyMapHarness
import uk.gov.communities.prsdb.webapp.journeyMap.discovery.SeededAddress

object JourneyDescriptors {
    private const val PROPERTY_OWNERSHIP_ID = 1L
    private const val LOCAL_SEED = "data-local.sql"
    private const val PROPERTY_WITHOUT_LETTING_AGENT_ID = 4L
    private const val PROPERTY_WITH_PENDING_INVITATIONS_ID = 13L
    private const val JOINT_PROPERTY_ID = 2L
    private const val TEAM_MEMBER_EDITOR_ID = 902L
    private const val JOINT_LANDLORD_INVITATION_SEED = "data-joint-landlord-invitation.sql"
    private const val JOINT_LANDLORD_INVITATION_TOKEN = "aaaabbbb-cccc-dddd-eeee-ffff00001111"
    private const val PROPERTY_WITH_PENDING_INVITATION_ID = 2L
    private const val PENDING_INVITATION_ID = 3L
    private const val ORGANISATION_INVITATION_TOKEN = "1234abcd-5678-abcd-1234-567abcd2222a"
    private const val LETTING_AGENT_TOKEN = "3334abcd-5678-abcd-1234-567abcd1111a"
    private const val LOCAL_COUNCIL_INVITATION_TOKEN = "c0c0c0c0-1111-2222-3333-444455556666"
    private const val INVITATION_EMAIL_LINK = "Link in the invitation email"

    // Property update journeys copy these from the property when they open, so each is explored on its own data
    private val propertyIsVacant = StartingPoint("Property is vacant", listOf(LOCAL_SEED, "journey-map/seeds/property-vacant.sql"))
    private val propertyHasLicence =
        StartingPoint("Property has a licence", listOf(LOCAL_SEED, "journey-map/seeds/property-licensed.sql"))

    // This postcode has both registered and unregistered addresses, so selecting each one reaches every branch
    private val registeredAndUnregisteredAddresses = SeededAddress("AA3 1AB", "Slate House")

    // Name and date of birth can only be changed by landlords whose identity was not verified through One Login
    private val unverifiedLandlordSeeds = listOf(LOCAL_SEED, "journey-map/seeds/landlord-unverified.sql")
    private val organisationAdminSeeds =
        listOf(LOCAL_SEED, "data-overlay-mockuser-is-not-a-landlord.sql", "journey-map/seeds/mockuser-is-org-admin.sql")
    private val organisationIsTrust =
        StartingPoint("Organisation is a trust", organisationAdminSeeds + "journey-map/seeds/organisation-is-trust.sql")

    val all =
        listOf(
            JourneyDescriptor(
                name = "landlordRegistration",
                title = "Register as a landlord",
                basePath = RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE,
                seedScripts = listOf("data-mockuser-not-landlord.sql"),
                start = { it.navigateToLandlordRegistrationPrivacyNoticePage() },
                address = SeededAddress("EG1 2AA", "1"),
            ),
            JourneyDescriptor(
                name = "propertyRegistration",
                title = "Register a property",
                basePath = RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE,
                seedScripts = listOf("data-local.sql"),
                start = { it.navigate("${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/$TASK_LIST_PATH_SEGMENT") },
                address = registeredAndUnregisteredAddresses,
                singleRenderPaths = setOf(TASK_LIST_PATH_SEGMENT),
            ),
            JourneyDescriptor(
                name = "landlordDeregistration",
                title = "Delete your landlord account",
                basePath = DeregisterLandlordController.LANDLORD_DEREGISTRATION_ROUTE,
                seedScripts = listOf("data-mockuser-landlord-with-properties-and-incomplete-property.sql"),
                start = { it.navigate(DeregisterLandlordController.LANDLORD_DEREGISTRATION_PATH) },
                startingPoints =
                    listOf(StartingPoint("Landlord has no properties", listOf("data-mockuser-landlord-with-no-properties.sql"))),
            ),
            JourneyDescriptor(
                name = "propertyDeregistration",
                title = "Delete a property record",
                basePath =
                    DeregisterPropertyController.PROPERTY_DEREGISTRATION_ROUTE.replace(
                        "{propertyOwnershipId}",
                        PROPERTY_OWNERSHIP_ID.toString(),
                    ),
                seedScripts = listOf(LOCAL_SEED),
                start = { it.navigate(DeregisterPropertyController.getPropertyDeregistrationPath(PROPERTY_OWNERSHIP_ID)) },
                startingPoints =
                    listOf(
                        StartingPoint(
                            "Property has a pending joint landlord invitation",
                            listOf(LOCAL_SEED, "journey-map/seeds/pending-invitation.sql"),
                        ),
                        StartingPoint("Property has joint landlords", listOf(LOCAL_SEED, "journey-map/seeds/joint-landlord.sql")),
                    ),
            ),
            propertyUpdate("updateBedrooms", "Update the number of bedrooms", UpdateBedroomsController.UPDATE_BEDROOMS_ROUTE),
            propertyUpdate(
                "updateCorrespondenceAddress",
                "Update the correspondence address",
                LandlordUpdateCorrespondenceAddressController.UPDATE_CORRESPONDENCE_ADDRESS_ROUTE,
                address = registeredAndUnregisteredAddresses,
            ),
            propertyUpdate(
                "updateCorrespondenceEmail",
                "Update the correspondence email address",
                LandlordUpdateCorrespondenceEmailController.UPDATE_CORRESPONDENCE_EMAIL_ROUTE,
            ),
            propertyUpdate(
                "updateElectricalSafety",
                "Update electrical safety",
                LandlordUpdateElectricalSafetyController.UPDATE_ELECTRICAL_SAFETY_ROUTE,
                startingPoints = listOf(propertyIsVacant),
            ),
            propertyUpdate(
                "updateEpc",
                "Update the energy performance certificate",
                LandlordUpdateEpcController.UPDATE_EPC_ROUTE,
                startingPoints = listOf(propertyIsVacant),
            ),
            propertyUpdate(
                "updateFurnishedStatus",
                "Update the furnished status",
                LandlordUpdateFurnishedStatusController.UPDATE_FURNISHED_STATUS_ROUTE,
            ),
            propertyUpdate(
                "updateGasSafety",
                "Update gas safety",
                LandlordUpdateGasSafetyController.UPDATE_GAS_SAFETY_ROUTE,
                startingPoints = listOf(propertyIsVacant),
            ),
            propertyUpdate(
                "updateHouseholdsAndTenants",
                "Update households and tenants",
                LandlordUpdateHouseholdsAndTenantsController.UPDATE_HOUSEHOLDS_AND_TENANTS_ROUTE,
            ),
            propertyUpdate("inviteJointLandlord", "Invite a joint landlord", InviteJointLandlordController.INVITE_JOINT_LANDLORD_ROUTE),
            propertyUpdate(
                "updateOccupancy",
                "Update whether the property is occupied",
                UpdateOccupancyController.UPDATE_OCCUPANCY_ROUTE,
                startingPoints = listOf(propertyIsVacant),
            ),
            propertyUpdate("updateOwnershipType", "Update the ownership type", UpdateOwnershipTypeController.UPDATE_OWNERSHIP_TYPE_ROUTE),
            propertyUpdate(
                "updateRentFrequencyAndAmount",
                "Update the rent frequency and amount",
                LandlordUpdateRentFrequencyAndAmountController.UPDATE_RENT_FREQUENCY_AND_AMOUNT_ROUTE,
            ),
            propertyUpdate(
                "updateRentIncludesBills",
                "Update whether rent includes bills",
                LandlordUpdateRentIncludesBillsController.UPDATE_RENT_INCLUDES_BILLS_ROUTE,
            ),
            propertyUpdate(
                "updateTenancyDetails",
                "Update tenancy details",
                LandlordUpdateTenancyDetailsController.UPDATE_TENANCY_DETAILS_ROUTE,
                // Tenancy details can only be changed here once they were left to provide later
                extraSeeds = listOf("journey-map/seeds/tenancy-provided-later.sql"),
            ),
            propertyUpdate(
                "updateLicensing",
                "Update licensing",
                LandlordUpdateLicensingController.UPDATE_LICENSING_ROUTE,
                startingPoints = listOf(propertyHasLicence),
            ),
            landlordUpdate(
                "updateLandlordName",
                "Update your name",
                UpdateLandlordNameController.UPDATE_NAME_ROUTE,
                unverifiedLandlordSeeds,
            ),
            landlordUpdate(
                "updateLandlordDateOfBirth",
                "Update your date of birth",
                UpdateLandlordDateOfBirthController.UPDATE_DATE_OF_BIRTH_ROUTE,
                unverifiedLandlordSeeds,
            ),
            landlordUpdate("updateLandlordEmail", "Update your email address", UpdateLandlordEmailController.UPDATE_EMAIL_ROUTE),
            landlordUpdate(
                "updateLandlordPhoneNumber",
                "Update your phone number",
                UpdateLandlordPhoneNumberController.UPDATE_PHONE_NUMBER_ROUTE,
            ),
            landlordUpdate("updateLandlordAddress", "Update your address", UpdateLandlordAddressController.UPDATE_ADDRESS_ROUTE),
            landlordUpdate(
                "updateOrganisationName",
                "Update the organisation name",
                UpdateOrganisationLandlordNameController.UPDATE_ORG_NAME_ROUTE,
                organisationAdminSeeds,
            ),
            landlordUpdate(
                "updateOrganisationAddress",
                "Update the organisation address",
                UpdateOrganisationalLandlordAddressController.UPDATE_ORG_ADDRESS_ROUTE,
                organisationAdminSeeds,
            ),
            landlordUpdate(
                "updateOrganisationEmail",
                "Update the organisation email address",
                UpdateOrganisationLandlordEmailController.UPDATE_ORG_EMAIL_ROUTE,
                organisationAdminSeeds,
            ),
            landlordUpdate(
                "updateOrganisationPhoneNumber",
                "Update the organisation phone number",
                UpdateOrganisationLandlordPhoneNumberController.UPDATE_ORG_PHONE_NUMBER_ROUTE,
                organisationAdminSeeds,
            ),
            landlordUpdate(
                "updateOrganisationType",
                "Update the organisation type",
                UpdateOrganisationTypeController.UPDATE_ORG_TYPE_ROUTE,
                organisationAdminSeeds,
                listOf(organisationIsTrust),
            ),
            landlordUpdate(
                "updateOrganisationCharity",
                "Update whether the organisation is a registered charity",
                UpdateOrganisationLandlordCharityController.UPDATE_ORG_CHARITY_ROUTE,
                organisationAdminSeeds,
            ),
            landlordUpdate(
                "updateCompaniesHouse",
                "Update whether the organisation is registered with Companies House",
                UpdateCompaniesHouseController.UPDATE_COMPANIES_HOUSE_ROUTE,
                organisationAdminSeeds,
                listOf(organisationIsTrust),
            ),
            landlordUpdate(
                "updateOrganisationMainContact",
                "Update the organisation's main contact",
                UpdateOrganisationMainContactController.UPDATE_ORG_MAIN_CONTACT_ROUTE,
                organisationAdminSeeds,
            ),
            landlordUpdate(
                "updateLeadTrustee",
                "Update the lead trustee",
                UpdateLeadTrusteeController.UPDATE_LEAD_TRUSTEE_ROUTE,
                organisationIsTrust.seedScripts,
            ),
            landlordUpdate(
                "updateGoverningBody",
                "Update the governing body",
                UpdateGoverningBodyController.UPDATE_GOVERNING_BODY_ROUTE,
                organisationIsTrust.seedScripts,
                listOf(
                    StartingPoint(
                        "Organisation has no governing body members",
                        organisationIsTrust.seedScripts + "journey-map/seeds/no-governing-body-members.sql",
                    ),
                ),
            ),
            propertyJourney(
                "cancelJointLandlordInvitation",
                "Cancel a joint landlord invitation",
                "${CancelJointLandlordInvitationController.CANCEL_JOINT_LANDLORD_INVITATION_ROUTE}/$PENDING_INVITATION_ID",
                PROPERTY_WITH_PENDING_INVITATION_ID,
                listOf(JOINT_LANDLORD_INVITATION_SEED),
            ),
            invitationJourney(
                "acceptOrRejectJointLandlordInvitation",
                "Accept or reject a joint landlord invitation",
                AcceptOrRejectJointLandlordInvitationController.ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE,
                JOINT_LANDLORD_INVITATION_TOKEN,
                listOf(JOINT_LANDLORD_INVITATION_SEED),
                listOf(
                    StartingPoint("Invited user is not a landlord", listOf("data-joint-landlord-invitation-mockuser-not-landlord.sql")),
                    StartingPoint(
                        "Invitation has expired",
                        listOf(JOINT_LANDLORD_INVITATION_SEED, "journey-map/seeds/joint-landlord-invitation-expired.sql"),
                    ),
                ),
            ),
            propertyJourney(
                "delegateToLettingAgent",
                "Delegate a property to a letting agent",
                DelegateToLettingAgentController.getDelegateToLettingAgentBasePath(PROPERTY_WITHOUT_LETTING_AGENT_ID),
                PROPERTY_WITHOUT_LETTING_AGENT_ID,
            ),
            propertyJourney(
                "cancelLettingAgentDelegation",
                "Remove a property's letting agent",
                CancelLettingAgentDelegationController.getRemoveLettingAgentBasePath(PROPERTY_OWNERSHIP_ID),
                PROPERTY_OWNERSHIP_ID,
            ),
            // The property details page needs a compliance record, which this seed's joint property does not have
            JourneyDescriptor(
                name = "leaveProperty",
                title = "Leave a property you jointly own",
                basePath = LeavePropertyController.getLeavePropertyBasePath(JOINT_PROPERTY_ID),
                seedScripts = listOf("data-mockuser-landlord-with-sole-and-joint-properties.sql"),
                start = { it.navigate(LeavePropertyController.getLeavePropertyPath(JOINT_PROPERTY_ID)) },
            ),
            propertyJourney(
                "switchToIndividual",
                "Register as the only landlord of a property",
                SwitchToIndividualController.getSwitchToIndividualBasePath(PROPERTY_WITH_PENDING_INVITATIONS_ID),
                PROPERTY_WITH_PENDING_INVITATIONS_ID,
                startingPoints =
                    listOf(
                        StartingPoint(
                            "Property has no pending invitations",
                            listOf(LOCAL_SEED, "journey-map/seeds/no-pending-invitations.sql"),
                        ),
                    ),
            ),
            JourneyDescriptor(
                name = "inviteOrganisationUser",
                title = "Invite a team member",
                basePath = InviteOrganisationalLandlordUserController.INVITE_ORGANISATIONAL_LANDLORD_USER_ROUTE,
                seedScripts = organisationAdminSeeds,
                start = { it.navigate(TeamMembersController.TEAM_MEMBERS_ROUTE) },
                arrange = { it.enableFeature(MULTI_USER_ORGANISATIONS) },
                startsFromLink = true,
            ),
            JourneyDescriptor(
                name = "changeOrgTeamMemberAccessLevel",
                title = "Change a team member's access level",
                basePath = ChangeOrgTeamMemberAccessLevelController.getChangeAccessLevelBasePath(TEAM_MEMBER_EDITOR_ID),
                seedScripts = organisationAdminSeeds + "journey-map/seeds/org-team-members.sql",
                start = { it.navigate(TeamMembersController.TEAM_MEMBERS_ROUTE) },
                arrange = { it.enableFeature(MULTI_USER_ORGANISATIONS) },
                startsFromLink = true,
            ),
            invitationJourney(
                "acceptOrganisationInvitation",
                "Join an organisation",
                AcceptOrganisationalLandlordUserInvitationController.ACCEPT_INVITATION_ROUTE,
                ORGANISATION_INVITATION_TOKEN,
                startingPoints =
                    listOf(
                        StartingPoint("Invited user is not a landlord", listOf(LOCAL_SEED, "data-overlay-mockuser-is-not-a-landlord.sql")),
                        StartingPoint(
                            "Invitation has expired",
                            listOf(LOCAL_SEED, "journey-map/seeds/organisation-invitation-expired.sql"),
                        ),
                    ),
                arrange = { it.enableFeature(MULTI_USER_ORGANISATIONS) },
            ),
            invitationJourney(
                "lettingAgentInvitation",
                "Accept an invitation to act as a letting agent",
                LettingAgentInvitationController.LETTING_AGENT_INVITATION_ROUTE,
                LETTING_AGENT_TOKEN,
                startingPoints =
                    listOf(
                        StartingPoint(
                            "Letting agent already has a password",
                            listOf(LOCAL_SEED, "journey-map/seeds/letting-agent-has-password.sql"),
                        ),
                    ),
            ),
            JourneyDescriptor(
                name = "organisationLandlordDeregistration",
                title = "Delete your organisation's landlord account",
                basePath = DeregisterOrganisationalLandlordController.ORGANISATIONAL_LANDLORD_DEREGISTRATION_ROUTE,
                seedScripts = organisationAdminSeeds,
                start = { it.navigate(LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE) },
                startsFromLink = true,
            ),
            invitationJourney(
                "localCouncilUserRegistration",
                "Register as a local council user",
                RegisterLocalCouncilUserController.LOCAL_COUNCIL_USER_REGISTRATION_ROUTE,
                LOCAL_COUNCIL_INVITATION_TOKEN,
                listOf("data-mockuser-not-local-council-user.sql", "journey-map/seeds/local-council-invitation.sql"),
            ),
        )

    // Invitations are accepted by following the link in the invitation email, which carries the invitation's token
    private fun invitationJourney(
        name: String,
        title: String,
        route: String,
        token: String,
        seedScripts: List<String> = listOf(LOCAL_SEED),
        startingPoints: List<StartingPoint> = emptyList(),
        arrange: (JourneyMapHarness) -> Unit = {},
    ) = JourneyDescriptor(
        name = name,
        title = title,
        basePath = route,
        seedScripts = seedScripts,
        start = { it.navigate("$route?$TOKEN=$token") },
        arrange = arrange,
        startingPoints = startingPoints,
        entryLabel = INVITATION_EMAIL_LINK,
    )

    // Journeys about a single property are started by following a link on that property's details page
    private fun propertyJourney(
        name: String,
        title: String,
        basePath: String,
        propertyOwnershipId: Long,
        seedScripts: List<String> = listOf(LOCAL_SEED),
        startingPoints: List<StartingPoint> = emptyList(),
    ) = JourneyDescriptor(
        name = name,
        title = title,
        basePath = basePath,
        seedScripts = seedScripts,
        start = { it.navigate(PropertyDetailsController.getPropertyDetailsPath(propertyOwnershipId, isLocalCouncilView = false)) },
        startingPoints = startingPoints,
        startsFromLink = true,
    )

    // Property details are changed by following a change link on the property's details page
    private fun propertyUpdate(
        name: String,
        title: String,
        route: String,
        extraSeeds: List<String> = emptyList(),
        startingPoints: List<StartingPoint> = emptyList(),
        address: SeededAddress? = null,
    ) = JourneyDescriptor(
        name = name,
        title = title,
        basePath = route.replace("{propertyOwnershipId}", PROPERTY_OWNERSHIP_ID.toString()),
        seedScripts = listOf(LOCAL_SEED) + extraSeeds,
        start = { it.navigate(PropertyDetailsController.getPropertyDetailsPath(PROPERTY_OWNERSHIP_ID, isLocalCouncilView = false)) },
        address = address,
        startingPoints = startingPoints,
        startsFromLink = true,
    )

    // Landlord details are changed by following a change link on the signed-in landlord's details page
    private fun landlordUpdate(
        name: String,
        title: String,
        route: String,
        seedScripts: List<String> = listOf(LOCAL_SEED),
        startingPoints: List<StartingPoint> = emptyList(),
    ) = JourneyDescriptor(
        name = name,
        title = title,
        basePath = route,
        seedScripts = seedScripts,
        start = { it.navigate(LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE) },
        address = registeredAndUnregisteredAddresses,
        startingPoints = startingPoints,
        startsFromLink = true,
    )
}
