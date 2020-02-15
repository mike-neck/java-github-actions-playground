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
import org.gradle.api.tasks.TaskAction
import kotlin.reflect.KClass

class CreateTablesTask : DefaultTask() {

  val awsAccessKey: Property<String>
  val awsSecretKey: Property<String>
  val profile: Property<String>
  val endpoint: Property<String>
  val tableNamePrefix: Property<String>

  init {
    awsAccessKey = project.prop()
    awsSecretKey = project.prop()
    profile = project.prop()
    endpoint = project.prop()
    tableNamePrefix = project.prop()
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
