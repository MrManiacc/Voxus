package com.jgfx.assets.urn

import com.google.common.base.Strings
import me.jraynor.urn.Name
import java.io.IOException
import java.util.*
import java.util.regex.Pattern

/**
 * A ResourceUrn is a urn of the structure "{moduleName}:{resourceName}[#{fragmentName}][!instance]".
 *
 *  * moduleName is the name of the module containing or owning the resource
 *  * resourceName is the name of the resource
 *  * fragmentName is an optional identifier for a sub-part of the resource
 *  * an instance urn indicates a resource that is an independent copy of a resource identified by the rest of the urn
 *
 * ResourceUrn is immutable and comparable.
 *
 * @author Immortius
 */
class ResourceUrn : Comparable<ResourceUrn> {
    val moduleName: Name
    val resourceName: Name
    val fragmentName: Name
    /**
     * @return Whether this urn identifies an independent copy of the resource
     */
    val isInstance: Boolean

    /**
     * Creates a urn with the module and resource name from the provided urn, but the fragment name provided. This urn will not be an instance urn.
     *
     * @param urn          The urn to create this urn from
     * @param fragmentName The fragment name this urn should have
     */
    constructor(urn: ResourceUrn, fragmentName: String) : this(urn, Name(fragmentName), false) {}

    /**
     * Creates a urn with the module and resource name from the provided urn, but the fragment name provided. This urn will not be an instance urn.
     *
     * @param urn          The urn to create this urn from
     * @param fragmentName The fragment name this urn should have
     */
    constructor(urn: ResourceUrn, fragmentName: Name) : this(urn, fragmentName, false) {}

    /**
     * Creates a urn with the module and resource name from the provided urn, but the fragment name provided.
     *
     * @param urn          The urn to create this urn from
     * @param fragmentName The fragment name this urn should have
     * @param instance     Whether this urn should be a fragment
     */
    constructor(urn: ResourceUrn, fragmentName: String, instance: Boolean) : this(
        urn,
        Name(fragmentName),
        instance
    ) {
    }

    /**
     * Creates a urn with the module and resource name from the provided urn, but the fragment name provided.
     *
     * @param urn          The urn to create this urn from
     * @param fragmentName The fragment name this urn should have
     * @param instance     Whether this urn should be a fragment
     */
    constructor(urn: ResourceUrn, fragmentName: Name, instance: Boolean) {
        moduleName = urn.moduleName
        resourceName = urn.resourceName
        this.fragmentName = fragmentName
        isInstance = instance
    }

    /**
     * Creates a ModuleUri with the given module:resource combo
     *
     * @param moduleName   The name of the module the resource belongs to
     * @param resourceName The name of the resource itself
     */
    constructor(moduleName: String, resourceName: String) : this(Name(moduleName), Name(resourceName), false) {}

    /**
     * Creates a ModuleUri for an instance with a given module:resource(!instance) combo
     *
     * @param moduleName   The name of the module the resource belongs to
     * @param resourceName The name of the resource itself
     * @param instance     Whether this urn identifies an instance
     */
    constructor(moduleName: String, resourceName: String, instance: Boolean) : this(
        Name(moduleName),
        Name(resourceName),
        Name.EMPTY,
        instance
    ) {
    }

    /**
     * Creates a ModuleUri with the given module:resource combo
     *
     * @param moduleName   The name of the module the resource belongs to
     * @param resourceName The name of the resource itself
     */
    constructor(moduleName: Name, resourceName: Name) : this(moduleName, resourceName, Name.EMPTY, false) {}

    /**
     * Creates a ModuleUri with the given module:resource(!instance) combo
     *
     * @param moduleName   The name of the module the resource belongs to
     * @param resourceName The name of the resource itself
     * @param instance     Whether this urn identifies an instance
     */
    constructor(moduleName: Name, resourceName: Name, instance: Boolean) : this(
        moduleName,
        resourceName,
        Name.EMPTY,
        instance
    ) {
    }

    /**
     * Creates a ModuleUri with the given module:resource#fragment combo
     *
     * @param moduleName   The name of the module the resource belongs to
     * @param resourceName The name of the resource itself
     * @param fragmentName The name of the fragment of the resource
     */
    constructor(moduleName: String, resourceName: String, fragmentName: String) : this(
        Name(moduleName),
        Name(resourceName),
        Name(fragmentName),
        false
    ) {
    }

    /**
     * Creates a ModuleUri with the given module:resource#fragment(!instance) combo
     *
     * @param moduleName   The name of the module the resource belongs to
     * @param resourceName The name of the resource itself
     * @param fragmentName The name of the fragment of the resource
     * @param instance     Whether this urn identifies an instance
     */
    constructor(moduleName: String, resourceName: String, fragmentName: String, instance: Boolean) : this(
        Name(
            moduleName
        ), Name(resourceName), Name(fragmentName), instance
    ) {
    }

