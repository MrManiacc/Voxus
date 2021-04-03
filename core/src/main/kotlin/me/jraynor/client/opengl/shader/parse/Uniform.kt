package me.jraynor.client.opengl.shader.parse

/**
 * Represents a uniform, which is used to automatically map data in shader
 */
class Uniform : Define, Comparable<Uniform> {
    private var target: String? = null
    private var type: String? = null

    constructor(line: Int, source: Array<String>?, element: String) {
        this.line = line
        this.source = source
        name = parse(element)
    }

    constructor(name: String?, type: String?) {
        this.name = name
        this.type = type
    }

    constructor(line: Int, copy: Uniform) {
        this.line = line
        name = copy.name
        type = copy.type
        target = copy.target
        this.inFrag = (copy.inFrag)
        this.inVertex = (copy.inVertex)
    }

    /**
     * Parses the uniform
     *
     * @param input the input data
     * @return returns the name
     */
    override fun parse(input: String): String? {
        type = input.split(":").toTypedArray()[1].trim { it <= ' ' }
        target = input.substring(input.indexOf("(") + 1, input.lastIndexOf(")"))
        when {
            target.equals("vert", ignoreCase = true) -> {
                inVertex = (true)
                inFrag = (false)
            }
            target.equals("frag", ignoreCase = true) -> {
                inFrag = (true)
                inVertex = (false)
            }
            else -> {
                inVertex = (true)
                inFrag = (true)
            }
        }
        return input.substring(if (input.contains("->")) input.indexOf("->") + 2 else 0, input.indexOf("("))
            .trim { it <= ' ' }
    }

    override fun toString(): String {
        return "Uniform{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", line='" + line + '\'' +
                '}'
    }

    /**
     * Serialize's the data
     *
     * @return returns the glsl representation of the uniform
     */
    override fun serialize(): String? {
        return "uniform $type $name;"
    }

    override fun compareTo(o: Uniform): Int {
        val value = priority - o.priority
        return if (value == 0) name!!.compareTo(o.name!!) else value
    }

    /**
     * Gets the priority based upon the type
     * Lower is higher priority
     *
     * @return the priority
     */
    private val priority: Int
        private get() = when (type) {
            "mat4" -> 0
            "mat3" -> 1
            "vec4" -> 2
            "vec3" -> 3
            "vec2" -> 4
            "float" -> 5
            "int" -> 6
            "bool" -> 7
            "sampler2D" -> 8
            else -> Int.MAX_VALUE
        }

    override fun equals(obj: Any?): Boolean {
        if (obj === this) {
            return true
        }
        if (obj is Uniform) {
            val other = obj
            val nameEquals = other.name == name
            val shaderMatch = other.inFrag === this.inFrag && other.inVertex === this.inVertex
            return nameEquals && shaderMatch
        }
        return false
    }

    override fun hashCode(): Int {
        var result = target?.hashCode() ?: 0
        result = 31 * result + (type?.hashCode() ?: 0)
        return result
    }
}