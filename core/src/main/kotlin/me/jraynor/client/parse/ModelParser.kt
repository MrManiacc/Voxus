package me.jraynor.client.parse

import assimp.*
import me.jraynor.client.opengl.model.ModelData
import me.jraynor.client.opengl.model.material.Material
import me.jraynor.client.opengl.model.material.MaterialData
import me.jraynor.client.opengl.model.mesh.MeshData
import java.io.File


/**
 * This object will allow us to parse model data from a given scene.
 */
internal class ModelParser(
    private val path: File,
    private val materialData: MaterialData,
    private val meshData: MeshData
) {

    /**
     * This will parse the model data for a given assimp scene
     */
    fun parse(): Boolean {
        val url = ModelParser::class.java.getResource("/models/${path.name}")
            ?: error("Failed to find url with name ${path.name}")
        val import = Importer()
        val uri = url.toURI()
        if (uri.exists()) {
            ASSIMP_LOAD_TEXTURES = false
            val scene = import.readFile(uri, AiPostProcessSteps.OptimizeMeshes.i or AiPostProcessSteps.Triangulate.i)
            if (scene == null) {
                println(import.errorString)
                return false
            }
            MaterialParser.parse(scene, materialData)
            MeshParser.parse(scene, meshData)
        }
        return true //This is so the loading isn't passed to the [InputStream] loader
    }


}