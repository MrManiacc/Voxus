package me.jraynor.client.opengl.shader.parse

import me.jraynor.client.opengl.shader.ShaderData
import com.jgfx.assets.urn.ResourceUrn
import me.jraynor.urn.Name
import java.util.function.Consumer

/**
 * This class will parse the shader and generate the proper source code
 */
object ShaderParser {
    private val cachedData: Map<ResourceUrn, ShaderData> = HashMap()
    private val globalCustoms: MutableMap<ResourceUrn, Custom> = HashMap()
    private val globalBinds: MutableMap<ResourceUrn, Bind> = HashMap()
    private val globalUniforms: MutableMap<ResourceUrn, Uniform> = HashMap()
    private val processedShaders: MutableSet<ResourceUrn> = HashSet()

    /**
     * This will parse the shader data
     *
     * @param resource the resource urn to parse
     * @return returns an optional shader data
     */
    fun parseShader(shaderData: ShaderData, resource: ResourceUrn): ShaderData? {
        for (i in 0 until shaderData.customLines.length) processLine(
            shaderData.customLines,
            i,
            shaderData,
            vertex = false,
            frag = false
        )
        for (i in 0 until shaderData.vertexSource.length) processLine(
            shaderData.vertexSource,
            i,
            shaderData,
            true,
            false
        )
        for (i in 0 until shaderData.fragmentSource.length) processLine(
            shaderData.fragmentSource,
            i,
            shaderData,
            false,
            true
        )
        if (!isReady(shaderData)) return null
        processGlobals(shaderData, resource)
        processImports(shaderData, resource)
        processedShaders.add(resource)
        return shaderData
    }

    /**
     * Builds the global shader data
     *
     * @param data the data to process
     */
    private fun processGlobals(data: ShaderData, urn: ResourceUrn) {
        data.binds.forEach { s, bind ->
            val resourceUrn = ResourceUrn(
                urn.getModuleName(),
                Name(urn.getResourceName().toString() + ":" + urn.getFragmentName()),
                Name(s)
            )
            globalBinds.put(resourceUrn, bind)
        }
        data.uniforms.forEach { s, imp ->
            val resourceUrn = ResourceUrn(
                urn.getModuleName(),
                Name(urn.getResourceName().toString() + ":" + urn.getFragmentName()),
                Name(s)
            )
            globalUniforms.put(resourceUrn, imp)
        }
        data.customs.forEach { s, custom ->
            val resourceUrn = ResourceUrn(
                urn.getModuleName(),
                Name(urn.getResourceName().toString() + ":" + urn.getFragmentName()),
                Name(s)
            )
            globalCustoms.put(resourceUrn, custom)
        }
    }

    /**
     * Processes the imports for the shader
     *
     * @param data the data to process
     */
    private fun processImports(data: ShaderData, urn: ResourceUrn) {
        data.importsMap.forEach { name, imports ->
            imports.forEach { _import ->
                when (_import.getDefinition()) {
                    "BINDS" -> {
                        val binds = getGlobalBinds(ResourceUrn(name))
                        binds.forEach(Consumer { bind: Bind? ->
                            val b = Bind(_import.getLine(), bind!!)
                            b.inVertex = _import.isInVertex()
                            b.inFrag = _import.isInFrag()
                            data.addBind(b)
                        })
                    }
                    "UNIFORMS" -> {
                        val uniforms = getGlobalUniforms(ResourceUrn(name))
                        uniforms.forEach(Consumer { uniform: Uniform? ->
                            val u = Uniform(_import.getLine(), uniform!!)
                            data.addUniform(u)
                        })
                    }
                    else -> {
                        val resourceUrn: ResourceUrn = extractUrn(name, _import.getDefinition())
                        val customs = getGlobalCustoms(resourceUrn)
                        customs.forEach(Consumer { custom: Custom? ->
                            val c = Custom(_import.getLine(), custom!!)
                            c.inVertex = _import.isInVertex()
                            c.inFrag = _import.isInFrag()
                            data.addCustom(c)
                        })
                    }
                }
            }
        }
    }

    /**
     * Computes the proper urn from the given input
     *
     * @param input the input urn
     * @return returns the resource urn
     */
    private fun extractUrn(input: String, fragment: String): ResourceUrn {
        return if (input.contains(":")) {
            val ids = input.split(":").toTypedArray()
            val moduleBuilder = StringBuilder()
            var name = ""
            for (i in ids.indices) {
                if (i < ids.size - 1) moduleBuilder.append(ids[i]).append(":") else name = ids[i]
            }
            var module = moduleBuilder.toString()
            module = module.substring(0, module.lastIndexOf(":"))
            ResourceUrn(module, name, fragment)
        } else ResourceUrn(input, fragment)
    }

    /**
     * Get's the binds by the given urn type, will try to get the instance first, then by type
     *
     * @param urn the type to check
     * @return returns a list of binds by the given urn
     */
    private fun getGlobalBinds(urn: ResourceUrn): List<Bind> {
        val output: MutableList<Bind> = Lists.newArrayList()
        globalBinds.forEach(BiConsumer<ResourceUrn, Bind> { resourceUrn: ResourceUrn, bind: Bind ->
            if (resourceUrn.equals(urn) || resourceUrn.isOfType(urn)) {
                output.add(bind)
            }
        })
        Collections.sort(output)
        return output
    }

