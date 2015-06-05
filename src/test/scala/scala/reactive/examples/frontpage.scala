package scala.reactive
package examples



import org.scalatest._
import org.scalatest.Matchers



/** Examples from the Reactive Collections frontpage.
 *  Note: if you are changing this file, please take care to update the website
 *  frontpage.
 */
class CircuitSimulationSuite extends FunSuite with Matchers {

  test("Half-adder reacts to input changes") {
    implicit val canLeak = Permission.newCanLeak

    // Define digital circuits
    def and(a: Signal[Boolean], b: Signal[Boolean]) =
      (a zip b) { _ && _ }
    def xor(a: Signal[Boolean], b: Signal[Boolean]) =
      (a zip b) { _ ^ _ }
    def halfAdder(a: Signal[Boolean], b: Signal[Boolean]) =
      (xor(a, b), and(a, b))
    def logger(name: String, r: Events[Boolean]) =
      r.onEvent(v => println(s"$name: $v"))

    // Simulate a half-adder
    val inputA = RCell(false)
    val inputB = RCell(false)
    val (sum, carry) = halfAdder(inputA, inputB)
    logger("sum", sum)
    logger("carry", carry)
    inputA := true
    assert(sum() == true)
    assert(carry() == false)
    inputB := true
    assert(sum() == false)
    assert(carry() == true)
  }

}