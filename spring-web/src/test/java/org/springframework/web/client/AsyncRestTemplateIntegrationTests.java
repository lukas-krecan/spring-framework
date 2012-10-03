/*
 * Copyright 2002-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.client;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.internal.ArrayComparisonFailure;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.FreePortScanner;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/** @author Arjen Poutsma */
public class AsyncRestTemplateIntegrationTests {

	private AsyncRestTemplate template;

	private static Server jettyServer;

	private static String helloWorld = "H\u00e9llo W\u00f6rld";

	private static String baseUrl;

	private static MediaType contentType;

	@BeforeClass
	public static void startJettyServer() throws Exception {
		int port = FreePortScanner.getFreePort();
		jettyServer = new Server(port);
		baseUrl = "http://localhost:" + port;
		ServletContextHandler handler = new ServletContextHandler();
		byte[] bytes = helloWorld.getBytes("UTF-8");
		contentType = new MediaType("text", "plain", Collections.singletonMap("charset", "UTF-8"));
		handler.addServlet(new ServletHolder(new GetServlet(bytes, contentType)), "/get");
		handler.addServlet(new ServletHolder(new GetServlet(new byte[0], contentType)), "/get/nothing");
		handler.addServlet(new ServletHolder(new GetServlet(bytes, null)), "/get/nocontenttype");
		handler.addServlet(
				new ServletHolder(new PostServlet(helloWorld, baseUrl + "/post/1", bytes, contentType)),
				"/post");
		handler.addServlet(new ServletHolder(new StatusCodeServlet(204)), "/status/nocontent");
		handler.addServlet(new ServletHolder(new StatusCodeServlet(304)), "/status/notmodified");
		handler.addServlet(new ServletHolder(new ErrorServlet(404)), "/status/notfound");
		handler.addServlet(new ServletHolder(new ErrorServlet(500)), "/status/server");
		handler.addServlet(new ServletHolder(new UriServlet()), "/uri/*");
		handler.addServlet(new ServletHolder(new MultipartServlet()), "/multipart");
		jettyServer.setHandler(handler);
		jettyServer.start();
	}

	@Before
	public void createTemplate() {
		template = new AsyncRestTemplate(new RestTemplate(new HttpComponentsClientHttpRequestFactory()), new SimpleAsyncTaskExecutor());
	}

	@AfterClass
	public static void stopJettyServer() throws Exception {
		if (jettyServer != null) {
			jettyServer.stop();
		}
	}

	@Test
	public void getString() throws InterruptedException, ExecutionException {
		Future<String> s = template.getForObject(baseUrl + "/{method}", String.class, "get");
		assertEquals("Invalid content", helloWorld, s.get());
	}

	@Test
	public void getEntity() throws InterruptedException, ExecutionException {
		Future<ResponseEntity<String>> entityFuture = template.getForEntity(baseUrl + "/{method}", String.class, "get");
		ResponseEntity<String> entity = entityFuture.get();
		assertEquals("Invalid content", helloWorld, entity.getBody());
		assertFalse("No headers", entity.getHeaders().isEmpty());
		assertEquals("Invalid content-type", contentType, entity.getHeaders().getContentType());
		assertEquals("Invalid status code", HttpStatus.OK, entity.getStatusCode());
	}

	@Test
	public void getNoResponse() throws InterruptedException, ExecutionException {
		Future<String> s = template.getForObject(baseUrl + "/get/nothing", String.class);
		assertNull("Invalid content", s.get());
	}

	@Test
	public void getNoContentTypeHeader() throws UnsupportedEncodingException, ArrayComparisonFailure, InterruptedException, ExecutionException {
		Future<byte[]> bytes = template.getForObject(baseUrl + "/get/nocontenttype", byte[].class);
		assertArrayEquals("Invalid content", helloWorld.getBytes("UTF-8"), bytes.get());
	}

	@Test
	public void getNoContent() throws InterruptedException, ExecutionException {
		Future<String> s = template.getForObject(baseUrl + "/status/nocontent", String.class);
		assertNull("Invalid content", s.get());

		Future<ResponseEntity<String>> entity = template.getForEntity(baseUrl + "/status/nocontent", String.class);
		assertEquals("Invalid response code", HttpStatus.NO_CONTENT, entity.get().getStatusCode());
		assertNull("Invalid content", entity.get().getBody());
	}

