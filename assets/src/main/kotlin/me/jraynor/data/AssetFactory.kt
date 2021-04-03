package me.jraynor.data

import com.jgfx.assets.urn.ResourceUrn
import me.jraynor.asset.Asset
import me.jraynor.asset.AssetType


/**
 * AssetFactorys are used to load AssetData into new assets.
 *
 * For many assets, the assets just have one asset implementation so the factory would simply call the constructor for the implementation and pass the urn and data
 * straight through. However other assets may have multiple implementations (e.g. Texture may have an OpenGL and a DirectX implementation) so the factory installed
 * will determine that. Additionally the factory may pass through other information (OpenGL texture handle, or a reference to a central OpenGL context).
 *
 * @author Immortius
 */
fun interface AssetFactory<T : Asset<U>, U : AssetData> {
    /**
     * @param urn       The urn of the asset to construct
     * @param assetType The assetType the asset belongs to
     * @param data      The data for the asset
     * @return The built asset
     */
    fun build(urn: ResourceUrn, assetType: AssetType<T, U>, data: U): T
}