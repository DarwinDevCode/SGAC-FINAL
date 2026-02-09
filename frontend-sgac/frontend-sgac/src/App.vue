<script setup lang="ts">
import { RouterView } from 'vue-router'
</script>

<template>
  <header style="background: #003366; color: white; padding: 1rem; text-align: center;">
    <h1>Sistema de Gestión Académica (SGAC)</h1>
  </header>

  <main>
    <RouterView />
  </main>
</template>

<style>

body {
  margin: 0;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background-color: #f0f2f5;
}
</style>
