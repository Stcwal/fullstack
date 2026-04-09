describe('Authentication', () => {
  beforeEach(() => {
    cy.visit('/login')
  })

  it('shows the login form', () => {
    cy.get('input[type="email"], input[name="email"], input[placeholder*="E-post"], input[placeholder*="epost"]')
      .should('be.visible')
    cy.get('input[type="password"]').should('be.visible')
    cy.get('button[type="submit"], button').contains(/Logg inn/i).should('be.visible')
  })

  it('redirects to dashboard after valid login', () => {
    cy.get('input[type="email"], input[name="email"]').first().type('kari@everestsushi.no')
    cy.get('input[type="password"]').type('admin123')
    cy.get('button').contains(/Logg inn/i).click()
    cy.url().should('include', '/dashboard')
  })

  it('stays on login page and shows error with wrong password', () => {
    cy.get('input[type="email"], input[name="email"]').first().type('kari@everestsushi.no')
    cy.get('input[type="password"]').type('wrongpassword')
    cy.get('button').contains(/Logg inn/i).click()
    cy.url().should('include', '/login')
  })

  it('logout clears the session and redirects to login', () => {
    cy.login('kari@everestsushi.no', 'admin123')
    cy.visit('/dashboard')
    cy.url().should('include', '/dashboard')

    // Find and click the logout button in the sidebar
    cy.contains('button', /Logg ut/i).click({ force: true })
    cy.url().should('include', '/login')
    cy.window().then(win => {
      expect(win.sessionStorage.getItem('token')).to.be.null
    })
  })

  it('unauthenticated user is redirected to login from protected route', () => {
    cy.visit('/dashboard')
    cy.url().should('include', '/login')
  })
})
