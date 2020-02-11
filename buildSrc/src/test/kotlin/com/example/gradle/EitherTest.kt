/*
 * Copyright 2020 Shinya Mochida
 * 
 * Licensed under the Apache License,Version2.0(the"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,software
 * Distributed under the License is distributed on an"AS IS"BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.gradle

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

class EitherTest {

  private val leftOfIntString: Either<Int, String> = Either.left(10)

  private val rightOfIntString: Either<Int, String> = Either.right("foo-bar-baz")

  @TestFactory
  fun map() : Iterable<DynamicTest> = listOf(
      "left を map しても left" { leftOfIntString.map { it.length } shouldBe Either.left<Int, Int>(10) },
      "right に id を map すると同じものになる" { rightOfIntString.map { it } shouldBe rightOfIntString }
  )
}
