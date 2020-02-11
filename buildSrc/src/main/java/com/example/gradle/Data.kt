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

//////////////////////
//////////////////////

interface DataFactory {
  fun string(name: String, value: String): Data
  fun identifierString(name: String, value: String): Identifier
  fun int(name: String, value: Int): Data
  fun identifierInt(name: String, value: Int): Identifier
  fun long(name: String, value: Long): Data
  fun identifierLong(name: String, value: Long): Identifier
}

//////////////

interface Identifier: Data

interface Data {
  fun <V: Any> op(): StringDataOp<V>
}

////

interface StringDataOp<V: Any> {
  fun onString(strMap: (String) -> V): LongDataOp<V>
}

interface LongDataOp<V: Any> {
  fun onLong(longMap: (Long) -> V): IntDataOp<V>
}

interface IntDataOp<V: Any> {
  fun onInt(intMap: (Int) -> V): NoMatchesOp<V>
}

interface NoMatchesOp<V: Any> {
  fun others(valueProvider: () -> V): V
  fun throwError(exception: () -> Exception): V
  fun orNull(): V?
}

//////////////////////
//////////////////////

interface DataDefinitionProvider {
  fun tableName(prefix: String?): String
  fun definition(): List<DataDefinition>
}

////

interface DataDefinition {
  val identifier: IdentifierType
  val name: String
  val type: DataType

  fun canBeIdentity(): Boolean = identifier != IdentifierType.NONE

  companion object {

    data class Default(override val identifier: IdentifierType, override val name: String, override val type: DataType): DataDefinition

    fun string(name: String): DataDefinition = Default(IdentifierType.NONE, name, DataType.STRING)
    fun int(name: String): DataDefinition = Default(IdentifierType.NONE, name, DataType.INT)
    fun long(name: String): DataDefinition = Default(IdentifierType.NONE, name, DataType.LONG)

    fun identifierHashableString(name: String): DataDefinition = Default(IdentifierType.HASHABLE, name, DataType.STRING)
    fun identifierHashableInt(name: String): DataDefinition = Default(IdentifierType.HASHABLE, name, DataType.INT)
    fun identifierHashableLong(name: String): DataDefinition = Default(IdentifierType.HASHABLE, name, DataType.LONG)

    fun identifierContinuousString(name: String): DataDefinition = Default(IdentifierType.CONTINUOUS, name, DataType.STRING)
    fun identifierContinuousInt(name: String): DataDefinition = Default(IdentifierType.CONTINUOUS, name, DataType.INT)
    fun identifierContinuousLong(name: String): DataDefinition = Default(IdentifierType.CONTINUOUS, name, DataType.LONG)
  }
}

enum class IdentifierType {
  NONE,
  HASHABLE,
  CONTINUOUS,
}

enum class DataType {
  STRING,
  INT,
  LONG
}
