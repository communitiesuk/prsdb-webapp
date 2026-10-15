package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_PHASE_TWO
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyRegistrationDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyRegistrationConfirmationEmail

@PrsdbWebService
class PropertyRegistrationConfirmationSender(
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val confirmationEmailSender: EmailNotificationService<PropertyRegistrationConfirmationEmail>,
    private val featureFlagManager: FeatureFlagManager,
    private val currentUserService: CurrentUserService,
) {
    fun sendConfirmationEmail(
        registrationData: PropertyRegistrationDataModel,
        landlord: Landlord,
        propertyOwnership: PropertyOwnership,
    ) = with(registrationData) {
        // TODO: PDJB-1274: Update emails to account for org landlord (check which org email address to use, currently registrant)
        confirmationEmailSender.sendEmail(
            currentUserService.getCurrentEmail(),
            PropertyRegistrationConfirmationEmail(
                RegistrationNumberDataModel
                    .fromRegistrationNumber(propertyOwnership.registrationNumber)
                    .toString(),
                propertyOwnership.address.toMultiLineAddress(),
                absoluteUrlProvider.buildLandlordDashboardUri().toString(),
                propertyOwnership.isOccupied,
                jointLandlordEmails,
                isDelegatedToLettingAgent,
                isPdjb939PhaseTwoEnabled = featureFlagManager.checkFeature(PROPERTY_REGISTRATION_PHASE_TWO),
                licenseProvideLater = licenseProvideLater,
                gasSafetyCertProvideLater = gasSafetyCertProvideLater,
                electricalSafetyCertProvideLater = electricalSafetyCertProvideLater,
                epcProvideLater = epcProvideLater,
                tenancyProvideLater = tenancyProvideLater,
            ),
        )
    }
}
