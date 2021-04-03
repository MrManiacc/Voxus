package me.jraynor.client.opengl.model.material

import assimp.AiTexture
import me.jraynor.client.opengl.model.texture.Texture
import me.jraynor.util.PropertyComponent
import org.joml.Vector3f

/**
 * This is a component that will be passed to a shader.
 */
class Material() : PropertyComponent() {
    /**exposes the name for usability**/
    val name: String get() = get("name") ?: "unnamed"

    /**
     * This will load the given texture
     */
    fun reload(force: Boolean = false) {
        if (force) dispose()
        forEach<Texture> { _, texture ->
            if (!has<Texture.IntTexParameter>())
                texture.defaults()
            texture.reload(force)
        }
    }

    /**
     * this will get the give texture for
     */
    fun texture(type: AiTexture.Type): Texture? {
        return get(type.name)
    }

    /**
     * This will get a color of the given type
     */
    fun color(type: AiTexture.Type): Vector3f? {
        return get(type.name)
    }


    /**
     * This will delete the texture
     */
    fun dispose() {
        forEach<Texture> { _, texture ->
            texture.dispose()
        }
    }


}
