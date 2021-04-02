package me.jraynor.client.opengl.camera

import com.artemis.Component
import me.jraynor.client.render.util.EditorComponent
import me.jraynor.common.Transform
import me.jraynor.common.util.degrees
import me.jraynor.common.util.radians
import org.joml.FrustumIntersection
import org.joml.Matrix4f

/**
 * This will create
 */
data class FirstPersonCamera(
        val fov: Float = 80f,
        var aspect: Float = 1f,
        val near: Float = 0.1f,
        val far: Float = 1000f
) : EditorComponent() {
    private val projectionMatrix = Matrix4f()
    private val viewMatrix = Matrix4f()

    private val projectView = Matrix4f()
    private val intersection = FrustumIntersection()

    /**
     * This will update and return the projection matrix.
     */
    fun projection(aspect: Float? = null): Matrix4f {
        if (aspect != null)
            this.aspect = aspect
        return projectionMatrix.identity().perspective(fov.radians, this.aspect, near, far)
    }

    /**
     * s
     * This will return the model matrix using the transform to update it's self
     */
    fun model(transform: Transform): Matrix4f {
        return viewMatrix.identity()
                .rotateX(transform.rotation!!.x.degrees)
                .rotateY(transform.rotation.y.degrees)
                .translate(-transform.position!!.x, -transform.position!!.y, -transform.position!!.z)
    }

    /**
     * This will return the view matrix * the projection matrix
     */
    fun projectionView(): Matrix4f {
        projectView.identity().perspective(fov.radians, aspect, near, far)
        return this.projectView.mul(viewMatrix)
    }

    /**
     * Gets the intersection helper
     */
    fun intersection(): FrustumIntersection {
        return this.intersection.set(projectionView())
    }


}

/**
 * This will control the input settings for the camera
 */
data class CameraSettings(
        val verticalSensitivity: Float = 50f,
        val horizontalSensitivity: Float = 50f,
        val walkSpeed: Float = 25f
) : EditorComponent()