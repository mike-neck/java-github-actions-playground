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

interface Either<L: Any, R: Any> {
  fun <T: Any> map(f: (R) -> T): Either<L, T>
  fun <T: Any> flatMap(f: (R) -> Either<L, T>): Either<L, T>
  fun <T: Any> errorMap(f: (L) -> T): Either<T, R>
  fun rescue(f: (L) -> R): R
  fun throwError(f: (L) -> Throwable): R

  val value: R?

  companion object {
    fun <L: Any, R: Any> left(left: L) : Either<L, R> = Left(left)
    fun <L: Any, R: Any> right(right: R): Either<L, R> = Right(right)
  }
}

fun <L: Any, R: Any> Either<L, R>.rescueFlat(f: (L) -> Either<L, R>): Either<L, R> = this.map { Either.right<L, R>(it) }.rescue(f)

data class Left<L: Any, R: Any>(val left: L): Either<L, R> {
  override fun <T : Any> map(f: (R) -> T): Either<L, T> = Left(left)

  override fun <T : Any> flatMap(f: (R) -> Either<L, T>): Either<L, T> = Left(left)

  override fun <T : Any> errorMap(f: (L) -> T): Either<T, R> = Left(f(left))

  override fun rescue(f: (L) -> R): R = f(left)

  override fun throwError(f: (L) -> Throwable): R = throw f(left)

  override val value: R? get() = null
}

data class Right<L: Any, R: Any>(val right: R): Either<L, R> {
  override fun <T : Any> map(f: (R) -> T): Either<L, T> = Right(f(right))

  override fun <T : Any> flatMap(f: (R) -> Either<L, T>): Either<L, T> = f(right)

  override fun <T : Any> errorMap(f: (L) -> T): Either<T, R> = Right(right)

  override fun rescue(f: (L) -> R): R = right

  override fun throwError(f: (L) -> Throwable): R = right

  override val value: R? get() = right
}
