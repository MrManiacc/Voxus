package me.jraynor.client.opengl.internal

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL14.GL_DEPTH_COMPONENT32
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.GL30.*
import org.lwjgl.opengl.GL32
import java.nio.ByteBuffer

/**
 * This allows us to render to frame buffers. Meaning we can render the main viewport into the imgui viewport, amoung
 * other things.
 */
class Fbo(var width: Int, var height: Int, private val useDepth: Boolean = false) {
    internal var textureId: Int? = null
    internal var depthId: Int? = null
    private var fboId: Int? = null

    /**
     * This will create our fbo in memeory
     */
    fun initialize() {
        getFboId()
        bindFrameBuffer()
        createColorTexture()
        if (useDepth)
            createDepthTexture()
        unbindFrameBuffer()
    }

    private fun createDepthTexture() {
        if (this.depthId != null)
            GL11.glDeleteTextures(depthId!!)
        depthId = GL11.glGenTextures()
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthId!!)
        GL11.glTexImage2D(
            GL11.GL_TEXTURE_2D,
            0,
            GL_DEPTH24_STENCIL8,
            width,
            height,
            0,
            GL_DEPTH_STENCIL,
            GL_UNSIGNED_INT_24_8,
            null as ByteBuffer?
        )
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
        GL32.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, GL_TEXTURE_2D, depthId!!, 0)
    }


    private fun createColorTexture() {
        if (this.textureId != null)
            GL11.glDeleteTextures(textureId!!)
        textureId = GL11.glGenTextures()
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId!!)
        GL11.glTexImage2D(
            GL11.GL_TEXTURE_2D,
            0,
            GL11.GL_RGB,
            width,
            height,
            0,
            GL11.GL_RGB,
            GL11.GL_UNSIGNED_BYTE,
            null as ByteBuffer?
        )
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
        GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, textureId!!, 0)
    }

    /**
     * This will create the fbo id if it's null, or
     */
    private fun getFboId(): Int {
        if (fboId == null) {
            fboId = GL30.glGenFramebuffers()
            bindFrameBuffer()
            GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0)
            unbindFrameBuffer()
        }
        return fboId!!
    }

    /**
     * This allows us to render the fbo to a texture
     */

    fun bindFrameBuffer() {
        fboId ?: return
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fboId!!)
    }

    fun bind() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)
        bindFrameBuffer()
        GL11.glViewport(0, 0, width, height)
    }

    /**
     * This will bind our texture for rendering
     */
    fun bindTexture() {
        textureId ?: return
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textureId!!)
    }

    fun unbindTexture() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)
    }

    /**
     * This will resize the texture for the given width and height
     */
    fun resize(width: Int, height: Int) {
        if (this.width != width || this.height != height) {
            this.width = width
            this.height = height
            getFboId()
            bindFrameBuffer()
            createColorTexture()
            if (useDepth)
                createDepthTexture()

            unbindFrameBuffer()
        }
    }

    /**
     * This will unbind our fbo
     */
    fun unbindFrameBuffer() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0)
    }

    fun dispose() {

    }


}