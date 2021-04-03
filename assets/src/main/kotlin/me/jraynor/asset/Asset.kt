package me.jraynor.asset

import com.jgfx.assets.urn.ResourceUrn
import me.jraynor.data.AssetData
import net.mostlyoriginal.api.utils.Preconditions


/**
 * Abstract base class common to all assets.
 *
 *
 * An asset is a resource that is used by the game - a texture, sound, block definition and the like. These are typically
 * loaded from a module, although they can also be created at runtime. Each asset is identified by a ResourceUrn that uniquely
 * identifies it and can be used to obtain it. This urn provides a lightweight way to serialize a reference to an Asset.
 *
 *
 *
 * Assets are created from a specific type of asset data. There may be a multiple implementations with a common base for a particular type of asset data
 * - this allows for implementation specific assets (e.g. OpenGL vs DirectX textures for example might have an OpenGLTexture and DirectXTexture implementing class
 * respectively, with a common Texture base class).
 *
 *
 *
 * Assets may be reloaded by providing a new batch of data, or disposed to free resources - disposed assets may no
 * longer be used.
 *
 *
 *
 * To support making Asset implementations thread safe reloading, creating an instance and disposal are all synchronized.
 * Implementations should consider thread safety around any methods they add if it is intended for assets to be used across multiple threads.
 *
 *
 * @author Immortius
 */
abstract class Asset<T : AssetData>(val urn: ResourceUrn, val assetType: AssetType<*, T>) {

    /**
     * Called to reload an asset with the given data.
     *
     * @param data The data to load.
     */
    abstract fun reload(data: T)


    /**
     * The constructor for an asset. It is suggested that implementing classes provide a constructor taking both the urn, and an initial AssetData to load.
     *
     * @param urn       The urn identifying the asset.
     * @param assetType The asset type this asset belongs to.
     */
    init {
        Preconditions.checkNotNull(urn)
        Preconditions.checkNotNull(assetType)
        assetType.registerAsset(this)
    }
}