<script setup lang="ts">
import { ref, onMounted } from 'vue';
import ConvocatoriaService from '../services/ConvocatoriaService';
import RecursosService, { type DocenteDTO, type AsignaturaDTO, type PeriodoDTO } from '../services/RecursosService';
import type { Convocatoria } from '../types/Convocatoria';
const convocatorias = ref<Convocatoria[]>([]);
const isEditing = ref(false);
const periodos = ref<PeriodoDTO[]>([]);
const asignaturas = ref<AsignaturaDTO[]>([]);
const docentes = ref<DocenteDTO[]>([]);

const initialState: Convocatoria = {
  idPeriodoAcademico: 0,
  idAsignatura: 0,
  idDocente: 0,
  cuposDisponibles: 30,
  fechaPublicacion: '',
  fechaCierre: '',
  estado: 'ABIERTO',
  activo: true
};

const currentConvocatoria = ref<Convocatoria>({ ...initialState });

const cargarCatalogos = async () => {
  try {
    const [resPeriodo, resAsignaturas, resDocentes] = await Promise.all([
      RecursosService.getPeriodoActivo(),
      RecursosService.getAsignaturas(),
      RecursosService.getDocentes()
    ]);

    if (resPeriodo.data) {
      periodos.value = [resPeriodo.data];

      if (!isEditing.value) {
        const p = periodos.value[0];
        if (p) {
          currentConvocatoria.value.idPeriodoAcademico = p.idPeriodoAcademico;
        }
      }
    } else {
      periodos.value = [];
    }

    asignaturas.value = resAsignaturas.data;
    docentes.value = resDocentes.data;

  } catch (error) {
    console.error("Error cargando listas desplegables:", error);
  }
};

const listarConvocatorias = async () => {
  try {
    const response = await ConvocatoriaService.getAll();
    convocatorias.value = response.data;
  } catch (e) {
    console.error("Error al cargar convocatorias:", e);
  }
};

const guardar = async () => {
  try {
    if(!currentConvocatoria.value.idPeriodoAcademico ||
      !currentConvocatoria.value.idAsignatura ||
      !currentConvocatoria.value.idDocente) {
      alert("Por favor complete todos los campos requeridos (Periodo, Asignatura, Docente)");
      return;
    }

    if (isEditing.value) {
      await ConvocatoriaService.update(currentConvocatoria.value);
      alert("Convocatoria actualizada correctamente");
    } else {
      await ConvocatoriaService.create(currentConvocatoria.value);
      alert("Convocatoria creada correctamente");
    }
    resetForm();
    listarConvocatorias();
  } catch (e) {
    console.error(e);
    alert("Error al guardar la convocatoria");
  }
};

const eliminar = async (id: number | undefined) => {
  if (!id) return;
  if (confirm("¿Está seguro de eliminar esta convocatoria?")) {
    try {
      await ConvocatoriaService.delete(id);
      listarConvocatorias();
    } catch (e) {
      console.error(e);
      alert("Error al eliminar");
    }
  }
};

const cargarEdicion = (item: Convocatoria) => {
  currentConvocatoria.value = { ...item };
  isEditing.value = true;
};

const resetForm = () => {
  currentConvocatoria.value = { ...initialState };
  isEditing.value = false;

  const primerPeriodo = periodos.value[0];
  if (primerPeriodo) {
    currentConvocatoria.value.idPeriodoAcademico = primerPeriodo.idPeriodoAcademico;
  }
};

onMounted(() => {
  cargarCatalogos();
  listarConvocatorias();
});
</script>

