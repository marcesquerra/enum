# enum
Helper to create enums out of a 'sealed trait' and some 'case object's

### Usage
```scala

import com.bryghts.enumeration.Enum

sealed trait CardSuit

object CardSuit extends Enum[CardSuit] {
  case object Spades   extends CardSuit
  case object Hearts   extends CardSuit
  case object Diamonds extends CardSuit
  case object Clubs    extends CardSuit
}

```
This adds to the CardSuit object this capabilities:

```scala

CardSuit("Spades")   // returns Some(CardSuit.Spades)
CardSuit("Bastos")   // returns None
CardSuit.all         // returns List(CardSuit.Spades, CardSuit.Hearts, CardSuit.Diamonds, CardSuit.Clubs)

```
### SBT Dependency

```sbt

libraryDependencies += "com.bryghts.enum" %% "enum" % "0.1.2"

```

### ScalaDoc
http://marcesquerra.github.io/enum/latest/api/
