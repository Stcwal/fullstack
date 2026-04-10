<template>
  <div>
    <div class="flex items-center justify-between mb-4">
      <h2 class="section-title">Sjekklistemaler</h2>
      <button class="btn btn-primary btn-sm" @click="openAddModal">
        + Legg til ny mal
      </button>
    </div>

    <div class="card">
      <div v-if="loading" class="text-muted text-sm">Laster inn...</div>
      <template v-else>
        <div
          v-for="template in templates"
          :key="template.id"
          class="status-row"
        >
          <div class="flex-1 min-w-0">
            <div class="font-medium truncate">{{ template.title }}</div>
            <div class="text-muted text-sm mt-1">
              {{ frequencyLabel(template.frequency) }} &middot;
              {{ template.moduleType === 'IK_ALKOHOL' ? 'IK-Alkohol' : 'IK-Mat' }} &middot;
              {{ template.items.length }} punkt{{ template.items.length !== 1 ? 'er' : '' }}
            </div>
          </div>
          <div class="flex items-center gap-3">
            <button
              class="btn btn-ghost btn-sm"
              style="color: #2563EB"
              @click="openEditModal(template)"
            >
              Rediger
            </button>
            <button
              class="btn btn-ghost btn-sm text-danger"
              @click="deleteTemplate(template)"
            >
              Slett
            </button>
          </div>
        </div>
        <div v-if="templates.length === 0" class="text-muted text-sm">
          Ingen sjekklistemaler registrert.
        </div>
      </template>
    </div>

    <AppModal
      :show="showModal"
      :title="isEditing ? 'Rediger mal' : 'Legg til ny mal'"
      @close="closeModal"
    >
      <div class="form-group">
        <label class="font-medium text-sm" for="tpl-title">Tittel</label>
        <input
          id="tpl-title"
          v-model="form.title"
          type="text"
          class="form-control"
          placeholder="F.eks. Daglig renhold"
          maxlength="120"
        />
      </div>

      <div class="form-group mt-3">
        <label class="font-medium text-sm" for="tpl-frequency">Frekvens</label>
        <select id="tpl-frequency" v-model="form.frequency" class="form-control">
          <option value="DAILY">Daglig</option>
          <option value="WEEKLY">Ukentlig</option>
          <option value="MONTHLY">Månedlig</option>
        </select>
      </div>

      <div class="form-group mt-3">
        <label class="font-medium text-sm" for="tpl-module">Modul</label>
        <select id="tpl-module" v-model="form.moduleType" class="form-control">
          <option value="IK_MAT">IK-Mat</option>
          <option value="IK_ALKOHOL">IK-Alkohol</option>
        </select>
      </div>

      <div class="form-group mt-3">
        <div class="flex items-center justify-between mb-2">
          <label class="font-medium text-sm">Punkter</label>
          <button
            type="button"
            class="btn btn-ghost btn-sm"
            style="color: #16A34A; font-size: 0.75rem;"
            @click="addItem"
          >
            + Legg til punkt
          </button>
        </div>

        <div v-if="form.itemTexts.length === 0" class="text-muted text-sm">
          Ingen punkter enda. Legg til minst ett.
        </div>

        <div
          v-for="(_, index) in form.itemTexts"
          :key="index"
          class="checklist-item-editor"
        >
          <input
            v-model="form.itemTexts[index]"
            type="text"
            class="form-control"
            :placeholder="`Punkt ${index + 1}`"
            maxlength="250"
          />
          <button
            type="button"
            class="btn btn-ghost btn-sm remove-item-btn"
            aria-label="Fjern punkt"
            @click="removeItem(index)"
          >
            &times;
          </button>
        </div>
      </div>

      <div v-if="error" class="text-sm mt-2" style="color: var(--c-danger)">{{ error }}</div>

      <template #footer>
        <button class="btn btn-secondary" @click="closeModal">Avbryt</button>
        <button class="btn btn-primary" :disabled="saving" @click="save">
          {{ saving ? 'Lagrer...' : 'Lagre' }}
        </button>
      </template>
    </AppModal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import AppModal from '@/components/AppModal.vue'
import { checklistsService, type ChecklistTemplate } from '@/services/checklists.service'
import type { ChecklistFrequency, ModuleType } from '@/types'

const templates = ref<ChecklistTemplate[]>([])
const loading = ref(false)
const showModal = ref(false)
const isEditing = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const error = ref('')

interface FormState {
  title: string
  frequency: ChecklistFrequency
  moduleType: ModuleType
  itemTexts: string[]
}

const emptyForm = (): FormState => ({
  title: '',
  frequency: 'DAILY',
  moduleType: 'IK_MAT',
  itemTexts: [''],
})

const form = reactive<FormState>(emptyForm())

function frequencyLabel(f: ChecklistFrequency): string {
  return { DAILY: 'Daglig', WEEKLY: 'Ukentlig', MONTHLY: 'Månedlig' }[f]
}

async function fetchTemplates() {
  loading.value = true
  try {
    templates.value = await checklistsService.getTemplates()
  } finally {
    loading.value = false
  }
}

function openAddModal() {
  isEditing.value = false
  editingId.value = null
  Object.assign(form, emptyForm())
  error.value = ''
  showModal.value = true
}

function openEditModal(template: ChecklistTemplate) {
  isEditing.value = true
  editingId.value = template.id
  form.title = template.title
  form.frequency = template.frequency
  form.moduleType = template.moduleType ?? 'IK_MAT'
  form.itemTexts = template.items.map(i => i.text)
  error.value = ''
  showModal.value = true
}

function closeModal() {
  showModal.value = false
}

function addItem() {
  form.itemTexts.push('')
}

function removeItem(index: number) {
  form.itemTexts.splice(index, 1)
}

async function save() {
  error.value = ''
  if (!form.title.trim()) {
    error.value = 'Tittel er påkrevd.'
    return
  }
  const validItems = form.itemTexts.map(t => t.trim()).filter(t => t.length > 0)
  if (validItems.length === 0) {
    error.value = 'Minst ett punkt er påkrevd.'
    return
  }

  saving.value = true
  try {
    const payload = { title: form.title.trim(), frequency: form.frequency, moduleType: form.moduleType, itemTexts: validItems }
    if (isEditing.value && editingId.value !== null) {
      const updated = await checklistsService.updateTemplate(editingId.value, payload)
      const idx = templates.value.findIndex(t => t.id === editingId.value)
      if (idx !== -1) templates.value[idx] = updated
    } else {
      const created = await checklistsService.createTemplate(payload)
      templates.value.push(created)
    }
    showModal.value = false
  } catch {
    error.value = 'Noe gikk galt. Prøv igjen.'
  } finally {
    saving.value = false
  }
}

async function deleteTemplate(template: ChecklistTemplate) {
  if (!window.confirm(`Er du sikker på at du vil slette "${template.title}"?`)) return
  await checklistsService.deleteTemplate(template.id)
  templates.value = templates.value.filter(t => t.id !== template.id)
}

onMounted(fetchTemplates)
</script>

<style scoped>
.checklist-item-editor {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.checklist-item-editor .form-control {
  flex: 1;
}

.remove-item-btn {
  flex-shrink: 0;
  color: var(--c-danger);
  font-size: 1.1rem;
  line-height: 1;
  padding: 0.25rem 0.5rem;
}
</style>
