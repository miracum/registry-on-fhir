import { shallowMount } from "@vue/test-utils";
import Vue from "vue";
import Home from "@/views/Home.vue";

Vue.config.ignoredElements = ["b-message", "b-loading"];

describe("Home.vue", () => {
  it("renders hero title screen", () => {
    const wrapper = shallowMount(Home);
    const titleElement = wrapper.find("h1");
    expect(titleElement.text()).not.toBeUndefined();
  });
});
