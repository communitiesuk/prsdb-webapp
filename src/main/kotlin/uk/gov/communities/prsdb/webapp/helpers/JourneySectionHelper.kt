package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.forms.tasks.RegisterPropertyMultiTaskTransaction
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.models.viewModels.SectionHeaderViewModel

open class JourneySectionHelper {
    companion object {
        fun getSectionHeaderInfoForStep(
            stepId: RegisterPropertyStepId,
            transaction: RegisterPropertyMultiTaskTransaction,
        ): SectionHeaderViewModel? {
            transaction.getSectionForStep(stepId)?.let {
                try {
                    MessageKeyConverter.convert(it)
                } catch (e: NotImplementedError) {
                    return null
                }
                return SectionHeaderViewModel(
                    MessageKeyConverter.convert(it),
                    it.sectionNumber,
                    transaction.taskLists.size,
                )
            }
            return null
        }
    }
}
