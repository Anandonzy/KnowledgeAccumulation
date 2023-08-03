package com.study.sort

/**
 * @Author wangziyu1
 * @Date 2022/8/3 16:16
 * @Version 1.0
 */
object QuickSortTests {

  def main(args: Array[String]): Unit = {

    val list = List(3, 12, 43, 23, 7, 1, 2, 0)
    println(quicsort(list))
  }

  def quicsort(list: List[Int]): List[Int] = list match {
    case Nil => Nil
    case List() => List()
    case head :: tail =>
      val (left, right) = tail.partition(_ < head)
      quicsort(left) ::: head :: quicsort(right)

  }

}
