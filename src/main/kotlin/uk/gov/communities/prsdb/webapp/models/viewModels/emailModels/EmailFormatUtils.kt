package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

fun formatAsBulletList(items: List<String>): String = items.joinToString("\n") { "* $it" }

fun formatEmailList(emails: List<String>): String = if (emails.size == 1) emails.first() else formatAsBulletList(emails)
