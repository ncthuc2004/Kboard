package com.kaius.keyboard.ime

object VietnameseTelexEngine {

    // Vowel table: [baseChar][vowelType (0: plain, 1: circumflex/hat, 2: horn/breve)][tone (0: none, 1: sac, 2: huyen, 3: hoi, 4: nga, 5: nang)]
    // vowelType:
    // a: 0=a, 1=â, 2=ă
    // e: 0=e, 1=ê
    // o: 0=o, 1=ô, 2=ơ
    // u: 0=u, 1=ư
    // i: 0=i
    // y: 0=y

    private val VOWELS_LOWER = mapOf(
        'a' to arrayOf(
            charArrayOf('a', 'á', 'à', 'ả', 'ã', 'ạ'),
            charArrayOf('â', 'ấ', 'ầ', 'ẩ', 'ẫ', 'ậ'),
            charArrayOf('ă', 'ắ', 'ằ', 'ẳ', 'ẵ', 'ặ')
        ),
        'e' to arrayOf(
            charArrayOf('e', 'é', 'è', 'ẻ', 'ẽ', 'ẹ'),
            charArrayOf('ê', 'ế', 'ề', 'ể', 'ễ', 'ệ')
        ),
        'o' to arrayOf(
            charArrayOf('o', 'ó', 'ò', 'ỏ', 'õ', 'ọ'),
            charArrayOf('ô', 'ố', 'ồ', 'ổ', 'ỗ', 'ộ'),
            charArrayOf('ơ', 'ớ', 'ờ', 'ở', 'ỡ', 'ợ')
        ),
        'u' to arrayOf(
            charArrayOf('u', 'ú', 'ù', 'ủ', 'ũ', 'ụ'),
            charArrayOf('ư', 'ứ', 'ừ', 'ử', 'ữ', 'ự')
        ),
        'i' to arrayOf(
            charArrayOf('i', 'í', 'ì', 'ỉ', 'ĩ', 'ị')
        ),
        'y' to arrayOf(
            charArrayOf('y', 'ý', 'ỳ', 'ỷ', 'ỹ', 'ỵ')
        )
    )

    private val TONE_KEYS = mapOf(
        's' to 1, // Sắc
        'f' to 2, // Huyền
        'r' to 3, // Hỏi
        'x' to 4, // Ngã
        'j' to 5, // Nặng
        'z' to 0  // Xóa dấu
    )

    // Reverse lookup map to identify base vowel, type and tone from accented char
    data class VowelInfo(val base: Char, val type: Int, val tone: Int, val isUpper: Boolean)

    private val CHAR_LOOKUP = mutableMapOf<Char, VowelInfo>()

    init {
        for ((base, types) in VOWELS_LOWER) {
            for (t in types.indices) {
                for (tone in types[t].indices) {
                    val ch = types[t][tone]
                    CHAR_LOOKUP[ch] = VowelInfo(base, t, tone, false)
                    CHAR_LOOKUP[ch.uppercaseChar()] = VowelInfo(base, t, tone, true)
                }
            }
        }
    }

    /**
     * Attempts to apply Telex rules when [key] is typed at the end of [word].
     * Returns the updated word if rule applied, or null if no Telex rule applies.
     */
    fun processKey(word: String, key: Char): String? {
        if (word.isEmpty()) return null
        val lowerKey = key.lowercaseChar()

        // 1. Double consonants: d + d -> đ
        if (lowerKey == 'd') {
            val lastChar = word.last()
            if (lastChar == 'd') {
                return word.dropLast(1) + 'đ'
            } else if (lastChar == 'D') {
                return word.dropLast(1) + 'Đ'
            } else if (lastChar == 'đ') {
                return word.dropLast(1) + "dd"
            } else if (lastChar == 'Đ') {
                return word.dropLast(1) + "Dd"
            }
        }

        // 2. Double vowels: aa->â, ee->ê, oo->ô
        if (lowerKey in listOf('a', 'e', 'o')) {
            val res = tryDoubleVowel(word, lowerKey, key.isUpperCase())
            if (res != null) return res
        }

        // 3. Horn/Breve key 'w': aw->ă, ow->ơ, uw->ư, w->ư
        if (lowerKey == 'w') {
            val res = tryHornBreve(word, key.isUpperCase())
            if (res != null) return res
        }

        // 4. Tone keys: s, f, r, x, j, z
        if (lowerKey in TONE_KEYS) {
            val tone = TONE_KEYS[lowerKey] ?: 0
            val res = tryApplyTone(word, tone, lowerKey)
            if (res != null) return res
        }

        return null
    }

