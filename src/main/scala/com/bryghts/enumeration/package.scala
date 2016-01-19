package com.bryghts

import scala.reflect.runtime.{universe => ru}
import scala.language.experimental.macros

/**
 * Created by Marc Esquerrà on 05/09/15.
 */
package object enumeration {

    abstract class AbstractEnumMetadata[T] extends EnumMetadata[T] {
        private val valuesByName: Map[String, T] = values.map(v => (v.toString.toLowerCase, v)).toMap

        override def values:Set[T]

        override def apply(name: String):Option[T] = Option(name.trim.toLowerCase()).flatMap(valuesByName.get _)
    }
    class ReflectionBaseEnumMetadataImplementation[T](tt : ru.TypeTag[T]) extends AbstractEnumMetadata[T] {
        override val typeName = helpers.decodedTypeName(tt)
        override val values: Set[T] = {
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
                .toSet
        }

    }
//
//    implicit def enumMetadataForEnum[T](implicit tt: ru.TypeTag[T]): EnumMetadata[T] =
//        new ReflectionBaseEnumMetadataImplementation(tt)

    implicit def enumMetadataForEnum[T]: EnumMetadata[T] =
        macro enumMetadataForEnumImp[T]

    def enumMetadataForEnumImp[T](c: helpers.Context)(implicit tt: c.WeakTypeTag[T]): c.Expr[EnumMetadata[T]] = {
        import c.universe._


        println("+" * 80)
        println {
            showRaw{
                reify {
                    Seq(Sample2.A, Sample2.B)
                }.tree
            }
        }
        println("+" * 80)


        val name: String = tt.tpe.typeSymbol.name.decodedName.toString
        val vs = {

            //                    def companionInstance(t: ModuleSymbol) = {
            //                        t.companion
            //                    }

            //                    val cls       = tt.tpe.typeSymbol.asClass
            //                    val o         = this.getClass
            //                    val cz        = classFor(cls)
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
                val r = q"$s"
//                    Select(Ident(s.owner), newTermName(s.name.dec))
                println(showRaw(r))
                r
            }).toList
//            val r = c.Expr[Set[T]](Apply(Select(Ident(newTermName("SetBuilder")), newTermName("apply")), instances.toList))
              val r = q"Set(..$instances)"
            println(showRaw(r))
            r
//            Apply(Select(New(AppliedTypeTree(Ident(typeOf[Set[T]].typeSymbol), List(Ident(typeOf[Set[T]].asInstanceOf[TypeRef].args.head.typeSymbol)))), nme.CONSTRUCTOR), instances.toList)
//            val stt = implicitly[WeakTypeTag[Set[T]]]
//            val targetType = stt.tpe
//            val tmp = c.Expr[Set[T]](Apply(Select(New(AppliedTypeTree(Ident(targetType.typeSymbol), List(Ident(targetType.asInstanceOf[TypeRef].args.head.typeSymbol)))), nme.CONSTRUCTOR), instances.toList))
//            tmp
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

trait Sample

object Sample2 {
    object A extends Sample
    object B extends Sample
}
