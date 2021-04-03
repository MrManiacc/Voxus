package me.jraynor.client.opengl.model

import me.jraynor.client.opengl.model.material.MaterialData
import me.jraynor.client.opengl.model.mesh.Mesh
import me.jraynor.client.opengl.model.mesh.MeshData
import me.jraynor.client.parse.ModelParser
import me.jraynor.common.asset.AssetData


import me.lignum.jvox.VoxFile
import me.lignum.jvox.Voxel
import org.joml.Vector3f
import org.joml.Vector3i
import org.joml.Vector4f
import java.util.ArrayList
import java.io.File


/**
 * This will load our model data from the given input stream.
 */
class ModelData(
    /**This stores all of the mesh data/all of the loadable meshes.**/
    val meshData: MeshData = MeshData(),
    /**This stores all of the material data/all of the loadable materials.**/
    val matData: MaterialData = MaterialData()
) : AssetData() {

    /**
     * This will load the file from the given path.
     */
    override fun load(path: String): Boolean {
        return ModelParser( File(path), matData, meshData).parse()
    }

}

