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
package com.example.gradle.db

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.model.*
import com.example.gradle.*
import com.example.gradle.aws.CredentialConfig
import com.example.gradle.aws.EndpointConfig

class DynamoDbCreateTable(
    private val client: AmazonDynamoDB,
    private val prefix: String?
) {

  fun createTable(definition: DataDefinitionProvider): String =
      runCatching { client.createTable(definition.toRequest(prefix)).tableDescription.tableName }
          .asEither
          .rescueFlat {
            when (it) {
              is ResourceInUseException -> Either.right(definition.tableName(prefix))
              else -> Either.left(it)
            }
          }
          .throwError { IllegalStateException("failed to create table(${definition.tableName(prefix)})", it) }

  fun createTables(vararg definitions: DataDefinitionProvider): List<String> = definitions.map { createTable(it) }

  companion object {

    operator fun invoke(credentialConfig: CredentialConfig, endpointConfig: EndpointConfig, prefix: String?): DynamoDbCreateTable =
        listOf(credentialConfig, endpointConfig)
            .fold(AmazonDynamoDBClientBuilder.standard()) { builder, config ->
              config.configure(builder)
            }.build()
            .let { DynamoDbCreateTable(it, prefix) }

    private val <T : Any> Result<T>.asEither: Either<Throwable, T> get() = this.fold(onSuccess = { Either.right(it) }, onFailure = { Either.left(it) })

    fun DataDefinitionProvider.toRequest(prefix: String?): CreateTableRequest =
        this.definition()
            .let { def ->
              CreateTableRequest(this.tableName(prefix), def.filter { it.canBeIdentity() }.map { KeySchemaElement(it.name, it.identifier.type) })
                  .withAttributeDefinitions(def.filter { it.canBeIdentity() }.map { it.attributeDef() })
            }
            .withProvisionedThroughput(ProvisionedThroughput(3L, 3L))

    private fun DataDefinition.attributeDef(): AttributeDefinition =
        AttributeDefinition(this.name, when (this.type) {
          DataType.STRING -> ScalarAttributeType.S
          DataType.LONG -> ScalarAttributeType.N
          DataType.INT -> ScalarAttributeType.N
        })

    private val IdentifierType.type: KeyType
      get() = when {
        this == IdentifierType.HASHABLE -> KeyType.HASH
        this == IdentifierType.CONTINUOUS -> KeyType.RANGE
        else -> throw UnsupportedOperationException("$this is invalid type for dynamodb key")
      }

  }
}
