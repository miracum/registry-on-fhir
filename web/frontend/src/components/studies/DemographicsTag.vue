<template>
  <div>
    <span v-if="age">
      <template v-if="age.low">
        Mindestalter
        <strong>{{ age.low.value }}</strong>
        {{ getAgeUnitFromCode(age.low.code) }}
      </template>
      <template v-if="age.low && age.high">und</template>
      <template v-if="age.high">
        Höchstalter
        <strong>{{ age.high.value }}</strong>
        {{ getAgeUnitFromCode(age.high.code) }}
      </template>
    </span>
    <b-tooltip :label="genderDisplay" class="gender-icon">
      <template v-if="gender == 'female'">
        <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 32 32"
          version="1.1" width="32px" height="32px">
          <g>
            <path
              d="M 16 2 C 13.800781 2 12 3.800781 12 6 C 12 7.128906 12.488281 8.144531 13.25 8.875 C 13.011719 9.046875 12.792969 9.226563 12.59375 9.4375 C 11.753906 10.328125 11.230469 11.515625 11 12.8125 L 11.03125 12.8125 L 9.03125 22.8125 L 8.78125 24 L 13 24 L 13 30 L 15 30 L 15 24 L 17 24 L 17 30 L 19 30 L 19 24 L 23.21875 24 L 22.96875 22.8125 L 20.96875 12.8125 C 20.738281 11.578125 20.238281 10.402344 19.40625 9.5 C 19.199219 9.273438 18.96875 9.058594 18.71875 8.875 C 19.492188 8.144531 20 7.136719 20 6 C 20 3.800781 18.199219 2 16 2 Z M 16 4 C 17.117188 4 18 4.882813 18 6 C 18 7.117188 17.117188 8 16 8 C 14.882813 8 14 7.117188 14 6 C 14 4.882813 14.882813 4 16 4 Z M 16 10 C 16.828125 10 17.421875 10.316406 17.9375 10.875 C 18.453125 11.433594 18.859375 12.261719 19.03125 13.1875 L 20.78125 22 L 11.21875 22 L 12.96875 13.1875 L 13 13.1875 C 13.175781 12.1875 13.558594 11.347656 14.0625 10.8125 C 14.566406 10.277344 15.152344 10 16 10 Z " />
          </g>
        </svg>
      </template>
      <template v-else-if="gender == 'male'">
        <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 32 32"
          version="1.1" width="32px" height="32px">
          <g>
            <path
              d="M 16 2 C 13.800781 2 12 3.800781 12 6 C 12 7.066406 12.433594 8.03125 13.125 8.75 C 11.273438 9.773438 10 11.746094 10 14 L 10 19.40625 L 10.28125 19.71875 L 12 21.4375 L 12 30 L 14 30 L 14 20.59375 L 13.71875 20.28125 L 12 18.5625 L 12 14 C 12 11.78125 13.78125 10 16 10 C 18.21875 10 20 11.78125 20 14 L 20 18.5625 L 18.28125 20.28125 L 18 20.59375 L 18 30 L 20 30 L 20 21.4375 L 21.71875 19.71875 L 22 19.40625 L 22 14 C 22 11.746094 20.726563 9.773438 18.875 8.75 C 19.566406 8.03125 20 7.066406 20 6 C 20 3.800781 18.199219 2 16 2 Z M 16 4 C 17.117188 4 18 4.882813 18 6 C 18 7.117188 17.117188 8 16 8 C 14.882813 8 14 7.117188 14 6 C 14 4.882813 14.882813 4 16 4 Z " />
          </g>
        </svg>
      </template>
      <template v-else>
        <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 32 32"
          version="1.1" width="32px" height="32px">
          <g>
            <path
              d="M 9 4 C 6.800781 4 5 5.800781 5 8 C 5 9.113281 5.476563 10.117188 6.21875 10.84375 C 4.886719 11.746094 4 13.285156 4 15 L 4 20.625 L 6 21.625 L 6 28 L 12 28 L 12 21.625 L 14 20.625 L 14 15 C 14 13.285156 13.113281 11.746094 11.78125 10.84375 C 12.523438 10.117188 13 9.113281 13 8 C 13 5.800781 11.199219 4 9 4 Z M 22 4 C 19.800781 4 18 5.800781 18 8 C 18 9.152344 18.523438 10.175781 19.3125 10.90625 C 18.40625 11.585938 17.746094 12.597656 17.53125 13.8125 C 17.53125 13.824219 17.53125 13.832031 17.53125 13.84375 L 16.03125 21.8125 L 15.78125 23 L 19 23 L 19 28 L 25 28 L 25 23 L 28.21875 23 L 27.96875 21.8125 L 26.46875 13.84375 C 26.46875 13.832031 26.46875 13.824219 26.46875 13.8125 C 26.253906 12.597656 25.59375 11.585938 24.6875 10.90625 C 25.476563 10.175781 26 9.152344 26 8 C 26 5.800781 24.199219 4 22 4 Z M 9 6 C 10.117188 6 11 6.882813 11 8 C 11 9.117188 10.117188 10 9 10 C 7.882813 10 7 9.117188 7 8 C 7 6.882813 7.882813 6 9 6 Z M 22 6 C 23.117188 6 24 6.882813 24 8 C 24 9.117188 23.117188 10 22 10 C 20.882813 10 20 9.117188 20 8 C 20 6.882813 20.882813 6 22 6 Z M 9 12 C 10.65625 12 12 13.34375 12 15 L 12 19.375 L 10 20.375 L 10 26 L 8 26 L 8 20.375 L 6 19.375 L 6 15 C 6 13.34375 7.34375 12 9 12 Z M 22 12 C 23.230469 12 24.277344 12.816406 24.5 14.15625 L 24.5 14.1875 L 24.53125 14.1875 L 25.8125 21 L 23 21 L 23 26 L 21 26 L 21 21 L 18.1875 21 L 19.46875 14.1875 L 19.5 14.1875 L 19.5 14.15625 C 19.722656 12.816406 20.769531 12 22 12 Z " />
          </g>
        </svg>
      </template>
    </b-tooltip>
  </div>
</template>

<script>
import fhirpath from "fhirpath";
import Constants from "@/const";

export default {
  name: "DemographicsTag",
  props: {
    eligibilityCharacteristic: {},
  },
  computed: {
    gender() {
      const gender = fhirpath.evaluate(
        this.eligibilityCharacteristic,
        `where(code.text='gender').valueCodeableConcept.coding.where(system='${Constants.SYSTEM_GENDER}').code`
      );
      if (gender.length) {
        return gender[0];
      }
      return null;
    },
    genderDisplay() {
      if (this.gender === "female") {
        return "nur weibliche Patientinnen";
      }

      if (this.gender === "male") {
        return "nur männliche Patienten";
      }

      return "alle Geschlechter";
    },
    age() {
      const age = fhirpath.evaluate(
        this.eligibilityCharacteristic,
        "where(code.coding.system=%ageSystem and code.coding.code='age').valueRange",
        {
          ageSystem: Constants.SYSTEM_AGE,
        }
      );
      if (age.length) {
        return age[0];
      }
      return null;
    },
  },
  methods: {
    getAgeUnitFromCode: (code) => {
      const ageUnitMap = {
        a: "Jahre",
        wk: "Wochen",
        d: "Tage",
        m: "Monate",
      };
      return ageUnitMap[code];
    },
  },
};
</script>

<style scoped>
.gender-icon {
  top: 4px;
}

.more-info-button {
  margin-bottom: 0.5rem;
}
</style>
