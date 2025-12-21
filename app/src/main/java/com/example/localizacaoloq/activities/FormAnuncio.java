package com.example.localizacaoloq.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.localizacaoloq.R;
import com.example.localizacaoloq.Repository.AnuncioRepository;
import com.example.localizacaoloq.Repository.ChaveRepository;
import com.example.localizacaoloq.Repository.LocalRepository;
import com.example.localizacaoloq.model.Anuncio;
import com.example.localizacaoloq.model.Chave;
import com.example.localizacaoloq.model.ListaChave;
import com.example.localizacaoloq.model.Local;
import com.example.localizacaoloq.model.User;

import java.text.SimpleDateFormat;
import java.util.*;

public class FormAnuncio extends AppCompatActivity {
    private ImageButton btnVoltar;
    private Button btnPublicar;

    // Campos do formulário (IDs CORRIGIDOS conforme XML)
    private EditText inputTitulo, inputMensagem;
    private AutoCompleteTextView inputLocal, inputChaveExistente;
    private EditText inputRestricaoValor;
    private EditText inputDataInicio, inputHoraInicio, inputDataFim, inputHoraFim;
    private RadioGroup radioGroupEntrega, radioGroupPolitica;
    private RadioButton radioCentralizado, radioDescentralizado, radioWhitelist, radioBlacklist;
    private Button btnAdicionarRestricao;
    private LinearLayout containerRestricoesSelecionadas;
    private TextView txtSemRestricoes;

    // Repositories
    private AnuncioRepository anuncioRepo;
    private LocalRepository localRepo;
    private ChaveRepository chaveRepo;

    // Dados
    private List<Local> listaLocais = new ArrayList<>();
    private List<Chave> listaChaves = new ArrayList<>();
    private Local localSelecionado;
    private Chave chaveSelecionada;

