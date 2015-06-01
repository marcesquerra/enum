package com.bryghts.enumeration

import scala.reflect.runtime.{universe => ru}

/**
 * Created by Marc Esquerrà on 31/05/2014.
 */
abstract class Enum[E](implicit tt : ru.TypeTag[E]) {

    private val typeName = helpers.decodedTypeName(tt)

    private def genReverseEnum: Map[String, E] = {
        val o = Class.forName(tt.tpe.typeSymbol.asClass.fullName + "$")
        val cz = Class.forName(tt.tpe.typeSymbol.asClass.fullName)

        def allSubclasses(in: ru.ClassSymbol):List[ru.ClassSymbol] =
            in :: in.knownDirectSubclasses.toList.map{_.asClass}.flatMap{allSubclasses(_)}

        allSubclasses(
            tt
                .tpe
                .typeSymbol
                .asClass)
            .filterNot(_.isTrait)
            .map{_.asClass.name.toString}
            .map{o.getName + _ + "$"}
            .map{Class.forName(_)}
            .toList
            .flatMap{_.getDeclaredFields.toList}
            .filter{f => cz.isAssignableFrom(f.getType)}
            .map{_.get(this).asInstanceOf[E]}
            .map{f => f.toString.toLowerCase -> f}
            .toMap
    }

    private val valuesByName: Map[String, E] = genReverseEnum
    val values:List[E] = valuesByName.map{case (_, v) => v}.toList

    def apply(name: String):Option[E] = Option(name.trim.toLowerCase()).flatMap(valuesByName.get _)

}
