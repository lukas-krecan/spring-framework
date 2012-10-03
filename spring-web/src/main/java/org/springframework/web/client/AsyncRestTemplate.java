/*
 * Copyright 2002-2012 the original author or authors.
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

package org.springframework.web.client;

import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

/**
 * An equivalent of RestTemplate the is able to execute the operations asynchronously. Delegates the work to 
 * a {@link RestTemplate} and executes the tasks using {@link AsyncTaskExecutor}. Please note that exception {@link RestClientException} are not thrown
 * from thge methods but from the futures instead. 
 *
 * @see RestTemplate 
 * @author Lukas Krecan
 *
 */
public class AsyncRestTemplate implements AsyncRestOperations {
	private RestOperations restTemplate;
	
	private AsyncTaskExecutor asyncTaskExecutor;

	public AsyncRestTemplate(){
		
	}
	
	public AsyncRestTemplate(RestOperations restTemplate,	AsyncTaskExecutor asyncTaskExecutor) {
		this.restTemplate = restTemplate;
		this.asyncTaskExecutor = asyncTaskExecutor;
	}

	public RestOperations getRestTemplate() {
		return restTemplate;
	}

	public void setRestTemplate(RestOperations restTemplate) {
		this.restTemplate = restTemplate;
	}

	public AsyncTaskExecutor getAsyncTaskExecutor() {
		return asyncTaskExecutor;
	}

	public void setAsyncTaskExecutor(AsyncTaskExecutor asyncTaskExecutor) {
		this.asyncTaskExecutor = asyncTaskExecutor;
	}

	public <T> Future<T> getForObject(final String url, final Class<T> responseType, final Object... uriVariables) {
		return asyncTaskExecutor.submit(new Callable<T>() {
			public T call(){
				return restTemplate.getForObject(url, responseType, uriVariables);
			}
		});
	}

	public <T> Future<T> getForObject(final String url, final Class<T> responseType,	final Map<String, ?> uriVariables) {
		return asyncTaskExecutor.submit(new Callable<T>() {
			public T call(){
				return restTemplate.getForObject(url, responseType, uriVariables);
			}
		});
	}

	public <T> Future<T> getForObject(final URI url, final Class<T> responseType) {
		return asyncTaskExecutor.submit(new Callable<T>() {
			public T call(){
				return restTemplate.getForObject(url, responseType);
			}
		});
	}

	public <T> Future<ResponseEntity<T>> getForEntity(final String url, final Class<T> responseType, final Object... uriVariables) {
		return asyncTaskExecutor.submit(new Callable<ResponseEntity<T>>() {
			public ResponseEntity<T> call(){
				return restTemplate.getForEntity(url, responseType, uriVariables);
			}
		});
	}

	public <T> Future<ResponseEntity<T>> getForEntity(final String url, final Class<T> responseType, final Map<String, ?> uriVariables) {
		return asyncTaskExecutor.submit(new Callable<ResponseEntity<T>>() {
			public ResponseEntity<T> call(){
				return restTemplate.getForEntity(url, responseType, uriVariables);
			}
		});
	}

	public <T> Future<ResponseEntity<T>> getForEntity(final URI url,final Class<T> responseType) {
		return asyncTaskExecutor.submit(new Callable<ResponseEntity<T>>() {
			public ResponseEntity<T> call(){
				return restTemplate.getForEntity(url, responseType);
			}
		});
	}

	public Future<HttpHeaders> headForHeaders(final String url, final Object... uriVariables) {
		return asyncTaskExecutor.submit(new Callable<HttpHeaders>() {
			public HttpHeaders call(){
				return restTemplate.headForHeaders(url, uriVariables);
			}
		});
	}

	public Future<HttpHeaders> headForHeaders(final String url, final Map<String, ?> uriVariables) {
		return asyncTaskExecutor.submit(new Callable<HttpHeaders>() {
			public HttpHeaders call(){
				return restTemplate.headForHeaders(url, uriVariables);
			}
		});
	}

	public Future<HttpHeaders> headForHeaders(final URI url) {
		return asyncTaskExecutor.submit(new Callable<HttpHeaders>() {
			public HttpHeaders call(){
				return restTemplate.headForHeaders(url);
			}
		});
	}

