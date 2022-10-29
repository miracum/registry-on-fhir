describe("Card", () => {
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

    it("clicking the more info button should expand to reveal more info", () => {
      cy.get(".study-card")
        .first()
        .find(".more-info-button")
        .click();

      cy.get(".notification").should("be.visible");
    });
  });
});
