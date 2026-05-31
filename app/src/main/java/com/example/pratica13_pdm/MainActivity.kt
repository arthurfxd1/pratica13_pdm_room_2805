package com.example.pratica13_pdm

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var tarefaDao: TarefaDao
    private lateinit var editTextTarefa: EditText
    private lateinit var btnSalvar: Button
    private lateinit var btnLimpar: Button
    private lateinit var containerTarefas: android.widget.LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = AppDatabase.getDatabase(this)
        tarefaDao = database.tarefaDao()

        editTextTarefa = findViewById(R.id.editTextTarefa)
        btnSalvar = findViewById(R.id.btnSalvar)
        btnLimpar = findViewById(R.id.btnLimpar)
        containerTarefas = findViewById(R.id.containerTarefas)

        atualizarListaDeTarefas()

        btnSalvar.setOnClickListener {
            val descricao = editTextTarefa.text.toString().trim()
            if (descricao.isNotEmpty()) {
                val novaTarefa = Tarefa(descricao = descricao)
                lifecycleScope.launch {
                    tarefaDao.inserir(novaTarefa)
                    editTextTarefa.text.clear()
                    Toast.makeText(this@MainActivity, "Salvo com sucesso!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnLimpar.setOnClickListener {
            lifecycleScope.launch {
                tarefaDao.apagarTudo()
            }
        }
    }

    private fun atualizarListaDeTarefas() {
        lifecycleScope.launch {
            tarefaDao.buscarTodas().collect { lista ->
                containerTarefas.removeAllViews()
                
                if (lista.isEmpty()) {
                    val tvSemTarefas = TextView(this@MainActivity)
                    tvSemTarefas.text = "Nenhuma tarefa cadastrada."
                    containerTarefas.addView(tvSemTarefas)
                } else {
                    for (tarefa in lista) {
                        val itemView = layoutInflater.inflate(R.layout.item_tarefa, containerTarefas, false)
                        
                        val checkBox = itemView.findViewById<android.widget.CheckBox>(R.id.checkBoxConcluida)
                        val textView = itemView.findViewById<TextView>(R.id.textViewDescricao)
                        
                        textView.text = tarefa.descricao
                        checkBox.isChecked = tarefa.estaConcluida

                        checkBox.setOnClickListener {
                            val tarefaAtualizada = tarefa.copy(estaConcluida = !tarefa.estaConcluida)
                            lifecycleScope.launch {
                                tarefaDao.atualizar(tarefaAtualizada)
                            }
                        }

                        itemView.setOnLongClickListener {
                            lifecycleScope.launch {
                                tarefaDao.deletar(tarefa)
                                Toast.makeText(this@MainActivity, "Tarefa removida!", Toast.LENGTH_SHORT).show()
                            }
                            true
                        }

                        containerTarefas.addView(itemView)
                    }
                }
            }
        }
    }
}
