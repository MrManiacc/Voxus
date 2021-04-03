package me.jraynor.files

import me.jraynor.data.AssetData
import me.jraynor.urn.Name


/**
 * A base implementation of that will handle files with specified file extensions.
 * The name of the corresponding asset is assumed to be the non-extension part of the file name.
 *
 * @author Immortius
 */
abstract class AbstractAssetFileFormat<T : AssetData>(vararg fileExtensions: String) :
    AssetFileFormat<T> {
    override val fileMatcher: FileExtensionPathMatcher = FileExtensionPathMatcher(*fileExtensions)

    /**
     * This will match the file with the extension
     */
    override fun getAssetName(filename: String): Name {
        val extensionStart = filename.lastIndexOf('.')
        return if (extensionStart != -1) {
            Name(filename.substring(0, extensionStart))
        } else Name(filename)
    }
}
