package me.jraynor.files

import com.google.common.base.Preconditions
import com.google.common.collect.Lists
import java.io.BufferedInputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.security.AccessController
import java.security.PrivilegedActionException
import java.security.PrivilegedExceptionAction
import java.util.*
import javax.annotation.concurrent.Immutable


/**
 * An asset data file. Provides details on the file's name, extension and allows the file to be opened as a stream.
 *
 *
 * FileFormats are not given direct access to the Path or File, as asset types provided by modules may not have IO permissions.
 *
 *
 *
 * Immutable.
 *
 *
 * @author Immortius
 */
@Immutable
class AssetDataFile(val path: Path) {

    /**
     * @return The path to the file (excluding file name) relative to the module
     */
    val paths: List<String>
        get() {
            val result: MutableList<String> = Lists.newArrayListWithCapacity(path.nameCount - 1)
            for (i in 0 until path.nameCount - 1) {
                result.add(path.getName(i).toString())
            }
            return result
        }

    /**
     * @return The name of the file (including extension)
     */
    val filename: String
        get() {
            val filename = path.fileName
            return filename?.toString() ?: throw IllegalStateException("AssetDataFile has empty path")
        }

    /**
     * @return The file extension.
     */
    val fileExtension: String
        get() {
            val filename = filename
            return if (filename.contains(".")) {
                filename.substring(filename.lastIndexOf(".") + 1)
            } else ""
        }

    /**
     * Opens a stream to read the file. It is up to the stream's user to close it after use.
     *
     * @return A new buffered input stream.
     * @throws IOException If there was an error opening the file
     */
    @Throws(IOException::class)
    fun openStream(): BufferedInputStream {
        return try {
            AccessController.doPrivileged(PrivilegedExceptionAction {
                Preconditions.checkState(Files.isRegularFile(path))
                BufferedInputStream(Files.newInputStream(path))
            } as PrivilegedExceptionAction<BufferedInputStream>)
        } catch (e: PrivilegedActionException) {
            throw IOException("Failed to open stream for '$path'", e)
        }
    }

    override fun toString(): String {
        return path.toAbsolutePath().toString()
    }

    override fun equals(obj: Any?): Boolean {
        if (obj === this) {
            return true
        }
        if (obj is AssetDataFile) {
            return obj.path == path
        }
        return false
    }

    override fun hashCode(): Int {
        return Objects.hash(path)
    }

}