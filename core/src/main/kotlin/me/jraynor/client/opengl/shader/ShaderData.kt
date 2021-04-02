package me.jraynor.client.opengl.shader

import me.jraynor.common.asset.AssetData
import me.jraynor.util.asList
import me.jraynor.util.asString
import org.apache.commons.io.IOUtils
import java.io.InputStream
import java.nio.charset.Charset

/**
 * This stores the information needed for the shader
 */
class ShaderData : AssetData() {
    var vertexSource: String? = null
    var fragmentSource: String? = null
    val binds = ArrayList<Bind>()
    val uniforms = ArrayList<Uniform>()
    private val structs: MutableMap<String, Struct> = HashMap()
    private var lastAttribute = 0
    private val imports = hashMapOf(
        Pair(
            "BasicMaterial", """
                struct Material {
                    vec4 color;
                };
            """
        ), Pair(
            "BasicLight", """
                   struct Light {
                    vec3 pos;
                    vec3 color;
                };
            """
        ), Pair(
            "Camera", """
                uniform vec3 camera;
            """
        )
    )


    /**
     * This will load the
     */
    override fun load(stream: InputStream): Boolean {
        var source = IOUtils.readLines(stream, Charset.defaultCharset()).asString()
        while (source.contains("//")) {
            val before = source.indexOf("//")
            val after = source.indexOf("\n", before)
            if (before != -1) {
                val pre = source.substring(before, after)
                source = source.replace(pre, "")
            }
        }
        vertexSource = source.substring(source.indexOf("#vertex") + "#vertex".length, source.indexOf("#fragment"))
        fragmentSource = source.substring(source.indexOf("#fragment") + "#fragment".length)
        this.vertexSource = mapImports(vertexSource!!)
        this.fragmentSource = mapImports(fragmentSource!!)
        mapStructs(vertexSource!!)
        mapStructs(fragmentSource!!)
        mapBinds(vertexSource!!)
        mapUniforms(source)
        println(uniforms)
        return true
    }

    /**
     * This will replace the imports correctly
     */
    private fun mapImports(input: String): String {
        var text = input
        while (text.contains("#import")) {
            val start = text.indexOf("#import")
            val end = text.indexOf(";", start)
            val word = text.substring(start + "#import ".length, end)
            if (word.isNotBlank()) {
                text = replaceImport(text, start, end + 1, word)
            }
        }
        return text
    }

    /**
     * This will replace all of the imports with the correct text
     */
    private fun replaceImport(original: String, start: Int, end: Int, import: String): String {
        val before = original.substring(0, start)
        val after = original.substring(end)
        return before + imports[import] + after.trim()
    }

    /**
     * This will map the binds
     */
    private fun mapBinds(vertexSource: String) {
        val lines = vertexSource.asList()
        lines.forEach {
            val line = it.trim()
            if (line.startsWith("in") && line.endsWith(";")) {
                val bind = line.substring("in".length, line.length - 1).trim().split(" ")
                binds.add(Bind(bind[1], lastAttribute++))
            }
        }
    }

    /**
     * This will map the binds
     */
    private fun mapUniforms(fullSource: String) {
        val lines = fullSource.asList()
        lines.forEach { it ->
            val line = it.trim()
            if (line.startsWith("uniform") && line.endsWith(";")) {
                val uniform = line.substring("uniform".length, line.length - 1).trim().split(" ")
                val type = uniform[0].trim()
                val name = uniform[1].trim()
                if (structs.containsKey(type)) {
                    println(type)
                    val current = structs[type]!!
                    current.uniforms.forEach { structUniform ->
                        this.uniforms.add(Uniform("$name.${structUniform.name}"))
                    }
                } else
                    uniforms.add(Uniform(uniform[1])) //TODO check here if it's a struct
            }
        }
    }

    private fun mapStructs(source: String) {
        val lines = source.asList()
        var lastStructName = ""
        var struct: Struct? = null
        lines.forEach {
            val line = it.trim()
            if (line.contains("struct")) {
                lastStructName = line.substring("struct".length, line.indexOf("{")).trim()
                struct = Struct(lastStructName)
                return@forEach
            }
            if (line.contains("};") && struct != null) {
                structs[lastStructName] = struct!!
                lastStructName = ""
                struct = null
                return@forEach
            }
            if (struct != null) {
                val name = line.substring(line.indexOf(" "), line.indexOf(";")).trim()
                struct!!.uniforms.add(Uniform(name))
            }
        }
    }


    data class Struct(val name: String, val uniforms: MutableList<Uniform> = ArrayList())

    /**
     * This is used for binding a name to a location in the shader
     */
    data class Bind(val name: String, val attribute: Int)

    /**
     * This is used for storing uniform names
     */
    data class Uniform(val name: String)

}