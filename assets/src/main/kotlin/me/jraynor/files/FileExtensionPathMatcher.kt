package me.jraynor.files

import com.google.common.base.Preconditions
import com.google.common.collect.Sets
import com.google.common.io.Files
import java.nio.file.Path
import java.nio.file.PathMatcher
import java.util.*
import kotlin.collections.ArrayList


/**
 * A PathMatcher that matches files ending with one of a set of file extensions.
 *
 * @author Immortius
 */
class FileExtensionPathMatcher(extensions: Collection<String>) :
    PathMatcher {
    private val extensions: Set<String>

    /**
     * @param extensions Additional extensions that files must have to match
     */
    constructor(vararg extensions: String) : this(extensions.asList()) {}

    override fun matches(path: Path): Boolean {
        val fileName = path.fileName
        return if (fileName != null) {
            extensions.contains(Files.getFileExtension(fileName.toString()))
        } else false
    }

    /**
     * @param extensions The extensions that files must have to match. Must not be empty
     */
    init {
        Preconditions.checkNotNull(extensions)
        Preconditions.checkArgument(!extensions.isEmpty(), "At least one extension must be provided")
        this.extensions = Sets.newHashSet(extensions)
    }
}