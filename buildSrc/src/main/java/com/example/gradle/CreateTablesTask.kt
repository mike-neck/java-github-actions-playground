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
import com.example.gradle.db.DynamoDbCreateTable
import com.example.gradle.db.Scrum
import com.example.gradle.db.StoryPoint
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject
import kotlin.reflect.KClass

open class CreateTablesTask
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

  init {
    awsAccessKey = aws._awsAccessKey
    awsSecretKey = aws._awsSecretKey
    profile = aws._profile
    endpoint = aws._endpoint
    tableNamePrefix = aws._tableNamePrefix
  }

  @TaskAction
  fun exec() =
      DynamoDbCreateTable(
          CredentialConfig(awsAccessKey, awsSecretKey, profile),
          EndpointConfig(endpoint),
          tableNamePrefix.orNull)
          .createTables(Scrum, StoryPoint)
          .forEach { logger.lifecycle("created {} table", it) }
}

inline fun <reified T : Any> Project.prop(klass: KClass<T> = T::class): Property<T> = this.objects.property(klass.java)