    /**
     * Get's the binds by the given urn type, will try to get the instance first, then by type
     *
     * @param urn the type to check
     * @return returns a list of binds by the given urn
     */
    private fun getGlobalUniforms(urn: ResourceUrn): List<Uniform> {
        val output: MutableList<Uniform> = Lists.newArrayList()
        globalUniforms.forEach(BiConsumer<ResourceUrn, Uniform> { resourceUrn: ResourceUrn, bind: Uniform ->
            if (resourceUrn.equals(
                    urn
                )
            ) output.add(bind) else if (resourceUrn.isOfType(urn)) output.add(bind)
        })
        Collections.sort(output)
        return output
    }

    /**
     * Gets a global custom via an import
     *
     * @param urn the urn to get the custom by
     * @return returns a list of sorted customs
     */
    private fun getGlobalCustoms(urn: ResourceUrn): List<Custom> {
        val output: MutableList<Custom> = Lists.newArrayList()
        globalCustoms.forEach(BiConsumer<ResourceUrn, Custom> { resourceUrn: ResourceUrn?, custom: Custom ->
            if (urn.equals(resourceUrn)) {
                output.add(custom)
            }
        })
        Collections.sort(output)
        return output
    }

    /**
     * this method checks to see if the global imports are present for the given shader
     *
     * @param data the data to process
     * @return returns true if ready
     */
    private fun isReady(data: ShaderData): Boolean {
        val present = AtomicBoolean(true)
        data.getImportsMap().forEach { urn, imports ->
            imports.forEach { anImport ->
                val _import = ResourceUrn(anImport.name)
                when (anImport.getDefinition()) {
                    "BINDS" -> {
                        val containsBind = AtomicBoolean(false)
                        globalBinds.forEach(BiConsumer<ResourceUrn, Bind> { resourceUrn: ResourceUrn, bind: Bind? ->
                            if (resourceUrn.isOfType(
                                    _import
                                )
                            ) containsBind.set(true)
                        })
                        if (!containsBind.get()) present.set(false)
                    }
                    "UNIFORMS" -> {
                        val containsUniform = AtomicBoolean(false)
                        globalUniforms.forEach(BiConsumer<ResourceUrn, Uniform> { resourceUrn: ResourceUrn, bind: Uniform? ->
                            if (resourceUrn.isOfType(
                                    _import
                                )
                            ) containsUniform.set(true)
                        })
                        if (!containsUniform.get()) present.set(false)
                    }
                    else -> {
                        val custom = ResourceUrn(_import, anImport.getDefinition())
                        if (!globalCustoms.containsKey(custom)) present.set(false)
                    }
                }
            }
        }
        return present.get()
    }

    /**
     * Process a given line
     */
    private fun processLine(input: Array<String>, index: Int, data: ShaderData, vertex: Boolean, frag: Boolean) {
        val line = input[index]
        if (line.trim { it <= ' ' }.startsWith("#define")) {
            val rawLine = line.replaceFirst("#define".toRegex(), "").trim { it <= ' ' }
            val type = rawLine.split("->").toTypedArray()[0].replace("->", "").trim { it <= ' ' }
            when (type) {
                "UNIFORMS" -> parseUniforms(input, index, data, vertex, frag)
                "BINDS" -> parseBinds(input, index, data, vertex, frag)
                else -> if (type == type.toLowerCase() && type.contains(":") && !type.contains("\"")) {
                    val imported = Import(index, input)
                    imported.inVertex = vertex
                    imported.inFrag = frag
                    data.addImport(imported)
                } else if (line.contains("\"")) {
                    val custom: Unit = data.addCustom(Custom(index, input))
                    custom.setInFrag(frag)
                    custom.setInVertex(vertex)
                }
            }
        }
    }

    /**
     * Parses a collection of uniforms from the uniform line
     */
    private fun parseUniforms(lines: Array<String>, index: Int, shaderData: ShaderData, vert: Boolean, frag: Boolean) {
        val line = lines[index]
        if (line.contains(",")) {
            val elements = line.split(",").toTypedArray()
            for (element in elements) {
                val uniform = Uniform(index, lines, element)
                shaderData.addUniform(uniform)
            }
        } else {
            val uniform = Uniform(index, lines, line)
            shaderData.addUniform(uniform)
        }
    }

    /**
     * Parses a collection of binds from the binds line
     */
    private fun parseBinds(lines: Array<String>, index: Int, data: ShaderData, vert: Boolean, frag: Boolean) {
        val line = lines[index]
        if (line.contains(",")) {
            var start = 0
            val elements: MutableList<String> = ArrayList()
            var inParenthesis = false
            for (i in 0 until line.length) {
                if (line[i] == '(') inParenthesis = true
                if (line[i] == ')') inParenthesis = false
                if (!inParenthesis && line[i] == ',') {
                    elements.add(line.substring(start, i).trim { it <= ' ' })
                    start = i
                }
            }
            if (start != 0) elements.add(line.substring(start + 1).trim { it <= ' ' })
            for (element in elements) {
                val bind = Bind(index, lines, element)
                bind.inFrag = frag
                bind.inVertex = vert
                data.addBind(bind)
            }
        } else {
            val bind = Bind(index, lines, line)
            bind.inFrag = frag
            bind.inVertex = vert
            data.addBind(bind)
        }
    }
}