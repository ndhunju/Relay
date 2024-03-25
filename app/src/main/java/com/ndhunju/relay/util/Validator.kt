package com.ndhunju.relay.util

import java.util.regex.Pattern

const val ENC_KEY_MIN_LENGTH = 6
fun isValidEncryptionKey(it: String?) = (it?.length ?: 0) > ENC_KEY_MIN_LENGTH

/**
 * Copied from [Patterns.PHONE].
 * For some reason, [Patterns.PHONE] is null while running the test cases
 */
val phonePattern: Pattern by lazy {
    Pattern.compile( // sdd = space, dot, or dash
        ("(\\+[0-9]+[\\- \\.]*)?" // +<digits><sdd>*
                + "(\\([0-9]+\\)[\\- \\.]*)?" // <digit><digit|sdd>+<digit>
                + "([0-9][0-9\\- \\.]+[0-9])")
    ) // <digit><digit|sdd>+<digit>
}

fun isValidPhoneNumber(it: String?) = it != null
        && it.length > 8 // Has to be at least 9 digits
        && phonePattern.matcher(it).matches()