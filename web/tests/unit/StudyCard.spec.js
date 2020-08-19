import { shallowMount, createLocalVue } from "@vue/test-utils";
import Buefy from "buefy";
import StudyCard from "@/components/studies/StudyCard.vue";
import Constants from "@/const";

const localVue = createLocalVue();
localVue.use(Buefy);

describe("StudyCard.vue", () => {
  it("displays study acronym as card title", () => {
    const study = {
      extension: [
        {
          url: Constants.SYSTEM_STUDY_ACRONYM,
          valueString: "ACRONYM",
        },
      ],
    };

    const wrapper = shallowMount(StudyCard, {
      localVue,
      propsData: {
        study,
      },
    });

    const titleElement = wrapper.find("p.title");

    expect(titleElement.text()).toMatch("ACRONYM");
  });
});
