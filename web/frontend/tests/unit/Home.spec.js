import { shallowMount } from "@vue/test-utils";
import Vue from "vue";
import Home from "@/views/StudyListHome.vue";

Vue.config.ignoredElements = ["b-message", "b-loading"];

describe("StudyListHome.vue", () => {
  it("renders hero title screen", () => {
    const wrapper = shallowMount(Home);
    const titleElement = wrapper.find("h1");
    expect(titleElement.text()).not.toBeUndefined();
  });
});
