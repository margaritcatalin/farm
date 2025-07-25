package ro.piata.localmarket.config.h2;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;

public class H2ConfigurationHelper {

    private H2ConfigurationHelper() {
        throw new AssertionError("This class should not be instantiated.");
    }

    public static Object createServer() throws SQLException {
        return createServer("9092");
    }

    public static Object createServer(String port) throws SQLException {
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Class<?> serverClass = Class.forName("org.h2.tools.Server", true, loader);
            Method createServer = serverClass.getMethod("createTcpServer", String[].class);
            String[] args = new String[]{"-tcp", "-tcpAllowOthers", "-tcpPort", port};
            return createServer.invoke(null, (Object) args); // ← fixed: wrap as single argument
        } catch (LinkageError | ClassNotFoundException e) {
            throw new RuntimeException("Failed to load and initialize org.h2.tools.Server", e);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException("Failed to get method org.h2.tools.Server.createTcpServer()", e);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException("Failed to invoke org.h2.tools.Server.createTcpServer()", e);
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t instanceof SQLException) {
                throw (SQLException) t;
            } else {
                throw new RuntimeException("Unchecked exception in org.h2.tools.Server.createTcpServer()", t);
            }
        }
    }

    public static void initH2Console() {
        initH2Console("src/main/resources");
    }

    static void initH2Console(String propertiesLocation) {
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Class<?> serverClass = Class.forName("org.h2.tools.Server", true, loader);
            Method createWebServer = serverClass.getMethod("createWebServer", String[].class);
            Method start = serverClass.getMethod("start");
            String[] args = new String[]{"-properties", propertiesLocation};
            Object server = createWebServer.invoke(null, (Object) args);
            start.invoke(server);
        } catch (Exception e) {
            throw new RuntimeException("Failed to start H2 webserver console", e);
        }
    }

    public static void initH2Console(ServletContext servletContext) {
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Class<?> servletClass = Class.forName("org.h2.server.web.JakartaWebServlet", true, loader);
            Servlet servlet = (Servlet) servletClass.getDeclaredConstructor().newInstance();
            ServletRegistration.Dynamic h2ConsoleServlet = servletContext.addServlet("H2Console", servlet);
            h2ConsoleServlet.addMapping("/h2-console/*");
            h2ConsoleServlet.setInitParameter("-properties", "src/main/resources/");
            h2ConsoleServlet.setLoadOnStartup(1);
        } catch (LinkageError | NoSuchMethodException | InvocationTargetException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to load and initialize org.h2.server.web.JakartaWebServlet", e);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Failed to instantiate org.h2.server.web.JakartaWebServlet", e);
        }
    }
}