	@Test
	public void getNotModified() throws InterruptedException, ExecutionException {
		String s = template.getForObject(baseUrl + "/status/notmodified", String.class).get();
		assertNull("Invalid content", s);

		ResponseEntity<String> entity = template.getForEntity(baseUrl + "/status/notmodified", String.class).get();
		assertEquals("Invalid response code", HttpStatus.NOT_MODIFIED, entity.getStatusCode());
		assertNull("Invalid content", entity.getBody());
	}

	@Test
	public void postForLocation() throws URISyntaxException, InterruptedException, ExecutionException {
		URI location = template.postForLocation(baseUrl + "/{method}", helloWorld, "post").get();
		assertEquals("Invalid location", new URI(baseUrl + "/post/1"), location);
	}

	@Test
	public void postForLocationEntity() throws URISyntaxException, InterruptedException, ExecutionException {
		HttpHeaders entityHeaders = new HttpHeaders();
		entityHeaders.setContentType(new MediaType("text", "plain", Charset.forName("ISO-8859-15")));
		HttpEntity<String> entity = new HttpEntity<String>(helloWorld, entityHeaders);
		URI location = template.postForLocation(baseUrl + "/{method}", entity, "post").get();
		assertEquals("Invalid location", new URI(baseUrl + "/post/1"), location);
	}

	@Test
	public void postForObject() throws URISyntaxException, InterruptedException, ExecutionException {
		String s = template.postForObject(baseUrl + "/{method}", helloWorld, String.class, "post").get();
		assertEquals("Invalid content", helloWorld, s);
	}

	@Test
	public void notFound() throws InterruptedException {
		try {
			Future<Object> future = template.execute(baseUrl + "/status/notfound", HttpMethod.GET, null, null);
			future.get();
			fail("HttpClientErrorException expected");
		}
		catch (ExecutionException e) {
			HttpClientErrorException ex = (HttpClientErrorException) e.getCause();
			assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
			assertNotNull(ex.getStatusText());
			assertNotNull(ex.getResponseBodyAsString());
		}
	}

	@Test
	public void serverError() throws InterruptedException {
		try {
			Future<Object> future = template.execute(baseUrl + "/status/server", HttpMethod.GET, null, null);
			future.get();
			fail("HttpServerErrorException expected");
		}
		catch (ExecutionException e) {
			HttpServerErrorException ex = (HttpServerErrorException) e.getCause();
			assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
			assertNotNull(ex.getStatusText());
			assertNotNull(ex.getResponseBodyAsString());
		}
	}

	@Test
	public void optionsForAllow() throws URISyntaxException, InterruptedException, ExecutionException {
		Set<HttpMethod> allowed = template.optionsForAllow(new URI(baseUrl + "/get")).get();
		assertEquals("Invalid response",
				EnumSet.of(HttpMethod.GET, HttpMethod.OPTIONS, HttpMethod.HEAD, HttpMethod.TRACE), allowed);
	}

	@Test
	public void uri() throws InterruptedException, URISyntaxException, ExecutionException {
		String result = template.getForObject(baseUrl + "/uri/{query}", String.class, "Z\u00fcrich").get();
		assertEquals("Invalid request URI", "/uri/Z%C3%BCrich", result);

		result = template.getForObject(baseUrl + "/uri/query={query}", String.class, "foo@bar").get();
		assertEquals("Invalid request URI", "/uri/query=foo@bar", result);

		result = template.getForObject(baseUrl + "/uri/query={query}", String.class, "T\u014dky\u014d").get();
		assertEquals("Invalid request URI", "/uri/query=T%C5%8Dky%C5%8D", result);
	}

	@Test
	public void multipart() throws UnsupportedEncodingException {
		MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
		parts.add("name 1", "value 1");
		parts.add("name 2", "value 2+1");
		parts.add("name 2", "value 2+2");
		Resource logo = new ClassPathResource("/org/springframework/http/converter/logo.jpg");
		parts.add("logo", logo);

		template.postForLocation(baseUrl + "/multipart", parts);
	}

