<script setup lang="ts">
import { RouterView } from 'vue-router'
</script>

<template>
  <header>
    <nav>
    </nav>
  </header>

  <main>
    <RouterView />
  </main>
</template>

<style>
  body {
    margin: 0;
    font-family: sans-serif;
    background-color: #f4f4f4;
  }
</style>
