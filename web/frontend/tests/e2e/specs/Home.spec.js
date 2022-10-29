describe("Homepage is loading correctly", () => {
  it("Visits the app root url", () => {
    cy.visit("/");

    cy.contains("span", "MIRACUM Studienregister");
  });

  it("Lists the expected initial studies on the main page", () => {
    cy.visit("/");
    cy.get(".study-card").should("have.length", 5);
  });
});
