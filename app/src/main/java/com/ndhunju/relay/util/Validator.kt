package com.ndhunju.relay.util

import androidx.core.util.PatternsCompat
import java.util.regex.Pattern

fun String?.isValidEmail(): Boolean {
    if (this == null) return false
    return PatternsCompat.EMAIL_ADDRESS.matcher(this).matches()
}

fun isValidName(it: String?) = it != null && it.isEmpty().not()

const val ENC_KEY_MIN_LENGTH = 6
fun isValidEncryptionKey(it: String?) = (it?.length ?: 0) > ENC_KEY_MIN_LENGTH

const val PASS_MIN_LENGTH = 6
val characterOnlyRegex by lazy { Regex("[a-zA-Z]") }
val numberOnlyRegex by lazy { Regex("[0-9]") }
fun isValidPassword(it: String?) = (it?.length ?: 0) > PASS_MIN_LENGTH
        && it?.contains(characterOnlyRegex) ?: false // At least one alphabet
        && it?.contains(numberOnlyRegex) ?: false // At least one number

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