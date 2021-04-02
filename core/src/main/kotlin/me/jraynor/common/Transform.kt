package me.jraynor.common

import com.artemis.Component
import me.jraynor.client.render.util.EditorComponent
import me.jraynor.common.util.degrees
import me.jraynor.common.util.radians
import org.joml.Matrix4f
import org.joml.Vector3f

/**
 * This class will store the need parts for a player
 */
/**
 * This will store a transform
 */
data class Transform(
    val position: Vector3f? = Vector3f(Math.random().toFloat() * 100f),
    val rotation: Vector3f? = Vector3f(),
    val scale: Vector3f? = Vector3f(1f)
) : EditorComponent() {
    private var matrix: Matrix4f? = null

    /**
     * This will translate the current position
     */
    fun translate(x: Float? = null, y: Float? = null, z: Float? = null, offset: Boolean = true) {
        if (x != null)
            if (offset) position!!.x += x
            else position!!.x = x
        if (y != null)
            if (offset) position!!.y += y
            else position!!.y = y
        if (z != null)
            if (offset) position!!.z += z
            else position!!.z = z

    }

    /**
     * This will translate the current position
     */
    fun rotate(x: Float? = null, y: Float? = null, z: Float? = null, offset: Boolean = true) {
        if (x != null)
            if (offset) rotation!!.x += x
            else rotation!!.x = x
        if (y != null)
            if (offset) rotation!!.y += y
            else rotation!!.y = y
        if (z != null)
            if (offset) rotation!!.z += z
            else rotation!!.z = z
    }

    /**
     * This will translate the current position
     */
    fun scale(x: Float? = null, y: Float? = null, z: Float? = null, offset: Boolean = true) {
        if (x != null)
            if (offset) scale!!.x += x
            else scale!!.x = x
        if (y != null)
            if (offset) scale!!.y += y
            else scale!!.y = y
        if (z != null)
            if (offset) scale!!.z += z
            else scale!!.z = z
    }

    /**
     * This will override all the data in this class with the other transform
     */
    fun transpose(other: Transform) {
        this.position!!.set(other.position)
        this.rotation!!.set(other.rotation)
        this.scale!!.set(other.scale)
    }

    /**
     * This will update the matrix and return it
     */
    fun matrix(recompute: Boolean = true): Matrix4f {
        if (matrix == null) matrix = Matrix4f()
        if (!recompute) return matrix!!
        return matrix!!.identity()
            .translate(position)
            .rotateX(rotation!!.x.radians)
            .rotateY(rotation.y.radians)
            .rotateZ(rotation.z.radians)
            .scale(scale)
    }
}