	public Future<URI> postForLocation(final String url, final Object request, final Object... uriVariables) {
		return asyncTaskExecutor.submit(new Callable<URI>() {
			public URI call(){
				return restTemplate.postForLocation(url, request, uriVariables);
			}
		});
	}

	public Future<URI> postForLocation(final String url, final Object request, final Map<String, ?> uriVariables) {
		return asyncTaskExecutor.submit(new Callable<URI>() {
			public URI call(){
				return restTemplate.postForLocation(url, request, uriVariables);
			}
		});
	}

	public Future<URI> postForLocation(final URI url, final Object request) {
		return asyncTaskExecutor.submit(new Callable<URI>() {
			public URI call(){
				return restTemplate.postForLocation(url, request);
			}
		});
	}

	public <T> Future<T> postForObject(final String url, final Object request, final Class<T> responseType, final Object... uriVariables) {
		return asyncTaskExecutor.submit(new Callable<T>() {
			public T call(){
				return restTemplate.postForObject(url, request, responseType, uriVariables);
			}
		});
	}

	public <T> Future<T> postForObject(final String url, final Object request, final Class<T> responseType, final Map<String, ?> uriVariables) {
		return asyncTaskExecutor.submit(new Callable<T>() {
			public T call(){
				return restTemplate.postForObject(url, request, responseType, uriVariables);
			}
		});
	}

	public <T> Future<T> postForObject(final URI url, final Object request, final Class<T> responseType) {
		return asyncTaskExecutor.submit(new Callable<T>() {
			public T call(){
				return restTemplate.postForObject(url, request, responseType);
			}
		});
	}

	public <T> Future<ResponseEntity<T>> postForEntity(final String url, final Object request, final Class<T> responseType, final Object... uriVariables) {
		return asyncTaskExecutor.submit(new Callable<ResponseEntity<T>>() {
			public ResponseEntity<T> call(){
				return restTemplate.postForEntity(url, request, responseType, uriVariables);
			}
		});
	}

	public <T> Future<ResponseEntity<T>> postForEntity(final String url, final Object request, final Class<T> responseType, final Map<String, ?> uriVariables) {
		return asyncTaskExecutor.submit(new Callable<ResponseEntity<T>>() {
			public ResponseEntity<T> call(){
				return restTemplate.postForEntity(url, request, responseType, uriVariables);
			}
		});
	}

	public <T> Future<ResponseEntity<T>> postForEntity(final URI url, final Object request, final 	Class<T> responseType) {
		return asyncTaskExecutor.submit(new Callable<ResponseEntity<T>>() {
			public ResponseEntity<T> call(){
				return restTemplate.postForEntity(url, request, responseType);
			}
		});
	}

	public Future<Void> put(final String url, final Object request, final Object... uriVariables) {
		return asyncTaskExecutor.submit(new Callable<Void>() {
			public Void call(){
				restTemplate.put(url, request, uriVariables);
				return null;
			}
		});
	}

	public Future<Void> put(final String url, final Object request, final Map<String, ?> uriVariables) {
		return asyncTaskExecutor.submit(new Callable<Void>() {
			public Void call(){
				restTemplate.put(url, request, uriVariables);
				return null;
			}
		});
	}

	public Future<Void> put(final URI url, final Object request) {
		return asyncTaskExecutor.submit(new Callable<Void>() {
			public Void call(){
				restTemplate.put(url, request);
				return null;
			}
		});
	}

	public Future<Void> delete(final String url, final Object... uriVariables) {
		return asyncTaskExecutor.submit(new Callable<Void>() {
			public Void call(){
				restTemplate.delete(url, uriVariables);
				return null;
			}
		});
	}

	public Future<Void> delete(final String url, final Map<String, ?> uriVariables) {
		return asyncTaskExecutor.submit(new Callable<Void>() {
			public Void call(){
				restTemplate.delete(url, uriVariables);
				return null;
			}
		});
		
	}

	public Future<Void> delete(final URI url) {
		return asyncTaskExecutor.submit(new Callable<Void>() {
			public Void call(){
				restTemplate.delete(url);
				return null;
			}
		});
	}

	public Future<Set<HttpMethod>> optionsForAllow(final String url, final Object... uriVariables) {
		return asyncTaskExecutor.submit(new Callable<Set<HttpMethod>>() {
			public Set<HttpMethod> call(){
				return restTemplate.optionsForAllow(url, uriVariables);
			}
		});
	}

