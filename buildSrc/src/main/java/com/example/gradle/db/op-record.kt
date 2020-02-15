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
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.example.gradle.Either

class DynamoDbInsert(
    private val client: AmazonDynamoDB,
    private val tableNamePrefix: String?,
    private val dataFactory: DataFactory = DynamoDbDataFactory
) {

  fun <T: Records> insert(record: T, dataDefinitionProvider: DataDefinitionProvider<T>): Either<String, T> {
    val datum = record.data(dataFactory)
    val attributes = datum.mapNotNull { data ->
      data.op<AttributeValue>()
          .onString { AttributeValue(it) }
          .onLong { AttributeValue().withN("$it") }
          .onInt { AttributeValue().withN("$it") }
          .orNull()
    }.toMap()
    return runCatching { client.putItem(dataDefinitionProvider.tableName(tableNamePrefix), attributes) }
        .asEither
        .errorMap { "${it.javaClass}/${it.message}" }
        .flatMap {
          if (it.sdkHttpMetadata.httpStatusCode in 200..299) Either.right<String, T>(record)
          else Either.left("failed because response status is ${it.sdkHttpMetadata.httpStatusCode}")
        }
  }
}

object DynamoDbDataFactory: DataFactory {

  override fun string(name: String, value: String): Data =
      object: Data {
        override fun <V : Any> op(): StringDataOp<V> =
            DefaultData(name, string = value)
      }

  override fun identifierString(name: String, value: String): Identifier =
      object: Identifier {
        override fun <V : Any> op(): StringDataOp<V> =
            DefaultData(name, string = value)
      }

  override fun int(name: String, value: Int): Data =
      object: Data {
        override fun <V : Any> op(): StringDataOp<V> =
            DefaultData(name, int = value)
      }

  override fun identifierInt(name: String, value: Int): Identifier =
      object: Identifier {
        override fun <V : Any> op(): StringDataOp<V> =
            DefaultData(name, int = value)
      }

  override fun long(name: String, value: Long): Data =
      object: Data {
        override fun <V : Any> op(): StringDataOp<V> =
            DefaultData(name, long = value)
      }

  override fun identifierLong(name: String, value: Long): Identifier =
      object: Identifier {
        override fun <V : Any> op(): StringDataOp<V> =
            DefaultData(name, long = value)
      }
}

class DefaultData<V: Any>(
    private val name: String,
    private val string: String? = null,
    private val long: Long? = null,
    private val int: Int? = null
): StringDataOp<V> {
  override fun onString(strMap: (String) -> V): LongDataOp<V> =
      object : LongDataOp<V> {
        override fun onLong(longMap: (Long) -> V): IntDataOp<V> =
            object : IntDataOp<V> {
              override fun onInt(intMap: (Int) -> V): NoMatchesOp<V> =
                  object : NoMatchesOp<V> {
                    private fun challenge(): V? =
                        when {
                          string != null -> strMap(string)
                          long != null -> longMap(long)
                          int != null -> intMap(int)
                          else -> null
                        }

                    override fun others(valueProvider: () -> V): Pair<String, V> =
                        when (val v = challenge()) {
                          null -> name to valueProvider()
                          else -> name to v
                        }

                    override fun throwError(exception: () -> Exception): Pair<String, V> =
                        when (val v = challenge()) {
                          null -> throw exception()
                          else -> name to v
                        }

                    override fun orNull(): Pair<String, V>? =
                        when (val v = challenge()) {
                          null -> null
                          else -> name to v
                        }
                  }
            }
      }
}
