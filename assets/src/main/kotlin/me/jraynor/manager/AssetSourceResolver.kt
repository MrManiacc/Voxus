package me.jraynor.manager


import com.google.common.collect.Sets
import com.jgfx.assets.urn.ResourceUrn
import me.jraynor.asset.Asset
import me.jraynor.asset.AssetType
import me.jraynor.data.AssetData
import me.jraynor.files.AbstractAssetFileFormat
import me.jraynor.files.AssetDataFile
import me.jraynor.files.AssetFileFormat
import java.io.File
import java.io.IOException
import java.nio.file.Paths
import java.util.*


/**
 * This class will determine all of the files for a given asset type, and load the asset data by creating the asset file format
 */
class AssetSourceResolver(private val typeManager: AssetTypeManager) {
    private val resolvedAssets = Sets.newConcurrentHashSet<ResourceUrn>()
    private val unresolvedAssets: MutableSet<AssetMetaData> = Sets.newConcurrentHashSet()

    /**
     * Resolves the assets for the given type
     *
     * @return returns false if there's unresolved assets
     */
    fun resolveAssets(): Boolean {
        var allLoaded = loadUnresolvedAssets()

        for (assetClass in typeManager.registeredTypes) {
            if (Asset::class.java.isAssignableFrom(assetClass)) {
                val metaList = getMetaData(assetClass as Class<out Asset<out AssetData>>)
                for (meta in metaList) {
                    if (!parseAsset(meta)) allLoaded = false
                }
            }
        }
        return allLoaded
    }

    /**
     * Attempts to load all of the unresolved asset types
     *
     * @return returns false if any failed to load
     */
    private fun loadUnresolvedAssets(): Boolean {
        var allLoaded = true
        for (meta in unresolvedAssets) {
            if (!parseAsset(meta)) {
                allLoaded = false
                println("failed to parse: " + meta.urn)
            }
        }
        return allLoaded
    }

    /**
     * Extracts the asset meta data from the files before loading     *
     * @return returns the asset data
     */
    private fun getMetaData(assetClass: Class<out Asset<out AssetData>>): List<AssetMetaData> {
        val metaList = ArrayList<AssetMetaData>()
        val assetFolder = typeManager.getAssetFolders(assetClass)
        val assetType: Optional<AssetType<out Asset<out AssetData>, out AssetData>> =
            typeManager.getAssetType(assetClass)
        val assetFormat: Optional<AbstractAssetFileFormat<out AssetData>> = typeManager.getFormat(assetClass)
        if (assetType.isPresent && assetFormat.isPresent && assetFolder.isPresent) {
            val type: AssetType<out Asset<out AssetData>, out AssetData> = assetType.get()
            val format: AbstractAssetFileFormat<out AssetData> = assetFormat.get()
            val folders = assetFolder.get()
            val loader = Thread.currentThread().contextClassLoader
            val url = loader.getResource("assets")
            for (folder in folders) {
                val assetsFolder = File(Objects.requireNonNull(url).path)
                if (!assetsFolder.exists()) return metaList
                val files = File(assetsFolder, folder).listFiles()
                if (files != null && files.isNotEmpty()) for (file in Objects.requireNonNull(files)) {
                    val filePath = Paths.get(file.toURI())
                    if (format.fileMatcher.matches(filePath)) {
                        val urn = ResourceUrn("engine", folder, format.getAssetName(file.name).lowerCase)
                        val assetDataFiles: List<AssetDataFile> = listOf(AssetDataFile(filePath))
                        metaList.add(AssetMetaData(assetDataFiles, format, type, urn))
                    }
                }
            }
        }
        return metaList
    }

    /**
     * Parses the asset, or put's it into the unready map, which will attempt to load the asset every time
     * a different asset is loaded. This allows for us to make shaders dependent upon other shader
     *
     * @param meta the meta to use to load the asset
     * @return returns false if the asset was unresolved
     */
    private fun parseAsset(meta: AssetMetaData): Boolean {
        if (resolvedAssets.contains(meta.urn)) {
            unresolvedAssets.remove(meta)
            return true
        }
        try {
            val data = meta.format.load(meta.urn, meta.files)
            if (data == null) {
                unresolvedAssets.add(meta)
            } else {
                if (meta.type.loadAsset(meta.urn, data) != null) {
                    unresolvedAssets.remove(meta)
                    resolvedAssets.add(meta.urn)
                    return true
                }
            }
        } catch (e: IOException) {
            unresolvedAssets.add(meta)
        }
        return false
    }

    private class AssetMetaData(
        val files: List<AssetDataFile>,
        val format: AssetFileFormat<*>,
        val type: AssetType<*, *>,
        val urn: ResourceUrn
    ) {


        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as AssetMetaData

            if (files != other.files) return false
            if (format != other.format) return false
            if (type != other.type) return false
            if (urn != other.urn) return false

            return true
        }

        override fun hashCode(): Int {
            var result = files.hashCode()
            result = 31 * result + format.hashCode()
            result = 31 * result + type.hashCode()
            result = 31 * result + urn.hashCode()
            return result
        }
    }
}