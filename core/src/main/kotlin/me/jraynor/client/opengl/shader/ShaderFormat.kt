package me.jraynor.client.opengl.shader

import com.jgfx.assets.urn.ResourceUrn
import me.jraynor.client.opengl.shader.parse.Bind
import me.jraynor.client.opengl.shader.parse.ShaderParser.parseShader
import me.jraynor.client.opengl.shader.parse.Uniform
import me.jraynor.files.AbstractAssetFileFormat
import me.jraynor.files.AssetDataFile
import java.io.IOException
import java.nio.file.Files


class ShaderFormat : AbstractAssetFileFormat<ShaderData>("glsl") {
    /**
     * Loads the shader data
     *
     * @param urn    The urn identifying the asset being loaded.
     * @param inputs The inputs corresponding to this asset
     * @return returns the shader data
     * @throws IOException
     */
    @Throws(IOException::class)
    override fun load(urn: ResourceUrn, inputs: List<AssetDataFile>): ShaderData? {
        val input = inputs[0]
        val stream = input.openStream()
        val lines = Files.readAllLines(input.path)
        stream.close()
        val data: ShaderData = buildShader(lines, urn)
        parseUniforms(data.vertexSource, data)
        parseUniforms(data.fragmentSource, data)
        parseBinds(data.vertexSource, data)
        parseBinds(data.fragmentSource, data)
        return parseShader(data, urn)
    }

    /**
     * Parses all of the uniforms
     */
    private fun parseUniforms(lines: Array<String>, data: ShaderData) {
        for (l in lines) {
            val line = l.trim { it <= ' ' }
            if (line.trim { it <= ' ' }.startsWith("uniform")) {
                val elements = line.split(" ".toRegex()).toTypedArray()
                val type = elements[1].trim { it <= ' ' }
                val name = elements[2].replace(";", "").trim { it <= ' ' }
                data.addUniform(Uniform(name, type))
            }
        }
    }

    /**
     * Parses all of the binds, attribute index will be in sequential order, starting with 0
     */
    private fun parseBinds(lines: Array<String>, data: ShaderData) {
        var attribute = 0
        for (l in lines) {
            val line = l.trim { it <= ' ' }
            if (line.trim { it <= ' ' }.startsWith("in")) {
                val elements = line.split(" ".toRegex()).toTypedArray()
                val type = elements[1].trim { it <= ' ' }
                val name = elements[2].replace(";", "").trim { it <= ' ' }
                data.addBind(Bind(attribute++, name, type))
            }
        }
    }

    /**
     * Builds the shader data from the shader source inputs
     *
     * @param lines the input to build for
     * @return returns the shader data
     */
    private fun buildShader(lines: List<String>, urn: ResourceUrn): ShaderData {
        val customLinesList = ArrayList<String>()
        val shaderLines = parseShaderLines(lines, customLinesList)
        val vertexSource = arrayOfNulls<String>(shaderLines[1] - shaderLines[0])
        val fragSource = arrayOfNulls<String>(shaderLines[3] - shaderLines[2])
        val customLines = Array(customLinesList.size) { "" }
        for (i in customLinesList.indices) customLines[i] = customLinesList[i]
        run {
            var i = shaderLines[0]
            var j = 0
            while (i < shaderLines[1]) {
                vertexSource[j] = lines[i].trim { it <= ' ' }
                i++
                j++
            }
        }
        var i = shaderLines[2]
        var j = 0
        while (i < shaderLines[3]) {
            fragSource[j] = lines[i].trim { it <= ' ' }
            i++
            j++
        }
        val vertSrc = ArrayList<String>()
        for (line in vertexSource) {
            if (!line!!.trim { it <= ' ' }.startsWith("//")) vertSrc.add(line)
        }
        val fragSrc = ArrayList<String>()
        for (line in fragSource) {
            if (!line!!.trim { it <= ' ' }.startsWith("//")) fragSrc.add(line)
        }
        return ShaderData(
            urn.fragmentName.lowerCase,
            vertSrc.toTypedArray(),
            fragSrc.toTypedArray(),
            customLines
        )
    }

    /**
     * Finds the start and stop lines for the given shader,
     *
     * @param lines the shader source
     * @return returns the start and stop of both shaders
     */
    private fun parseShaderLines(lines: List<String>, customLines: MutableList<String>): IntArray {
        val shaderLines = intArrayOf(
            -1, -1, -1, -1
        )
        var inVertex = false
        var inFrag = false
        for (i in lines.indices) {
            val line = lines[i].trim { it <= ' ' }
            if (line.startsWith("#ifdef")) {
                val id = line.split(" ".toRegex()).toTypedArray()[1]
                if (id.equals("VERTEX_SHADER", ignoreCase = true)) {
                    shaderLines[0] = i + 1
                    inVertex = true
                }
                if (id.equals("FRAGMENT_SHADER", ignoreCase = true)) {
                    shaderLines[2] = i + 1
                    inFrag = true
                }
            } else if (line.startsWith("#endif")) {
                if (inVertex) {
                    inVertex = false
                    shaderLines[1] = i
                }
                if (inFrag) {
                    shaderLines[3] = i
                    inFrag = false
                }
            }
            if (!inFrag && !inVertex) {
                customLines.add(line)
            }
        }
        return shaderLines
    }


}