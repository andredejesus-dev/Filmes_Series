import javax.swing.*;
import java.awt.*;
import org.json.JSONObject;
import java.net.URL;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class InterfaceFilmesSeries extends JFrame {

    private JTextField inputNome = new JTextField();
    private JComboBox<String> tipoBox = new JComboBox<>(new String[]{"Filme", "Série"});
    private JLabel capaLabel = new JLabel();
    private JLabel labelStatus = new JLabel("Aguardando busca...");

    public InterfaceFilmesSeries() {
        setTitle("Consulta de Filmes e Séries");
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Nome:"), gbc);

        gbc.gridx = 1;
        add(inputNome, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Tipo:"), gbc);

        gbc.gridx = 1;
        add(tipoBox, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JButton btn = new JButton("Consultar");
        add(btn, gbc);

        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        capaLabel.setPreferredSize(new Dimension(200, 300));
        capaLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(capaLabel, gbc);

        gbc.gridy = 4;
        labelStatus.setHorizontalAlignment(SwingConstants.CENTER);
        labelStatus.setVerticalAlignment(SwingConstants.TOP);
        add(labelStatus, gbc);

        btn.addActionListener(e -> {
            String nome = inputNome.getText().trim();
            String tipo = tipoBox.getSelectedItem().toString();

            labelStatus.setText("Buscando...");
            capaLabel.setIcon(null);

            new SwingWorker<JSONObject, Void>() {
                @Override
                protected JSONObject doInBackground() throws Exception {
                    return FilmeSerieApi.buscar(nome, tipo);
                }

                @Override
                protected void done() {
                    try {
                        JSONObject dados = get();
                        if (dados != null) {
                            mostrarResultado(dados, tipo);
                        } else {
                            labelStatus.setText("Nenhum resultado encontrado.");
                        }
                    } catch (Exception ex) {
                        labelStatus.setText("Erro na consulta.");
                    }
                }
            }.execute();
        });

        setVisible(true);
    }

    private void mostrarResultado(JSONObject dados, String tipo) {

        String nome = dados.has("title") ? dados.getString("title") :
                dados.has("name") ? dados.getString("name") : "N/A";

        String dataRaw = dados.has("release_date") ? dados.getString("release_date") :
                dados.has("first_air_date") ? dados.getString("first_air_date") : "N/A";

        String data = "N/A";
        if(!dataRaw.equals("N/A") && dataRaw.contains("-")){
            String[] partes = dataRaw.split("-");
            data = partes[2] + "/" + partes[1] + "/" + partes[0];
        }

        String nota = dados.has("vote_average") ? String.valueOf(dados.getDouble("vote_average")) : "N/A";

        String texto = "<html><div style='text-align:center;'>" +
                "🎬 " + nome + "<br>" +
                "📅 " + data + "<br>" +
                "⭐ " + nota;

        if(tipo.equalsIgnoreCase("Série")){
            texto += "<br>📺 Temp: " + dados.optInt("number_of_seasons", 0);
            texto += "<br>🎞️ Ep: " + dados.optInt("number_of_episodes", 0);
        }

        texto += "</div></html>";

        labelStatus.setText(texto);

        String path = dados.optString("poster_path", "");
        if(!path.isEmpty()){
            try {
                URL url = new URL("https://image.tmdb.org/t/p/w500" + path);
                BufferedImage img = ImageIO.read(url);
                ImageIcon icon = new ImageIcon(
                        img.getScaledInstance(200, 300, Image.SCALE_SMOOTH)
                );
                capaLabel.setIcon(icon);
            } catch (Exception e){
                capaLabel.setIcon(null);
            }
        } else {
            capaLabel.setIcon(null);
        }
    }
}