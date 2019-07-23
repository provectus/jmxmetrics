package com.provectus.model.results

class BooleanAsNumberValueTransformer(private val valueForTrue: Number, private val valueForFalse: Number) :
    ValueTransformer {

    override fun apply(value: Any?): Any? {
        if (value == null) {
            return null
        }

        return if (value is Boolean) {
            if ((value as Boolean?)!!) {
                valueForTrue
            } else {
                valueForFalse
            }
        } else value
    }
}