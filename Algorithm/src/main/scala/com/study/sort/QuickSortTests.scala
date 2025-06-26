package com.study.sort

/**
 * @Author wangziyu1
 * @Date 2022/8/3 16:16
 * @Version 1.0
 *          快速排序测试
 */
object QuickSortTests {

  def main(args: Array[String]): Unit = {
    // 测试快速排序
    val list = List(3, 12, 43, 23, 7, 1, 2, 0)
    println(quicsort(list))

//    //快速排序
//    val list2 = List(3, 12, 43, 23, 7, 1, 2, 0)
//    println(quicsort(list2))
//
//    //冒泡排序
//    val list3 = List(3, 12, 43, 23, 7, 1, 2, 0)
//    println(bubbleSort(list3))
//
//    //测试选择排序
//    val list4 = List(3, 12, 43, 23, 7, 1, 2, 0)
//    println(selectSort(list4))


  }

  //选择排序
  private def selectSort(list4: List[Int]) = {
    for (i <- 0 until list4.length - 1) {
      var minIndex = i
      for (j <- i + 1 until list4.length) {
        if (list4(j) < list4(minIndex)) {
          minIndex = j
        }
      }
      if (minIndex != i) {
        val temp = list4(i)
        list4.updated(i, list4(minIndex))
        list4.updated(minIndex, temp)
      }
    }
    list4
  }

  private def bubbleSort(list3: List[Int]) = {
    for (i <- 0 until list3.length - 1) {
      for (j <- 0 until list3.length - 1 - i) {
        if (list3(j) > list3(j + 1)) {
          val temp = list3(j)
          list3.updated(j, list3(j + 1))
          list3.updated(j + 1, temp)
        }
      }
    }
    list3
  }

  def quicsort(list: List[Int]): List[Int] = list match {
    case Nil => Nil
    case List() => List()
    case head :: tail =>
      val (left, right) = tail.partition(_ < head)
      quicsort(left) ::: head :: quicsort(right)

  }

}
