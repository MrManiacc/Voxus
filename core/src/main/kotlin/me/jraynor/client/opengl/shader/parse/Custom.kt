package me.jraynor.client.opengl.shader.parse

/**
 * Represents a custom piece of code that can be imported
 */
class Custom : Define, Comparable<Custom> {
    private var value: String? = null

    constructor(line: Int, source: Array<String>?) : super(line, source) {}
    constructor(line: Int, copy: Custom) {
        this.line = line
        name = copy.name
        value = copy.value
    }

    /**
     * Parses the custom type
     *
     * @param input input line
     * @return returns name
     */
    override fun parse(input: String): String? {
        value = input.substring(input.indexOf("\"") + 1, input.lastIndexOf("\""))
        return input.substring(input.indexOf(" ") + 1, input.indexOf("->")).trim { it <= ' ' }
    }

    /**
     * Customs are just text so we return the text value
     *
     * @return returns the text value
     */
    override fun serialize(): String? {
        return value
    }

    override fun toString(): String {
        return "Custom{" +
                "value='" + value + '\'' +
                ", line=" + line +
                ", name='" + name + '\'' +
                '}'
    }

    override fun compareTo(o: Custom): Int {
        return name!!.compareTo(o.name!!)
    }
}