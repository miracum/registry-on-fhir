describe("Search", () => {
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

    it("Searching for a study by acronym", () => {
      cy.get("#search")
        .type("prosa")
        .should("have.value", "prosa");

      const card = cy.get(".study-card").first();

      card.get(".title").contains("PROSa");
    });

    it("Filter by site", () => {
      cy.get(".checkbox input[value='Location/3']").click({ force: true });

      cy.get(".study-card").should("have.length", 3);
    });
  });
});
