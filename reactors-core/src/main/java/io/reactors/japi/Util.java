package io.reactors.japi;



import java.util.function.Consumer;
import scala.reflect.*;
import scala.runtime.*;



class Util {
  public static <T> io.reactors.Arrayable<T> arrayable() {
    ClassTag<T> tag = ClassTag$.MODULE$.apply(Object.class);
    io.reactors.Arrayable<T> a = io.reactors.Arrayable$.MODULE$.ref(tag);
    return a;
  }

  public static <T> AbstractFunction1<T, BoxedUnit> toScalaFunction(Consumer<T> c) {
    return new AbstractFunction1<T, BoxedUnit>() {
      public BoxedUnit apply(T x) {
        c.accept(x);
        return BoxedUnit.UNIT;
      }
    };
  }
}
