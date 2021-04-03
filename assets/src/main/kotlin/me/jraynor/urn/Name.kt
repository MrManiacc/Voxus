package me.jraynor.urn

import net.mostlyoriginal.api.utils.Preconditions
import java.util.*

/**
 * A name is a normalised string used as an identifier. Primarily this means it is case insensitive.
 *
 *
 * The original case-sensitive name is retained and available for display purposes, since it may use camel casing for readability.
 *
 *
 * This class is immutable.
 *
 *
 * @author Immortius
 */
class Name(name: String) : Comparable<Name?> {
    private val originalName: String
    private val normalisedName: String

    /**
     * @return Whether this name is empty (equivalent to an empty string)
     */
    val isEmpty: Boolean
        get() = normalisedName.isEmpty()

    /**
     * @return The Name in lowercase consistent with Name equality (so two names that are equal will have the same lowercase)
     */
    val lowerCase: String get() = normalisedName


    /**
     * @return The Name in uppercase consistent with Name equality (so two names that are equal will have the same uppercase)
     */
    val toUpperCase: String
        get() = originalName.toUpperCase(Locale.ENGLISH)


    override fun equals(obj: Any?): Boolean {
        if (obj === this) {
            return true
        }
        if (obj is Name) {
            return normalisedName == obj.normalisedName
        }
        return false
    }

    override fun hashCode(): Int {
        return normalisedName.hashCode()
    }

    override fun toString(): String {
        return originalName
    }

    companion object {
        /**
         * The Name equivalent of an empty String
         */
        val EMPTY = Name("")
    }

    init {
        Preconditions.checkNotNull(name)
        originalName = name
        normalisedName = name.toLowerCase(Locale.ENGLISH)
    }

    override fun compareTo(other: Name?): Int {
        return other?.normalisedName?.let { normalisedName.compareTo(it) } ?: 0
    }
}