	@Test
	public void exchangeGet() throws Exception {
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.set("MyHeader", "MyValue");
		HttpEntity<?> requestEntity = new HttpEntity(requestHeaders);
		ResponseEntity<String> response =
				template.exchange(baseUrl + "/{method}", HttpMethod.GET, requestEntity, String.class, "get").get();
		assertEquals("Invalid content", helloWorld, response.getBody());
	}

	@Test
	public void exchangePost() throws Exception {
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.set("MyHeader", "MyValue");
		requestHeaders.setContentType(MediaType.TEXT_PLAIN);
		HttpEntity<String> requestEntity = new HttpEntity<String>(helloWorld, requestHeaders);
		HttpEntity<Void> result = template.exchange(baseUrl + "/{method}", HttpMethod.POST, requestEntity, Void.class, "post").get();
		assertEquals("Invalid location", new URI(baseUrl + "/post/1"), result.getHeaders().getLocation());
		assertFalse(result.hasBody());
	}

	/** Servlet that sets the given status code. */
	private static class StatusCodeServlet extends GenericServlet {

		private final int sc;

		private StatusCodeServlet(int sc) {
			this.sc = sc;
		}

		@Override
		public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
			((HttpServletResponse) response).setStatus(sc);
		}
	}

	/** Servlet that returns an error message for a given status code. */
	private static class ErrorServlet extends GenericServlet {

		private final int sc;

		private ErrorServlet(int sc) {
			this.sc = sc;
		}

		@Override
		public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
			((HttpServletResponse) response).sendError(sc);
		}
	}

	private static class GetServlet extends HttpServlet {

		private final byte[] buf;

		private final MediaType contentType;

		private GetServlet(byte[] buf, MediaType contentType) {
			this.buf = buf;
			this.contentType = contentType;
		}

		@Override
		protected void doGet(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			if (contentType != null) {
				response.setContentType(contentType.toString());
			}
			response.setContentLength(buf.length);
			FileCopyUtils.copy(buf, response.getOutputStream());
		}
	}

	private static class PostServlet extends HttpServlet {

		private final String s;

		private final String location;

		private final byte[] buf;

		private final MediaType contentType;

		private PostServlet(String s, String location, byte[] buf, MediaType contentType) {
			this.s = s;
			this.location = location;
			this.buf = buf;
			this.contentType = contentType;
		}

		@Override
		protected void doPost(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			assertTrue("Invalid request content-length", request.getContentLength() > 0);
			assertNotNull("No content-type", request.getContentType());
			String body = FileCopyUtils.copyToString(request.getReader());
			assertEquals("Invalid request body", s, body);
			response.setStatus(HttpServletResponse.SC_CREATED);
			response.setHeader("Location", location);
			response.setContentLength(buf.length);
			response.setContentType(contentType.toString());
			FileCopyUtils.copy(buf, response.getOutputStream());
		}
	}

	private static class UriServlet extends HttpServlet {

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.setContentType("text/plain");
			resp.setCharacterEncoding("UTF-8");
			resp.getWriter().write(req.getRequestURI());
		}
	}

	private static class MultipartServlet extends HttpServlet {

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			assertTrue(ServletFileUpload.isMultipartContent(req));
			FileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
			try {
				List items = upload.parseRequest(req);
				assertEquals(4, items.size());
				FileItem item = (FileItem) items.get(0);
				assertTrue(item.isFormField());
				assertEquals("name 1", item.getFieldName());
				assertEquals("value 1", item.getString());

				item = (FileItem) items.get(1);
				assertTrue(item.isFormField());
				assertEquals("name 2", item.getFieldName());
				assertEquals("value 2+1", item.getString());

				item = (FileItem) items.get(2);
				assertTrue(item.isFormField());
				assertEquals("name 2", item.getFieldName());
				assertEquals("value 2+2", item.getString());

				item = (FileItem) items.get(3);
				assertFalse(item.isFormField());
				assertEquals("logo", item.getFieldName());
				assertEquals("logo.jpg", item.getName());
				assertEquals("image/jpeg", item.getContentType());
			}
			catch (FileUploadException ex) {
				throw new ServletException(ex);
			}

		}
	}

}
