package me.jraynor.client.opengl.internal

import me.jraynor.client.opengl.model.mesh.Mesh
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import java.util.concurrent.ConcurrentHashMap

/**
 * This class will store all vaos, allowing for them to be unloaded
 */
internal object Vaos {
    internal val vaos = ConcurrentHashMap<Int, Vao>()

    /**
     * This will dispose of all of the vaos
     */
    fun disposeAll() {
        vaos.values.forEach {
            it.dispose()
        }
    }

}

/**
 *  ~Vertex Array Object~
 *
 * This will create a new vertex array object,
 * it can store multiple vbos, and easily be bound and rendered.
 * it's very customizable and easy to use.
 * @param builder - the vao will using the [Mesh] class to initialize
 */
class Vao(private var mesh: Mesh? = null) {
    var id = -1
    private val vbos = HashMap<Int, Vbo>()
    private var count = 0
    private var indexVbo: Vbo? = null

    //Arrays by default
    private var drawType = DrawType.ARRAYS
    private var loaded = false

    init {
        if (mesh != null)
            load()
    }

    /**
     * This will allow for the loading a mesh
     */
    fun load(mesh: Mesh? = null): Vao {
        if (mesh != null)
            this.mesh = mesh
        id = GL30.glGenVertexArrays();
        bind()
        mapFloatBuffers()
        mapIntBuffers()
        mapIndices()
        unbind()
        Vaos.vaos[id] = this
        return this
    }

    /**
     * Binds the vao for drawing
     */
    fun bind() {
        GL30.glBindVertexArray(id)
        vbos.forEach {
            GL20.glEnableVertexAttribArray(it.key)
            it.value.bind()

        }
    }

    /**
     * This will draw the element
     */
    fun draw(bind: Boolean = true) {
        if (bind)
            bind()
        when (drawType) {
            DrawType.ELEMENTS -> glDrawElements(GL_TRIANGLES, count, GL11.GL_UNSIGNED_INT, 0);
            DrawType.ARRAYS -> GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, count)
        }
        if (bind)
            unbind()
    }

    /**
     * Unbinds the vao
     */
    fun unbind() {
        vbos.forEach {
            it.value.unbind()
            GL20.glDisableVertexAttribArray(it.key)
        }
        GL30.glBindVertexArray(0)
    }

    /**
     * This will load all of the float data to fbos
     */
    private fun mapFloatBuffers() {
        mesh!!.floatBuffers.forEach {
            val attribute = it.key
            val data = it.value.first
            val attributeSize = it.value.second
            val vbo = Vbo(GL_ARRAY_BUFFER)
            vbo.bind()
            vbo.storeData(data)
            GL20.glVertexAttribPointer(
                attribute,
                attributeSize,
                GL11.GL_FLOAT,
                false,
                0,
                0
            )
            vbo.unbind()
            vbos[attribute] = vbo
            if (attribute == 0)
                count = data.count() / attributeSize
        }
    }

    /**
     * This will load all of the float data to fbos
     */
    private fun mapIntBuffers() {
        mesh!!.intBuffers.forEach {
            val attribute = it.key
            val data = it.value.first
            val attributeSize = it.value.second
            val vbo = Vbo(GL_ARRAY_BUFFER)
            vbo.bind()
            vbo.storeData(data)
            GL30.glVertexAttribIPointer(attribute, attributeSize, GL11.GL_INT, 0, 0)
            vbo.unbind()
            vbos[attribute] = vbo
        }
    }

    /**
     * Maps the indices
     */
    private fun mapIndices() {
        if (mesh!!.indices != null) {
            indexVbo = Vbo(GL_ELEMENT_ARRAY_BUFFER)
            indexVbo!!.bind()
            indexVbo!!.storeData(mesh!!.indices!!)
            this.count = mesh!!.indices!!.count()
            drawType = DrawType.ELEMENTS
        }
    }

    /**
     * This will dispose and delete all of the vbos, and current id
     */
    fun dispose() {
        GL30.glDeleteVertexArrays(id)
        vbos.forEach {
            it.value.dispose()
        }
        if (indexVbo != null)
            indexVbo!!.dispose()
        vbos.clear()
        indexVbo = null
        loaded = false
        Vaos.vaos.remove(id)
        id = -1
    }

    /**
     * This is use to create a new vao with a builder
     */
    companion object {
        const val BYTES_PER_FLOAT = 4
        const val BYTES_PER_INT = 4

        /**Simple routines **/
        fun new(): Mesh {
            return Mesh()
        }

        /**
         * This is used to determine the draw tyep
         */
        enum class DrawType {
            ARRAYS, ELEMENTS
        }

    }

    /**
     * ~Vertex Buffer Object~
     *
     * This is an internal structure/object that that cant store arbitrary data.
     *
     * The end user will never interact with this, it's entirely internal
     */
    private class Vbo(
        private val type: Int
    ) {
        private var vboId: Int = -1

        /**
         * This will bind the vbo
         */
        internal fun bind() {
            if (vboId == -1)
                vboId = glGenBuffers()
            glBindBuffer(type, vboId)
        }

        /**
         * THis will unbind the vbos
         */
        internal fun unbind() {
            glBindBuffer(type, 0)
        }

        internal fun storeData(data: IntArray) {
            val buffer = BufferUtils.createIntBuffer(data.count())
            buffer.put(data)
            buffer.flip()
            glBufferData(type, data, GL_STATIC_DRAW)
        }

        /**
         * Stores a float array
         */
        internal fun storeData(data: FloatArray) {
            val buffer = BufferUtils.createFloatBuffer(data.count())
            buffer.put(data)
            buffer.flip()
            glBufferData(type, data, GL_STATIC_DRAW)
        }

        /**
         * this will delte the vbo id
         */
        fun dispose() {
            glDeleteBuffers(vboId)
        }


    }


}
