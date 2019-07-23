package com.provectus.utils


object NumberUtils {

    /**
     * Useful for figuring out if an Object is a number.
     */
    fun isNumeric(value: Any?): Boolean {
        if (value == null) return false
        if (value is Number) return true
        if (value is String) {
            return if (value.isEmpty()) true else isNumber(value)
        }
        return false
    }

    fun isValidNumber(value: Any?): Boolean {
        if (value == null) {
            return false
        }

        return if (value is Number) {
            isValidNumber(value as Number?)
        } else (value as? String)?.let { isNumeric(it) } ?: false

    }

    fun isValidNumber(number: Number): Boolean {
        if (number is Double) {
            if (java.lang.Double.isNaN(number.toDouble()) || java.lang.Double.isInfinite(number.toDouble())) {
                return false
            }
        }

        if (number is Float) {
            if (java.lang.Float.isNaN(number.toFloat()) || java.lang.Float.isInfinite(number.toFloat())) {
                return false
            }
        }

        return true
    }

    fun isNumber(str: String): Boolean {
        if (str.isEmpty()) {
            return false
        } else {
            val chars = str.toCharArray()
            var sz = chars.size
            var hasExp = false
            var hasDecPoint = false
            var allowSigns = false
            var foundDigit = false
            val start = if (chars[0] == '-') 1 else 0
            var i: Int
            if (sz > start + 1 && chars[start] == '0' && chars[start + 1] == 'x') {
                i = start + 2
                if (i == sz) {
                    return false
                } else {
                    while (i < chars.size) {
                        if ((chars[i] < '0' || chars[i] > '9') && (chars[i] < 'a' || chars[i] > 'f') && (chars[i] < 'A' || chars[i] > 'F')) {
                            return false
                        }

                        ++i
                    }

                    return true
                }
            } else {
                --sz

                i = start
                while (i < sz || i < sz + 1 && allowSigns && !foundDigit) {
                    if (chars[i] in '0'..'9') {
                        foundDigit = true
                        allowSigns = false
                    } else if (chars[i] == '.') {
                        if (hasDecPoint || hasExp) {
                            return false
                        }

                        hasDecPoint = true
                    } else if (chars[i] != 'e' && chars[i] != 'E') {
                        if (chars[i] != '+' && chars[i] != '-') {
                            return false
                        }

                        if (!allowSigns) {
                            return false
                        }

                        allowSigns = false
                        foundDigit = false
                    } else {
                        if (hasExp) {
                            return false
                        }

                        if (!foundDigit) {
                            return false
                        }

                        hasExp = true
                        allowSigns = true
                    }
                    ++i
                }

                return if (i < chars.size) {
                    if (chars[i] in '0'..'9') {
                        true
                    } else if (chars[i] != 'e' && chars[i] != 'E') {
                        if (chars[i] == '.') {
                            if (!hasDecPoint && !hasExp) foundDigit else false
                        } else if (allowSigns || chars[i] != 'd' && chars[i] != 'D' && chars[i] != 'f' && chars[i] != 'F') {
                            if (chars[i] != 'l' && chars[i] != 'L') {
                                false
                            } else {
                                foundDigit && !hasExp
                            }
                        } else {
                            foundDigit
                        }
                    } else {
                        false
                    }
                } else {
                    !allowSigns && foundDigit
                }
            }
        }
    }

}