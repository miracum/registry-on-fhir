import { shallowMount, createLocalVue } from "@vue/test-utils";
import VueRouter from "vue-router";
import App from "@/App.vue";

const localVue = createLocalVue();
localVue.use(VueRouter);
const router = new VueRouter();

describe("App.vue", () => {
  it("renders title in navbar", () => {
    const wrapper = shallowMount(App, {
      localVue,
      router,
    });

    expect(wrapper.text()).toMatch("MIRACUM Studienregister");
  });
});
