package tests

import org.specs2._

import com.bryghts.enumeration._

trait Palo

object Tmp {
val tmp = enumMetadataForEnum[Palo]
}

object Palo extends Enum[Palo](Tmp.tmp) {
    case object Oros       extends Palo
    case object Bastos     extends Palo
    case object Espadas    extends Palo
    case object Copas      extends Palo

    object IsNotAPalo
}

/**
 * Created by Marc Esquerrà on 23/06/15.
 */
class EnumSpecification extends Specification { def is = s2"""

 An Enum must
   contain a collaction with all the values of the enum    $e1
   be able to return a component by name                   $e2
   return None if asked for an invalid name                $e3
   return the trait name when asked for typeName           $e4
                                                     """

    def e1 = Palo.values must_=== Set(Palo.Oros, Palo.Bastos, Palo.Espadas, Palo.Copas)
    def e2 = Palo("Oros") must_=== Some(Palo.Oros)
    def e3 = Palo("Picas") must_=== None
    def e4 = Palo.typeName must_=== "Palo"

}
