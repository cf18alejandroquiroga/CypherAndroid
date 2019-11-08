package com.example.filewriter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    TextView outputText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button createButton = findViewById(R.id.createButton);
        final Button decryptButton = findViewById(R.id.decryptButton);
        final EditText inputText = findViewById(R.id.inputText);
        final EditText inputTitle = findViewById(R.id.inputTitle);
        outputText = findViewById(R.id.outputText);

        createButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String title = inputTitle.getText().toString();
                try {
                    if(title.length() == 0) title = "default";
                    createFile(inputText, title);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                inputText.setText("");
            }
        });

        decryptButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    String title = inputTitle.getText().toString();
                    decryptFile(title);
                }
                catch(Exception ex){}
            }
        });
    }

    private void decryptFile(String title){

        if (title.equals("")) title = "default";
        File f = new File(getFilesDir(),title + ".xml");
        outputText.setText(getInfoFromFile(f));
    }

    private void createFile(EditText input, String title) throws IOException {

        File f = new File(getFilesDir(),title + ".xml");

        int nextData = getIdOfTheInsert(f);

        String inputText = input.getText().toString();
        String encryptedText = processInfoToFile(inputText, nextData);
        String currentInfo = textForAppending(f, countLines(f));

        FileOutputStream fOut = openFileOutput(title + ".xml",
                MODE_PRIVATE);
        OutputStreamWriter osw = new OutputStreamWriter(fOut);

        osw.write(currentInfo + encryptedText);
        osw.flush();
        osw.close();
    }

    private String processInfoToFile(String text, int nextData){

        try {
            final String xmlHead = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<content_file>";

            if (nextData > 1){
                return getEntryData(text, nextData);
            }
            else {
                return xmlHead + getEntryData(text, nextData);
            }
        }
        catch(Exception ex){
            return "";
        }
    }

    private String getEntryData(String text, int dataId){
        Long tsLong = System.currentTimeMillis();
        String ts = formatDate(tsLong);

        return  "\n\t<data id=\"" + dataId + "\">" +
                "\n\t\t<time>" + ts + "</time>" +
                "\n\t\t<text>" + text + "</text>" +
                "\n\t\t<cypher_text>" + encryptText(text) + "</cypher_text>" +
                "\n\t</data>\n</content_file>";
    }

    private String formatDate(long value){

        Date date = new Date(value);
        SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yy hh:mm:ss");
        return df2.format(date);
    }

    private int getIdOfTheInsert(File file){
        try {
            String line;
            int numberLines = countLines(file);
            int id = 1;
            FileReader fr = new FileReader(file);
            LineNumberReader lnr = new LineNumberReader(fr);

            for (int i = 0; i < numberLines; i++){
                line = lnr.readLine();
                if (line.contains("<data id=\"")){
                    id = Integer.parseInt(line.split("\"")[1]);
                }
            }
            Log.d("TEST ID", String.valueOf(id));
            return id + 1;
        }
        catch(Exception ex) {
            //File doesn't exist, it returns 1.
            return 1;
        }
    }

    private String getInfoFromFile(File file){
        try{
            String line, info = "";
            int numberLines = countLines(file);
            FileReader fr = new FileReader(file);
            LineNumberReader lnr = new LineNumberReader(fr);

            for (int i = 0; i < numberLines; i++){
                line = lnr.readLine();
                if (line.contains("<text>") && line.contains("</text>")) info += line + "\n";
                if (line.contains("<cypher_text>") && line.contains("</cypher_text>")) info += line + "\n";

                info = info.replace("<text>","text: ")
                        .replace("</text>","")
                        .replace("<cypher_text>", "cypher_text: ")
                        .replace("</cypher_text>", "");
            }

            return info;
        }
        catch(Exception ex) {
            //File doesn't exist, it returns 1.
            return "ERROR";
        }
    }

    private int countLines(File file){
        int n = 0;
        try {
            FileReader fr = new FileReader(file);
            LineNumberReader lnr = new LineNumberReader(fr);

            while (lnr.readLine() != null){ n++;}
            lnr.close();
            return n;
        }
        catch(Exception ex) {
            //File doesn't exist, it returns 0.
            return 0;
        }
    }

    private String encryptText(String text){
        RSA rsa = new RSA();
        try{
            rsa.setContext(getBaseContext());
            rsa.genKeyPair(1024);
            rsa.saveToDiskPrivateKey("rsa.pri");
            rsa.saveToDiskPublicKey("rsa.pub");
            return rsa.Encrypt(text);
        }
        catch(Exception ex){
            return null;
        }
    }

    private String textForAppending(File file, int lines){
        String text = "";

        try {
            FileReader fr = new FileReader(file);
            LineNumberReader lnr = new LineNumberReader(fr);

            text += lnr.readLine();
            for(int i = 2; i < lines; i++) text += "\n" + lnr.readLine();

            lnr.close();
            Log.d("TEST TEXT", text);
            return text;
        }
        catch(Exception ex) {
            return "";
        }
    }
}
