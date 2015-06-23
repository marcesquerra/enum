package com.bryghts.enumeration

import scala.reflect.runtime.{universe => ru}

/**
 * Created by Marc Esquerrà on 31/05/2014.
 */
abstract class Enum[E](implicit tt : ru.TypeTag[E]) {

    private val typeName = helpers.decodedTypeName(tt)

    private val valuesByName: Map[String, E] = {
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
            .map(companionInstance(_).asInstanceOf[E])
            .map{f => f.toString.toLowerCase -> f}
            .toMap
    }

    val values:Set[E] = valuesByName.map{case (_, v) => v}.toSet

    def apply(name: String):Option[E] = Option(name.trim.toLowerCase()).flatMap(valuesByName.get _)

}
