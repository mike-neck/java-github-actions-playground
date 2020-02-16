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
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.time.ZoneId
import javax.inject.Inject

open class NewStoryPointsTask
@Inject constructor(aws: Aws) : DefaultTask() {

  @Input
  val awsAccessKey: Property<String>
  @Input
  val awsSecretKey: Property<String>
  @Input
  val profile: Property<String>
  @Input
  val endpoint: Property<String>
  @Input
  val tableNamePrefix: Property<String>

  @Input
  val remaining: Property<Int>
  @Input
  val finished: Property<Int>
  @Input
  val date: Property<String>
  @Input
  val timeZone: Property<String>

  init {
    awsAccessKey = aws._awsAccessKey
    awsSecretKey = aws._awsSecretKey
    profile = aws._profile
    endpoint = aws._endpoint
    tableNamePrefix = aws._tableNamePrefix

    remaining = project.prop()
    finished = project.prop()
    date = project.prop()
    timeZone = project.prop()
  }

  fun setRemaining(value: Int): Unit = remaining.set(value)

  fun setFinished(value: Int): Unit = finished.set(value)

  fun setDate(date: String): Unit = this.date.set(date)

  fun setTimeZone(tz: String): Unit = this.timeZone.set(tz)

  private val String.asZone: ZoneId get() = ZoneId.of(this)

  private fun storyPoint(): Either<List<String>, StoryPoint> =
      doing()
          .then { trying { remaining.orNull }.onNull { "lack of parameter [remaining]" } }.map { it.remove() }
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

class TaskExecutionException(taskName: String, errors: List<String>) : Exception("$taskName failed\n${errors.joinToString("\n- ", "- ")}")
