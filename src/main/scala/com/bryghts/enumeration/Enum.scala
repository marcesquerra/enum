package com.bryghts.enumeration

trait EnumMetadata[T] {
    val typeName: String
    def values: Set[T]
    def apply(name: String): Option[T]
}

/**
 * Created by Marc Esquerrà on 31/05/2014.
 */
abstract class Enum[E](em: EnumMetadata[E]) {

    val typeName = em.typeName
    def values:Set[E] = em.values
    def apply(name: String):Option[E] = em.apply(name)

}



