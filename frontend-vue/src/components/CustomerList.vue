<script setup lang="ts">
import { ref, onMounted } from 'vue';
import CustomerService from '../services/CustomerService';
import type { Customer } from '../types/Customer';

const customers = ref<Customer[]>([]);

const initialCustomerState: Customer = {
  firstName: "",
  lastName: "",
  email: ""
};

const currentCustomer = ref<Customer>({ ...initialCustomerState });
const isEditing = ref<boolean>(false);

const retrieveCustomers = async () => {
  try {
    const response = await CustomerService.getAll();
    customers.value = response.data;
  } catch (e) {
    console.error("Error al cargar clientes:", e);
  }
};

const saveCustomer = async () => {
  try {
    if (isEditing.value && currentCustomer.value.id) {
      await CustomerService.update(currentCustomer.value);
      alert("Cliente actualizado exitosamente");
    } else {
      await CustomerService.create(currentCustomer.value);
      alert("Cliente creado exitosamente");
    }
    resetForm();
    retrieveCustomers();
  } catch (e) {
    console.error("Error al guardar:", e);
  }
};

const deleteCustomer = async (id: number | undefined) => {
  if (!id) return;

  if (confirm("¿Estás seguro de eliminar este cliente?")) {
    try {
      await CustomerService.delete(id);
      retrieveCustomers();
    } catch (e) {
      console.error(e);
    }
  }
};

const editCustomer = (customer: Customer) => {
  currentCustomer.value = { ...customer };
  isEditing.value = true;
};

const resetForm = () => {
  currentCustomer.value = { ...initialCustomerState };
  isEditing.value = false;
};

onMounted(() => {
  retrieveCustomers();
});
</script>

<template>
  <div class="container">
    <h1>Gestión de Clientes (TS)</h1>

    <div class="card">
      <h3>{{ isEditing ? 'Editar Cliente' : 'Nuevo Cliente' }}</h3>
      <form @submit.prevent="saveCustomer">
        <div class="form-group">
          <input v-model="currentCustomer.firstName" placeholder="Nombre" required />
          <input v-model="currentCustomer.lastName" placeholder="Apellido" required />
          <input v-model="currentCustomer.email" type="email" placeholder="Email" required />
        </div>

        <div class="actions">
          <button type="submit" class="btn-save">{{ isEditing ? 'Actualizar' : 'Guardar' }}</button>
          <button type="button" v-if="isEditing" @click="resetForm" class="btn-cancel">Cancelar</button>
        </div>
      </form>
    </div>

    <div class="list">
      <table>
        <thead>
        <tr>
          <th>ID</th>
          <th>Nombre</th>
          <th>Apellido</th>
          <th>Email</th>
          <th>Acciones</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="customer in customers" :key="customer.id">
          <td>{{ customer.id }}</td>
          <td>{{ customer.firstName }}</td>
          <td>{{ customer.lastName }}</td>
          <td>{{ customer.email }}</td>
          <td>
            <button @click="editCustomer(customer)" class="btn-edit">Editar</button>
            <button @click="deleteCustomer(customer.id)" class="btn-delete">Eliminar</button>
          </td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
  .container { max-width: 900px; margin: 2rem auto; font-family: 'Segoe UI', sans-serif; }
  .card { background: #fff; padding: 1.5rem; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); margin-bottom: 2rem; }
  .form-group { display: flex; gap: 10px; margin-bottom: 10px; }
  input { flex: 1; padding: 10px; border: 1px solid #ccc; border-radius: 4px; }
  button { padding: 10px 20px; border: none; border-radius: 4px; cursor: pointer; color: white; font-weight: bold; }
  .btn-save { background-color: #42b983; }
  .btn-cancel { background-color: #6c757d; margin-left: 10px; }
  .btn-edit { background-color: #ffc107; color: #333; margin-right: 5px; }
  .btn-delete { background-color: #dc3545; }
  table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
  th, td { padding: 12px 15px; text-align: left; border-bottom: 1px solid #eee; }
  th { background-color: #f8f9fa; color: #333; }
</style>
