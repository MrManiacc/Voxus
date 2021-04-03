package me.jraynor.client.opengl.model

import assimp.AiTexture
import imgui.ImGui
import me.jraynor.client.opengl.internal.Vao
import me.jraynor.client.opengl.model.material.Material
import me.jraynor.client.opengl.model.texture.Texture
import me.jraynor.client.opengl.shader.Shader
import me.jraynor.common.asset.Asset
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.GL_TEXTURE_2D
import org.lwjgl.opengl.GL11.glEnable

/**
 * This will load all of the meshes into memory.
 */
class Model(name: String = "no_name") : Asset<ModelData>(name, ModelData::class.java, "obj", "models") {
    private val vaos = ArrayList<Pair<Vao, Int>>()
    private val materials = ArrayList<Material>()

    /**
     * This will load the actual meshes into memory, as well as the textures.
     */
    override fun reload(data: ModelData) {
        if (vaos.isNotEmpty() || materials.isNotEmpty())
            dispose()

        data.matData.forEach { _, material ->
            materials.add(material)
            material.reload(true)
        }

        data.meshData.forEach { mesh ->
            val vao = mesh.make()
            vao.load()
            vaos.add(Pair(vao, mesh.materialIndex))
        }
    }

    override fun render() {
        super.render()
//        vaos.forEach { data ->
//            data.first.bind()
//
//            material(data.second)?.let { mat ->
//                mat.texture(AiTexture.Type.diffuse)?.let {
//                    it.bind(0)
//                    ImGui.image(it.textureId, it.width.toFloat(), it.height.toFloat())
//                    it.unbind()
//                }
//            }
//            data.first.unbind()
//        }
    }

    /**
     * This will return the materail at the index or null
     */
    fun material(index: Int): Material? {
        if (index < 0 || index >= this.materials.size) return null
        return this.materials[index]
    }

    /**
     * This will render our models
     */
    fun render(shader: Shader) {
        vaos.forEach { data ->
            material(data.second)?.let { mat ->
                data.first.bind()
                mat.texture(AiTexture.Type.diffuse)?.let {
                    it.bind(0)
                    shader.loadTexture("diffuse", 0)
                    data.first.draw(false)
                }
                data.first.unbind()
                GL11.glBindTexture(GL_TEXTURE_2D, 0)
            }
        }
    }

    /**
     * This will delete the model.
     */
    override fun dispose() {
        this.vaos.forEach {
            it.first.dispose()
        }
        this.vaos.clear()
        this.materials.forEach {
            it.dispose()
        }
        this.materials.clear()
    }


}