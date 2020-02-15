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

import com.example.gradle.aws.CredentialConfig
import com.example.gradle.aws.EndpointConfig
import com.example.gradle.db.DynamoDbInsert
import com.example.gradle.db.StoryPoint
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskAction
import java.time.ZoneId

class NewStoryPointsTask : DefaultTask() {

  val awsAccessKey: Property<String>
  val awsSecretKey: Property<String>
  val profile: Property<String>
  val endpoint: Property<String>
  val tableNamePrefix: Property<String>

  val onGoing: Property<Int>
  val finished: Property<Int>
  val date: Property<String>
  val timeZone: Property<String>

  init {
    awsAccessKey = project.prop()
    awsSecretKey = project.prop()
    profile = project.prop()
    endpoint = project.prop()
    tableNamePrefix = project.prop()

    onGoing = project.prop()
    finished = project.prop()
    date = project.prop()
    timeZone = project.prop()
  }

  interface OrLeft<T: Any> {
    fun onNull(l: () -> String): Either<String, T>
  }

  private fun <T: Any> trying(r: () -> T?): OrLeft<T> =
      when (val v = r()) {
        null -> object : OrLeft<T> {
          override fun onNull(l: () -> String): Either<String, T> = Either.left(l())
        }
        else -> object : OrLeft<T> {
          override fun onNull(l: () -> String): Either<String, T> = Either.right(v)
        }
      }

  private inline fun <reified T> listOf(list: List<T>, item: T): List<T> =
      mutableListOf(*list.toTypedArray()).apply { this.add(item) }.toList()

  private fun <P1: Any, P2: Any> Either<List<String>, P1>.then(f: () -> Either<String, P2>): Either<List<String>, Pair<P1, P2>> =
      when (this) {
        is Right -> this.flatMap { p1 -> f().errorMap { listOf(it) }.map { p1 to it } }
        is Left -> when (val e = f()) {
          is Left<String, P2> -> Either.left(listOf(this.left, e.left))
          else -> Either.left(this.left)
        }
        else -> throw IllegalStateException("invalid either type: $this")
      }

  private fun doing(): Either<List<String>, Unit> = Either.right(Unit)

  private fun <T> Pair<Unit, T>.remove(): T = this.second

  private val String.asZone: ZoneId get() = ZoneId.of(this)

  private fun storyPoint(): Either<List<String>, StoryPoint> =
      doing()
          .then { trying { onGoing.orNull }.onNull { "lack of parameter [onGoing]" } }.map { it.remove() }
          .then { trying { finished.orNull }.onNull { "lack of parameter [finished]" } }
          .then { trying { date.orNull }.onNull { "lack of parameter [date](with format: yyyy-MM-dd)" } }
          .then { trying { timeZone.orNull }.onNull { "lack of parameter [timeZone]" } }
          .map { StoryPoint(it.first.first.first, it.first.first.second, it.first.second, it.second.asZone) }

  private val task: Task get() = this

  @TaskAction
  fun newStoryPoint() =
      DynamoDbInsert(
          CredentialConfig(awsAccessKey, awsSecretKey, profile),
          EndpointConfig(endpoint),
          tableNamePrefix.orNull
      ).insert(
          storyPoint().throwError { TaskExecutionException(task.name, it) },
          StoryPoint
      )
}

class TaskExecutionException(taskName: String, errors: List<String>): Exception("$taskName failed\n${errors.joinToString("\n- ", "- ")}")
