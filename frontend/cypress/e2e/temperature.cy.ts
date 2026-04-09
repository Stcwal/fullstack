describe('Temperature Logging', () => {
  beforeEach(() => {
    cy.visit('/login')
    cy.login('kari@everestsushi.no', 'admin123')
    cy.visit('/fryser')
  })

  it('shows temperature unit sub-nav', () => {
    cy.get('.sub-nav', { timeout: 8000 }).should('be.visible')
    cy.get('.sub-nav-item').should('have.length.greaterThan', 0)
  })

  it('shows the temperature form when a unit is selected', () => {
    // First unit is auto-selected on load
    cy.get('#d-temperature', { timeout: 8000 }).should('be.visible')
    cy.get('button[type="submit"]').contains(/Lagre måling/i).should('be.visible')
  })

  it('shows validation error when temperature is empty', () => {
    cy.get('#d-temperature', { timeout: 8000 }).clear()
    cy.get('button[type="submit"]').click()
    cy.contains('Temperatur er påkrevd.').should('be.visible')
  })

  it('logs a temperature reading and shows success feedback', () => {
    cy.get('#d-temperature', { timeout: 8000 }).clear().type('-18')
    cy.get('button[type="submit"]').click()
    cy.contains(/Lagret!/i, { timeout: 6000 }).should('be.visible')
  })

  it('new reading appears in the recent readings list', () => {
    cy.get('#d-temperature', { timeout: 8000 }).clear().type('-19.5')
    cy.get('#d-note').type('Cypress test måling')
    cy.get('button[type="submit"]').click()
    cy.contains(/Lagret!/i, { timeout: 6000 })
    cy.contains('-19.5').should('be.visible')
  })
})