    /**
     * Creates a ModuleUri with the given module:resource#fragment combo
     *
     * @param moduleName   The name of the module the resource belongs to
     * @param resourceName The name of the resource itself
     * @param fragmentName The name of the fragment of the resource
     */
    constructor(moduleName: Name, resourceName: Name, fragmentName: Name) : this(
        moduleName,
        resourceName,
        fragmentName,
        false
    ) {
    }

    /**
     * Creates a ModuleUri with the given module:resource#fragment(!instance) combo
     *
     * @param moduleName   The name of the module the resource belongs to
     * @param resourceName The name of the resource itself
     * @param fragmentName The name of the fragment of the resource
     * @param instance     Whether this urn identifies an instance
     */
    constructor(moduleName: Name, resourceName: Name, fragmentName: Name, instance: Boolean) {
        this.moduleName = moduleName
        this.resourceName = resourceName
        this.fragmentName = fragmentName
        isInstance = instance
    }

    /**
     * Creates a ModuleUrn from a string in the format "module:object(#fragment)(!instance)".
     *
     * @param urn The urn to parse
     */
    constructor(urn: String) {
        val match = URN_PATTERN.matcher(urn)
        if (match.matches()) {
            moduleName = Name(match.group(1))
            resourceName = Name(match.group(2))
            if (!Strings.isNullOrEmpty(match.group(3))) {
                fragmentName = Name(match.group(3))
            } else {
                fragmentName = Name.EMPTY
            }
            isInstance = !Strings.isNullOrEmpty(match.group(4))
        } else {
            throw IOException("Invalid Urn: '$urn'")
        }
    }



    /**
     * @return The root of the ResourceUrn, without the fragment name or instance marker.
     */
    val rootUrn: ResourceUrn
        get() = if (fragmentName.isEmpty && !isInstance) {
            this
        } else ResourceUrn(moduleName, resourceName)

    /**
     * Checks to see if the input matches the current instance's resource urn
     *
     * @param urn the urn to match against
     * @return returns true if they match
     */
    fun isOfType(urn: ResourceUrn): Boolean {
        val root = rootUrn
        return root == urn
    }

    /**
     * @return If this urn is an instance, returns the urn without the instance marker. Otherwise this urn.
     */
    val parentUrn: ResourceUrn
        get() {
            return if (isInstance) {
                ResourceUrn(moduleName, resourceName, fragmentName)
            } else {
                this
            }
        }

    /**
     * @return This instance urn version of this urn. If this urn is already an instance, this urn is returned.
     */
    val instanceUrn: ResourceUrn
        get() {
            return if (!isInstance) {
                ResourceUrn(moduleName, resourceName, fragmentName, true)
            } else {
                this
            }
        }

    override fun toString(): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append(moduleName)
        stringBuilder.append(RESOURCE_SEPARATOR)
        stringBuilder.append(resourceName)
        if (!fragmentName.isEmpty) {
            stringBuilder.append(FRAGMENT_SEPARATOR)
            stringBuilder.append(fragmentName)
        }
        if (isInstance) {
            stringBuilder.append(INSTANCE_INDICATOR)
        }
        return stringBuilder.toString()
    }


    override operator fun compareTo(o: ResourceUrn): Int {
        var result: Int = moduleName.compareTo(o.moduleName)
        if (result == 0) {
            result = resourceName.compareTo(o.resourceName)
        }
        if (result == 0) {
            result = fragmentName.compareTo(o.fragmentName)
        }
        if (result == 0) {
            if (isInstance && !o.isInstance) {
                result = 1
            } else if (!isInstance && o.isInstance) {
                result = -1
            }
        }
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResourceUrn

        if (moduleName != other.moduleName) return false
        if (resourceName != other.resourceName) return false
        if (fragmentName != other.fragmentName) return false
        if (isInstance != other.isInstance) return false

        return true
    }

    override fun hashCode(): Int {
        var result = moduleName.hashCode()
        result = 31 * result + resourceName.hashCode()
        result = 31 * result + fragmentName.hashCode()
        result = 31 * result + isInstance.hashCode()
        return result
    }

    companion object {
        const val RESOURCE_SEPARATOR = ":"
        const val FRAGMENT_SEPARATOR = "#"
        const val INSTANCE_INDICATOR = "!instance"
        private val URN_PATTERN = Pattern.compile("([^:]+):([^#!]+)(?:#([^!]+))?(!instance)?")

        /**
         * @param urn The string to check for validity
         * @return Whether urn is a valid ResourceUrn
         */
        fun isValid(urn: String?): Boolean {
            return URN_PATTERN.matcher(urn).matches()
        }
    }
}