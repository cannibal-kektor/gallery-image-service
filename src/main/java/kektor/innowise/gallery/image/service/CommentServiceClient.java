package kektor.innowise.gallery.image.service;


import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(url = "/api/comments")
public interface CommentServiceClient {

    @DeleteExchange("/image/{imageId}")
    void deleteImageComments(@PathVariable Long imageId);

}