package com.bryghts.enumeration

trait EnumMetadata[T] {
    val typeName: String
    val values: Set[T]
    def apply(name: String): Option[T]
}

/**
 * Created by Marc Esquerrà on 31/05/2014.
 */
abstract class Enum[E](implicit em: EnumMetadata[E]) {

    val typeName = em.typeName
    val values:Set[E] = em.values
    def apply(name: String):Option[E] = em.apply(name)

}