<template>
  <div class="container">
    <h2>Gestión de Convocatorias</h2>

    <div class="card">
      <h3>{{ isEditing ? 'Editar Convocatoria' : 'Nueva Convocatoria' }}</h3>
      <form @submit.prevent="guardar" class="form-grid">

        <div class="form-group">
          <label>Periodo Académico:</label>
          <select v-model="currentConvocatoria.idPeriodoAcademico" required>
            <option value="0" disabled>Seleccione...</option>
            <option v-for="p in periodos" :key="p.idPeriodoAcademico" :value="p.idPeriodoAcademico">
              {{ p.nombrePeriodo }}
            </option>
          </select>
        </div>

        <div class="form-group">
          <label>Asignatura:</label>
          <select v-model="currentConvocatoria.idAsignatura" required>
            <option value="0" disabled>Seleccione...</option>
            <option v-for="a in asignaturas" :key="a.idAsignatura" :value="a.idAsignatura">
              {{ a.nombreAsignatura }}
            </option>
          </select>
        </div>

        <div class="form-group">
          <label>Docente:</label>
          <select v-model="currentConvocatoria.idDocente" required>
            <option value="0" disabled>Seleccione...</option>
            <option v-for="d in docentes" :key="d.idDocente" :value="d.idDocente">
              {{ d.nombreCompletoUsuario }}
            </option>
          </select>
        </div>

        <div class="form-group">
          <label>Cupos Disponibles:</label>
          <input type="number" v-model="currentConvocatoria.cuposDisponibles" required min="1" />
        </div>

        <div class="form-group">
          <label>Fecha Publicación:</label>
          <input type="date" v-model="currentConvocatoria.fechaPublicacion" required />
        </div>

        <div class="form-group">
          <label>Fecha Cierre:</label>
          <input type="date" v-model="currentConvocatoria.fechaCierre" required />
        </div>

        <div class="form-group">
          <label>Estado:</label>
          <select v-model="currentConvocatoria.estado">
            <option value="ABIERTO">ABIERTO</option>
            <option value="CERRADO">CERRADO</option>
            <option value="FINALIZADO">FINALIZADO</option>
          </select>
        </div>

        <div class="form-group full-width">
          <button type="submit" class="btn-save">{{ isEditing ? 'Actualizar' : 'Guardar' }}</button>
          <button type="button" v-if="isEditing" @click="resetForm" class="btn-cancel">Cancelar</button>
        </div>
      </form>
    </div>

    <table>
      <thead>
      <tr>
        <th>ID</th>
        <th>Periodo</th>
        <th>Asignatura</th>
        <th>Docente</th> <th>Estado</th>
        <th>Acciones</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="c in convocatorias" :key="c.idConvocatoria">
        <td>{{ c.idConvocatoria }}</td>
        <td>{{ c.nombrePeriodo }}</td>
        <td>{{ c.nombreAsignatura }}</td>
        <td>{{ c.nombreDocente }}</td> <td>
            <span :class="c.estado === 'ABIERTO' ? 'badge-ok' : 'badge-err'">
                {{ c.estado }}
            </span>
      </td>
        <td>
          <button @click="cargarEdicion(c)" class="btn-edit">Editar</button>
          <button @click="eliminar(c.idConvocatoria)" class="btn-delete">Eliminar</button>
        </td>
      </tr>
      <tr v-if="convocatorias.length === 0">
        <td colspan="6" style="text-align: center; color: #666; padding: 20px;">
          No hay convocatorias registradas.
        </td>
      </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
  .container { max-width: 1000px; margin: 20px auto; font-family: 'Segoe UI', sans-serif; }
  .card { background: #fff; padding: 25px; border-radius: 8px; margin-bottom: 25px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }
  h2, h3 { color: #333; margin-bottom: 20px; }

  .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
  .form-group { display: flex; flex-direction: column; }
  .full-width { grid-column: span 2; display: flex; gap: 10px; justify-content: flex-end; }

  label { font-size: 0.9rem; font-weight: 600; margin-bottom: 5px; color: #555; }
  input, select { padding: 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 1rem; }
  input:focus, select:focus { outline: none; border-color: #42b983; }

  table { width: 100%; border-collapse: collapse; background: white; box-shadow: 0 2px 5px rgba(0,0,0,0.1); border-radius: 8px; overflow: hidden; }
  th, td { border-bottom: 1px solid #eee; padding: 12px 15px; text-align: left; }
  th { background: #f8f9fa; font-weight: 600; color: #444; }
  tr:hover { background-color: #f1f1f1; }

  .btn-save { background: #28a745; color: white; padding: 10px 25px; border: none; border-radius: 4px; cursor: pointer; font-weight: bold; }
  .btn-save:hover { background: #218838; }

  .btn-cancel { background: #6c757d; color: white; padding: 10px 25px; border: none; border-radius: 4px; cursor: pointer; }
  .btn-cancel:hover { background: #5a6268; }

  .btn-edit { background: #ffc107; color: #333; border: none; padding: 6px 12px; border-radius: 4px; cursor: pointer; margin-right: 5px; }
  .btn-delete { background: #dc3545; color: white; border: none; padding: 6px 12px; border-radius: 4px; cursor: pointer; }

  .badge-ok { background: #d4edda; color: #155724; padding: 4px 8px; border-radius: 12px; font-size: 0.85em; font-weight: bold; }
  .badge-err { background: #f8d7da; color: #721c24; padding: 4px 8px; border-radius: 12px; font-size: 0.85em; font-weight: bold; }
</style>
