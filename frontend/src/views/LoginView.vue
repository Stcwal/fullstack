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

const devAccounts = [
  { label: 'Admin',  email: 'kari@everestsushi.no', password: 'admin123' },
  { label: 'Leder',  email: 'ola@everestsushi.no',  password: 'leder123' },
  { label: 'Ansatt', email: 'per@everestsushi.no',  password: 'ansatt123' },
]

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
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path d="M5 12.5L10 17.5L19 8" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" style="color: var(--c-primary)"/>
          </svg>
        </div>
        <h1 class="login-title">IK-Kontrollsystem</h1>
        <p class="login-subtitle">Everest Sushi &amp; Fusion AS</p>
      </div>
      <div class="login-divider"></div>

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

    <div class="dev-creds">
      <p class="dev-label">Testkontoer</p>
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
/* ---- Page background — deep navy slate, intentional contrast vs light app ---- */
.login-page {
  min-height: 100dvh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px 16px 32px;
  background:
    radial-gradient(ellipse at 50% 20%, oklch(0.19 0.04 255) 0%, oklch(0.12 0.025 255) 65%),
    oklch(0.12 0.025 255);
}

/* ---- Card ---- */
.login-card {
  width: 100%;
  max-width: 388px;
  background: var(--c-surface);
  border-radius: var(--r-xl);
  box-shadow: 0 24px 48px rgba(0,0,0,0.4), 0 1px 0 rgba(255,255,255,0.06) inset;
  padding: 36px 32px 32px;
  border: 1px solid var(--c-border);
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
  margin-bottom: 16px;
  /* Stamp-like presentation */
  width: 52px;
  height: 52px;
  border-radius: var(--r);
  background: oklch(0.975 0.025 145);
  border: 1.5px solid oklch(0.86 0.09 145);
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--c-text);
  letter-spacing: -0.025em;
  line-height: 1.2;
  margin-bottom: 4px;
}

.login-subtitle {
  font-size: 0.8125rem;
  color: var(--c-text-2);
  font-weight: 400;
}

/* ---- Divider ---- */
.login-divider {
  height: 1px;
  background: var(--c-border);
  margin: 0 0 24px;
}

/* ---- Form ---- */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.field-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.field-label {
  font-size: 0.6875rem;
  font-weight: 700;
  color: var(--c-text-2);
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.field-input {
  width: 100%;
  height: 44px;
  padding: 0 14px;
  font-size: 0.9375rem;
  color: var(--c-text);
  background: var(--c-surface-2);
  border: 1.5px solid var(--c-border);
  border-radius: var(--r-sm);
  outline: none;
  transition: border-color 0.15s ease, box-shadow 0.15s ease, background 0.15s ease;
  font-family: inherit;
}

.field-input::placeholder {
  color: var(--c-text-3);
}

.field-input:focus {
  border-color: var(--c-primary);
  box-shadow: 0 0 0 3px oklch(0.52 0.17 145 / 0.14);
  background: var(--c-surface);
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

.password-toggle:hover { color: var(--c-text-2); }

/* ---- Submit button ---- */
.login-submit {
  width: 100%;
  margin-top: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 0.9375rem;
}

.login-submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

/* ---- Loading spinner ---- */
.loading-spinner {
  display: inline-block;
  width: 15px;
  height: 15px;
  border: 2px solid rgba(255,255,255,0.3);
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
  margin-top: 24px;
  text-align: center;
}

.quick-login-row {
  display: flex;
  gap: 6px;
  justify-content: center;
  margin-top: 8px;
}

.quick-login-btn {
  padding: 7px 16px;
  background: transparent;
  border: 1px solid rgba(255,255,255,0.14);
  border-radius: var(--r-sm);
  color: oklch(0.78 0.01 250);
  font-size: 0.75rem;
  font-weight: 700;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s;
  font-family: inherit;
  min-height: 34px;
}
.quick-login-btn:hover {
  background: rgba(255,255,255,0.08);
  border-color: rgba(255,255,255,0.24);
  color: #fff;
}
.quick-login-btn:disabled { opacity: 0.4; cursor: not-allowed; }

/* ---- Subtext below quick-login ---- */
.dev-label {
  font-size: 0.6875rem;
  font-weight: 600;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: oklch(0.5 0.01 250);
}
</style>
