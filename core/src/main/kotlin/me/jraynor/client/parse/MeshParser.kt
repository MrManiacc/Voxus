package me.jraynor.client.parse

import assimp.AiMesh
import assimp.AiScene
import me.jraynor.client.opengl.model.mesh.Mesh
import me.jraynor.client.opengl.model.mesh.MeshData
import me.jraynor.common.util.toImmutable
import org.joml.Vector2f
import org.joml.Vector3f

/**
 * This will parse all of the mesh data from the scene and store it inside the
 */
object MeshParser {
    private lateinit var data: MeshData

    /**
     * This will parse the mesh data
     */
    fun parse(scene: AiScene, meshData: MeshData) {
        data = meshData
        scene.meshes.forEach {
            meshData.add(parseMesh(it))
        }
    }

    /**
     * This will read all of the meshes
     */
    private fun parseMesh(meshIn: AiMesh): Mesh {
        val mesh = Mesh()
        val vertices = ArrayList<Float>()
        val normals = ArrayList<Float>()
        val uvs = ArrayList<Float>()

        for (i in 0 until meshIn.numVertices) {
            if (meshIn.hasPositions()) {
                val vertex = Vector3f(meshIn.vertices[i].x, meshIn.vertices[i].y, meshIn.vertices[i].z)
                vertices.add(vertex.x)
                vertices.add(vertex.y)
                vertices.add(vertex.z)
            }
            if (meshIn.hasNormals()) {
                val normal = Vector3f(meshIn.normals[i].x, meshIn.normals[i].y, meshIn.normals[i].z)
                normals.add(normal.x)
                normals.add(normal.y)
                normals.add(normal.z)
            }
            if (meshIn.hasTextureCoords(0)) {
                val uv = Vector2f(meshIn.textureCoords[0][i][0], meshIn.textureCoords[0][i][1])
                uvs.add(uv.x)
                uvs.add(uv.y)
            }
        }
        mesh.with(vertices.toFloatArray(), 0, 3)
        mesh.with(normals.toFloatArray(), 1, 3)
        mesh.with(uvs.toFloatArray(), 2, 2)

        if (meshIn.hasFaces()) {
            val indices = ArrayList<Int>()
            for (i in 0 until meshIn.numFaces) {
                val faces = meshIn.faces[i]
                for (j in 0 until faces.size) {
                    indices.add(faces[j])
                }
            }
            mesh.with(indices.toIntArray())
        }

        return mesh
    }


}

