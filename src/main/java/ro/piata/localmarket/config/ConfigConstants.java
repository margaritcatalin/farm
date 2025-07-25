package ro.piata.localmarket.config;

public interface ConfigConstants {
    String SPRING_PROFILE_DEVELOPMENT = "dev";
    String SPRING_PROFILE_TEST = "test";
    String SPRING_PROFILE_E2E = "e2e";
    String SPRING_PROFILE_PRODUCTION = "prod";
    String SPRING_PROFILE_CLOUD = "cloud";
    String SPRING_PROFILE_HEROKU = "heroku";
    String SPRING_PROFILE_API_DOCS = "api-docs";
    String SPRING_PROFILE_K8S = "k8s";

    public static final String LOGIN_REGEX = "^(?>[a-zA-Z0-9!$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*)|(?>[_.@A-Za-z0-9-]+)$";

    public static final String SYSTEM = "system";
    public static final String DEFAULT_LANGUAGE = "en";

}
