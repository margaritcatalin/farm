package ro.piata.localmarket.config.apidoc.customizer;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.core.Ordered;
import ro.piata.localmarket.config.LocalMarketProperties;

public class LocalMarketOpenApiCustomizer implements OpenApiCustomizer, Ordered {
    public static final int DEFAULT_ORDER = 0;
    private int order = 0;
    private final LocalMarketProperties.ApiDocs properties;

    public LocalMarketOpenApiCustomizer(LocalMarketProperties.ApiDocs properties) {
        this.properties = properties;
    }

    @Override
    public void customise(OpenAPI openAPI) {
        Contact contact = (new Contact()).name(this.properties.getContactName()).url(this.properties.getContactUrl()).email(this.properties.getContactEmail());
        openAPI.info((new Info()).contact(contact).title(this.properties.getTitle()).description(this.properties.getDescription()).version(this.properties.getVersion()).termsOfService(this.properties.getTermsOfServiceUrl()).license((new License()).name(this.properties.getLicense()).url(this.properties.getLicenseUrl())));

        for (LocalMarketProperties.ApiDocs.Server server : this.properties.getServers()) {
            openAPI.addServersItem((new Server()).url(server.getUrl()).description(server.getDescription()));
        }

    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return this.order;
    }
}
