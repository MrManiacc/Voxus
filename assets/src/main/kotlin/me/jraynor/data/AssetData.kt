package me.jraynor.data
/**
 * AssetData is the implementation agnostic data for an asset - typically it isn't dependant on either the source format
 * or the implementation consuming the resource. For instance, for a texture the asset data would not depend on the
 * format of the image the texture is sourced from, nor whether textures are handled by LWJGL or some other renderer.
 * <p>
 * This separation allows support for multiple implementations on either end, as well as the direct procedural creation
 * of assets.
 * </p>
 */
interface AssetData {
}