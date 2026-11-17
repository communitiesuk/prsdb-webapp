package uk.gov.communities.prsdb.webapp.journeys.shared.helpers

import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.ElectricalSafetyTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.EpcTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.GasSafetyTask

// These check answers pages' headings are the same as their section header, so we don't show a section header on them.
// Call these from outside the task's section so they override the header that the section applies.
private fun JourneyBuilder<*>.hideSectionHeader(step: JourneyStep<*, *, *>) {
    configureStep(step) {
        withAdditionalContentProperty { "sectionHeaderInfo" to null }
    }
}

fun JourneyBuilder<*>.hideCheckAnswersSectionHeader(task: GasSafetyTask) = hideSectionHeader(task.checkGasSafetyAnswersStep)

fun JourneyBuilder<*>.hideCheckAnswersSectionHeader(task: ElectricalSafetyTask) = hideSectionHeader(task.checkElectricalSafetyAnswersStep)

fun JourneyBuilder<*>.hideCheckAnswersSectionHeader(task: EpcTask) = hideSectionHeader(task.checkEpcAnswersStep)
