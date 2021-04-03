package me.jraynor.files

import com.jgfx.assets.urn.ResourceUrn
import me.jraynor.data.AssetData
import java.io.IOException


/**
 * An AssetFileFormat handles loading a file representation of an asset into the appropriate
 *
 * @author Immortius
 */
interface AssetFileFormat<T : AssetData> : FileFormat {
    /**
     * @param urn    The urn identifying the asset being loaded.
     * @param inputs The inputs corresponding to this asset
     * @return The loaded asset
     * @throws IOException If there are any errors loading the asset
     */
    @Throws(IOException::class)
    fun load(urn: ResourceUrn, inputs: List<AssetDataFile>): T?
}