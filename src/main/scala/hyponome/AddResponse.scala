/*
 * Copyright 2016 Henry Till
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hyponome

import java.net.URI
import hyponome.event._

final case class AddResponse(
  status: AddStatus,
  file: URI,
  hash: SHA256Hash,
  name: Option[String],
  contentType: String,
  length: Long)

object AddResponse {
  def apply(a: Add, s: AddStatus): AddResponse = {
    val uri  = getURI(a.hostname, a.port, a.hash, a.name)
    AddResponse(s, uri, a.hash, a.name, a.contentType, a.length)
  }
}