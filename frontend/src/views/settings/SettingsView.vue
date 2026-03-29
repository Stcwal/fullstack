<template>
  <div>
    <div class="mb-4">
      <h1 class="page-title">Innstillinger</h1>
      <p class="page-subtitle">Administrasjon av systemet</p>
    </div>

    <nav class="sub-nav mb-4">
      <RouterLink
        to="/innstillinger/enheter"
        class="sub-nav-item"
        :class="{ active: route.path.startsWith('/innstillinger/enheter') }"
      >
        Enheter
      </RouterLink>
      <RouterLink
        to="/innstillinger/brukere"
        class="sub-nav-item"
        :class="{ active: route.path.startsWith('/innstillinger/brukere') }"
      >
        Brukere
      </RouterLink>
      <RouterLink
        to="/innstillinger/org"
        class="sub-nav-item"
        :class="{ active: route.path.startsWith('/innstillinger/org') }"
      >
        Organisasjon
      </RouterLink>
    </nav>

    <RouterView />
  </div>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'
import { RouterLink, RouterView } from 'vue-router'

const route = useRoute()
</script>
