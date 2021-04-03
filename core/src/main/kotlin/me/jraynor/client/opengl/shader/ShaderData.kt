package me.jraynor.client.opengl.shader
//package me.jraynor.client.opengl.shader
//
//import me.jraynor.common.asset.AssetData
//import me.jraynor.util.asList
//import me.jraynor.util.asString
//import org.apache.commons.io.IOUtils
//import java.io.InputStream
//import java.nio.charset.Charset
//
///**
// * This stores the information needed for the shader
// */
//class me.jraynor.client.opengl.shader.ShaderData : AssetData() {
//    var vertexSource: String? = null
//    var fragmentSource: String? = null
//    val binds = ArrayList<Bind>()
//    val uniforms = ArrayList<Uniform>()
//    private val structs: MutableMap<String, Struct> = HashMap()
//    private var lastAttribute = 0
//    private val imports = hashMapOf(
//        Pair(
//            "BasicMaterial", """
//                struct Material {
//                    vec4 color;
//                };
//            """
//        ), Pair(
//            "Material", """
//                uniform sampler2D diffuse;
//            """
//        ),
//        Pair(
//            "BasicLight", """
//                   struct Light {
//                    vec3 pos;
//                    vec3 color;
//                };
//            """
//        ), Pair(
//            "Camera", """
//                uniform vec3 camera;
//            """
//        )
//    )
//
//
//    /**
//     * This will load the
//     */
//    override fun load(stream: InputStream): Boolean {
//        var source = IOUtils.readLines(stream, Charset.defaultCharset()).asString()
//        while (source.contains("//")) {
//            val before = source.indexOf("//")
//            val after = source.indexOf("\n", before)
//            if (before != -1) {
//                val pre = source.substring(before, after)
//                source = source.replace(pre, "")
//            }
//        }
//        vertexSource = source.substring(source.indexOf("#vertex") + "#vertex".length, source.indexOf("#fragment"))
//        fragmentSource = source.substring(source.indexOf("#fragment") + "#fragment".length)
//        this.vertexSource = mapImports(vertexSource!!)
//        this.fragmentSource = mapImports(fragmentSource!!)
//        mapStructs(vertexSource!!)
//        mapStructs(fragmentSource!!)
//        mapBinds(vertexSource!!)
//        mapUniforms(vertexSource!!)
//        mapUniforms(fragmentSource!!)
//        return true
//    }
//
//    /**
//     * This will replace the imports correctly
//     */
//    private fun mapImports(input: String): String {
//        if (!input.contains("#import")) return input
//        var text = input
//        while (text.contains("#import")) {
//            val start = text.indexOf("#import")
//            val end = text.indexOf(";", start)
//            val word = text.substring(start + "#import ".length, end)
//            if (word.isNotBlank()) {
//                text = replaceImport(text, start, end + 1, word)
//            }
//        }
//        return text
//    }
//
//    /**
//     * This will replace all of the imports with the correct text
//     */
//    private fun replaceImport(original: String, start: Int, end: Int, import: String): String {
//        val before = original.substring(0, start)
//        val after = original.substring(end)
//        return before + imports[import] + after.trim()
//    }
//
//    /**
//     * This will map the binds
//     */
//    private fun mapBinds(vertexSource: String) {
//        val lines = vertexSource.asList()
//        lines.forEach {
//            val line = it.trim()
//            if (line.startsWith("in") && line.endsWith(";")) {
//                val bind = line.substring("in".length, line.length - 1).trim().split(" ")
//                binds.add(Bind(bind[1], lastAttribute++))
//            }
//        }
//    }
//
//    /**
//     * This will map the binds
//     */
//    private fun mapUniforms(fullSource: String) {
//        val lines = fullSource.asList()
//        lines.forEach { it ->
//            val line = it.trim()
//            if (line.startsWith("uniform") && line.endsWith(";")) {
//                val uniform = line.substring("uniform".length, line.length - 1).trim().split(" ")
//                val type = uniform[0].trim()
//                val name = uniform[1].trim()
//                if (structs.containsKey(type)) {
//                    val current = structs[type]!!
//                    current.uniforms.forEach { structUniform ->
//                        this.uniforms.add(Uniform("$name.${structUniform.name}"))
//                    }
//                } else
//                    uniforms.add(Uniform(uniform[1])) //TODO check here if it's a struct
//            }
//        }
//    }
//
//    private fun mapStructs(source: String) {
//        if (!source.contains("struct")) return
//        val lines = source.asList()
//        var lastStructName = ""
//        var struct: Struct? = null
//        lines.forEach {
//            val line = it.trim()
//            if (line.contains("struct")) {
//                lastStructName = line.substring("struct".length, line.indexOf("{")).trim()
//                struct = Struct(lastStructName)
//                return@forEach
//            }
//            if (line.contains("};") && struct != null) {
//                structs[lastStructName] = struct!!
//                lastStructName = ""
//                struct = null
//                return@forEach
//            }
//            if (struct != null) {
//                val name = line.substring(line.indexOf(" "), line.indexOf(";")).trim()
//                struct!!.uniforms.add(Uniform(name))
//            }
//        }
//    }
//
//
//    data class Struct(val name: String, val uniforms: MutableList<Uniform> = ArrayList())
//
//    /**
//     * This is used for binding a name to a location in the shader
//     */
//    data class Bind(val name: String, val attribute: Int)
//
//    /**
//     * This is used for storing uniform names
//     */
//    data class Uniform(val name: String)
//
//}

