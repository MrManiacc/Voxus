package me.jraynor.client.opengl.model.texture

import gli_.gl
import gln.texture.glBindTexture
import gln.texture.glTexParameteri
import me.jraynor.util.PropertyComponent
import org.lwjgl.opengl.GL11
import java.io.File
import java.nio.ByteBuffer
import me.jraynor.util.IOUtil
import org.lwjgl.stb.STBImage.*
import org.lwjgl.system.MemoryStack.stackPush

import java.nio.IntBuffer

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE
import kotlin.experimental.and
import kotlin.math.round


/**
 * This allows us to store texture data very easily
 */
class Texture : PropertyComponent() {
    private var textureId: Int = -1
    private val util = IOUtil()

    /**Used to check to see if the texture is loaded into memory or not**/
    val isLoaded: Boolean get() = textureId != -1

    /**This will get the texture width**/
    val width: Int get() = this["width"] ?: -1

    /**This will get the texture height**/
    val height: Int get() = this["height"] ?: -1

    /**This will get the texture component**/
    val comp: Int get() = this["comp"] ?: -1


    /**
     * This will load the given texture
     */
    fun readTexture() {
        ifPresent<File>("path") {
            val stack = stackPush()
            val w: IntBuffer = stack.mallocInt(1)
            val h: IntBuffer = stack.mallocInt(1)
            val comp: IntBuffer = stack.mallocInt(1)
            val bufferData = util.ioResourceToByteBuffer(it.absolutePath, 8 * 1024) ?: return@ifPresent
            // Use info to read image metadata without decoding the entire image.
            // We don't need this for this demo, just testing the API.
            if (!stbi_info_from_memory(bufferData, w, h, comp)) {
                throw RuntimeException("Failed to read image information: " + stbi_failure_reason());
            } else {
                println("OK with reason: " + stbi_failure_reason());
            }
            // Decode the image
            this["image"] = stbi_load_from_memory(bufferData, w, h, comp, 0) ?: return@ifPresent
            this["width"] = w.get(0)
            this["height"] = h.get(0)
            this["comp"] = comp.get(0)
        }
    }

    /**
     * This will add the defaeult paramters if none have been specified
     */
    fun defaults() {
        this["target"] = gl.Target._2D
        add(IntTexParameter(gl.Target._2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR))
        add(IntTexParameter(gl.Target._2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR))
        add(IntTexParameter(gl.Target._2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE))
        add(IntTexParameter(gl.Target._2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE))
    }

    private fun premultiplyAlpha() {
        ifPresent<ByteBuffer>("image") {
            val stride: Int = width * 4
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val i = y * stride + x * 4
                    val alpha: Float = (it.get(i + 3) and 0xFF.toByte()) / 255.0f
                    it.put(i + 0, round((it.get(i + 0) and 0xFF.toByte()) * alpha).toByte())
                    it.put(i + 1, round((it.get(i + 1) and 0xFF.toByte()) * alpha).toByte())
                    it.put(i + 2, round((it.get(i + 2) and 0xFF.toByte()) * alpha).toByte())
                }
            }
        }
    }

    /**
     * This will correctly bind the texture
     */
    fun bind() {
        if (textureId != -1) return
        val target: gl.Target = this["target"] ?: gl.Target._2D //By default it will be a texture 2d
        glBindTexture(target, textureId)
    }

    /**
     * this will load the texture
     */
    fun reload(force: Boolean = false) {
        if (force) dispose()
        if (textureId != -1) return
        ifPresent<ByteBuffer>("image") {
            val target: gl.Target = this["target"] ?: gl.Target._2D //By default it will be a texture 2d
            this.textureId = glGenTextures()
            glBindTexture(target, textureId);
            forEach<IntTexParameter> { _, property ->
                glTexParameteri(property.target, property.name, property.parameter)
            }

            val format = if (comp == 3) {
                if (width and 3 != 0) {
                    glPixelStorei(GL_UNPACK_ALIGNMENT, 2 - (width and 1))
                }
                GL_RGB
            } else {
                premultiplyAlpha()
                glEnable(GL_BLEND)
                glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA)
                GL_RGBA;
            }

            glTexImage2D(
                target.i,
                0,
                format,
                width,
                height,
                0,
                format,
                GL_UNSIGNED_BYTE,
                it
            ) //This actually uploads the buffer datea

        }
    }


    /**
     * This will delete the texture
     */
    fun dispose() {
        if (textureId != -1) {
            glDeleteTextures(textureId)
            textureId = -1
        }
        ifPresent<ByteBuffer>("image") {
            it.clear()
            remove<ByteBuffer>("image")//We no longer need the image data
        }

    }

    /**
     * This allows use to provide texture parameters easily
     */
    data class IntTexParameter(val target: gl.Target, val name: Int, val parameter: Int)


}