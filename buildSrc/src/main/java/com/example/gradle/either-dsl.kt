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

interface OrLeft<T : Any> {
  fun onNull(l: () -> String): Either<String, T>
}

fun <T : Any> trying(r: () -> T?): OrLeft<T> =
    when (val v = r()) {
      null -> object : OrLeft<T> {
        override fun onNull(l: () -> String): Either<String, T> = Either.left(l())
      }
      else -> object : OrLeft<T> {
        override fun onNull(l: () -> String): Either<String, T> = Either.right(v)
      }
    }

inline fun <reified T> listOf(list: List<T>, item: T): List<T> =
    mutableListOf(*list.toTypedArray()).apply { this.add(item) }.toList()

fun <P1 : Any, P2 : Any> Either<List<String>, P1>.then(f: () -> Either<String, P2>): Either<List<String>, Pair<P1, P2>> =
    when (this) {
      is Right -> this.flatMap { p1 -> f().errorMap { listOf(it) }.map { p1 to it } }
      is Left -> when (val e = f()) {
        is Left<String, P2> -> Either.left(listOf(this.left, e.left))
        else -> Either.left(this.left)
      }
      else -> throw IllegalStateException("invalid either type: $this")
    }

fun doing(): Either<List<String>, Unit> = Either.right(Unit)

fun <T> Pair<Unit, T>.remove(): T = this.second