import me.jraynor.client.opengl.shader.parse.Bind
import me.jraynor.client.opengl.shader.parse.Custom
import me.jraynor.client.opengl.shader.parse.Import
import me.jraynor.client.opengl.shader.parse.Uniform
import me.jraynor.data.AssetData
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ShaderData(
    val name: String,
    val vertexSource: Array<String>,
    val fragmentSource: Array<String>,
    val customLines: Array<String>
) : AssetData {

    val uniformMap: MutableMap<String, Uniform> = HashMap()
    val importsMap: MutableMap<String, MutableList<Import>> = HashMap()
    val customsMap: MutableMap<String, Custom> = HashMap()
    val bindsMap: MutableMap<String, Bind> = HashMap()
    val binds: MutableList<Bind> = ArrayList()
    val uniforms: MutableList<Uniform> = ArrayList()
    val customs: MutableList<Custom> = ArrayList()

    //This defines if this is a util class or a runnable shader class

    var executable = false


    var vertex: String? = null


    var fragment: String? = null

    /**
     * Determines if the shader is executable or not
     */
    private fun determineIfExecutable() {
        for (src in vertexSource) {
            if (src.trim { it <= ' ' }.startsWith("void main")) executable = true
        }
        if (executable) {
            executable = false
            for (src in fragmentSource) {
                if (src.trim { it <= ' ' }.startsWith("void main")) executable = true
            }
        }
    }

    /**
     * Adds an import
     *
     * @param imp the import to add
     */
    fun addImport(imp: Import): Import {
        imp.name?.let {
            if (importsMap.containsKey(imp.name)) importsMap[imp.name]!!.add(imp) else importsMap[it] =
                ArrayList(listOf(imp))
        }
        return imp
    }

    /**
     * adds a new custom type
     *
     * @param custom the custom type
     */
    fun addCustom(custom: Custom): Custom {
        custom.name?.let {
            customsMap[it] = custom
        }
        customs.add(custom)
        return custom
    }

    /**
     * Compiles the source
     */
    fun compile() {
        sort()
        val compiledVertex = compileShader(vertexSource, true)
        val vertexBuilder = StringBuilder()
        for (line in compiledVertex.keys) vertexBuilder.append(compiledVertex[line]).append("\n")
        vertex = vertexBuilder.toString()
        val compiledFrag = compileShader(fragmentSource, false)
        val fragBuilder = StringBuilder()
        for (line in compiledFrag.keys) fragBuilder.append(compiledFrag[line]).append("\n")
        fragment = fragBuilder.toString()
    }

    /**
     * Compiles the shader, depending on if it's the vertex or not it will do the correct lookups for variables
     *
     * @param input  the input source
     * @param vertex if true, we're processing the vertex shader, otherwise its the fragment shader
     * @return returns a map of the lines
     */
    private fun compileShader(input: Array<String>, vertex: Boolean): Map<Int, String> {
        //We need to find out if we're inserting it at the given position and if we are we need to
        val output = HashMap<Int, String>()
        for (i in input.indices) {
            val bind = getBind(i, vertex)
            val uniform: Optional<Uniform> = getUniform(i, vertex)
            val custom: Optional<Custom> = getCustom(i, vertex)
            if (custom.isPresent() && !output.containsValue(custom.get().serialize())) {
                var j = i
                while (output.containsKey(j)) j++
                custom.get().serialize()?.let { output[j] = it }
            }
            if (bind.isPresent && !output.containsValue(bind.get().serialize())) {
                var j = i
                while (output.containsKey(j)) j++
                output[j] = bind.get().serialize()
            }
            if (uniform.isPresent() && !output.containsValue(uniform.get().serialize())) {
                var j = i
                while (output.containsKey(j)) j++
                custom.get().serialize()?.let { output[j] = it }
            }
        }
        for (i in input.indices) {
            val orig = Optional.ofNullable(input[i])
            if (!isImportLine(i, vertex)) {
                var j = i
                while (output.containsKey(j)) j++
                if (orig.isPresent) if (orig.get().isNotEmpty() && !orig.get().trim { it <= ' ' }.startsWith("//")) {
                    output[j] = orig.get()
                }
            }
        }
        return output
    }

    /**
     * Adds a bind
     *
     * @param bind the bind to add
     */
    fun addBind(bind: Bind): Bind {
        bind.name?.let { bindsMap[it] = bind }
        binds.add(bind)
        return bind
    }

    /**
     * Adds a uniform
     *
     * @param uniform the uniform to add
     */
    fun addUniform(uniform: Uniform): Uniform {
        uniform.name?.let { uniformMap[it] = uniform }
        uniforms.add(uniform)
        return uniform
    }

    /**
     * Get's a bind by the given name
     *
     * @param name the name of the bind
     * @return returns a bind with the given name
     */
    fun getBind(name: String?): Optional<Bind> {
        val bind = AtomicReference<Optional<Bind>>(Optional.empty())
        binds.forEach(Consumer { b: Bind ->
            if (b.name.equals(name)) bind.set(Optional.of(b))
        })
        return bind.get()
    }

    /**
     * Gets a bind via it's line or empty
     *
     * @param line the line to get
     * @return returns a bind or empty
     */
    fun getBind(line: Int, vertex: Boolean): Optional<Bind> {
        val output = AtomicReference<Optional<Bind>>(Optional.empty())
        binds.forEach(Consumer { bind: Bind ->
            if (bind.line == line && (vertex && bind.inVertex || !vertex && bind.inFrag)) output.set(
                Optional.of(bind)
            )
        })
        return output.get()
    }

    /**
     * Gets a bind via it's line or empty
     *
     * @param line the line to get
     * @return returns a bind or empty
     */
    fun getUniform(line: Int, vertex: Boolean): Optional<Uniform> {
        val output: AtomicReference<Optional<Uniform>> = AtomicReference(Optional.empty<Uniform>())
        uniforms.forEach(Consumer<Uniform> { uniform: Uniform ->
            if (line == uniform.line) {
                if (uniform.inFrag && !vertex) {
                    output.set(Optional.of<Uniform>(uniform))
                }
                if (uniform.inVertex && vertex) output.set(Optional.of<Uniform>(uniform))
            }
        })
        return output.get()
    }

    /**
     * Gets a bind via it's line or empty
     *
     * @param line the line to get
     * @return returns a bind or empty
     */
    fun getCustom(line: Int, vertex: Boolean): Optional<Custom> {
        val output: AtomicReference<Optional<Custom>> = AtomicReference(Optional.empty<Custom>())
        customs.forEach(Consumer { custom: Custom ->
            if (custom.line == line) {
                if (custom.inFrag && !vertex) {
                    output.set(Optional.of<Custom>(custom))
                }
                if (custom.inVertex && vertex) output.set(Optional.of<Custom>(custom))
            }
        })
        return output.get()
    }

    /**
     * Get's a bind by the given name
     *
     * @param name the name of the bind
     * @return returns a bind with the given name
     */
    fun getUniform(name: String?): Optional<Uniform> {
        val uniform: AtomicReference<Optional<Uniform>> = AtomicReference<Optional<Uniform>>(Optional.empty())
        uniforms.forEach(Consumer { b: Uniform ->
            if (b.name.equals(name)) uniform.set(
                Optional.of<Uniform>(b)
            )
        })
        return uniform.get()
    }

    /**
     * Get's a bind by the given name
     *
     * @param name the name of the bind
     * @return returns a bind with the given name
     */
    fun getCustom(name: String?): Optional<Custom> {
        val custom: AtomicReference<Optional<Custom>> = AtomicReference<Optional<Custom>>(Optional.empty())
        customs.forEach(Consumer { b: Custom ->
            if (b.name.equals(name)) custom.set(Optional.of<Custom>(b))
        })
        return custom.get()
    }

    /**
     * Checks to see if the line is an import line
     *
     * @param line   the line to check
     * @param vertex if true, we're checking against the vertex shader, otherwise fragement shader
     * @return returns true if this line was originally an imported line
     */
    private fun isImportLine(line: Int, vertex: Boolean): Boolean {
        val output = AtomicReference(false)
        importsMap.values.forEach(Consumer<List<Import>> { imports: List<Import> ->
            imports.forEach(
                Consumer { anImport: Import ->
                    if (anImport.inVertex && vertex) {
                        if (anImport.line == line) output.set(true)
                    }
                    if (anImport.inFrag && !vertex) if (anImport.line == line) output.set(true)
                })
        })
        return output.get()
    }

    /**
     * Sorts the values, and updates the lines accordingly
     */
    fun sort() {
        binds.sort()
        val activeVertLines = HashSet<Int>()
        val activeFragLines = HashSet<Int>()
        binds.forEach(Consumer { bind: Bind ->
            if (bind.inVertex) {
                while (activeVertLines.contains(bind.line)) {
                    bind.line = (bind.line?.plus(1))
                }
                bind.line?.let { activeVertLines.add(it) }
            }
            if (bind.inFrag) {
                while (activeFragLines.contains(bind.line)) {
                    bind.line = (bind.line?.plus(1))
                }
                bind.line?.let { activeFragLines.add(it) }
            }
        })
        uniforms.forEach(Consumer { uniform: Uniform ->
            if (uniform.inVertex) {
                while (activeVertLines.contains(uniform.line)) {
                    uniform.line = (uniform.line?.plus(1))
                }
                uniform.line?.let {
                    activeVertLines.add(it)
                }
            }
            if (uniform.inFrag) {
                while (activeFragLines.contains(uniform.line)) {
                    uniform.line = (uniform.line?.plus(1))
                }
                uniform.line?.let { activeFragLines.add(it) }
            }
        })
        uniforms.sort()
    }

    /**
     * Prints the shader data
     */
    override fun toString(): String {
        val builder = StringBuilder()
        builder.append(name.capitalize()).append(" Shader").append("{").append("\n")
        if (!customs.isEmpty()) {
            builder.append("\tCustoms{").append("\n")
            customs.forEach(Consumer<Custom> { custom: Custom ->
                builder.append("\t\t").append(custom.toString()).append("\n")
            })
            builder.append("\t").append("}").append("\n")
        }
        if (!binds.isEmpty()) {
            builder.append("\tBinds{").append("\n")
            binds.forEach(Consumer { bind: Bind ->
                builder.append("\t\t").append(bind.toString()).append("\n")
            })
            builder.append("\t").append("}").append("\n")
        }
        if (!uniforms.isEmpty()) {
            builder.append("\tUniforms{").append("\n")
            uniforms.forEach(Consumer<Uniform> { uniform: Uniform ->
                builder.append("\t\t").append(uniform.toString()).append("\n")
            })
            builder.append("\t").append("}").append("\n")
        }
        builder.append("}")
        return builder.toString()
    }

    init {
        determineIfExecutable()
    }
}