    // Lista de restrições adicionadas (ListaChave)
    private List<ListaChave> restricoesAdicionadas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_form_anuncio);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        inicializarRepositorios();
        inicializarViews();
        configurarListeners();
        carregarLocais();
        carregarChaves();
    }

    private void inicializarRepositorios() {
        anuncioRepo = AnuncioRepository.getInstance();
        anuncioRepo.setContext(getApplicationContext());
        localRepo = LocalRepository.getInstance();
        chaveRepo = ChaveRepository.getInstance();
    }

    private void inicializarViews() {
        btnVoltar = findViewById(R.id.btn_back);
        btnPublicar = findViewById(R.id.btn_publicar);

        // Campos de texto
        inputTitulo = findViewById(R.id.input_titulo);
        inputMensagem = findViewById(R.id.input_mensagem);
        inputLocal = findViewById(R.id.input_local);

        // Data/Hora SEPARADOS (conforme XML)
        inputDataInicio = findViewById(R.id.input_data_inicio);
        inputHoraInicio = findViewById(R.id.input_hora_inicio);
        inputDataFim = findViewById(R.id.input_data_fim);
        inputHoraFim = findViewById(R.id.input_hora_fim);

        // Restrições (conforme XML)
        inputChaveExistente = findViewById(R.id.input_chave_existente);
        inputRestricaoValor = findViewById(R.id.input_restricao_valor);
        containerRestricoesSelecionadas = findViewById(R.id.container_restricoes_selecionadas);
        txtSemRestricoes = findViewById(R.id.txt_sem_restricoes);

        // Radio groups e buttons
        radioGroupEntrega = findViewById(R.id.radio_group_entrega);
        radioGroupPolitica = findViewById(R.id.radio_group_politica);
        radioCentralizado = findViewById(R.id.radio_centralizado);
        radioDescentralizado = findViewById(R.id.radio_descentralizado);
        radioWhitelist = findViewById(R.id.radio_whitelist);
        radioBlacklist = findViewById(R.id.radio_blacklist);

        // Botões
        btnAdicionarRestricao = findViewById(R.id.btn_adicionar_restricao);
    }

    private void configurarListeners() {
        btnVoltar.setOnClickListener(v -> finish());
        btnPublicar.setOnClickListener(v -> publicarAnuncio());
        btnAdicionarRestricao.setOnClickListener(v -> adicionarRestricao());

        // Listeners para data/hora SEPARADOS
        inputDataInicio.setOnClickListener(v -> mostrarDatePicker(inputDataInicio));
        inputHoraInicio.setOnClickListener(v -> mostrarTimePicker(inputHoraInicio));
        inputDataFim.setOnClickListener(v -> mostrarDatePicker(inputDataFim));
        inputHoraFim.setOnClickListener(v -> mostrarTimePicker(inputHoraFim));

        inputDataInicio.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) mostrarDatePicker(inputDataInicio);
        });
        inputHoraInicio.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) mostrarTimePicker(inputHoraInicio);
        });
        inputDataFim.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) mostrarDatePicker(inputDataFim);
        });
        inputHoraFim.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) mostrarTimePicker(inputHoraFim);
        });
    }

    private void carregarLocais() {
        new Thread(() -> {
            List<Local> locais = localRepo.findAll();
            runOnUiThread(() -> {
                if (locais != null && !locais.isEmpty()) {
                    listaLocais.clear();
                    listaLocais.addAll(locais);

                    List<String> nomesLocais = new ArrayList<>();
                    for (Local local : locais) {
                        nomesLocais.add(local.getNome());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_dropdown_item_1line,
                            nomesLocais
                    );

                    inputLocal.setAdapter(adapter);

                    inputLocal.setOnItemClickListener((parent, view, position, id) -> {
                        localSelecionado = listaLocais.get(position);
                        Toast.makeText(this,
                                "Local: " + localSelecionado.getNome(),
                                Toast.LENGTH_SHORT).show();
                    });
                } else {
                    Toast.makeText(this, "Nenhum local cadastrado", Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    private void carregarChaves() {
        new Thread(() -> {
            List<Chave> chaves = chaveRepo.findAll();
            runOnUiThread(() -> {
                if (chaves != null && !chaves.isEmpty()) {
                    listaChaves.clear();
                    listaChaves.addAll(chaves);

                    List<String> nomesChaves = new ArrayList<>();
                    for (Chave chave : chaves) {
                        nomesChaves.add(chave.getNome());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_dropdown_item_1line,
                            nomesChaves
                    );

                    inputChaveExistente.setAdapter(adapter);

                    inputChaveExistente.setOnItemClickListener((parent, view, position, id) -> {
                        chaveSelecionada = listaChaves.get(position);
                        Toast.makeText(this,
                                "Chave: " + chaveSelecionada.getNome(),
                                Toast.LENGTH_SHORT).show();
                    });
                } else {
                    Toast.makeText(this, "Nenhuma chave cadastrada", Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    private void mostrarDatePicker(final EditText editText) {
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    editText.setText(sdf.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.show();
    }

    private void mostrarTimePicker(final EditText editText) {
        final Calendar calendar = Calendar.getInstance();

        TimePickerDialog timePicker = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    editText.setText(sdf.format(calendar.getTime()));
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        timePicker.show();
    }

    private void adicionarRestricao() {
        if (chaveSelecionada == null) {
            Toast.makeText(this, "Selecione uma chave da lista", Toast.LENGTH_SHORT).show();
            return;
        }

        String valor = inputRestricaoValor.getText().toString().trim();
        if (valor.isEmpty()) {
            inputRestricaoValor.setError("Digite o valor da restrição");
            return;
        }

        ListaChave restricao = new ListaChave(
                chaveSelecionada,
                valor
        );

        restricoesAdicionadas.add(restricao);

        adicionarRestricaoNaLista(chaveSelecionada.getNome(), valor, restricoesAdicionadas.size() - 1);

        inputChaveExistente.setText("");
        inputRestricaoValor.setText("");
        chaveSelecionada = null;

        Toast.makeText(this, "Restrição adicionada", Toast.LENGTH_SHORT).show();
    }

    private void adicionarRestricaoNaLista(String nomeChave, String valor, int position) {
        txtSemRestricoes.setVisibility(View.GONE);

        View itemView = getLayoutInflater().inflate(R.layout.item_atributo, containerRestricoesSelecionadas, false);

        TextView txtChaveNome = itemView.findViewById(R.id.textKey);
        TextView txtRestricaoValor = itemView.findViewById(R.id.textValue);
        ImageButton btnRemover = itemView.findViewById(R.id.btnDelete);

        txtChaveNome.setText(nomeChave);
        txtRestricaoValor.setText("Valor: " + valor);

        btnRemover.setOnClickListener(v -> {
            restricoesAdicionadas.remove(position);
            containerRestricoesSelecionadas.removeView(itemView);

            if (restricoesAdicionadas.isEmpty()) {
                txtSemRestricoes.setVisibility(View.VISIBLE);
            }

            Toast.makeText(this, "Restrição removida", Toast.LENGTH_SHORT).show();
        });

        containerRestricoesSelecionadas.addView(itemView);
    }

    private void publicarAnuncio() {
        if (!validarFormulario()) {
            return;
        }

        String titulo = inputTitulo.getText().toString().trim();
        String mensagem = inputMensagem.getText().toString().trim();
        String modoEntrega = radioCentralizado.isChecked() ? "centralizado" : "descentralizado";
        String politica = radioWhitelist.isChecked() ? "whitelist" : "blacklist";

        Date inicio = combinarDataHora(
                inputDataInicio.getText().toString(),
                inputHoraInicio.getText().toString()
        );
        Date fim = combinarDataHora(
                inputDataFim.getText().toString(),
                inputHoraFim.getText().toString()
        );

        if (inicio == null || fim == null) {
            Toast.makeText(this, "Erro ao processar datas", Toast.LENGTH_LONG).show();
            return;
        }

        if (inicio.after(fim)) {
            Toast.makeText(this, "Data de início deve ser anterior à data de fim", Toast.LENGTH_LONG).show();
            return;
        }

        btnPublicar.setEnabled(false);
        btnPublicar.setText("Publicando...");

        new Thread(() -> {
            User userLogado = anuncioRepo.getCurrentUser();

            if (userLogado == null) {
                runOnUiThread(() -> {
                    btnPublicar.setEnabled(true);
                    btnPublicar.setText("Publicar Anúncio");
                    Toast.makeText(this,
                            "Erro: usuário não identificado. Faça login novamente.",
                            Toast.LENGTH_LONG).show();
                });
                return;
            }

            Anuncio novoAnuncio = new Anuncio(
                    titulo,
                    mensagem,
                    localSelecionado,
                    restricoesAdicionadas,
                    userLogado,
                    modoEntrega,
                    politica,
                    inicio,
                    fim
            );

            Anuncio anuncioCriado = anuncioRepo.create(novoAnuncio);

            runOnUiThread(() -> {
                btnPublicar.setEnabled(true);
                btnPublicar.setText("Publicar Anúncio");

                if (anuncioCriado != null) {
                    Toast.makeText(this, "Anúncio publicado com sucesso!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(this,
                            "Erro ao publicar anúncio. Verifique os dados.",
                            Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    private boolean validarFormulario() {
        if (TextUtils.isEmpty(inputTitulo.getText())) {
            inputTitulo.setError("Título é obrigatório");
            inputTitulo.requestFocus();
            return false;
        }

        if (inputTitulo.getText().toString().trim().length() > 200) {
            inputTitulo.setError("Título muito longo (máx. 200 caracteres)");
            inputTitulo.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(inputMensagem.getText())) {
            inputMensagem.setError("Mensagem é obrigatória");
            inputMensagem.requestFocus();
            return false;
        }

        if (inputMensagem.getText().toString().trim().length() > 1000) {
            inputMensagem.setError("Mensagem muito longa (máx. 1000 caracteres)");
            inputMensagem.requestFocus();
            return false;
        }

        if (localSelecionado == null) {
            inputLocal.setError("Selecione um local da lista");
            Toast.makeText(this, "Selecione um local", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(inputDataInicio.getText())) {
            inputDataInicio.setError("Data de início é obrigatória");
            inputDataInicio.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(inputHoraInicio.getText())) {
            inputHoraInicio.setError("Hora de início é obrigatória");
            inputHoraInicio.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(inputDataFim.getText())) {
            inputDataFim.setError("Data de fim é obrigatória");
            inputDataFim.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(inputHoraFim.getText())) {
            inputHoraFim.setError("Hora de fim é obrigatória");
            inputHoraFim.requestFocus();
            return false;
        }

        return true;
    }

    private Date combinarDataHora(String dataStr, String horaStr) {
        try {
            String dateTimeStr = dataStr + " " + horaStr;
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return sdf.parse(dateTimeStr);
        } catch (Exception e) {
            Log.e("FormAnuncio", "Erro ao parsear data/hora: " + e.getMessage());
            return null;
        }
    }
}