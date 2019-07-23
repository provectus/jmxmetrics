package com.provectus.service

import com.provectus.model.Query


import javax.management.Attribute
import javax.management.ObjectInstance
import javax.management.ObjectName
import javax.management.openmbean.CompositeData
import javax.management.openmbean.TabularData
import com.provectus.model.Result


class JmxResultProcessor(
    val query: Query,
    val objectInstance: ObjectInstance,
    val attributes: List<Attribute>,
    val className: String,
    val objDomain: String
) {

    val results: List<Result>
        get() {
            val builder = ResultsBuilder()
            for (attribute in attributes) {
                builder.add(attribute.name, attribute.value)
            }
            return builder.build()
        }

    /**
     * Result list builders.
     * Recursively walks in the value to add results.
     */
    private inner class ResultsBuilder {
        private val accumulator = mutableListOf<Result>()
        private val epoch = System.currentTimeMillis()

        fun add(attributeName: String, value: Any?) {
            addAny(attributeName, mutableListOf(), value)
        }

        private fun newValuePath(
            valuePath: MutableList<String>,
            name: String
        ): MutableList<String> {
            val copy:MutableList<String> = valuePath.toMutableList()
                copy.add(name)
            return copy
        }

        /**
         * Add one or more results from a value of any type.
         * This is a recursive function.
         */
        @Suppress("UNCHECKED_CAST")
        private fun addAny(attributeName: String, valuePath: MutableList<String>, value: Any?) {
            if (value == null) {
                return
            }

            when (value) {
                is CompositeData -> addComposite(attributeName, valuePath, (value as CompositeData?))
                is Array<*> -> when {
                    value.isArrayOf<CompositeData>() -> value.forEach { add(attributeName, it) }
                    value.isArrayOf<ObjectName>() -> addArray(attributeName, valuePath, value as Array<ObjectName>)
                    else ->
                        for (i in 0 until java.lang.reflect.Array.getLength(value)) {
                            val arrayValue = java.lang.reflect.Array.get(value, i)
                            addAny(attributeName, newValuePath(valuePath, Integer.toString(i)), arrayValue)
                        }
                }
                is TabularData -> addTabular(attributeName, valuePath, value)
                is Map<*, *> -> addMap(attributeName, valuePath,value)
                is Iterable<*> -> addIterable(attributeName, valuePath, value)
                else -> addNew(attributeName, valuePath,value)
            }
        }

        /**
         * Add results from a value of type map.
         */
        private fun addMap(attributeName: String, valuePath: MutableList<String>, map: Map<*, *>?) {
            if (map != null) {
                for ((key, value) in map) {
                    addAny(attributeName, newValuePath(valuePath, key.toString()), value)
                }
            }
        }

        /**
         * Add results from a value of type object name array.
         */
        private fun addArray(attributeName: String, valuePath: MutableList<String>, objs: Array<ObjectName>) {
            val values = mutableMapOf<String,Any>()
            for (obj in objs) {
                values[obj.canonicalName] = obj.keyPropertyListString
            }
            addNew(attributeName, valuePath, values)
        }

        /**
         * Add results from a value of type composite data.
         * This is a recursive function.
         */
        private fun addComposite(attributeName: String, valuePath: MutableList<String>, cds: CompositeData?) {
            val keys = cds?.compositeType?.keySet()
            if (keys!=null) {
                for (key in keys) {
                    if (query.keys.isNotEmpty() && !query.keys.contains(key)) {
                        continue
                    }

                    val value = cds.get(key)
                    addAny(attributeName, newValuePath(valuePath, key), value)
                }
            }
        }

        /**
         * Add results from a value of type composite data.
         * This is a recursive function.
         */
        private fun addIterable(attributeName: String, valuePath: MutableList<String>, iterable: Iterable<*>?) {
            if (iterable!=null) {
                for ((index, value) in iterable.withIndex()) {
                    addAny(attributeName, newValuePath(valuePath, Integer.toString(index)), value)
                }
            }
        }

        /**
         * Add results from a value of type tabular data.
         * This is a recursive function.
         */
        @Suppress("UNCHECKED_CAST")
        private fun addTabular(attributeName: String, valuePath: MutableList<String>, tds: TabularData?) {
            // @see TabularData#keySet JavaDoc:
            // "Set<List<?>>" but is declared as a {@code Set<?>} for
            // compatibility reasons. The returned set can be used to iterate
            // over the keys."
            if (tds!=null) {
                val keys = tds.keySet() as Set<List<*>>
                for (key in keys) {
                    // ie: attributeName=LastGcInfo.Par Survivor Space
                    // i haven't seen this be smaller or larger than List<1>, but
                    // might as well loop it.

                    val compositeData = tds.get(key.toTypedArray())
                    val attributeName2 = key.joinToString(".")
                    addComposite(attributeName, newValuePath(valuePath, attributeName2), compositeData)
                }
            }
        }

        /**
         * Create and add a new result.
         */
        private fun addNew(attributeName: String, valuePath: MutableList<String>, value: Any) {
            accumulator.add(
                Result(
                    attributeName = attributeName,
                    className = className,
                    objDomain = objDomain,
                    typeName = objectInstance.objectName.keyPropertyListString,
                    valuePath = valuePath.toList(),
                    value = value,
                    epoch = epoch,
                    keyAlias = query.resultAlias
                )
            )
        }

        /**
         * Return the built list
         */
        fun build(): List<Result> {
            return accumulator.toList()
        }
    }

}
