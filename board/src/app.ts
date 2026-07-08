import express from "express";
import "reflect-metadata";

import config from "./common/config";
import logger from "./common/utils/logger";

async function startServer() {
  const app = express();

  const port = config.port;

  // loader 호출
  await require("./common/loader").default({ expressApp: app });

  // 웹 어플리케이션 시작
  app.listen(port, () => {
    logger.info("%d 번 포트로 웹 시작", port);
  });
}

startServer();
