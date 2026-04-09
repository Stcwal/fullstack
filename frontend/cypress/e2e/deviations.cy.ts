describe('Deviations', () => {
  beforeEach(() => {
    cy.visit('/login')
    cy.login('kari@everestsushi.no', 'admin123')
    cy.visit('/avvik')
  })

  it('shows the deviations page with report button', () => {
    cy.get('h1').contains(/Avvik/i).should('be.visible')
    cy.get('button').contains(/Rapporter nytt avvik/i).should('be.visible')
  })

  it('opens the report modal when button is clicked', () => {
    cy.get('button').contains(/Rapporter nytt avvik/i).click()
    cy.get('#dev-title').should('be.visible')
    cy.get('#dev-desc').should('be.visible')
    cy.get('#dev-severity').should('be.visible')
  })

  it('report button is disabled when fields are empty', () => {
    cy.get('button').contains(/Rapporter nytt avvik/i).click()
    cy.get('button').contains(/Rapporter avvik/i).should('be.disabled')
  })

  it('closes the modal when Avbryt is clicked', () => {
    cy.get('button').contains(/Rapporter nytt avvik/i).click()
    cy.get('#dev-title').should('be.visible')
    cy.get('button').contains(/Avbryt/i).click()
    cy.get('#dev-title').should('not.exist')
  })

  it('reports a new deviation and shows it in the list', () => {
    cy.get('button').contains(/Rapporter nytt avvik/i).click()
    cy.get('#dev-title').type('Cypress testavvik')
    cy.get('#dev-desc').type('Automatisk testavvik opprettet av Cypress')
    cy.get('#dev-severity').select('CRITICAL')
    cy.get('button').contains(/Rapporter avvik/i).click()
    // Modal should close and new deviation should be visible in the list
    cy.get('#dev-title').should('not.exist')
    cy.contains('Cypress testavvik').should('be.visible')
  })

  it('new deviation has OPEN status badge', () => {
    cy.get('button').contains(/Rapporter nytt avvik/i).click()
    cy.get('#dev-title').type('Status test avvik')
    cy.get('#dev-desc').type('Sjekker at status er Åpen')
    cy.get('button').contains(/Rapporter avvik/i).click()
    cy.contains('Status test avvik')
      .closest('.card')
      .find('.badge-danger')
      .contains(/Åpen/i)
      .should('be.visible')
  })
})
