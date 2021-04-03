//package me.jraynor.client.opengl.shader
//
//import me.jraynor.client.opengl.model.texture.Texture
//import me.jraynor.common.asset.Asset
//import org.joml.Matrix4f
//import org.joml.Vector2i
//import org.joml.Vector3f
//import org.joml.Vector4f
//import org.lwjgl.BufferUtils
//import org.lwjgl.opengl.GL11
//import org.lwjgl.opengl.GL20
//
///**
// * This class is used for bindings to glsl shaders
// */
//class Shader(name: String? = null) : Asset<me.jraynor.client.opengl.shader.ShaderData>(name, me.jraynor.client.opengl.shader.ShaderData::class.java, "glsl", "shaders") {
//    private var vertexId: Int? = null
//    private var fragmentId: Int? = null
//    private var programId: Int? = null
//    private val uniforms = HashMap<String, Int>()
//    private var compiled = false
//    private val matrixBuffer = BufferUtils.createFloatBuffer(16)
//
//    /**
//     * This will reload the asset data
//     */
//    override fun reload(data: me.jraynor.client.opengl.shader.ShaderData) {
//        compiled = compile(data)
//        if (compiled)
//            link(data)
//        else
//            unload()
//    }
//
//    /**
//     * This will compile the shader source
//     * @return returns true if successful
//     */
//    private fun compile(data: me.jraynor.client.opengl.shader.ShaderData): Boolean {
//        vertexId = GL20.glCreateShader(GL20.GL_VERTEX_SHADER)
//        GL20.glShaderSource(vertexId!!, data.vertexSource!!)
//        GL20.glCompileShader(vertexId!!)
//        if (GL20.glGetShaderi(vertexId!!, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
//            val error = GL20.glGetShaderInfoLog(vertexId!!)
//            println(
//                "Failed to compile vertex shader[$name], with error: $error, for source: \n${data.vertexSource}"
//            )
//            return false
//        }
//        fragmentId = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER)
//        GL20.glShaderSource(fragmentId!!, data.fragmentSource!!)
//        GL20.glCompileShader(fragmentId!!)
//        if (GL20.glGetShaderi(fragmentId!!, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
//            println(
//                "Failed to compile fragment shader[$name], with error: \n${
//                    GL20.glGetShaderInfoLog(
//                        fragmentId!!,
//                        500
//                    )
//                }\nfor source: \n${data.fragmentSource}"
//            )
//            return false
//        }
//        println("successfully compiled shader with name $name")
//        return true
//    }
//
//    /**
//     * This will link the shader
//     */
//    private fun link(data: me.jraynor.client.opengl.shader.ShaderData) {
//        this.programId = GL20.glCreateProgram()
//        GL20.glAttachShader(programId!!, vertexId!!)
//        GL20.glAttachShader(programId!!, fragmentId!!)
//        data.binds.forEach {
//            GL20.glBindAttribLocation(programId!!, it.attribute, it.name)
//        }
//        GL20.glLinkProgram(programId!!)
//        GL20.glValidateProgram(programId!!)
//        data.uniforms.forEach {
//            this.uniforms[it.name] = GL20.glGetUniformLocation(programId!!, it.name)
//        }
//    }
//
//    /**
//     * This will start program
//     */
//    fun start() {
//        if (compiled)
//            GL20.glUseProgram(programId!!)
//    }
//
//    /**
//     * loads matrix to the shader
//     *
//     * @param name the matrix name in the shader
//     * @param mat  the matrix value
//     */
//    fun loadMat4(name: String?, mat: Matrix4f) {
//        if (compiled)
//            if (uniforms.containsKey(name)) {
//                mat.get(matrixBuffer)
//                GL20.glUniformMatrix4fv(uniforms[name]!!, false, matrixBuffer)
//            }
//    }
//
//    /**
//     * Pass a uniform float to shader
//     *
//     * @param name  uniform's name
//     * @param value the value of the uniform
//     */
//    fun loadFloat(name: String?, value: Float) {
//        if (compiled)
//            if (uniforms.containsKey(name)) GL20.glUniform1f(uniforms[name]!!, value)
//    }
//
//    /**
//     * Pass a vec3 to a shader
//     *
//     * @param name the uniforms name
//     * @param vec  the vec to passed to the shader
//     */
//    fun loadVec3(name: String?, vec: Vector3f) {
//        if (compiled)
//            if (uniforms.containsKey(name)) GL20.glUniform3f(uniforms[name]!!, vec.x, vec.y, vec.z)
//    }
//
//    /**
//     * Pass a vec3 to a shader
//     *
//     * @param name the uniforms name
//     * @param vec  the vec to passed to the shader
//     */
//    fun loadTexture(name: String, slot: Int) {
//        if (compiled) {
//            if (uniforms.containsKey(name)) {
//                val pos = uniforms[name]!!
//                println(pos)
//                GL20.glUniform1i(uniforms[name]!!, slot)
//            }
//        }
//    }
//
//    /**
//     * Pass a vec3 to a shader
//     *
//     * @param name the uniforms name
//     * @param vec  the vec to passed to the shader
//     */
//    fun loadVec2(name: String?, vec: Vector2i) {
//        if (compiled)
//            if (uniforms.containsKey(name)) GL20.glUniform2i(uniforms[name]!!, vec.x, vec.y)
//    }
//
//    /**
//     * loads vec4 to the shader
//     *
//     * @param name the vec4 name in the shader
//     * @param vec  the vec4 value
//     */
//    fun loadVec4(name: String?, vec: Vector4f) {
//        if (uniforms.containsKey(name)) GL20.glUniform4f(uniforms[name]!!, vec.x, vec.y, vec.z, vec.w)
//    }
//
//    /**
//     * loads boolean to the shader
//     *
//     * @param name  the boolean name in the shader
//     * @param value the boolean value
//     */
//    fun loadBool(name: String?, value: Boolean) {
//        if (uniforms.containsKey(name)) {
//            val `val` = if (value) 1 else 0
//            GL20.glUniform1i(uniforms[name]!!, `val`)
//        }
//    }
//
//    /**
//     * this will stop the shader program
//     */
//    fun stop() {
//        if (compiled)
//            GL20.glUseProgram(0)
//
//    }
//
//    /**
//     * This will dispose of the asset data
//     */
//    override fun dispose() {
//        GL20.glDetachShader(programId!!, vertexId!!)
//        GL20.glDetachShader(programId!!, fragmentId!!)
//        GL20.glDeleteShader(vertexId!!)
//        GL20.glDeleteShader(fragmentId!!)
//        GL20.glDeleteProgram(programId!!)
//    }
//}
//
package me.jraynor.client.opengl.shader

