package kektor.innowise.gallery.image.conf;

import kektor.innowise.gallery.image.service.CommentServiceClient;
import kektor.innowise.gallery.image.service.UserServiceClient;
import kektor.innowise.gallery.security.conf.client.ProtectedCommentServiceClient;
import kektor.innowise.gallery.security.conf.client.ProtectedUserServiceClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class WebClientConfig {

    @Bean
    public CommentServiceClient commentServiceClient(@ProtectedCommentServiceClient RestClient commentRestClient) {
        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(commentRestClient))
                .build()
                .createClient(CommentServiceClient.class);
    }

    @Bean
    public UserServiceClient userServiceClient(@ProtectedUserServiceClient RestClient userRestClient) {
        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(userRestClient))
                .build()
                .createClient(UserServiceClient.class);
    }

}
