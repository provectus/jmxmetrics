package com.provectus.model.typename

import java.util.regex.Pattern

object StringUtils {
    private val DOT_SLASH_UNDERSCORE_PAT = Pattern.compile("[./]")
    private val SLASH_UNDERSCORE_PAT = Pattern.compile("/", Pattern.LITERAL)
    private val SPACE_PAT = Pattern.compile("[ \"']+")

    /**
     * Replaces all . and / with _ and removes all spaces and double/single quotes.
     */
    fun cleanupStr(name: String): String? {
        return cleanupStr(name, false)
    }

    /**
     * Replaces all . and / with _ and removes all spaces and double/single quotes.
     * Chomps any trailing . or _ character.
     *
     * @param allowDottedKeys whether we remove the dots or not.
     */
    fun cleanupStr(name: String?, allowDottedKeys: Boolean): String? {
        if (name == null) {
            return null
        }
        val pattern = if (!allowDottedKeys) {
            DOT_SLASH_UNDERSCORE_PAT
        } else {
            SLASH_UNDERSCORE_PAT
        }
        var clean = pattern.matcher(name).replaceAll("_")
        clean = SPACE_PAT.matcher(clean).replaceAll("")
        clean = chomp(clean, ".")
        clean = chomp(clean, "_")
        return clean
    }

    fun chomp(str: String, separator: String): String? {
        return if (str.isNotEmpty()) {
            if (str.endsWith(separator)) str.substring(0, str.length - separator.length) else str
        } else {
            str
        }
    }
}