import com.jgfx.assets.urn.ResourceUrn
import me.jraynor.asset.Asset
import me.jraynor.asset.AssetType
import me.jraynor.client.opengl.shader.parse.Bind
import me.jraynor.client.opengl.shader.parse.Uniform
import org.joml.*
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import java.nio.FloatBuffer
import java.util.function.Consumer

class Shader(urn: ResourceUrn, assetType: AssetType<Shader, ShaderData>, data: ShaderData) :
    Asset<ShaderData>(urn, assetType) {
    private var programID = 0
    private var vertexID = 0
    private var fragmentID = 0

    private val uniforms: MutableMap<String, Int> = HashMap()
    private val matrixBuffer: FloatBuffer = BufferUtils.createFloatBuffer(16)

    /**
     * Load the shader asset
     *
     * @param data The data to load.
     */
    override fun reload(data: ShaderData) {
        if (data.executable) {
            data.compile()
            loadShader(data)
        }
    }

    /**
     * Loads the actual shaders with the provided data
     *
     * @param data the data to load
     */
    private fun loadShader(data: ShaderData) {
        vertexID = GL20.glCreateShader(GL20.GL_VERTEX_SHADER)
        GL20.glShaderSource(vertexID, data.vertex!!)
        GL20.glCompileShader(vertexID)
        if (GL20.glGetShaderi(vertexID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            println(GL20.glGetShaderInfoLog(vertexID, 500))
            System.err.println("Failed to compile vertex shader")
            System.exit(-1)
        }
        fragmentID = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER)
        GL20.glShaderSource(fragmentID, data.fragment!!)
        GL20.glCompileShader(fragmentID)
        if (GL20.glGetShaderi(fragmentID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            println(GL20.glGetShaderInfoLog(fragmentID, 500))
            System.err.println("Failed to compile fragment shader")
            System.exit(-1)
        }
        programID = GL20.glCreateProgram()
        GL20.glAttachShader(programID, vertexID)
        GL20.glAttachShader(programID, fragmentID)
        //Parse the binds
        data.binds.forEach { this.parseBinds.accept(it) }
        GL20.glLinkProgram(programID)
        GL20.glValidateProgram(programID)
        //parse the uniforms
        data.uniforms.forEach { this.parseUniforms.accept(it) }

    }

    /**
     * Parses the binds for the shader
     */
    private val parseBinds = Consumer { bind: Bind ->
        GL20.glBindAttribLocation(
            programID,
            bind.attribute,
            bind.name
        )
    }

    /**
     * Parses the uniforms for the shader
     */
    private val parseUniforms: Consumer<Uniform> =
        Consumer<Uniform> { uniform: Uniform ->
            uniforms[uniform.name!!] = GL20.glGetUniformLocation(programID, uniform.name!!)
        }

    /**
     * Pass a uniform float to shader
     *
     * @param name  uniform's name
     * @param value the value of the uniform
     */
    fun loadFloat(name: String, value: Float) {
        GL20.glUniform1f(uniforms[name]!!, value)
    }

    /**
     * Pass a vec3 to a shader
     *
     * @param name the uniforms name
     * @param vec  the vec to passed to the shader
     */
    fun loadVec3(name: String, vec: Vector3f) {
        GL20.glUniform3f(uniforms[name]!!, vec.x, vec.y, vec.z)
    }

    /**
     * loads vec4 to the shader
     *
     * @param name the vec4 name in the shader
     * @param vec  the vec4 value
     */
    fun loadVec4(name: String, vec: Vector4f) {
        GL20.glUniform4f(uniforms[name]!!, vec.x, vec.y, vec.z, vec.w)
    }

    /**
     * loads boolean to the shader
     *
     * @param name  the boolean name in the shader
     * @param value the boolean value
     */
    fun loadBool(name: String, value: Boolean) {
        val `val` = if (value) 1 else 0
        GL20.glUniform1i(uniforms[name]!!, `val`)
    }

    /**
     * loads matrix to the shader
     *
     * @param name the matrix name in the shader
     * @param mat  the matrix value
     */
    fun loadMat4(name: String, mat: Matrix4f) {
        mat[matrixBuffer]
        GL20.glUniformMatrix4fv(uniforms[name]!!, false, matrixBuffer)
    }

    /**
     * Loads an int
     */
    fun loadInt(name: String, value: Int) {
        GL20.glUniform1i(uniforms[name]!!, value)
    }

    /**
     * loads matrix to the shader
     *
     * @param name the matrix name in the shader
     */
    fun loadVec2(name: String, vec: Vector2f) {
        GL20.glUniform2f(uniforms[name]!!, vec.x, vec.y)
    }

    /**
     * loads matrix to the shader
     *
     * @param name the matrix name in the shader
     */
    fun loadVec2i(name: String, vec: Vector2i) {
        GL20.glUniform2i(uniforms[name]!!, vec.x, vec.y)
    }

    /**
     * Dispose of the shader
     */
    fun dispose() {
        GL20.glDetachShader(programID, vertexID)
        GL20.glDetachShader(programID, fragmentID)
        GL20.glDeleteShader(vertexID)
        GL20.glDeleteShader(fragmentID)
        GL20.glDeleteProgram(programID)
    }

    /**
     * Must be called before using shader
     */
    fun start() {
        GL20.glUseProgram(programID)
    }

    /**
     * Must be called after using shader
     */
    fun stop() {
        GL20.glUseProgram(0)
    }


    /**
     * The constructor for an asset. It is suggested that implementing classes provide a constructor taking both the urn, and an initial AssetData to load.
     *
     * @param urn       The urn identifying the asset.
     * @param assetType The asset type this asset belongs to.
     */
    init {
        reload(data)
    }
}