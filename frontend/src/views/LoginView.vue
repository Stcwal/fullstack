<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import AppAlert from '@/components/AppAlert.vue'

const router = useRouter()
const authStore = useAuthStore()

const email = ref('')
const password = ref('')
const showPassword = ref(false)
const loading = ref(false)
const errorMessage = ref('')

// DEV ONLY — tree-shaken from production builds
const isDev = import.meta.env.DEV
const devAccounts = isDev ? [
  { label: 'Admin',  email: 'kari@everestsushi.no', password: 'admin123' },
  { label: 'Leder',  email: 'ola@everestsushi.no',  password: 'leder123' },
  { label: 'Ansatt', email: 'per@everestsushi.no',  password: 'ansatt123' },
] : []

async function quickLogin(e: string, p: string) {
  email.value = e
  password.value = p
  errorMessage.value = ''
  loading.value = true
  try {
    await authStore.login(e, p)
    router.push({ name: 'dashboard' })
  } catch {
    errorMessage.value = 'Mock login feilet — sjekk konsollet.'
  } finally {
    loading.value = false
  }
}

async function handleLogin() {
  errorMessage.value = ''

  if (!email.value.trim() || !password.value) {
    errorMessage.value = 'Fyll inn e-post og passord.'
    return
  }

  loading.value = true
  try {
    await authStore.login(email.value.trim(), password.value)
    router.push({ name: 'dashboard' })
  } catch {
    errorMessage.value = 'Feil e-post eller passord. Prøv igjen.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-card card">
      <!-- Logo / Brand -->
      <div class="login-brand">
        <div class="login-logo" aria-hidden="true">
          <svg width="36" height="36" viewBox="0 0 36 36" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect width="36" height="36" rx="10" fill="#16A34A"/>
            <path d="M10 18.5L15.5 24L26 13" stroke="white" stroke-width="2.8" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <h1 class="login-title">IK-Kontrollsystem</h1>
        <p class="login-subtitle">Everest Sushi &amp; Fusion AS</p>
      </div>

      <!-- Error alert -->
      <div v-if="errorMessage" class="mb-3">
        <AppAlert type="danger" :message="errorMessage" />
      </div>

      <!-- Form -->
      <form class="login-form" @submit.prevent="handleLogin" novalidate>
        <div class="field-group">
          <label class="field-label" for="login-email">E-postadresse</label>
          <input
            id="login-email"
            v-model="email"
            type="email"
            class="field-input"
            placeholder="din@epost.no"
            autocomplete="username"
            :disabled="loading"
          />
        </div>

        <div class="field-group">
          <label class="field-label" for="login-password">Passord</label>
          <div class="password-wrap">
            <input
              id="login-password"
              v-model="password"
              :type="showPassword ? 'text' : 'password'"
              class="field-input password-input"
              placeholder="••••••••"
              autocomplete="current-password"
              :disabled="loading"
            />
            <button
              type="button"
              class="password-toggle"
              :aria-label="showPassword ? 'Skjul passord' : 'Vis passord'"
              @click="showPassword = !showPassword"
              tabindex="-1"
            >
              <!-- Eye open -->
              <svg v-if="!showPassword" width="18" height="18" fill="none" viewBox="0 0 24 24">
                <path d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <!-- Eye closed -->
              <svg v-else width="18" height="18" fill="none" viewBox="0 0 24 24">
                <path d="M17.94 17.94A10.07 10.07 0 0112 20c-7 0-11-8-11-8a18.45 18.45 0 015.06-5.94M9.9 4.24A9.12 9.12 0 0112 4c7 0 11 8 11 8a18.5 18.5 0 01-2.16 3.19m-6.72-1.07a3 3 0 11-4.24-4.24M1 1l22 22" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
          </div>
        </div>

        <button
          type="submit"
          class="btn btn-primary btn-lg login-submit"
          :disabled="loading"
        >
          <span v-if="loading" class="loading-spinner" aria-hidden="true"></span>
          {{ loading ? 'Logger inn...' : 'Logg inn' }}
        </button>
      </form>
    </div>

    <!-- DEV ONLY: quick-login buttons — not included in production builds -->
    <div v-if="isDev" class="dev-creds">
      <p class="text-muted text-sm" style="margin-bottom: 8px; font-weight: 500;">Hurtiglogg inn (kun utvikling)</p>
      <div class="quick-login-row">
        <button
          v-for="acc in devAccounts"
          :key="acc.label"
          class="quick-login-btn"
          :disabled="loading"
          @click="quickLogin(acc.email, acc.password)"
        >
          {{ acc.label }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100dvh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px 16px 32px;
  background: linear-gradient(135deg, #0F172A 0%, #1E293B 100%);
}

/* ---- Card ---- */
.login-card {
  width: 100%;
  max-width: 400px;
  background: #ffffff;
  border-radius: var(--r-xl);
  box-shadow: var(--shadow-lg, 0 20px 40px rgba(0,0,0,0.3));
  padding: 40px 36px 36px;
  border: none;
}

/* ---- Brand section ---- */
.login-brand {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  margin-bottom: 28px;
}

.login-logo {
  margin-bottom: 14px;
}

.login-title {
  font-size: 1.375rem;
  font-weight: 700;
  color: var(--c-text);
  letter-spacing: -0.01em;
  line-height: 1.2;
  margin-bottom: 4px;
}

.login-subtitle {
  font-size: 0.875rem;
  color: var(--c-text-2);
  font-weight: 400;
}

/* ---- Form ---- */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.field-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field-label {
  font-size: 0.8125rem;
  font-weight: 600;
  color: var(--c-text-2);
  letter-spacing: 0.01em;
}

.field-input {
  width: 100%;
  height: 44px;
  padding: 0 14px;
  font-size: 0.9375rem;
  color: var(--c-text);
  background: var(--c-surface-2, #F8FAFC);
  border: 1.5px solid var(--c-border);
  border-radius: var(--r-sm);
  outline: none;
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
  font-family: inherit;
}

.field-input::placeholder {
  color: var(--c-text-3);
}

.field-input:focus {
  border-color: var(--c-primary);
  box-shadow: 0 0 0 3px rgba(22, 163, 74, 0.12);
  background: #ffffff;
}

.field-input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* ---- Password wrapper ---- */
.password-wrap {
  position: relative;
}

.password-input {
  padding-right: 46px;
}

.password-toggle {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  color: var(--c-text-3);
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--r-xs);
  transition: color 0.15s ease;
  line-height: 0;
}

.password-toggle:hover {
  color: var(--c-text-2);
}

/* ---- Submit button ---- */
.login-submit {
  width: 100%;
  margin-top: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.login-submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

/* ---- Loading spinner ---- */
.loading-spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.35);
  border-top-color: #ffffff;
  border-radius: 50%;
  animation: spin 0.65s linear infinite;
  flex-shrink: 0;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ---- Dev credentials ---- */
.dev-creds {
  margin-top: 28px;
  text-align: center;
  opacity: 0.7;
}

.cred-list {
  display: flex;
  flex-direction: column;
  gap: 5px;
  margin-top: 2px;
}

.cred-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 0.75rem;
  color: #94A3B8;
  font-family: 'SF Mono', 'Fira Code', ui-monospace, monospace;
}

.cred-role {
  color: #64748B;
  font-family: inherit;
  font-weight: 500;
  min-width: 40px;
  text-align: right;
  font-size: 0.6875rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.cred-email {
  color: #94A3B8;
}

.cred-sep {
  color: #475569;
}

.quick-login-row {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.quick-login-btn {
  padding: 7px 18px;
  background: rgba(255,255,255,0.08);
  border: 1px solid rgba(255,255,255,0.15);
  border-radius: 8px;
  color: #e2e8f0;
  font-size: 0.8125rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
  font-family: inherit;
  min-height: 36px;
}
.quick-login-btn:hover { background: rgba(255,255,255,0.14); }
.quick-login-btn:disabled { opacity: 0.5; cursor: not-allowed; }
</style>
