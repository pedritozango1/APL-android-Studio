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
import com.example.localizacaoloq.Repository.LocalRepository;
import com.example.localizacaoloq.model.Anuncio;
import com.example.localizacaoloq.model.Local;
import com.example.localizacaoloq.model.User;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public class FormAnuncio extends AppCompatActivity {
    private ImageButton btnVoltar;
    private Button btnPublicar;

    // Campos do formulário
    private EditText inputTitulo, inputMensagem, inputInicio, inputFim;
    private AutoCompleteTextView inputLocal, inputRestricoesExistentes;
    private EditText inputRestricaoChave, inputRestricaoValor;
    private RadioGroup radioGroupEntrega, radioGroupPolitica;
    private RadioButton radioCentralizado, radioDescentralizado, radioWhitelist, radioBlacklist;
    private Button btnAdicionarRestricao;

    private AnuncioRepository anuncioRepo;
    private LocalRepository localRepo;
    private List<Local> listaLocais;
    private ArrayAdapter<Local> localAdapter;
    private Local localSelecionado; // IMPORTANTE: Guardar o local selecionado

    // Para armazenar restrições temporárias
    private Map<String, String> restricoes = new HashMap<>();

    // Pattern para validação de data/hora
    private static final Pattern DATE_TIME_PATTERN = Pattern.compile("^\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}$");

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
    }

    private void inicializarRepositorios() {
        anuncioRepo = AnuncioRepository.getInstance();
        anuncioRepo.setContext(getApplicationContext()); // IMPORTANTE: Definir contexto
        localRepo = LocalRepository.getInstance();
        listaLocais = new ArrayList<>();
    }

    private void inicializarViews() {
        btnVoltar = findViewById(R.id.btn_back);
        btnPublicar = findViewById(R.id.btn_publicar);

        // Campos de texto
        inputTitulo = findViewById(R.id.input_titulo);
        inputMensagem = findViewById(R.id.input_mensagem);
        inputLocal = findViewById(R.id.input_local);
        inputInicio = findViewById(R.id.input_inicio);
        inputFim = findViewById(R.id.input_fim);
        inputRestricoesExistentes = findViewById(R.id.input_restricoes_existentes);
        inputRestricaoChave = findViewById(R.id.input_restricao_chave);
        inputRestricaoValor = findViewById(R.id.input_restricao_valor);

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
        btnVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnPublicar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publicarAnuncio();
            }
        });

        btnAdicionarRestricao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adicionarRestricao();
            }
        });

        // Listeners para data/hora
        inputInicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDateTimePicker(inputInicio);
            }
        });

        inputFim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDateTimePicker(inputFim);
            }
        });

        inputInicio.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) mostrarDateTimePicker(inputInicio);
            }
        });

        inputFim.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) mostrarDateTimePicker(inputFim);
            }
        });
    }

    private void carregarLocais() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Local> locais = localRepo.findAll();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (locais != null && !locais.isEmpty()) {
                            listaLocais.clear();
                            listaLocais.addAll(locais);

                            // CORRIGIDO: Usar ArrayAdapter com conversão para String
                            List<String> nomesLocais = new ArrayList<>();
                            for (Local local : locais) {
                                nomesLocais.add(local.getNome());
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    FormAnuncio.this,
                                    android.R.layout.simple_dropdown_item_1line,
                                    nomesLocais
                            );

                            inputLocal.setAdapter(adapter);

                            inputLocal.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    localSelecionado = listaLocais.get(position);
                                    Toast.makeText(FormAnuncio.this,
                                            "Local selecionado: " + localSelecionado.getNome(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(FormAnuncio.this,
                                    "Nenhum local cadastrado", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }).start();
    }
    private void mostrarDateTimePicker(final EditText editText) {
        final Calendar calendar = Calendar.getInstance();

        // DatePicker
        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        // TimePicker
                        TimePickerDialog timePicker = new TimePickerDialog(
                                FormAnuncio.this,
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker timeView, int hourOfDay, int minute) {
                                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                        calendar.set(Calendar.MINUTE, minute);

                                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                                        editText.setText(sdf.format(calendar.getTime()));
                                    }
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                true
                        );
                        timePicker.show();
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.show();
    }

    private void adicionarRestricao() {
        String chave = inputRestricaoChave.getText().toString().trim();
        String valor = inputRestricaoValor.getText().toString().trim();

        if (chave.isEmpty() || valor.isEmpty()) {
            Toast.makeText(this, "Preencha chave e valor da restrição", Toast.LENGTH_SHORT).show();
            return;
        }

        restricoes.put(chave, valor);

        // Limpar campos
        inputRestricaoChave.setText("");
        inputRestricaoValor.setText("");

        Toast.makeText(this, "Restrição adicionada: " + chave + " = " + valor, Toast.LENGTH_SHORT).show();
    }

    private void publicarAnuncio() {
        if (!validarFormulario()) {
            return;
        }

        // Coletar dados do formulário
        String titulo = inputTitulo.getText().toString().trim();
        String mensagem = inputMensagem.getText().toString().trim();

        // Modo de entrega
        String modoEntrega = radioCentralizado.isChecked() ? "centralizado" : "descentralizado";

        // Política
        String politica = radioWhitelist.isChecked() ? "whitelist" : "blacklist";

        // Datas
        Date inicio = parseDateTime(inputInicio.getText().toString());
        Date fim = parseDateTime(inputFim.getText().toString());

        // Validar datas
        if (inicio == null || fim == null) {
            Toast.makeText(this, "Erro ao processar datas", Toast.LENGTH_LONG).show();
            return;
        }

        if (inicio.after(fim)) {
            Toast.makeText(this, "Data de início deve ser anterior à data de fim", Toast.LENGTH_LONG).show();
            return;
        }

        // Mostrar loading
        btnPublicar.setEnabled(false);
        btnPublicar.setText("Publicando...");

        // Enviar para a API em thread separada
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Obter usuário logado
                User userLogado = anuncioRepo.getCurrentUser();

                if (userLogado == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnPublicar.setEnabled(true);
                            btnPublicar.setText("Publicar Anúncio");
                            Toast.makeText(FormAnuncio.this,
                                    "Erro: usuário não identificado. Faça login novamente.",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }

                // Criar objeto Anuncio com todos os dados corretos
                Anuncio novoAnuncio = new Anuncio(titulo, mensagem, localSelecionado, userLogado,
                        modoEntrega, politica, inicio, fim);

                // Enviar para a API
                Anuncio anuncioCriado = anuncioRepo.create(novoAnuncio);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnPublicar.setEnabled(true);
                        btnPublicar.setText("Publicar Anúncio");

                        if (anuncioCriado != null) {
                            Toast.makeText(FormAnuncio.this,
                                    "Anúncio publicado com sucesso!", Toast.LENGTH_LONG).show();
                            finish(); // Voltar para tela anterior
                        } else {
                            Toast.makeText(FormAnuncio.this,
                                    "Erro ao publicar anúncio. Verifique os dados e tente novamente.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }).start();
    }

    private boolean validarFormulario() {
        // Validar título
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

        // Validar mensagem
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

        // Validar local selecionado
        if (localSelecionado == null) {
            inputLocal.setError("Selecione um local da lista");
            Toast.makeText(this, "Por favor, selecione um local da lista", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validar datas
        if (TextUtils.isEmpty(inputInicio.getText())) {
            inputInicio.setError("Data de início é obrigatória");
            inputInicio.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(inputFim.getText())) {
            inputFim.setError("Data de fim é obrigatória");
            inputFim.requestFocus();
            return false;
        }

        // Validar formato das datas
        if (!DATE_TIME_PATTERN.matcher(inputInicio.getText().toString()).matches()) {
            inputInicio.setError("Formato inválido. Use: dd/mm/aaaa hh:mm");
            inputInicio.requestFocus();
            return false;
        }

        if (!DATE_TIME_PATTERN.matcher(inputFim.getText().toString()).matches()) {
            inputFim.setError("Formato inválido. Use: dd/mm/aaaa hh:mm");
            inputFim.requestFocus();
            return false;
        }

        return true;
    }

    private Date parseDateTime(String dateTimeStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return sdf.parse(dateTimeStr);
        } catch (Exception e) {
            Log.e("FormAnuncio", "Erro ao parsear data: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}