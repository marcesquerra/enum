package com.bryghts

import scala.reflect.runtime.{universe => ru}

/**
 * Created by Marc Esquerrà on 05/09/15.
 */
package object enumeration {

    class ReflectionBaseEnumMetadataImplementation[T](tt : ru.TypeTag[T]) extends EnumMetadata[T] {
        override val typeName = helpers.decodedTypeName(tt)

        private val valuesByName: Map[String, T] = {
            import scala.reflect.runtime.universe._
            val m = runtimeMirror(this.getClass.getClassLoader)

            def classFor(t: ru.Symbol): Class[_] =
                m.runtimeClass(t.asClass)

            def companionInstance(t: ru.ModuleSymbol): Any = {
                val r = m.reflectModule(t)
                r.instance
            }

            val c         = tt.tpe.typeSymbol.asClass
            val o         = this.getClass
            val cz        = classFor(c)
            val companion = helpers.companion(c)

            def allSubclasses(in: ru.ClassSymbol):List[ru.ClassSymbol] =
                in :: in.knownDirectSubclasses.toList.map{_.asClass}.flatMap{allSubclasses(_)}

            allSubclasses(companion.moduleClass.asClass)
                .filterNot(_.isTrait)
                .flatMap(_.asType.toType.members)
                .filter(_.isModule)
                .map(_.asModule)
                .filter(_.moduleClass.asType.toType weak_<:< tt.tpe)
                .map(companionInstance(_).asInstanceOf[T])
                .map{f => f.toString.toLowerCase -> f}
                .toMap
        }

        override val values:Set[T] = valuesByName.map{case (_, v) => v}.toSet

        override def apply(name: String):Option[T] = Option(name.trim.toLowerCase()).flatMap(valuesByName.get _)
    }

    implicit def enumMetadataForEnum[T](implicit tt: ru.TypeTag[T]): EnumMetadata[T] =
        new ReflectionBaseEnumMetadataImplementation(tt)

}
