package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.governingBody

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.OrgCompaniesHouseDetailsHelper
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig
import uk.gov.communities.prsdb.webapp.services.LandlordService

@JourneyFrameworkComponent
class UpdateGoverningBodyCyaStepConfig(
    private val landlordService: LandlordService,
    private val orgCompaniesHouseDetailsHelper: OrgCompaniesHouseDetailsHelper,
) : AbstractCheckYourAnswersStepConfig<UpdateGoverningBodyJourneyState>() {
    override fun chooseTemplate(state: UpdateGoverningBodyJourneyState) = "forms/governingBodyUpdateCheckAnswersForm"

    override fun getStepSpecificContent(state: UpdateGoverningBodyJourneyState): Map<String, Any?> =
        mapOf(
            "title" to "landlordDetails.update.title",
            "showWarning" to true,
            "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
            "governingBodyMemberCards" to
                orgCompaniesHouseDetailsHelper.getGovBodyMemberCards(
                    state,
                    state.orgGovBodyMembersTask,
                ),
        )

    override fun afterStepDataIsAdded(state: UpdateGoverningBodyJourneyState) {
        val members =
            state.orgGovBodyMembersTask.governingBodyMembersMap
                ?.toSortedMap()
                ?.values
                ?.toList()
                ?: throw PrsdbWebException("Governing body member state is missing")

        landlordService.updateOrganisationLandlordGoverningBodyMembers(members)
    }
}

@JourneyFrameworkComponent
final class UpdateGoverningBodyCyaStep(
    stepConfig: UpdateGoverningBodyCyaStepConfig,
) : AbstractCheckYourAnswersStep<UpdateGoverningBodyJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "governing-body-check-your-answers"
    }
}
