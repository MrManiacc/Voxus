package me.jraynor.client.opengl.shader.parse


/**
 * Represents some bind that can be appended to the shader dynamically
 */
class Bind : Define, Comparable<Bind> {
     var attribute = 0

     var type: String? = null

     var passBind = false

    constructor(line: Int, source: Array<String>?, input: String) {
        this.line = line
        this.source = source
        name = parse(input)
    }

    constructor(line: Int, name: String?, attribute: Int, type: String?, passBind: Boolean) {
        this.line = line
        this.passBind = passBind
        this.name = name
        this.attribute = attribute
        this.type = type
    }

    constructor(attribute: Int, name: String?, type: String?) {
        this.attribute = attribute
        this.name = name
        this.type = type
    }

    /**
     * Creates a copy with the specified line; used for importing
     *
     * @param line the line to set to
     * @param copy the copy
     */
    constructor(line: Int, copy: Bind) : this(line, copy.name, copy.attribute, copy.type, copy.passBind) {}

    /**
     * Parsed the asset
     *
     * @param input the input line
     * @return returns the name
     */
    override fun parse(input: String): String {
        val name = input.substring(if (input.contains("->")) input.indexOf("->") + 2 else 0, input.indexOf("("))
            .trim { it <= ' ' }
        attribute = input.substring(input.indexOf("(") + 1, input.indexOf(",")).toInt()
        passBind = java.lang.Boolean.parseBoolean(
            input.substring(input.indexOf(",") + 1, input.indexOf(")")).trim { it <= ' ' })
        type = input.substring(input.indexOf(":") + 1).trim { it <= ' ' }
        return name
    }

    /**
     * Serialize's the data
     *
     * @return returns the glsl representation of the uniform
     */
    override fun serialize(): String {
        return "in $type $name;"
    }

    override fun toString(): String {
        return "Bind{" +
                "name=" + name +
                ", attribute=" + attribute +
                ", type='" + type + '\'' +
                ", passBind=" + passBind +
                ", line=" + line +
                '}'
    }

    override fun compareTo(o: Bind): Int {
        return if (attribute == o.attribute) 0 else attribute - o.attribute
    }
}