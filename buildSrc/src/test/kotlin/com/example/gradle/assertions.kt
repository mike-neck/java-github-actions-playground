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

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.opentest4j.AssertionFailedError

infix fun <A : Any, B> A.shouldBe(b: B): Asserting =
    object : Asserting {
      override fun run() =
          Assertions.assertEquals(b, this@shouldBe)
    }

interface Asserting {
  fun run()
}

operator fun String.invoke(asserting: () -> Asserting): DynamicTest = DynamicTest.dynamicTest(this) { asserting().run() }

infix fun String.shouldStartWith(prefix: String): Asserting =
    object : Asserting {
      override fun run() =
        when (this@shouldStartWith.startsWith(prefix)) {
          false -> throw AssertionFailedError("expected \"${this@shouldStartWith}\" to start with \"$prefix\", but not", prefix, this@shouldStartWith)
          else -> doNothing()
        }
    }

private fun doNothing(): Unit = Unit

infix fun String.shouldContain(part: String): Asserting =
    object: Asserting {
      override fun run() = when(this@shouldContain.contains(part)) {
        false -> throw AssertionFailedError("expected \"${this@shouldContain}\" to contain \"$part\", but not", part, this@shouldContain)
        true -> doNothing()
      }
    }
