package me.jraynor.client.opengl.model

import me.jraynor.client.opengl.model.mesh.Mesh
import me.jraynor.common.asset.AssetData
import java.io.InputStream
import java.io.IOException

import me.lignum.jvox.VoxReader

import me.lignum.jvox.VoxFile
import me.lignum.jvox.Voxel
import org.joml.Vector3f
import org.joml.Vector3i
import org.joml.Vector4f
import java.util.ArrayList


/**
 * This will load our model data from the given input stream.
 */
class ModelData(val meshes: MutableList<Mesh> = ArrayList()) : AssetData() {

    /**
     * This will load a voxel file into the meshes.
     */
    override fun load(stream: InputStream): Boolean {
        val voxFile: VoxFile = try {
            VoxReader(stream).use { reader ->
                // VoxReader::read will never return null,
                // but it can throw an InvalidVoxException.
                reader.read()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
        mapToMesh(voxFile)
        return true
    }

    /**
     * This will map the
     */
    private fun mapToMesh(voxFile: VoxFile) {
        val materials = voxFile.materials
        val palette = voxFile.palette
        val models = voxFile.models

        models.forEach {
            val mesh = Mesh()
            val size = it.size
            val scale = Vector3f(0.01f)
            val voxels = it.voxels
            processVoxelModel(
                mesh,
                voxels,
                palette,
                scale
            )
            meshes.add(mesh)
        }
    }

    /**
     * Converts the int color to a vec4f color
     */
    private fun intColorToVec4f(color: Int): Vector4f {
        val r = (color and 0xff) / 255.0f
        val g = (color shr 8 and 0xff) / 255.0f
        val b = (color shr 16 and 0xff) / 255.0f
        val a = (color shr 24 and 0xff) / 255.0f
        return Vector4f(r, g, b, a)
    }

    /**
     * This will create a mesh out of the given data
     */
    private fun processVoxelModel(
        mesh: Mesh,
        voxels: Array<Voxel>,
        palette: IntArray,
        scale: Vector3f
    ) {

        val indices: MutableList<Int> = ArrayList()
        val vertices: MutableList<Float> = ArrayList()
        val colors: MutableList<Float> = ArrayList()
        val normals: MutableList<Float> = ArrayList()
        val activeFaces = HashSet<Vector3i>()
        voxels.forEach { voxel ->
            val pos = voxel.position
            activeFaces.add(Vector3i(pos.x.toInt(), pos.y.toInt(), pos.z.toInt()))
        }
        voxels.forEach { voxel ->
            val color = intColorToVec4f(palette[voxel.colourIndex])
            val pos = voxel.position
            val center = Vector3f(pos.x * scale.x, pos.y * scale.y, pos.z * scale.z)
            for (face in 0 until 6) {
                if (isFaceValid(face, Vector3i(pos.x.toInt(), pos.y.toInt(), pos.z.toInt()), activeFaces)) {
                    val verts = getVertices(center, face, 0.01f) ?: continue
                    for (i in 0 until 4) {
                        vertices.add(verts[i * 3])
                        normals.add(this.normals[face * 3])
                        colors.add(color.x)
                        vertices.add(verts[i * 3 + 1])
                        normals.add(this.normals[face * 3 + 1])
                        colors.add(color.y)
                        vertices.add(verts[i * 3 + 2])
                        normals.add(this.normals[face * 3 + 2])
                        colors.add(color.z)
                    }
                    val len = vertices.size / 3
                    indices.add(len - 4)
                    indices.add(len - 3)
                    indices.add(len - 2)
                    indices.add(len - 2)
                    indices.add(len - 3)
                    indices.add(len - 1)
                }
            }
        }
        val indicesArray = IntArray(indices.size)
        indices.forEachIndexed { i, value ->
            indicesArray[i] = value
        }
        val vertexArray = FloatArray(vertices.size)
        vertices.forEachIndexed { i, value ->
            vertexArray[i] = value
        }
        val colorArray = FloatArray(colors.size)
        colors.forEachIndexed { i, value ->
            colorArray[i] = value
        }
        val normalArray = FloatArray(normals.size)
        normals.forEachIndexed { i, value ->
            normalArray[i] = value
        }
        mesh.with(indicesArray)
        mesh.with(vertexArray, 0, 3)
        mesh.with(normalArray, 1, 3)
        mesh.with(colorArray, 2, 3)
    }

    /**
     * This will check to see if the given face is valid for the chunk.
     */
    private fun isFaceValid(face: Int, pos: Vector3i, active: Set<Vector3i>): Boolean {
        when (face) {
            0 -> {//North
                if (active.contains(Vector3i(pos).sub(0, 0, 1)))
                    return false
            }
            1 -> {
                if (active.contains(Vector3i(pos).add(0, 0, 1)))
                    return false
            }
            2 -> {
                if (active.contains(Vector3i(pos).add(1, 0, 0)))
                    return false
            }
            3 -> {
                if (active.contains(Vector3i(pos).sub(1, 0, 0)))
                    return false
            }
            4 -> {
                if (active.contains(Vector3i(pos).add(0, 1, 0)))
                    return false
            }
            5 -> {
                if (active.contains(Vector3i(pos).sub(0, 1, 0)))
                    return false
            }
        }
        return true
    }

    /**We want to store our normals***/
    private val normals = floatArrayOf(
        0f, 0f, -1f,
        0f, 0f, 1f,
        1f, 0f, 0f,
        -1f, 0f, 0f,
        0f, 1f, 0f,
        0f, -1f, 0f,
    )

    /**
     * This will convert the rgb color into a float array of cubes colors
     */
    private fun getColors(rgb: Vector4f): FloatArray {
        val array = FloatArray(18)
        for (i in array.indices) {
            when (i % 3) {
                0 -> { //red
                    array[i] = rgb.x
                }
                1 -> { //green
                    array[i] = rgb.y
                }
                2 -> {//blue
                    array[i] = rgb.z
                }
            }
        }
        return array
    }

    /**
     * This will get the computed vertices for a given position face and size
     */
    private fun getVertices(position: Vector3f, face: Int, size: Float = 0.1f): FloatArray? {
        return when (face) {
            0 -> {
                floatArrayOf(
                    position.x + size, position.y + size, position.z + -size,
                    position.x + size, position.y + -size, position.z + -size,
                    position.x + -size, position.y + size, position.z + -size,
                    position.x + -size, position.y + -size, position.z + -size
                )
            }
            1 -> {
                floatArrayOf(
                    position.x + -size, position.y + size, position.z + size,
                    position.x + -size, position.y + -size, position.z + size,
                    position.x + size, position.y + size, position.z + size,
                    position.x + size, position.y + -size, position.z + size
                )
            }
            2 -> {
                floatArrayOf(
                    position.x + size, position.y + size, position.z + size,
                    position.x + size, position.y + -size, position.z + size,
                    position.x + size, position.y + size, position.z + -size,
                    position.x + size, position.y + -size, position.z + -size
                )
            }
            3 -> {
                floatArrayOf(
                    position.x + -size, position.y + size, position.z + -size,
                    position.x + -size, position.y + -size, position.z + -size,
                    position.x + -size, position.y + size, position.z + size,
                    position.x + -size, position.y + -size, position.z + size
                )
            }
            4 -> {
                floatArrayOf(
                    position.x + -size, position.y + size, position.z + -size,
                    position.x + -size, position.y + size, position.z + size,
                    position.x + size, position.y + size, position.z + -size,
                    position.x + size, position.y + size, position.z + size
                )
            }
            5 -> {
                floatArrayOf(
                    position.x + -size, position.y + -size, position.z + size,
                    position.x + -size, position.y + -size, position.z + -size,
                    position.x + size, position.y + -size, position.z + size,
                    position.x + size, position.y + -size, position.z + -size
                )
            }
            else -> null
        }
    }


}

