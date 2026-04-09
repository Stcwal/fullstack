describe('Checklists', () => {
  beforeEach(() => {
    cy.visit('/login')
    cy.login('kari@everestsushi.no', 'admin123')
    cy.visit('/generelt')
  })

  it('shows the checklists page with frequency filter', () => {
    cy.get('h1').contains(/Sjekklister/i).should('be.visible')
    cy.get('.sub-nav').should('be.visible')
    cy.get('.sub-nav-item').should('have.length', 3)
  })

  it('defaults to daily frequency', () => {
    cy.get('.sub-nav-item.active').should('contain.text', 'Daglig')
  })

  it('can switch to weekly frequency', () => {
    cy.get('.sub-nav-item').contains('Ukentlig').click()
    cy.get('.sub-nav-item.active').should('contain.text', 'Ukentlig')
  })

  it('can switch to monthly frequency', () => {
    cy.get('.sub-nav-item').contains('Månedlig').click()
    cy.get('.sub-nav-item.active').should('contain.text', 'Månedlig')
  })

  it('shows checklist items', () => {
    cy.get('.checklist-item', { timeout: 8000 }).should('have.length.greaterThan', 0)
  })

  it('can toggle a checklist item (optimistic update)', () => {
    cy.get('.checklist-checkbox', { timeout: 8000 }).first().then($cb => {
      const wasChecked = $cb.is(':checked')
      cy.wrap($cb).click({ force: true })
      // Optimistic update: state flips immediately
      if (wasChecked) {
        cy.wrap($cb).should('not.be.checked')
      } else {
        cy.wrap($cb).should('be.checked')
      }
    })
  })

  it('completed items show visual done state', () => {
    cy.get('.checklist-checkbox', { timeout: 8000 }).first().then($cb => {
      if (!$cb.is(':checked')) {
        cy.wrap($cb).click({ force: true })
      }
    })
    cy.get('.checklist-item.is-done').should('have.length.greaterThan', 0)
  })
})
