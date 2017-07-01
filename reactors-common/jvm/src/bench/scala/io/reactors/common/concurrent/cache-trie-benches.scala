package io.reactors.common.concurrent



import java.util.concurrent.ConcurrentHashMap
import org.scalameter.api._
import org.scalameter.japi.JBench
import org.scalatest.FunSuite
import scala.collection.concurrent.TrieMap
import scala.util.Random



class CacheTrieFootprintBenches extends JBench.OfflineReport {
  override def measurer = new Executor.Measurer.MemoryFootprint

  override def defaultConfig = Context(
    exec.benchRuns -> 8,
    exec.independentSamples -> 1,
    verbose -> true
  )

  val sizes = Gen.range("size")(100000, 1000000, 250000)

  case class Wrapper(value: Int)

  val elems = (0 until 1000000).map(i => Wrapper(i)).toArray

  @gen("sizes")
  @benchmark("cache-trie.size")
  @curve("chm")
  def chmInsert(size: Int) = {
    val chm = new ConcurrentHashMap[Wrapper, Wrapper]
    var i = 0
    while (i < size) {
      val v = elems(i)
      chm.put(v, v)
      i += 1
    }
    chm
  }

  @gen("sizes")
  @benchmark("cache-trie.insert")
  @curve("ctrie")
  def ctrieInsert(size: Int) = {
    val trie = new TrieMap[Wrapper, Wrapper]
    var i = 0
    while (i < size) {
      val v = elems(i)
      trie.put(v, v)
      i += 1
    }
    trie
  }

  @gen("sizes")
  @benchmark("cache-trie.insert")
  @curve("cachetrie")
  def cachetrieInsert(size: Int) = {
    val trie = new CacheTrie[Wrapper, Wrapper]
    var i = 0
    while (i < size) {
      val v = elems(i)
      trie.insert(v, v)
      i += 1
    }
    trie
  }
}


class CacheTrieBenches extends JBench.OfflineReport {
  override def historian =
    org.scalameter.reporting.RegressionReporter.Historian.Complete()

  override def defaultConfig = Context(
    exec.minWarmupRuns -> 60,
    exec.maxWarmupRuns -> 120,
    exec.independentSamples -> 1,
    verbose -> true
  )

  case class Wrapper(value: Int)

  val elems = (0 until 1000000).map(i => Wrapper(i)).toArray

  val sizes = Gen.range("size")(100000, 1000000, 250000)

  val chms = for (size <- sizes) yield {
    val chm = new ConcurrentHashMap[Wrapper, Wrapper]
    for (i <- 0 until size) chm.put(elems(i), elems(i))
    (size, chm)
  }

  val ctries = for (size <- sizes) yield {
    val trie = new TrieMap[Wrapper, Wrapper]
    for (i <- 0 until size) trie.put(elems(i), elems(i))
    (size, trie)
  }

  val cachetries = for (size <- sizes) yield {
    val trie = new CacheTrie[Wrapper, Wrapper]
    for (i <- 0 until size) {
      trie.insert(elems(i), elems(i))
    }
    (size, trie)
  }

  val artificialCachetries = for (size <- sizes) yield {
    val trie = new CacheTrie[Wrapper, Wrapper](size)
    for (i <- 0 until size) {
      trie.unsafeCacheInsert(i, elems(i), elems(i))
    }
    (size, trie)
  }

//  @gen("chms")
//  @benchmark("cache-trie.apply")
//  @curve("CHM")
//  def chmLookup(sc: (Int, ConcurrentHashMap[Wrapper, Wrapper])): Int = {
//    val (size, chm) = sc
//    var i = 0
//    var sum = 0
//    while (i < size) {
//      sum += chm.get(elems(i)).value
//      i += 1
//    }
//    sum
//  }

//  @gen("cachetries")
//  @benchmark("cache-trie.apply")
//  @curve("cachetrie-slow-path")
//  def cachetrieSlowLookup(sc: (Int, CacheTrie[Wrapper, Wrapper])): Int = {
//    val (size, trie) = sc
//    var i = 0
//    var sum = 0
//    while (i < size) {
//      sum += trie.slowLookup(elems(i)).value
//      i += 1
//    }
//    sum
//  }
//
//  @gen("ctries")
//  @benchmark("cache-trie.apply")
//  @curve("ctrie")
//  def ctrie(sc: (Int, TrieMap[Wrapper, Wrapper])): Int = {
//    val (size, trie) = sc
//    var i = 0
//    var sum = 0
//    while (i < size) {
//      sum += trie.lookup(elems(i)).value
//      i += 1
//    }
//    sum
//  }

  @gen("artificialCachetries")
  @benchmark("cache-trie.apply")
  @curve("cachetrie")
  def cachetrieFastLookup(sc: (Int, CacheTrie[Wrapper, Wrapper])): Int = {
    val (size, trie) = sc
    var i = 0
    var sum = 0
    while (i < size) {
      sum += trie.fastLookup(elems(i)).value
      i += 1
    }
    sum
  }

  @gen("sizes")
  @benchmark("cache-trie.insert")
  @curve("chm")
  def chmInsert(size: Int) = {
    val chm = new ConcurrentHashMap[Wrapper, Wrapper]
    var i = 0
    while (i < size) {
      val v = elems(i)
      chm.put(v, v)
      i += 1
    }
    chm
  }

  @gen("sizes")
  @benchmark("cache-trie.insert")
  @curve("ctrie")
  def ctrieInsert(size: Int) = {
    val trie = new TrieMap[Wrapper, Wrapper]
    var i = 0
    while (i < size) {
      val v = elems(i)
      trie.put(v, v)
      i += 1
    }
    trie
  }

  @gen("sizes")
  @benchmark("cache-trie.insert")
  @curve("cachetrie")
  def cachetrieInsert(size: Int) = {
    val trie = new CacheTrie[Wrapper, Wrapper]
    var i = 0
    while (i < size) {
      val v = elems(i)
      trie.insert(v, v)
      i += 1
    }
    trie
  }
}


class BirthdaySimulations extends FunSuite {
  test("run birthday simulations") {
    birthday(4, 1)
    birthday(16, 1)
    birthday(16, 2)
    birthday(32, 1)
    birthday(32, 2)
  }

  def birthday(days: Int, collisions: Int): Unit = {
    var sum = 0L
    val total = 1000
    for (k <- 1 to total) {
      val slots = new Array[Int](days)
      var i = 1
      while (i <= days) {
        val day = Random.nextInt(days)
        if (slots(day) == collisions) {
          sum += i - 1
          i = days + 2
        }
        slots(day) += 1
        i += 1
      }
      if (i == days + 1) {
        sum += i
      }
    }
    println(s"For $days, collisions $collisions, average: ${(1.0 * sum / total)}")
  }
}