    private fun tryDoubleVowel(word: String, key: Char, isUpper: Boolean): String? {
        // Find last matching vowel in word
        for (i in word.indices.reversed()) {
            val ch = word[i]
            val info = CHAR_LOOKUP[ch] ?: continue
            if (info.base == key) {
                if (info.type == 0) {
                    // plain -> circumflex (type 1)
                    val newChar = getAccentedChar(info.base, 1, info.tone, info.isUpper)
                    return word.substring(0, i) + newChar + word.substring(i + 1)
                } else if (info.type == 1) {
                    // already circumflex -> revert to plain + repeated key
                    val plainChar = getAccentedChar(info.base, 0, info.tone, info.isUpper)
                    return word.substring(0, i) + plainChar + word.substring(i + 1) + (if (isUpper) key.uppercaseChar() else key)
                }
            }
        }
        return null
    }

    private fun tryHornBreve(word: String, isUpper: Boolean): String? {
        // Try to modify last o, u, or a
        for (i in word.indices.reversed()) {
            val ch = word[i]
            val info = CHAR_LOOKUP[ch] ?: continue
            when (info.base) {
                'a' -> {
                    val targetType = if (info.type == 2) 0 else 2 // a -> ă (type 2)
                    val newChar = getAccentedChar('a', targetType, info.tone, info.isUpper)
                    return word.substring(0, i) + newChar + word.substring(i + 1)
                }
                'o' -> {
                    val targetType = if (info.type == 2) 0 else 2 // o -> ơ (type 2)
                    val newChar = getAccentedChar('o', targetType, info.tone, info.isUpper)
                    return word.substring(0, i) + newChar + word.substring(i + 1)
                }
                'u' -> {
                    val targetType = if (info.type == 1) 0 else 1 // u -> ư (type 1)
                    val newChar = getAccentedChar('u', targetType, info.tone, info.isUpper)
                    return word.substring(0, i) + newChar + word.substring(i + 1)
                }
            }
        }
        // If word ends with nothing modifiable, 'w' alone can become 'ư'
        return word + (if (isUpper) 'Ư' else 'ư')
    }

    private fun tryApplyTone(word: String, targetTone: Int, toneKey: Char): String? {
        // Find best vowel index to place tone
        val vowelIndices = mutableListOf<Int>()
        for (i in word.indices) {
            if (word[i] in CHAR_LOOKUP) {
                vowelIndices.add(i)
            }
        }
        if (vowelIndices.isEmpty()) return null

        val targetIdx = pickToneVowelIndex(word, vowelIndices)
        val ch = word[targetIdx]
        val info = CHAR_LOOKUP[ch] ?: return null

        // If same tone already exists, pressing tone key again appends the raw tone key (e.g. toas -> toá, toás -> toas)
        val finalTone = if (info.tone == targetTone && targetTone != 0) 0 else targetTone
        val newChar = getAccentedChar(info.base, info.type, finalTone, info.isUpper)

        // Clear tone from any other vowel in the word to keep single tone
        var result = word
        for (idx in vowelIndices) {
            if (idx != targetIdx) {
                val otherInfo = CHAR_LOOKUP[result[idx]]
                if (otherInfo != null && otherInfo.tone != 0) {
                    val stripped = getAccentedChar(otherInfo.base, otherInfo.type, 0, otherInfo.isUpper)
                    result = result.substring(0, idx) + stripped + result.substring(idx + 1)
                }
            }
        }

        return result.substring(0, targetIdx) + newChar + result.substring(targetIdx + 1)
    }

    private fun pickToneVowelIndex(word: String, indices: List<Int>): Int {
        if (indices.size == 1) return indices[0]

        // If there is circumflex or horn vowel (ê, ơ, ư, ô, â, ă), it gets tone
        for (idx in indices) {
            val info = CHAR_LOOKUP[word[idx]]
            if (info != null && info.type > 0) return idx
        }

        // Standard Vietnamese vowel priority (oa, oe, uy): tone on the second vowel if word ends with consonant, else on first
        val lastChar = word.last()
        val endsWithVowel = lastChar in CHAR_LOOKUP
        return if (endsWithVowel && indices.size > 1) {
            indices[indices.size - 2] // e.g. "hòa", "hòe" -> 'o'
        } else {
            indices.last() // e.g. "toán" -> 'a'
        }
    }

    private fun getAccentedChar(base: Char, type: Int, tone: Int, isUpper: Boolean): Char {
        val types = VOWELS_LOWER[base] ?: return base
        val safeType = type.coerceIn(0, types.size - 1)
        val safeTone = tone.coerceIn(0, types[safeType].size - 1)
        val ch = types[safeType][safeTone]
        return if (isUpper) ch.uppercaseChar() else ch
    }
}
