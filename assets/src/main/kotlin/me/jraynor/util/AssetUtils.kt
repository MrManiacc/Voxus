package me.jraynor.util

import com.googlecode.gentyref.GenericTypeReflector
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.*


object AssetUtils {
    /**
     * Used to obtain the bound value for a generic parameter of a type. Example, for a field of type List&lt;String&gt;, the 0th generic parameter is String.class.
     * A List with no parameter will return Optional.absent()
     *
     * @param target The type to obtain the generic parameter of.
     * @param index  The index of the the parameter to obtain
     * @return An optional that contains the parameter type if bound.
     */
    fun getTypeParameterBinding(target: Type, index: Int): Optional<Type> {
        return getTypeParameterBindingForInheritedClass(target,
            Objects.requireNonNull(getClassOfType(target)) as Class<*>, index)
    }

    /**
     * Used to obtained the bound value for a generic parameter of a particular class or interface that the type inherits.
     *
     * @param target     The type to obtain the generic parameter of.
     * @param superClass The superclass which the parameter belongs to
     * @param index      The index of the parameter to obtain
     * @param <T>        The type of the superclass that the parameter belongs to
     * @return An optional that contains the parameter if bound.
    </T> */
    fun <T> getTypeParameterBindingForInheritedClass(target: Type, superClass: Class<T>, index: Int): Optional<Type> {
        require(superClass.typeParameters.isNotEmpty()) { "Class '$superClass' is not parameterized" }
        require(superClass.isAssignableFrom(Objects.requireNonNull(getClassOfType(target)))) { "Class '$target' does not implement '$superClass'" }
        val type: Type = GenericTypeReflector.getExactSuperType(target, superClass)
        if (type is ParameterizedType) {
            val paramType = type.actualTypeArguments[index]
            if (paramType is Class<*> || paramType is ParameterizedType) {
                return Optional.of(paramType)
            }
        }
        return Optional.empty()
    }

    /**
     * Returns the raw class of a type, or null if the type doesn't represent a class.
     *
     * @param type The type to get the class of
     * @return the raw class of a type, or null if the type doesn't represent a class.
     */
    fun getClassOfType(type: Type?): Class<*>? {
        if (type is Class<*>) {
            return type
        } else if (type is ParameterizedType) {
            return type.rawType as Class<*>
        }
        return null
    }
}