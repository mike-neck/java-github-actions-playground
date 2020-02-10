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

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.model.*
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject
import kotlin.reflect.KClass

class CreateTableTask @Inject constructor(project: Project) : DefaultTask() {

  val awsAccessKey: Property<String>
  val awsSecretKey: Property<String>
  val profile: Property<String>
  val endpoint: Property<String>

  init {
    awsAccessKey = project.prop()
    awsSecretKey = project.prop()
    profile = project.prop()
    endpoint = project.prop()
  }

  @TaskAction
  fun exec() {
    val client = listOf(
        CredentialConfig(awsAccessKey, awsSecretKey, profile),
        EndpointConfig(endpoint)
    ).fold(AmazonDynamoDBClientBuilder.standard()) { builder, config ->
      config.configure(builder)
    }.build()


  }

  fun createScrumTableRequest(): CreateTableRequest =
      CreateTableRequest()
          .withTableName("scrum")
          .withKeySchema(Scrum.definition().filter { it.identifier }.map { KeySchemaElement(it.name, KeyType.HASH) })
          .withAttributeDefinitions(Scrum.definition().map { it.attributeDef() })
          .withProvisionedThroughput(ProvisionedThroughput(2L, 2L))

  private fun DataDefinition.attributeDef(): AttributeDefinition =
      AttributeDefinition(this.name, when (this.type) {
        DataType.STRING -> ScalarAttributeType.S
        DataType.LONG -> ScalarAttributeType.N
        DataType.INT -> ScalarAttributeType.N
      })

  companion object {
    inline fun <reified T : Any> Project.prop(klass: KClass<T> = T::class): Property<T> = this.objects.property(klass.java)
  }
}
