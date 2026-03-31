<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { useChecklistsStore } from '@/stores/checklists'
import AppProgressBar from '@/components/AppProgressBar.vue'

const store = useChecklistsStore()

// Filter to only IK_ALKOHOL checklists
const alkoholChecklists = computed(() =>
  store.checklists.filter(c => c.moduleType === 'IK_ALKOHOL')
)

const completedCount = computed(() =>
  alkoholChecklists.value.reduce((acc, cl) =>
    acc + cl.items.filter(i => i.completed).length, 0)
)

const totalCount = computed(() =>
  alkoholChecklists.value.reduce((acc, cl) => acc + cl.items.length, 0)
)

const progressPct = computed(() =>
  totalCount.value > 0 ? Math.round((completedCount.value / totalCount.value) * 100) : 0
)

onMounted(() => store.fetchAll())
</script>

<template>
  <section aria-label="Alkohol-sjekklister">
    <h1 class="section-title">Alkohol-sjekklister</h1>

    <div v-if="totalCount > 0" class="progress-summary" aria-label="Fremdrift">
      <AppProgressBar :value="progressPct" />
      <p class="progress-text">{{ completedCount }} av {{ totalCount }} fullført</p>
    </div>

    <div v-if="store.loading" class="loading-text" aria-live="polite">Laster sjekklister...</div>

    <p v-else-if="alkoholChecklists.length === 0" class="empty-state">
      Ingen alkohol-sjekklister konfigurert. En administrator kan opprette maler under Innstillinger.
    </p>

    <div v-else class="checklist-list">
      <div
        v-for="checklist in alkoholChecklists"
        :key="checklist.id"
        class="checklist-card card"
        :aria-label="checklist.title"
      >
        <h2 class="checklist-title">{{ checklist.title }}</h2>
        <ul class="item-list" :aria-label="`Elementer i ${checklist.title}`">
          <li
            v-for="item in checklist.items"
            :key="item.id"
            class="checklist-item"
            :class="{ 'checklist-item--done': item.completed }"
          >
            <button
              class="checklist-check"
              :aria-pressed="item.completed"
              :aria-label="`${item.completed ? 'Fjern avkryssing' : 'Kryss av'}: ${item.text}`"
              @click="store.toggleItem(checklist.id, item.id)"
            >
              <span class="check-icon" aria-hidden="true">{{ item.completed ? '✓' : '' }}</span>
            </button>
            <span class="item-text" :class="{ 'item-text--done': item.completed }">
              {{ item.text }}
            </span>
            <span v-if="item.completedBy" class="item-meta">{{ item.completedBy }}</span>
          </li>
        </ul>
      </div>
    </div>
  </section>
</template>

<style scoped>
.section-title { font-size: 18px; font-weight: 700; color: #0f172a; margin-bottom: 16px; }
.progress-summary { margin-bottom: 16px; }
.progress-text { font-size: 12px; color: #64748b; margin-top: 4px; }
.loading-text, .empty-state { color: #94a3b8; font-size: 14px; padding: 32px 0; text-align: center; }
.checklist-list { display: flex; flex-direction: column; gap: 12px; }
.checklist-card { padding: 16px; }
.checklist-title { font-size: 15px; font-weight: 700; color: #0f172a; margin-bottom: 10px; }
.item-list { list-style: none; padding: 0; display: flex; flex-direction: column; gap: 4px; }
.checklist-item { display: flex; align-items: center; gap: 10px; padding: 8px 10px; border-radius: 8px; }
.checklist-item--done { background: hsl(var(--ik-alkohol-hue, 38), 100%, 97%); }
.checklist-check {
  width: 22px; height: 22px; min-width: 22px; min-height: 22px;
  border-radius: 5px; border: 2px solid #cbd5e1;
  background: #fff; display: flex; align-items: center; justify-content: center;
  cursor: pointer; color: #fff; font-size: 12px; font-weight: 700;
}
.checklist-item--done .checklist-check {
  background: hsl(var(--ik-alkohol-hue, 38), 85%, 45%);
  border-color: hsl(var(--ik-alkohol-hue, 38), 85%, 45%);
}
.checklist-check:focus-visible { outline: 2px solid hsl(var(--ik-alkohol-hue, 38), 85%, 45%); outline-offset: 2px; }
.item-text { font-size: 14px; color: #0f172a; flex: 1; }
.item-text--done { text-decoration: line-through; color: #94a3b8; }
.item-meta { font-size: 11px; color: #94a3b8; }
</style>
