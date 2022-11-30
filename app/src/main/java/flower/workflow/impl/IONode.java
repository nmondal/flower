package flower.workflow.impl;

import zoomba.lang.core.types.ZTypes;

import java.io.IOException;
import java.net.Authenticator;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

public interface IONode extends MapDependencyWorkFlow.MapFNode {

    String protocol();

    default Map ioConfiguration(){
        return (Map<String, ?>) config().getOrDefault( protocol(), Collections.emptyMap());
    }

    interface HTTPLike extends IONode {

        String URL = "url" ;

        String VERB = "verb" ;

        String DATA = "data" ;

        String HEADER = "header" ;

        @Override
        default String protocol() {
            if ( config().containsKey("http") ) return "http" ;
            if ( config().containsKey("https") ) return "https" ;
            return "";
        }


        default String data() {
            return ZTypes.jsonString(config().getOrDefault(DATA, ""));
        }

        default String verb() {
            return config().getOrDefault(VERB, "").toString();
        }

        default Map<String,String> headers() {
            return (Map)config().getOrDefault(HEADER, Collections.emptyMap());
        }

        default <T> T transform(Map<String,Object> params, T toBeTransformed){
            return toBeTransformed;
        }

        @Override
        default Function<Map<String, Object>, Object> body() {
            final String base = ioConfiguration().getOrDefault(URL, "").toString();
            return params -> {
                final String url = protocol() + "://" + transform( params, base);
                final String data = transform(params,data());
                final Map<String,String> headers = transform(params,headers());
                HttpClient client = HttpClient.newBuilder()
                        .version(HttpClient.Version.HTTP_2)
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .connectTimeout(Duration.ofSeconds(20))
                        .authenticator(Authenticator.getDefault())
                        .build();

                HttpRequest request = HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create(url))
                        .setHeader("User-Agent", "Java 11 HttpClient Bot") // add request header
                        .build();

                HttpResponse<String> response = null;
                try {
                    response = client.send(request, HttpResponse.BodyHandlers.ofString());
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(response.statusCode());
                System.out.println(response.body());
                return response;
            };
        }
    }

}
