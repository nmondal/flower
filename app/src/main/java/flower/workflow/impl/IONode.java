package flower.workflow.impl;

import zoomba.lang.core.types.ZTypes;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
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

        static Object transformResponse( HttpResponse<?> response){
            List<String> types = response.headers().allValues("content-type");
            if ( types.get(0).toLowerCase(Locale.ROOT).contains("json")){
                return ZTypes.json(response.body().toString());
            }
            return response;
        }

        @Override
        default Function<Map<String, Object>, Object> body() {
            final String base = ioConfiguration().getOrDefault(URL, "").toString();
            return params -> {
                final String url = protocol() + "://" + DynamicExecution.ZMB.transform( base, params);
                final String data = DynamicExecution.ZMB.transform(data(), params);
                final Map<String,String> headers = DynamicExecution.ZMB.transform(headers(), params);
                HttpClient client = HttpClient.newBuilder()
                        .version(HttpClient.Version.HTTP_2)
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .connectTimeout(Duration.ofSeconds(20))
                        .build();

                HttpRequest request = HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create(url))
                        .setHeader("User-Agent", "Java 11 HttpClient Bot") // add request header
                        .build();

                HttpResponse<?> response = null;
                try {
                    response = client.send(request, HttpResponse.BodyHandlers.ofString());
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return transformResponse(response);
            };
        }
    }
}
