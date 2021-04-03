package me.jraynor.files


import me.jraynor.urn.Name
import java.nio.file.PathMatcher


/**
 * Common base interface for all file formats.  A file format is used to load one or more files and either create or modify an
 *
 * @author Immortius
 */
interface FileFormat {
    /**
     * @return A path matcher that will filter for files relevant for this format.
     */
    val fileMatcher: PathMatcher?

    /**
     * This method is use to obtain the name of the resource represented by the given filename. The ModuleAssetDataProducer will combine it with a module id to
     * determine the complete ResourceUrn.
     *
     * @param filename The filename of an asset, including extension
     * @return The asset name corresponding to the given filename
     */
    fun getAssetName(filename: String): Name
}