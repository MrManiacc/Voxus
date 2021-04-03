package me.jraynor.client.opengl.shader.parse

/**
 * An abstract definition of a #define
 */
abstract class Define(var line: Int? = null, var source: Array<String>? = null) {
    var name: String? = null
    var inVertex = false
    var inFrag = false

    init {
        if (line != null && source != null)
            name = parse(source!![line!!])
    }


    /**
     * Parse the definition
     *
     * @param line the line
     * @return should already return a name
     */
    protected abstract fun parse(line: String): String?

    /**
     * Serialize the data
     *
     * @return returns the serialized string
     */
    abstract fun serialize(): String?
}