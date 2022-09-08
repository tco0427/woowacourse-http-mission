package nextstep.jwp.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.coyote.request.HttpRequest;

public class RequestMapping {

    private static Map<String, Controller> controllers = new HashMap<>();

    static {
        controllers.put("/", new RootController());
        controllers.put("/index.html", new RootController());
        controllers.put("/login.html", new LoginController());
        controllers.put("/register.html", new RegisterController());
        controllers.put("/css", new ResourceController());
        controllers.put("/js", new ResourceController());
    }

    public static Optional<Controller> getController(HttpRequest request) {
        final String requestPath = request.getRequestPath();
        return Optional.ofNullable(controllers.get(requestPath));
    }
}
