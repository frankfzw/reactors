package org.reactors
package containers
package benchmarks



import org.scalameter.api._
import org.scalameter.picklers.noPickler._



class RContainerBoxingBench extends Bench.Forked[Long] {
  override def defaultConfig: Context = Context(
    exec.minWarmupRuns -> 2,
    exec.maxWarmupRuns -> 5,
    exec.independentSamples -> 1,
    verbose -> false
  )

  def measurer: Measurer[Long] =
    for (table <- Measurer.BoxingCount.all()) yield {
      table.copy(value = table.value.valuesIterator.sum)
    }

  def aggregator: Aggregator[Long] = Aggregator.median

  override def reporter = Reporter.Composite(
    LoggingReporter(),
    ValidationReporter()
  )

  measure method "RContainer.<combinators>" config (
    reports.validation.predicate -> { (n: Any) => n == 0 }
  ) in {
    using(Gen.single("numEvents")(10000)) in { numEvents =>
      val emitter = new Events.Emitter[Int]

      var i = 0
      while (i < numEvents) {
        emitter.react(i)
        i += 1
      }
      emitter.unreact()
    }
  }

}