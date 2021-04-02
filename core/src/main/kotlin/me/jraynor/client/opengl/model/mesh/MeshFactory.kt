@file:Suppress("UNCHECKED_CAST")

package me.jraynor.client.opengl.model.mesh

import org.lwjgl.util.par.ParShapes.par_shapes_create_cube
import org.lwjgl.util.par.ParShapes.par_shapes_create_parametric_sphere
import org.lwjgl.util.par.ParShapesMesh
import java.nio.Buffer
import java.nio.FloatBuffer
import java.nio.IntBuffer

/**
 * This will generate some default meshes.
 */
object MeshFactory {
    /**
     * This will generate a flat give with the given vertex count
     */
    fun generateGrid(vertexCount: Int, scale: Float): Mesh {
        val count: Int = vertexCount * vertexCount
        val vertices = FloatArray(count * 3)
        val normals = FloatArray(count * 3)
        val textureCoords = FloatArray(count * 2)
        val indices = IntArray(6 * (vertexCount - 1) * (vertexCount - 1))
        var vertexPointer = 0
        for (i in 0 until vertexCount) {
            for (j in 0 until vertexCount) {
                vertices[vertexPointer * 3] = (j.toFloat() / (vertexCount - 1f)) * scale
                vertices[vertexPointer * 3 + 1] = 0f
                vertices[vertexPointer * 3 + 2] = (i.toFloat() / (vertexCount - 1f)) * scale

                normals[vertexPointer * 3] = 0f
                normals[vertexPointer * 3 + 1] = 1f
                normals[vertexPointer * 3 + 2] = 0f

                textureCoords[vertexCount * 2] = j.toFloat() / (vertexCount - 1f)
                textureCoords[vertexCount * 2 + 1] = i.toFloat() / (vertexCount - 1f)

                vertexPointer++
            }
        }
        var pointer = 0
        for (z in 0 until vertexCount - 1) {
            for (x in 0 until vertexCount - 1) {
                val topLeft = z * vertexCount + x//Top left
                val topRight = topLeft + 1
                val bottomLeft = ((z + 1) * vertexCount) + x
                val bottomRight = bottomLeft + 1
                indices[pointer++] = topLeft
                indices[pointer++] = bottomLeft
                indices[pointer++] = topRight
                indices[pointer++] = topRight
                indices[pointer++] = bottomLeft
                indices[pointer++] = bottomRight
            }
        }
        val mesh = Mesh()
        mesh.with(vertices, 0, 3)
        mesh.with(normals, 1, 3)
        mesh.with(textureCoords, 2, 2)
        mesh.with(indices)
        return mesh
    }

    /**
     * This will generate a 3d sphere mesh
     */
    fun generateSphere(slices: Int, stacks: Int): Mesh {
        return par_shapes_create_parametric_sphere(slices, stacks)?.let { toMesh(it) }!!
    }

    /**
     * This will generate a 3d sphere mesh
     */
    fun generateCube(): Mesh {
        return par_shapes_create_cube()?.let { toMesh(it) }!!
    }

    /**
     * This will generate our mesh from the givne parshape mesh
     */
    private fun toMesh(meshIn: ParShapesMesh): Mesh {
        val mesh = Mesh()
        val vertexCount = meshIn.npoints()
        val points = toArray<FloatArray>(meshIn.points(vertexCount * 3))
        points?.let { mesh.with(it, 0, 3) }
        val indices = toArray<IntArray>(meshIn.triangles(meshIn.ntriangles() * 3))
        indices?.let { mesh.with(it) }
        val normals = toArray<FloatArray>(meshIn.normals(vertexCount * 3))
        normals?.let { mesh.with(it, 1, 3) }
        return mesh
    }

    /**
     * This should convert the given buffer into an array
     */
    private fun <A : Any> toArray(buffer: Buffer?): A? {
        if (buffer is FloatBuffer) {
            val array = FloatArray(buffer.capacity())
            buffer.get(array)
            return array as A
        } else if (buffer is IntBuffer) {
            val array = IntArray(buffer.capacity())
            buffer.get(array)
            return array as A
        }
        return null
    }

}