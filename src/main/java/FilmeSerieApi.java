import org.json.JSONObject;
import org.json.JSONArray;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class FilmeSerieApi {

    private static final String API_KEY = System.getenv("API_KEY");

    public static JSONObject buscar(String nome, String tipo) throws Exception {

        if (API_KEY == null || API_KEY.isEmpty()) {
            throw new RuntimeException("Defina a variável de ambiente API_KEY.");
        }

        HttpClient client = HttpClient.newHttpClient();
        String nomeFormatado = URLEncoder.encode(nome, StandardCharsets.UTF_8);
        String urlBusca;

        if(tipo.equalsIgnoreCase("Filme")){
            urlBusca = "https://api.themoviedb.org/3/search/movie?api_key=" + API_KEY +
                    "&language=pt-BR&query=" + nomeFormatado;
        } else {
            urlBusca = "https://api.themoviedb.org/3/search/tv?api_key=" + API_KEY +
                    "&language=pt-BR&query=" + nomeFormatado;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlBusca))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject jsonBusca = new JSONObject(response.body());

        if(jsonBusca.has("results") && jsonBusca.getJSONArray("results").length() > 0){
            JSONObject primeiro = jsonBusca.getJSONArray("results").getJSONObject(0);
            int id = primeiro.getInt("id");

            String urlDetalhes;

            if(tipo.equalsIgnoreCase("Filme")){
                urlDetalhes = "https://api.themoviedb.org/3/movie/" + id +
                        "?api_key=" + API_KEY + "&language=pt-BR";
            } else {
                urlDetalhes = "https://api.themoviedb.org/3/tv/" + id +
                        "?api_key=" + API_KEY + "&language=pt-BR";
            }

            HttpRequest reqDetalhes = HttpRequest.newBuilder()
                    .uri(URI.create(urlDetalhes))
                    .GET()
                    .build();

            HttpResponse<String> resDetalhes = client.send(reqDetalhes, HttpResponse.BodyHandlers.ofString());
            return new JSONObject(resDetalhes.body());
        }

        return null;
    }
}