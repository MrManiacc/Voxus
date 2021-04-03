package me.jraynor.client.opengl.shader.parse

import com.jgfx.assets.urn.ResourceUrn

/**
 * Represents some arbitrary code that will be imported from one file to another
 */
class Import(line: Int, source: Array<String>?) : Define(line, source) {
    private var urn: ResourceUrn? = null
    private var definition: String? = null
    override fun parse(input: String): String? {
        val name = input.substring(input.indexOf(" "), input.indexOf("->")).trim { it <= ' ' }
        urn = ResourceUrn(name)
        definition = input.substring(input.indexOf("->") + 2).trim { it <= ' ' }
        return name
    }

    /**
     * An import isn't supposed to be used like this
     *
     * @return returns the serialized value
     */
    override fun serialize(): String? {
        return "N/A"
    }

    override fun toString(): String {
        return "Import{" +
                "line='" + line + '\'' +
                "definition='" + definition + '\'' +
                ", name='" + name + '\'' +
                '}'
    }
}