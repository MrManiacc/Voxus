package me.jraynor.client.opengl.light

import com.artemis.Component
import me.jraynor.client.opengl.shader.Shader
import me.jraynor.client.render.util.EditorComponent
import org.joml.Vector3f

/**
 * This will load a light
 */
data class Light(val position: Vector3f = Vector3f(), val color: Vector3f = Vector3f(0.523f, 0.5f, 0.645f)) :
    EditorComponent() {
    /**
     * This will load the light to the shader
     */
    fun load(name: String, shader: Shader) {
        shader.loadVec3("$name.pos", position)
        shader.loadVec3("$name.color", color)
    }
}