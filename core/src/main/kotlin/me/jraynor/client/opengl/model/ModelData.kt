package me.jraynor.client.opengl.model

import me.jraynor.client.opengl.model.mesh.Mesh
import me.jraynor.common.asset.AssetData

/**
 * This will load our model data from the given input stream.
 */
class ModelData(val meshes: MutableList<Mesh> = ArrayList()) : AssetData() {

    /**
     * This will load our assimp model from file.
     */
    override fun load(path: String): Boolean {
        println(path)
        //TODO load meshes from file
        return true
    }
}


