package me.jraynor.client.opengl.model.texture

import assimp.AiTexture
import com.jogamp.opengl.GL.GL_TEXTURE0
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
import org.lwjgl.opengl.GL12.glTexParameteri
import org.lwjgl.opengl.GL13.glActiveTexture
import java.io.InputStream
import java.net.URI
import java.nio.file.Path
import kotlin.experimental.and
import kotlin.math.round


/**
 * This allows us to store texture data very easily
 */
data class Texture(var textureId: Int = -1) : PropertyComponent() {
    private val util = IOUtil()

    /**Used to check to see if the texture is loaded into memory or not**/
    val isLoaded: Boolean get() = textureId != -1

    /**This will get the texture width**/
    val width: Int get() = this["width"] ?: -1

    /**This will get the texture height**/
    val height: Int get() = this["height"] ?: -1

    /**This will get the texture component**/
    val comp: Int get() = this["comp"] ?: -1

    /**gets the uri**/
    val uri: URI? get() = this["uri"]

    /**gets the texture by the given type**/
    val type: AiTexture.Type? get() = this["type"]

    /**gets the file path**/
    val file: File? get() = this["path"]

    /**Stores the buffer data**/
    private var image: ByteBuffer? = null

    var textureType = GL11.GL_TEXTURE_2D

    /**stores all of the parmater types**/
    private val params: MutableList<IntTexParameter> = ArrayList()

    /**
     * This will load the given texture
     */
    fun readTexture(path: Path) {
        val stack = stackPush()
        val w: IntBuffer = stack.mallocInt(1)
        val h: IntBuffer = stack.mallocInt(1)
        val comp: IntBuffer = stack.mallocInt(1)
//        val bufferData = util.ioResourceToByteBuffer(path.toString()z, 8 * 1024) ?: return
        // Use info to read image metadata without decoding the entire image.
        // We don't need this for this demo, just testing the API.
        if (!stbi_info(path.toString(), w, h, comp)) {
            throw RuntimeException("Failed to read image information: " + stbi_failure_reason());
        } else {
            println("OK with reason: " + stbi_failure_reason());
        }
        // Decode the image
        image = stbi_load(path.toString(), w, h, comp, 4) ?: error(stbi_failure_reason()!!)
        this["width"] = w.get(0)
        this["height"] = h.get(0)
        this["comp"] = comp.get(0)

    }

    /**
     * This will add the defaeult paramters if none have been specified
     */
    fun defaults() {
        this["target"] = gl.Target._2D
        params.add(IntTexParameter(textureType, GL_TEXTURE_MIN_FILTER, GL_NEAREST))
        params.add(IntTexParameter(textureType, GL_TEXTURE_MAG_FILTER, GL_NEAREST))
        params.add(IntTexParameter(textureType, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE))
        params.add(IntTexParameter(textureType, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE))
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
    fun bind(unit: Int = 0, type: Int = textureType) {
        if (textureId != -1) return
        glActiveTexture(GL_TEXTURE0 + unit)
        glBindTexture(type, textureId)
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    /**
     * this will load the texture
     */
    fun reload(force: Boolean = false) {
//        if (force) dispose()
        glEnable(GL_TEXTURE_2D);
        val it = image ?: return
        this.textureId = glGenTextures()
        bind(0)

        this.params.forEach {
            glTexParameteri(textureType, it.name, it.parameter)
        }

        val format = if (comp == 3) {
            GL_RGB8
        } else {
//            premultiplyAlpha()
//            glEnable(GL_BLEND)
//            glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA)
            GL_RGBA8;
        }

        glTexImage2D(
            textureType,
            0,
            GL_RGBA8,
            width,
            height,
            0,
            GL_RGBA,
            GL_UNSIGNED_BYTE,
            it

        ) //This actually uploads the buffer datea
        unbind()
    }

    fun unbind() {
        glBindTexture(textureType, 0)
    }


    /**
     * This will delete the texture
     */
    fun dispose() {
        if (textureId != -1) {
            glDeleteTextures(textureId)
            textureId = -1
        }
        image?.clear()
        image = null
    }

    /**
     * This allows use to provide texture parameters easily
     */
    data class IntTexParameter(val target: Int, val name: Int, val parameter: Int)


}