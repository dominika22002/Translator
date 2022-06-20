import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.google.gson.*;
import com.google.gson.reflect.*;

public class MainForm extends JFrame {

    private JPanel panel;
    private JPanel subPanel1;
    private JTextField textField1;
    private JPanel subPanel2;
    private JButton translateButton;
    private JLabel label1;
    private JLabel label2;
    private JTextField textField2;
    private JComboBox comboBox1;
    private JComboBox comboBox2;
    private JCheckBox detectCheckBox;
    private JLabel from;
    private JLabel to;

    private String[] chooseLang = { "en", "fr", "de", "it", "pl", "pt", "es" };

    private static Map<String,String> languageShortcuts = new HashMap<String, String>(){{
        put("en","English");
        put("fr","French");
        put("de","German");
        put("it","Italian");
        put("pl","Polish");
        put("pt","Portuguese");
        put("es","Spanish");
    }};

    private Map<String,String> languageFullNames = new HashMap<String, String>(){{
        put("English","en");
        put("French","fr");
        put("German","de");
        put("Italian","it");
        put("Polish","pl");
        put("Portuguese","pt");
        put("Spanish","es");
    }};


    public MainForm() {
        setSize(450, 300);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Translate api");
        setContentPane(panel);
        setVisible(true);

        for (int i = 0; i < chooseLang.length; i++) {
            comboBox1.addItem(languageShortcuts.get(chooseLang[i]));
        }

        for (int i = 0; i < chooseLang.length; i++) {
            comboBox2.addItem(languageShortcuts.get(chooseLang[i]));
        }

        comboBox2.setSelectedItem("Polish");
        textField2.setEditable(false);

        translateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String fromLang = languageFullNames.get(comboBox1.getSelectedItem().toString());
                String toLang = languageFullNames.get(comboBox2.getSelectedItem().toString());
                Boolean detect = detectCheckBox.isSelected();

                String forTranslate = callApiTranslate(textField1.getText(), fromLang, toLang, detect);
                textField2.setText(forTranslate);
            }
        });

        detectCheckBox.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (detectCheckBox.isSelected())
                {
                    label1.setText("Text to detect:    ");
                    label2.setText("Language Detected:");
                    comboBox1.setVisible(false);
                    comboBox2.setVisible(false);
                    from.setVisible(false);
                    to.setVisible(false);

                }
                else
                {
                    label1.setText("Text to translate:");
                    label2.setText("Translated text:");
                    comboBox1.setVisible(true);
                    comboBox2.setVisible(true);
                    from.setVisible(true);
                    to.setVisible(true);
                }
            }
        });
    }

    public static String callApiTranslate(String forTranslate, String fromLang, String toLang, Boolean detect) {

        try {
            String fromLangEnd = fromLang;
            URL url;
            String data;
            if (detect) {
                url = new URL("https://libretranslate.de/detect");
            }
            else {
                url = new URL("https://libretranslate.de/translate");
            }
            //do wezwania api
            HttpURLConnection http = (HttpURLConnection)url.openConnection();
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.setRequestProperty("accept", "application/json");
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            if (detect) {
                data = "q=" + forTranslate + "&api_key=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx";
            }
            else {
                data = "q=" + forTranslate + "&source=" + fromLangEnd + "&target=" + toLang + "&format=text&api_key=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx";
            }
            //do sprawdzenia poprawnosci zapytania
            byte[] out = data.getBytes(StandardCharsets.UTF_8);

            OutputStream stream = http.getOutputStream();
            stream.write(out);
            System.out.println(http.getResponseCode() + " " + http.getResponseMessage());

            //do wyswietlania zapytania http
            StringBuilder result = new StringBuilder();
            BufferedReader rda = new BufferedReader(new InputStreamReader(http.getInputStream()));
            String linea;
            while((linea = rda.readLine()) != null) {
                result.append(linea);
            }

            if (detect) {
                result.deleteCharAt(0);
                result.deleteCharAt(result.length()-1);
            }
            rda.close();
            http.disconnect();

            System.out.println(result);
            JsonElement root = new JsonParser().parse(result.toString());
            if (detect) {
                Double confidence = root.getAsJsonObject().get("confidence").getAsDouble();
                if (confidence < 0.8)
                {
                    return "To little confidence of the result. Write something else.";
                }
                return languageShortcuts.get(root.getAsJsonObject().get("language").getAsString());
            }
            else {
                return root.getAsJsonObject().get("translatedText").getAsString();
            }
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public static void main(String args[]){

        MainForm frame = new MainForm();
    }
}
