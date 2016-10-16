/*
 * Copyright 2016 Bjoern Bilger
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
package com.jrestless.aws.service.io;

import java.util.List;
import java.util.Map;

/**
 * Response object to be returned by a lambda function if the function is
 * intended to be called by jrestless' AWS service framework.
 *
 * @author Bjoern Bilger
 *
 */
public interface ServiceResponse {
	String getBody();
	Map<String, List<String>> getHeaders();
	int getStatusCode();
	String getReasonPhrase();

}
