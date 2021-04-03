package me.jraynor.client.opengl.model.material

import me.jraynor.common.asset.AssetData
import java.util.ArrayList

/**
 * this will store all of the
 */
data class MaterialData(
    /**This keeps track of the materials internally**/
    private val materials: MutableSet<Material> = HashSet()
) : AssetData() {

    /**
     * this will get the index of the material if it exists otherwise it will be -1
     */
    operator fun get(materialIn: Material): Int {
        var index = -1
        materials.forEachIndexed lit@{ i: Int, material: Material ->
            if (materialIn == material) {
                index = i
                return@lit
            }
        }
        return index
    }

    /**
     * This will attempt to get the material at the given index or return a null material.
     */
    operator fun get(index: Int): Material? {
        var materialOut: Material? = null
        materials.forEachIndexed lit@{ i, material ->
            if (index == i) {
                materialOut = material
                return@lit
            }
        }
        return materialOut
    }


    /***
     * adds a new material
     */
    fun add(material: Material): Boolean {
        return materials.add(material)
    }

    /**
     * This will allow us to add new materials easily
     */
    operator fun plus(material: Material) {
        if (!add(material))
            println("Warning~ failed to add material $material")
    }

    /**
     * This will allow us to add new materials easily
     */
    operator fun plusAssign(material: Material) {
        if (!add(material))
            println("Warning~ failed to add material $material")
    }

    /**
     * true if the materials set has the given material
     */
    fun has(material: Material): Boolean {
        return materials.contains(material)
    }

    /**
     * This will iterate each material
     */
    fun forEach(iterator: (Int, Material) -> Unit) {
        materials.forEachIndexed(iterator)
    }

    /**
     * This will iterate each material
     */
    fun forEach(iterator: (Material) -> Unit) {
        materials.forEach(iterator)
    }

    /**
     * This will attempt to remove the given material or return null if it doesn't exits
     */
    fun remove(material: Material): Material? {
        if (!has(material)) return null
        if (materials.remove(material)) return material
        return null
    }

    /**
     * This will attempt to remove the material from the given index.
     */
    fun remove(index: Int): Material? {
        var toRemove: Material? = null
        forEach lit@{ i, material ->
            if (index == i) {
                toRemove = material
                return@lit
            }
        }
        return this.remove(toRemove ?: return null)
    }

}