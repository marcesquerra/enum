package com.bryghts.enumeration

import scala.reflect.runtime.{universe => ru}

/**
 * Created by Marc Esquerrà on 31/05/2015.
 */
package object helpers {


    def decodedTypeName[E](tt : ru.TypeTag[E]) =
        tt.tpe.typeSymbol.name.decodedName.toString

    def companion(t: ru.ClassSymbol): ru.ModuleSymbol = {
        val classMirror = ru.rootMirror.reflectClass(t)

        classMirror.symbol.companion.asModule
    }
}
