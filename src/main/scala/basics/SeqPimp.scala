package nozzle.basics

trait SeqPimp {
  implicit class SeqPimp[A](s: Seq[A]) {
    /**
     * typesafe version of contains
     * it does not compile when the type of the container
     * does not match the type of the element
     */
    def has(a: A) = s.contains(a)
  }
}

object SeqPimp extends SeqPimp
