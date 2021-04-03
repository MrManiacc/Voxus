package me.jraynor.client.opengl.model.mesh

import me.jraynor.client.opengl.internal.Vao


/**
 * This is the edit class, it controls the vao
 */
class Mesh {
    internal val floatBuffers: HashMap<Int, Pair<FloatArray, Int>> = HashMap()
    internal val intBuffers: HashMap<Int, Pair<IntArray, Int>> = HashMap()
    internal var indices: IntArray? = null

    /**The index of the material**/
    var materialIndex = 0

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Mesh

        if (floatBuffers != other.floatBuffers) return false
        if (intBuffers != other.intBuffers) return false
        if (indices != null) {
            if (other.indices == null) return false
            if (!indices.contentEquals(other.indices)) return false
        } else if (other.indices != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = floatBuffers.hashCode()
        result = 31 * result + intBuffers.hashCode()
        result = 31 * result + (indices?.contentHashCode() ?: 0)
        return result
    }


}

