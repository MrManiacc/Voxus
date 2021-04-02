package me.jraynor.client.opengl.model.mesh

import me.jraynor.client.opengl.internal.Vao


/**
 * This is the edit class, it controls the vao
 */
class Mesh() {
    internal val floatBuffers: HashMap<Int, Pair<FloatArray, Int>> = HashMap()
    internal val intBuffers: HashMap<Int, Pair<IntArray, Int>> = HashMap()
    internal var indices: IntArray? = null

    /**
     * This method will create a float array, and convert if into
     */
    fun with(buffer: FloatArray, index: Int, dimensions: Int = 3): Mesh {
        this.floatBuffers[index] = Pair(buffer, dimensions)
        return this
    }

    /**
     * This method will create a float array, and convert if into
     */
    fun with(buffer: IntArray, index: Int, dimensions: Int = 3): Mesh {
        this.intBuffers[index] = Pair(buffer, dimensions)
        return this
    }

    /**
     * Adds the indices
     */
    fun with(indices: IntArray): Mesh {
        this.indices = indices
        return this
    }

    /**
     * Builds the vao¬
     */
    fun make(): Vao {
        return Vao(this)
    }
}

