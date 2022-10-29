FROM index.docker.io/cypress/included:3.8.3@sha256:14803e6c8860d8ffc92086ea597298f932a3c667dd6c7929f247711977e356a7

RUN npm install --save-dev @vue/cli-plugin-babel@v5.0.8
