package zachkingcade.dev.reporting.adapter.outbound;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class OutboundConfiguration {

    @Bean
    public RestClient userServiceRestClient(@Value("${reporting.outbound.user-service-base-url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }

    @Bean
    public RestClient ledgerRestClient(@Value("${reporting.outbound.ledger-service-base-url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }
}
