package me.jraynor.client.opengl.model.material

import com.artemis.Component
import me.jraynor.client.opengl.shader.Shader
import org.joml.Vector4f

/**
 * This is a component that will be passed to a shader.
 */
class Material(val color: Vector4f = Vector4f(1f, 1f, 1f, 1f)) : Component() {
    /**
     * This will load the given material to the shader.
     */
    fun load(name: String, shader: Shader) {
        shader.loadVec4("$name.color", color)
    }
}
