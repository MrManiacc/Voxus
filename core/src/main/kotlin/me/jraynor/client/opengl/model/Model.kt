package me.jraynor.client.opengl.model

import me.jraynor.client.opengl.internal.Vao
import me.jraynor.common.asset.Asset

/**
 * This will load all of the meshes into memory.
 */
class Model(name: String = "no_name") : Asset<ModelData>(name, ModelData::class.java, "obj", "models") {
    private val vaos = ArrayList<Vao>()

    /**
     * This will load the actual meshes into memory, as well as the textures.
     */
    override fun reload(data: ModelData) {
        if (vaos.isNotEmpty())
            dispose()
        data.meshes.forEach {
            val vao = it.make()
            vao.load()
            vaos.add(vao)
        }
    }

    /**
     * This will render our models
     */
    fun render() {
        vaos.forEach {
            it.draw(true)
        }
    }

    /**
     * This will delete the model.
     */
    override fun dispose() {
        this.vaos.forEach {
            it.dispose()
        }
        this.vaos.clear()
    }

}