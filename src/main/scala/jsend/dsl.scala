package nozzle.jsend

package object dsl {
     
  def `for`[T <: Product] = new {
    def serializeOneAs(singularNameP: String) = new JSendSingular[T] {
      val singularName = singularNameP
      def andMultipleAs(pluralNameP: String) = new JSendSingular[T] with JSendPlural[T] {
        val singularName = singularNameP
        val pluralName = pluralNameP
      }
    }
    def serializeMultipleAs(pluralNameP: String) = new JSendPlural[T] {
      val pluralName = pluralNameP
    }
  }
}

