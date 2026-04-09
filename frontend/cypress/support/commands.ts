/// <reference types="cypress" />

/**
 * cy.login(email, password)
 * Logs in via the API directly and seeds sessionStorage,
 * then visits the app so Vue/Pinia hydrate correctly.
 */
Cypress.Commands.add('login', (email: string, password: string) => {
  cy.request('POST', '/api/auth/login', { email, password }).then(resp => {
    // cy.request() bypasses the Axios interceptor, so resp.body is the raw
    // ApiResponse wrapper: { success, message, data: LoginResponse }
    const data  = resp.body.data ?? resp.body
    const token = data.token

    // Backend LoginResponse is flat (userId, firstName, etc.) — no nested
    // user object. Reconstruct the shape the auth store expects.
    const user = data.user ?? {
      id:                data.userId,
      email:             data.email,
      firstName:         data.firstName,
      lastName:          data.lastName,
      role:              data.role,
      organizationId:    data.organizationId,
      primaryLocationId: data.primaryLocationId,
    }

    cy.window().then(win => {
      win.sessionStorage.setItem('token', token)
      win.sessionStorage.setItem('user',  JSON.stringify(user))
    })
  })
})

declare global {
  namespace Cypress {
    interface Chainable {
      login(email: string, password: string): Chainable<void>
    }
  }
}
