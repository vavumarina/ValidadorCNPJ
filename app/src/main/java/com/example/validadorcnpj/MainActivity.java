package com.example.validadorcnpj;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private EditText etCnpj;
    private TextView tvResultadoCnpj;
    private Button btnValidarCnpj, btnConsultarCpf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etCnpj = findViewById(R.id.etCnpj);
        tvResultadoCnpj = findViewById(R.id.tvResultadoCnpj);
        btnValidarCnpj = findViewById(R.id.btnValidarCnpj);
        btnConsultarCpf = findViewById(R.id.btnConsultarCpf);

        btnValidarCnpj.setOnClickListener(v -> {
            String cnpj = etCnpj.getText().toString().trim();
            if (cnpj.length() == 14) {
                new ConsultaCNPJTask().execute(cnpj);
            } else {
                Toast.makeText(MainActivity.this, "CNPJ deve ter 14 dígitos", Toast.LENGTH_SHORT).show();
            }
        });

        btnConsultarCpf.setOnClickListener(v -> consultarCPF());
    }

    private void consultarCPF() {
        String url = "https://servicos.receita.fazenda.gov.br/Servicos/CPF/ConsultaSituacao/ConsultaPublica.asp";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    private class ConsultaCNPJTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String cnpj = params[0];
            String urlString = "https://www.receitaws.com.br/v1/cnpj/" + cnpj;

            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();
                return response.toString();

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject json = new JSONObject(result);

                    if (json.has("status") && json.getString("status").equals("ERROR")) {
                        Toast.makeText(MainActivity.this, "CNPJ inválido ou não encontrado", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    StringBuilder dados = new StringBuilder();
                    dados.append("Situação: ").append(json.getString("situacao")).append("\n\n");
                    dados.append("Razão Social: ").append(json.getString("nome")).append("\n\n");

                    if (json.has("fantasia") && !json.isNull("fantasia")) {
                        dados.append("Nome Fantasia: ").append(json.getString("fantasia")).append("\n\n");
                    }

                    dados.append("Endereço:\n");
                    dados.append(json.getString("logradouro")).append(", ");
                    dados.append(json.getString("numero")).append("\n");
                    dados.append(json.getString("bairro")).append("\n");
                    dados.append(json.getString("municipio")).append(" - ");
                    dados.append(json.getString("uf")).append("\n");
                    dados.append("CEP: ").append(json.getString("cep"));

                    tvResultadoCnpj.setText(dados.toString());
                    tvResultadoCnpj.setVisibility(View.VISIBLE);

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Erro ao processar os dados", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Falha na conexão", Toast.LENGTH_SHORT).show();
            }
        }
    }
}