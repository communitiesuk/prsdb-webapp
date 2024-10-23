package uk.gov.communities.prsdb.webapp.constants

import kotlin.math.pow

val REG_NUM_CHARSET =
    listOf(
        'C',
        'D',
        'F',
        'G',
        'H',
        'J',
        'K',
        'M',
        'N',
        'P',
        'Q',
        'R',
        'S',
        'T',
        'V',
        'W',
        'X',
        'Y',
        'Z',
        '2',
        '3',
        '4',
        '5',
        '6',
        '7',
        '9',
    )

val REG_NUM_BASE = REG_NUM_CHARSET.size

const val REG_NUM_LENGTH = 8

const val REG_NUM_SEG_LENGTH = REG_NUM_LENGTH / 2

const val MIN_REG_NUM = 0L

val MAX_REG_NUM = REG_NUM_BASE.toDouble().pow(REG_NUM_LENGTH).toLong() - 1
