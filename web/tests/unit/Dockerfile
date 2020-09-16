FROM node:14.10-alpine
WORKDIR /app
COPY package*.json ./
RUN npm ci --no-optional
COPY . ./
RUN npm run test:unit
