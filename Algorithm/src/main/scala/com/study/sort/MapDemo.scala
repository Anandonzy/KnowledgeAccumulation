package com.study.sort

/**
 * @author wangziyu
 * @since 2024/5/8 11:25
 * @version 1.0
 *          验证 map 结构的返回值.
 */
object MapDemo {
  def main(args: Array[String]): Unit = {


    // 创建一个不可变的Map，其中包含一些键值对
    val myMap: Map[Int, String] = Map(1 -> "Scala", 2 -> "Java", 3 -> "Python")

    // 使用get方法获取存在的键
    val existingKey: Option[String] = myMap.get(1)
    println(s"Key 1: $existingKey") // 输出: Key 1: Some(Scala)

    // 使用get方法获取不存在的键
    val nonExistingKey: Option[String] = myMap.get(4)
    println(s"Key 4: $nonExistingKey") // 输出: Key 4: None
  }

}
