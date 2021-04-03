package me.jraynor.client.opengl.model.mesh

import me.jraynor.common.asset.AssetData
import java.util.ArrayList

/**
 * this will store all of the
 */
class MeshData(
    /**This keeps track of the meshes internally**/
    private val meshes: MutableSet<Mesh> = HashSet()
) : AssetData() {


    /**
     * this will get the index of the mesh if it exists otherwise it will be -1
     */
    operator fun get(meshIn: Mesh): Int {
        var index = -1
        meshes.forEachIndexed lit@{ i: Int, mesh: Mesh ->
            if (meshIn == mesh) {
                index = i
                return@lit
            }
        }
        return index
    }

    /**
     * This will attempt to get the mesh at the given index or return a null mesh.
     */
    operator fun get(index: Int): Mesh? {
        var meshOut: Mesh? = null
        meshes.forEachIndexed lit@{ i, mesh ->
            if (index == i) {
                meshOut = mesh
                return@lit
            }
        }
        return meshOut
    }


    /***
     * adds a new mesh
     */
    fun add(mesh: Mesh): Boolean {
        return meshes.add(mesh)
    }

    /**
     * This will allow us to add new meshes easily
     */
    operator fun plus(mesh: Mesh) {
        if (!add(mesh))
            println("Warning~ failed to add mesh $mesh")
    }

    /**
     * This will allow us to add new meshes easily
     */
    operator fun plusAssign(mesh: Mesh) {
        if (!add(mesh))
            println("Warning~ failed to add mesh $mesh")
    }

    /**
     * true if the meshes set has the given mesh
     */
    fun has(mesh: Mesh): Boolean {
        return meshes.contains(mesh)
    }

    /**
     * This will iterate each mesh
     */
    fun forEach(iterator: (Int, Mesh) -> Unit) {
        meshes.forEachIndexed(iterator)
    }

    /**
     * This will iterate each mesh
     */
    fun forEach(iterator: (Mesh) -> Unit) {
        meshes.forEach(iterator)
    }

    /**
     * This will attempt to remove the given mesh or return null if it doesn't exits
     */
    fun remove(mesh: Mesh): Mesh? {
        if (!has(mesh)) return null
        if (meshes.remove(mesh)) return mesh
        return null
    }

    /**
     * This will attempt to remove the mesh from the given index.
     */
    fun remove(index: Int): Mesh? {
        var toRemove: Mesh? = null
        forEach lit@{ i, mesh ->
            if (index == i) {
                toRemove = mesh
                return@lit
            }
        }
        return this.remove(toRemove ?: return null)
    }


}