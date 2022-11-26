describe("Homepage", () => {
  context("after loading studies", () => {
    beforeEach(() => {
      cy.server();
      cy.route("GET", "**/ResearchStudy/**").as("getStudies");
      cy.visit("/", {
        onBeforeLoad: (win) => {
          // eslint-disable-next-line no-param-reassign
          win.fetch = null;
        },
      });
      cy.wait("@getStudies", { timeout: 30000 });
    });

    it("Visits the app root url", () => {
      cy.visit("/");

      cy.contains("span", "MIRACUM Studienregister");
    });

    it("Lists the expected initial studies on the main page", () => {
      cy.visit("/");
      cy.get(".study-card").should("have.length", 5);
    });
  });
});