	public Future<Set<HttpMethod>> optionsForAllow(final String url, final Map<String, ?> uriVariables) {
		return asyncTaskExecutor.submit(new Callable<Set<HttpMethod>>() {
			public Set<HttpMethod> call(){
				return restTemplate.optionsForAllow(url, uriVariables);
			}
		});
	}

	public Future<Set<HttpMethod>> optionsForAllow(final URI url) {
		return asyncTaskExecutor.submit(new Callable<Set<HttpMethod>>() {
			public Set<HttpMethod> call(){
				return restTemplate.optionsForAllow(url);
			}
		});
	}

	public <T> Future<ResponseEntity<T>> exchange(final String url,
			final HttpMethod method, final HttpEntity<?> requestEntity,
			final Class<T> responseType, final Object... uriVariables) {
		return asyncTaskExecutor.submit(new Callable<ResponseEntity<T>>() {
			public ResponseEntity<T> call(){
				return restTemplate.exchange(url, method, requestEntity, responseType, uriVariables);
			}
		});	
	}

	public <T> Future<ResponseEntity<T>> exchange(final String url,
			final HttpMethod method, final HttpEntity<?> requestEntity,
			final Class<T> responseType,final  Map<String, ?> uriVariables) {
		return asyncTaskExecutor.submit(new Callable<ResponseEntity<T>>() {
			public ResponseEntity<T> call(){
				return restTemplate.exchange(url, method, requestEntity, responseType, uriVariables);
			}
		});
	}

	public <T> Future<ResponseEntity<T>> exchange(final URI url, final HttpMethod method,
			final HttpEntity<?> requestEntity, final Class<T> responseType) {
		return asyncTaskExecutor.submit(new Callable<ResponseEntity<T>>() {
			public ResponseEntity<T> call(){
				return restTemplate.exchange(url, method, requestEntity, responseType);
			}
		});
	}

	public <T> Future<ResponseEntity<T>> exchange(final String url,
			final HttpMethod method, final HttpEntity<?> requestEntity,
			final ParameterizedTypeReference<T> responseType, final Object... uriVariables) {
		return asyncTaskExecutor.submit(new Callable<ResponseEntity<T>>() {
			public ResponseEntity<T> call(){
				return restTemplate.exchange(url, method, requestEntity, responseType, uriVariables);
			}
		});
	}

	public <T> Future<ResponseEntity<T>> exchange(final String url,
			final HttpMethod method, final  HttpEntity<?> requestEntity,
			final ParameterizedTypeReference<T> responseType,
			final Map<String, ?> uriVariables) {
		return asyncTaskExecutor.submit(new Callable<ResponseEntity<T>>() {
			public ResponseEntity<T> call(){
				return restTemplate.exchange(url, method, requestEntity, responseType, uriVariables);
			}
		});
	}

	public <T> Future<ResponseEntity<T>> exchange(final URI url, final HttpMethod method,
			final HttpEntity<?> requestEntity,	final ParameterizedTypeReference<T> responseType) {
		return asyncTaskExecutor.submit(new Callable<ResponseEntity<T>>() {
			public ResponseEntity<T> call(){
				return restTemplate.exchange(url, method, requestEntity, responseType);
			}
		});
	}

	public <T> Future<T> execute(final String url, final HttpMethod method,
			final RequestCallback requestCallback,
			final ResponseExtractor<T> responseExtractor, final Object... uriVariables) {
		return asyncTaskExecutor.submit(new Callable<T>() {
			public T call(){
				return restTemplate.execute(url, method, requestCallback, responseExtractor, uriVariables);
			}
		});
	}

	public <T> Future<T> execute(final String url, final HttpMethod method,
			final RequestCallback requestCallback,
			final ResponseExtractor<T> responseExtractor, final Map<String, ?> uriVariables) {
		return asyncTaskExecutor.submit(new Callable<T>() {
			public T call(){
				return restTemplate.execute(url, method, requestCallback, responseExtractor, uriVariables);
			}
		});
	}

	public <T> Future<T> execute(final URI url, final HttpMethod method,
			final RequestCallback requestCallback,
			final ResponseExtractor<T> responseExtractor) {
		return asyncTaskExecutor.submit(new Callable<T>() {
			public T call(){
				return restTemplate.execute(url, method, requestCallback, responseExtractor);
			}
		});
	}
}
