<template>
  <div id="app">
    <header>
      <nav class="navbar container" role="navigation" aria-label="main navigation">
        <div class="navbar-brand">
          <a class="navbar-item" href="/">
            <picture>
              <source srcset="@/assets/miracum-logo-no-text.webp" type="image/webp" />
              <source srcset="@/assets/miracum-logo-no-text.png" type="image/png" />
              <img src="@/assets/miracum-logo-no-text.png" alt="Logo MIRACUM" />
            </picture>
            <span class="navbar-item has-text-white">MIRACUM Studienregister</span>
          </a>
        </div>
      </nav>
    </header>
    <main class="router-content container">
      <router-view />
    </main>
    <footer class="footer has-background-white">
      <div class="columns has-text-centered">
        <div class="column">
          <picture>
            <source srcset="@/assets/logo-bmbf.webp" type="image/webp" />
            <source srcset="@/assets/logo-bmbf.png" type="image/png" />
            <img src="@/assets/logo-bmbf.png" alt="Logo BMBF" />
          </picture>
        </div>
        <div class="column">
          <picture>
            <source srcset="@/assets/logo-mii.webp" type="image/webp" />
            <source srcset="@/assets/logo-mii.png" type="image/png" />
            <img src="@/assets/logo-mii.png" alt="Logo MII" />
          </picture>
        </div>
        <div class="column">
          <picture>
            <source srcset="@/assets/logo-tmf.webp" type="image/webp" />
            <source srcset="@/assets/logo-tmf.png" type="image/png" />
            <img src="@/assets/logo-tmf.png" alt="Logo TMF e.V." />
          </picture>
        </div>
      </div>
      <div class="columns is-centered">
        <div class="column is-half has-text-centered">
          <p class="has-text-grey-light is-size-7">{{ version }}</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script>
export default {
  name: "App",
  components: {},
  computed: {
    version() {
      return process.env.VUE_APP_VERSION;
    },
  },
};
</script>

<style>
#app {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 100vh;
  color: #ffffff;
  font-size: 16px;
}

html,
body {
  background-color: #1b2259;
}

header,
footer {
  flex: none;
}

footer img {
  height: 85px;
  width: auto;
}

main {
  flex: 1 0 auto;
  width: 100%;
  margin-bottom: 3rem;
}

main.router-content {
  padding: 0 0.25rem;
}

.navbar {
  background-color: transparent !important;
}

.navbar-brand > .navbar-item > picture > img {
  border-radius: 50%;
  min-height: 4rem;
}
</style>

<style lang="scss">
// Import Bulma's core
@import "~bulma/sass/utilities/_all";

// Set your colors
$primary: #1b2259;
$primary-invert: findColorInvert($primary);

$success: #00a579;
$success-invert: findColorInvert($success);

$loading-background: rgba(255, 255, 255, 0);

// Setup $colors to use as bulma classes (e.g. 'is-twitter')
$colors: (
  "white": (
    $white,
    $black,
  ),
  "black": (
    $black,
    $white,
  ),
  "light": (
    $light,
    $light-invert,
  ),
  "dark": (
    $dark,
    $dark-invert,
  ),
  "primary": (
    $primary,
    $primary-invert,
  ),
  "info": (
    $info,
    $info-invert,
  ),
  "success": (
    $success,
    $success-invert,
  ),
  "warning": (
    $warning,
    $warning-invert,
  ),
  "danger": (
    $danger,
    $danger-invert,
  ),
);

// Tabs
$tabs-link-color: $white;
$tabs-link-active-color: $success;
$tabs-toggle-link-active-background-color: $success;
$tabs-toggle-link-active-border-color: $success;

// Input/Checkbox
$input-hover-color: $grey-light;

// Import Bulma and Buefy styles
@import "~bulma";
@import "~buefy/src/scss/buefy";
</style>
