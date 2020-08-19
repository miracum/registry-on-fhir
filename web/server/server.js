const express = require("express");
const path = require("path");
const logger = require("morgan");
const { createProxyMiddleware } = require("http-proxy-middleware");
const debug = require("debug")("server:server");
const http = require("http");
const bearerToken = require("express-bearer-token");
const promBundle = require("express-prom-bundle");
const history = require("connect-history-api-fallback");
const axios = require("axios");
const health = require("@cloudnative/health-connect");

const FHIR_URL = process.env.FHIR_URL || "http://localhost:8082/fhir";

const healthcheck = new health.HealthChecker();

const metricsMiddleware = promBundle({
  includeMethod: true,
  includePath: true,
  normalizePath: [
    ["^/css/.*", "/css/#file"],
    ["^/img/.*", "/img/#file"],
    ["^/js/.*", "/js/#file"],
    ["^/static/.*", "/static/#file"],
    ["^/details/.*", "/details/#study-id"],
  ],
});

const app = express();
app.use(bearerToken());

app.use((req, res, next) => {
  if (req.path.endsWith("/metrics")) {
    const expectedToken = process.env.METRICS_BEARER_TOKEN;
    if (expectedToken) {
      if (!req.token) {
        return res.sendStatus(403);
      }
      if (req.token === expectedToken) {
        return next();
      }
      return res.sendStatus(403);
    }
  }
  return next();
});

app.use(metricsMiddleware);
app.use(logger("dev"));
app.use(express.json());

const livePromise = () =>
  new Promise((resolve) => {
    resolve();
  });
const liveCheck = new health.LivenessCheck("is alive", livePromise);
healthcheck.registerLivenessCheck(liveCheck);

const readyPromise = () => axios.get(`${FHIR_URL}/metadata`);
const readyCheck = new health.ReadinessCheck("can connect to fhir server", readyPromise);
healthcheck.registerReadinessCheck(readyCheck);

app.use("/live", health.LivenessEndpoint(healthcheck));
app.use("/ready", health.ReadinessEndpoint(healthcheck));
app.use("/health", health.HealthEndpoint(healthcheck));

const proxyRequestFilter = (_pathname, req) => {
  return req.method === "GET";
};

app.use(
  "^/fhir",
  createProxyMiddleware(proxyRequestFilter, {
    target: FHIR_URL,
    changeOrigin: true,
    pathRewrite: {
      "^/fhir": "/",
    },
    //   headers: {
    //     Authorization:
    //       process.env.API_BEARER_TOKEN,
    //   },
  })
);

app.use(history());

app.use(express.static(path.join(__dirname, "..", "dist")));

app.get("/", (_req, res) => {
  res.render(path.join(__dirname, "..", "dist/index.html"));
});

function normalizePort(val) {
  const port = parseInt(val, 10);

  // eslint-disable-next-line no-restricted-globals
  if (isNaN(port)) {
    // named pipe
    return val;
  }

  if (port >= 0) {
    // port number
    return port;
  }

  return false;
}

const port = normalizePort(process.env.PORT || "3000");
app.set("port", port);

function onError(error) {
  if (error.syscall !== "listen") {
    throw error;
  }

  const bind = typeof port === "string" ? `Pipe ${port}` : `Port ${port}`;

  switch (error.code) {
    case "EACCES":
      console.error(`${bind} requires elevated privileges`);
      process.exit(1);
      break;
    case "EADDRINUSE":
      console.error(`${bind} is already in use`);
      process.exit(1);
      break;
    default:
      throw error;
  }
}

const server = http.createServer(app);

function onListening() {
  const addr = server.address();
  const bind = typeof addr === "string" ? `pipe ${addr}` : `port ${addr.port}`;
  debug(`Listening on ${bind}`);
}

server.listen(port);
server.on("error", onError);
server.on("listening", onListening);

module.exports = app;
