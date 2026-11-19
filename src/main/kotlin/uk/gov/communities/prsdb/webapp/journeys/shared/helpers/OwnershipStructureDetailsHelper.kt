package uk.gov.communities.prsdb.webapp.journeys.shared.helpers

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.helpers.converters.OwnershipTypeAnswersConverter
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.UnrecoverableJourneyStateException
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.anyoneInOwnershipChain
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.getFreeholdersOrNull
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.getSuperiorLandlordsOrNull
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.FreeholdersTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.OwnershipStructureTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.OwnershipTypeTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.SuperiorLandlordsTask
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

@PrsdbWebService
class OwnershipStructureDetailsHelper(
    private val freeholderSummaryCardHelper: FreeholderSummaryCardHelper,
    private val superiorLandlordSummaryCardHelper: SuperiorLandlordSummaryCardHelper,
) {
    fun getOwnershipStructureCyaContent(
        state: CheckYourAnswersJourneyState,
        ownershipStructureTask: OwnershipStructureTask,
    ): Map<String, Any?> {
        val ownershipType =
            ownershipStructureTask.ownershipTypeTask.ownershipType
                ?: throw UnrecoverableJourneyStateException(
                    state.journeyId,
                    "Check your answers was reached before the ownership type task was complete",
                )
        return getOwnershipContent(state, ownershipStructureTask.ownershipTypeTask, ownershipType) +
            getFreeholdersContent(state, ownershipStructureTask.freeholdersTask, ownershipType) +
            getSuperiorLandlordsContent(state, ownershipStructureTask.superiorLandlordsTask, ownershipType)
    }

    private fun getOwnershipContent(
        state: CheckYourAnswersJourneyState,
        ownershipTypeTask: OwnershipTypeTask,
        ownershipType: OwnershipType,
    ): Map<String, Any?> {
        val answers = OwnershipTypeAnswersConverter.convert(ownershipType)

        return mapOf(
            "ownershipRows" to
                listOfNotNull(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "propertyDetails.peopleResponsible.ownership.ownsProperty",
                        answers.ownsProperty,
                        Destination.VisitableStep(
                            ownershipTypeTask.ownsPropertyStep,
                            state.getCyaJourneyId(ownershipTypeTask.ownsPropertyStep),
                        ),
                    ),
                    answers.ownershipKind?.let {
                        SummaryListRowViewModel.forCheckYourAnswersPage(
                            "propertyDetails.peopleResponsible.ownership.ownershipType",
                            it,
                            Destination.VisitableStep(
                                ownershipTypeTask.ownershipKindStep,
                                state.getCyaJourneyId(ownershipTypeTask.ownershipKindStep),
                            ),
                        )
                    },
                    answers.freeholdType?.let {
                        SummaryListRowViewModel.forCheckYourAnswersPage(
                            "propertyDetails.peopleResponsible.ownership.freeholdType",
                            it,
                            Destination.VisitableStep(
                                ownershipTypeTask.freeholdTypeStep,
                                state.getCyaJourneyId(ownershipTypeTask.freeholdTypeStep),
                            ),
                        )
                    },
                ),
        )
    }

    private fun getFreeholdersContent(
        state: CheckYourAnswersJourneyState,
        freeholdersTask: FreeholdersTask,
        ownershipType: OwnershipType,
    ): Map<String, Any?> {
        if (!ownershipType.requiresFreeholderRecords()) return emptyMap()

        val freeholders =
            freeholdersTask.getFreeholdersOrNull()
                ?: throw UnrecoverableJourneyStateException(
                    state.journeyId,
                    "Check your answers was reached with an ownership type requiring freeholder records but no freeholders",
                )
        val freeholdersListStep = freeholdersTask.freeholdersDetailsTask.freeholdersListStep
        val headingType = if (ownershipType.isJointFreeholder()) "joint" else "nonJoint"
        return mapOf(
            "freeholdersHeadingKey" to "registerProperty.freeholders.checkAnswers.heading.$headingType",
            "freeholdersChangeLinkKey" to "registerProperty.freeholders.checkAnswers.changeLink.$headingType",
            "freeholdersChangeUrl" to
                Destination.VisitableStep(freeholdersListStep, state.getCyaJourneyId(freeholdersListStep)).toUrlStringOrNull(),
            "freeholdersCards" to freeholderSummaryCardHelper.getSummaryCards(freeholders),
        )
    }

    private fun getSuperiorLandlordsContent(
        state: CheckYourAnswersJourneyState,
        superiorLandlordsTask: SuperiorLandlordsTask,
        ownershipType: OwnershipType,
    ): Map<String, Any?> {
        if (!ownershipType.requiresSuperiorLandlordInfo()) return emptyMap()

        val anyoneInOwnershipChainStep = superiorLandlordsTask.anyoneInOwnershipChainStep
        val anyoneInOwnershipChain =
            superiorLandlordsTask.anyoneInOwnershipChain
                ?: throw UnrecoverableJourneyStateException(
                    state.journeyId,
                    "Check your answers was reached before the anyone in ownership chain step was answered",
                )

        return if (anyoneInOwnershipChain) {
            val superiorLandlords =
                superiorLandlordsTask.getSuperiorLandlordsOrNull()
                    ?: throw UnrecoverableJourneyStateException(
                        state.journeyId,
                        "Check your answers was reached with anyone in ownership chain answered yes but no superior landlords",
                    )
            val listStep = superiorLandlordsTask.superiorLandlordsDetailsTask.superiorLandlordsListStep
            mapOf(
                "superiorLandlordsChangeUrl" to Destination.VisitableStep(listStep, state.getCyaJourneyId(listStep)).toUrlStringOrNull(),
                "superiorLandlordsCards" to superiorLandlordSummaryCardHelper.getSummaryCards(superiorLandlords),
            )
        } else {
            mapOf(
                "superiorLandlordsRows" to
                    listOf(
                        SummaryListRowViewModel.forCheckYourAnswersPage(
                            "registerProperty.superiorLandlords.checkAnswers.anyoneInOwnershipChain",
                            "commonText.no",
                            Destination.VisitableStep(anyoneInOwnershipChainStep, state.getCyaJourneyId(anyoneInOwnershipChainStep)),
                        ),
                    ),
            )
        }
    }
}
