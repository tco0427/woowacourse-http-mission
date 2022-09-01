package org.apache.coyote.http11;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import nextstep.jwp.db.InMemoryUserRepository;
import nextstep.jwp.exception.NoSuchUserException;
import nextstep.jwp.exception.UncheckedServletException;
import nextstep.jwp.model.User;
import org.apache.coyote.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Http11Processor implements Runnable, Processor {

    private static final Logger log = LoggerFactory.getLogger(Http11Processor.class);

    private final Socket connection;

    public Http11Processor(final Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        process(connection);
    }

    @Override
    public void process(final Socket connection) {
        try (final var inputStream = connection.getInputStream();
             final var outputStream = connection.getOutputStream();
             final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             final BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

            String responseBody = "Hello world!";

            final HttpRequestHeader httpRequestHeader = makeHttpRequestHeader(bufferedReader);
            String requestUrl = httpRequestHeader.getRequestUrl();

            int index = requestUrl.indexOf("?");
            if (index != -1) {
                String queryString = requestUrl.substring(index + 1);
                requestUrl = requestUrl.substring(0, index);

                final Map<String, String> data = makeDataFromQueryString(queryString);

                final String account = data.get("account");
                final User user = InMemoryUserRepository.findByAccount(account)
                        .orElseThrow(NoSuchUserException::new);
                final String userInformation = user.toString();

                log.info(userInformation);
            }

            if (requestUrl.contains(".html") || requestUrl.contains(".css") || requestUrl.contains(".js")) {
                responseBody = new String(readAllFile(requestUrl), UTF_8);
            }

            if (requestUrl.equals("/")) {
                responseBody = new String(readDefaultFile(), UTF_8);
            }

            final String response = makeResponse(responseBody, ContentType.from(requestUrl));
            outputStream.write(response.getBytes());
            outputStream.flush();
        } catch (IOException | UncheckedServletException e) {
            log.error(e.getMessage(), e);
        }
    }

    private HttpRequestHeader makeHttpRequestHeader(final BufferedReader bufferedReader) throws IOException {
        final String httpStartLine = bufferedReader.readLine();

        final Map<String, String> httpHeaderLines = new HashMap<>();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.isBlank()) {
                break;
            }

            final String[] header = line.split(": ");
            httpHeaderLines.put(header[0], header[1]);
        }

        return new HttpRequestHeader(httpStartLine, httpHeaderLines);
    }

    private HashMap<String, String> makeDataFromQueryString(final String queryString) {
        final HashMap<String, String> data = new HashMap<>();
        final String[] queries = queryString.split("&");

        for (String query : queries) {
            final String[] values = query.split("=");
            data.put(values[0], values[1]);
        }

        return data;
    }

    private static byte[] readAllFile(final String requestUrl) throws IOException {
        final URL resourceUrl = ClassLoader.getSystemResource("static" + requestUrl);
        final Path path = new File(resourceUrl.getPath()).toPath();
        return Files.readAllBytes(path);
    }

    private static byte[] readDefaultFile() throws IOException {
        final URL resourceUrl = ClassLoader.getSystemResource("static/index.html");
        final Path path = new File(resourceUrl.getPath()).toPath();
        return Files.readAllBytes(path);
    }

    private static String makeResponse(final String responseBody, final String contentType) {
        return String.join("\r\n",
                "HTTP/1.1 200 OK ",
                "Content-Type: " + contentType + ";charset=utf-8 ",
                "Content-Length: " + responseBody.getBytes().length + " ",
                "",
                responseBody);
    }
}
