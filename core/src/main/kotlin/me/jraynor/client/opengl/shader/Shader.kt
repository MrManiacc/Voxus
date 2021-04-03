package me.jraynor.client.opengl.shader

import me.jraynor.client.opengl.model.texture.Texture
import me.jraynor.common.asset.Asset
import org.joml.Matrix4f
import org.joml.Vector2i
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20

/**
 * This class is used for bindings to glsl shaders
 */
class Shader(name: String) : Asset<ShaderData>(name, ShaderData::class.java, "glsl", "shaders") {
    private var vertexId: Int? = null
    private var fragmentId: Int? = null
    private var programId: Int? = null
    private val uniforms = HashMap<String, Int>()
    private var compiled = false
    private val matrixBuffer = BufferUtils.createFloatBuffer(16)

    /**
     * This will reload the asset data
     */
    override fun reload(data: ShaderData) {
        compiled = compile(data)
        if (compiled)
            link(data)
        else
            unload()
    }

    /**
     * This will compile the shader source
     * @return returns true if successful
     */
    private fun compile(data: ShaderData): Boolean {
        vertexId = GL20.glCreateShader(GL20.GL_VERTEX_SHADER)
        GL20.glShaderSource(vertexId!!, data.vertexSource!!)
        GL20.glCompileShader(vertexId!!)
        if (GL20.glGetShaderi(vertexId!!, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            val error = GL20.glGetShaderInfoLog(vertexId!!)
            println(
                "Failed to compile vertex shader[$name], with error: $error, for source: \n${data.vertexSource}"
            )
            return false
        }
        fragmentId = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER)
        GL20.glShaderSource(fragmentId!!, data.fragmentSource!!)
        GL20.glCompileShader(fragmentId!!)
        if (GL20.glGetShaderi(fragmentId!!, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            println(
                "Failed to compile fragment shader[$name], with error: \n${
                    GL20.glGetShaderInfoLog(
                        fragmentId!!,
                        500
                    )
                }\nfor source: \n${data.fragmentSource}"
            )
            return false
        }
        println("successfully compiled shader with name $name")
        return true
    }

    /**
     * This will link the shader
     */
    private fun link(data: ShaderData) {
        this.programId = GL20.glCreateProgram()
        GL20.glAttachShader(programId!!, vertexId!!)
        GL20.glAttachShader(programId!!, fragmentId!!)
        data.binds.forEach {
            GL20.glBindAttribLocation(programId!!, it.attribute, it.name)
        }
        GL20.glLinkProgram(programId!!)
        GL20.glValidateProgram(programId!!)
        data.uniforms.forEach {
            this.uniforms[it.name] = GL20.glGetUniformLocation(programId!!, it.name)
        }
    }

    /**
     * This will start program
     */
    fun start() {
        if (compiled)
            GL20.glUseProgram(programId!!)
    }

    /**
     * loads matrix to the shader
     *
     * @param name the matrix name in the shader
     * @param mat  the matrix value
     */
    fun loadMat4(name: String?, mat: Matrix4f) {
        if (compiled)
            if (uniforms.containsKey(name)) {
                mat.get(matrixBuffer)
                GL20.glUniformMatrix4fv(uniforms[name]!!, false, matrixBuffer)
            }
    }

    /**
     * Pass a uniform float to shader
     *
     * @param name  uniform's name
     * @param value the value of the uniform
     */
    fun loadFloat(name: String?, value: Float) {
        if (compiled)
            if (uniforms.containsKey(name)) GL20.glUniform1f(uniforms[name]!!, value)
    }

    /**
     * Pass a vec3 to a shader
     *
     * @param name the uniforms name
     * @param vec  the vec to passed to the shader
     */
    fun loadVec3(name: String?, vec: Vector3f) {
        if (compiled)
            if (uniforms.containsKey(name)) GL20.glUniform3f(uniforms[name]!!, vec.x, vec.y, vec.z)
    }

    /**
     * Pass a vec3 to a shader
     *
     * @param name the uniforms name
     * @param vec  the vec to passed to the shader
     */
    fun loadTexture(name: String, slot: Int) {
        if (compiled) {
            if (uniforms.containsKey(name)) {
                val pos = uniforms[name]!!
                println(pos)
                GL20.glUniform1i(uniforms[name]!!, slot)
            }
        }
    }

    /**
     * Pass a vec3 to a shader
     *
     * @param name the uniforms name
     * @param vec  the vec to passed to the shader
     */
    fun loadVec2(name: String?, vec: Vector2i) {
        if (compiled)
            if (uniforms.containsKey(name)) GL20.glUniform2i(uniforms[name]!!, vec.x, vec.y)
    }

    /**
     * loads vec4 to the shader
     *
     * @param name the vec4 name in the shader
     * @param vec  the vec4 value
     */
    fun loadVec4(name: String?, vec: Vector4f) {
        if (uniforms.containsKey(name)) GL20.glUniform4f(uniforms[name]!!, vec.x, vec.y, vec.z, vec.w)
    }

    /**
     * loads boolean to the shader
     *
     * @param name  the boolean name in the shader
     * @param value the boolean value
     */
    fun loadBool(name: String?, value: Boolean) {
        if (uniforms.containsKey(name)) {
            val `val` = if (value) 1 else 0
            GL20.glUniform1i(uniforms[name]!!, `val`)
        }
    }

    /**
     * this will stop the shader program
     */
    fun stop() {
        if (compiled)
            GL20.glUseProgram(0)

    }

    /**
     * This will dispose of the asset data
     */
    override fun dispose() {
        GL20.glDetachShader(programId!!, vertexId!!)
        GL20.glDetachShader(programId!!, fragmentId!!)
        GL20.glDeleteShader(vertexId!!)
        GL20.glDeleteShader(fragmentId!!)
        GL20.glDeleteProgram(programId!!)
    }
}

