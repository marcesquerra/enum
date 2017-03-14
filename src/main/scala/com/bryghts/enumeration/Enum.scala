package com.bryghts.enumeration

import scala.language.experimental.macros

sealed trait EnumMetadata[T] {
    val typeName: String
    def values: Set[T]
    def apply(name: String): Option[T]
}

/**
 * Created by Marc Esquerrà on 31/05/2014.
 */
abstract class Enum[E](implicit em: EnumMetadata[E]) {

    val typeName = em.typeName
    def values:Set[E] = em.values
    def apply(name: String):Option[E] = em.apply(name)

}

object EnumMetadata {

    implicit def enumMetadataForEnum[T]: EnumMetadata[T] =
        macro enumMetadataForEnumImp[T]

    def enumMetadataForEnumImp[T](c: helpers.Context)(implicit tt: c.WeakTypeTag[T]): c.Expr[EnumMetadata[T]] = {
        import c.universe._

        val name: String = tt.tpe.typeSymbol.name.decodedName.toString
        val vs = {

            val companion = tt.tpe.typeSymbol.companionSymbol//helpers.companion(cls)

            def allSubclasses(in: ClassSymbol):List[ClassSymbol] =
                in :: in.knownDirectSubclasses.toList.map{_.asClass}.flatMap{allSubclasses(_)}

            def allInstances(mod: ModuleSymbol): Set[Symbol] =
                mod.moduleClass.asClass.asType.toType.members.sorted.toSet
                    .flatMap{ i : Symbol => i match {
                        case instance if instance.isModule && (instance.asModule.moduleClass.asType.toType weak_<:< tt.tpe) => Set(instance)
                        case m if m.isModule => allInstances(m.asModule)
                        case _ => Set.empty[Symbol]
                    }}


            val instances = (allInstances(companion.asModule).map{s =>
                q"$s"
            }).toList
            q"Set(..$instances)"
        }

        val tmp = q"""
            new AbstractEnumMetadata[$tt] {
                override val typeName: String =
                    $name

                lazy val values: Set[$tt] =
                    $vs
            }
        """
        c.Expr[com.bryghts.enumeration.EnumMetadata[T]](tmp)
    }

}

abstract class AbstractEnumMetadata[T] extends EnumMetadata[T] {
    private val valuesByName: Map[String, T] = values.map(v => (v.toString.toLowerCase, v)).toMap

    override def values:Set[T]

    override def apply(name: String):Option[T] = Option(name.trim.toLowerCase()).flatMap(valuesByName.get _)
}

