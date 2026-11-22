package cl.duoc.mpazneb.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mpazneb.data.repository.diasHasta
import cl.duoc.mpazneb.data.repository.fechaLegible
import cl.duoc.mpazneb.viewmodel.FeriadosUiState
import cl.duoc.mpazneb.viewmodel.FeriadosViewModel

/**
 * Tarjeta con los próximos feriados de Chile (API EXTERNA Nager.Date, consumida con Retrofit).
 * Ayuda a la docente a planificar cuándo dejar disponibles las actividades.
 */
@Composable
fun TarjetaFeriados(viewModel: FeriadosViewModel = viewModel()) {
    val ui by viewModel.uiState.collectAsState()

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("📅 Próximos feriados en Chile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            when (val estado = ui) {
                FeriadosUiState.Cargando -> Text("Consultando la API de feriados…")
                is FeriadosUiState.Error -> {
                    Text(estado.mensaje, color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = viewModel::fetchFeriados) { Text("Reintentar") }
                }
                is FeriadosUiState.Listo -> {
                    if (estado.feriados.isEmpty()) Text("No hay feriados próximos.")
                    estado.feriados.forEach { feriado ->
                        Row {
                            Text(
                                "${fechaLegible(feriado.date).replaceFirstChar { it.uppercase() }}: ${feriado.localName}",
                                modifier = Modifier.weight(1f)
                            )
                            Text("en ${diasHasta(feriado.date)} días", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Text("Fuente: API pública Nager.Date", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
